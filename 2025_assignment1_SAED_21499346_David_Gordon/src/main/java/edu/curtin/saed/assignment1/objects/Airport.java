package edu.curtin.saed.assignment1.objects;

import java.io.BufferedReader;
import java.io.IOException;
import java.lang.ProcessHandle.Info;
import java.lang.System.Logger.Level;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.logging.Logger;

import edu.curtin.saed.assignment1.StandardFlightRequests;
import edu.curtin.saed.assignment1.StandardPlaneServicing;
import edu.curtin.saed.assignment1.App;

public class Airport 
{
    private static final Logger LOG = Logger.getLogger(Airport.class.getName());
    private int id; 
    private double x; 
    private double y; 

    private BlockingQueue<Integer> destinationQ = new LinkedBlockingQueue<>(); //blocking queue storing array of destinations
    private BlockingQueue<Plane> availPlanes = new LinkedBlockingQueue<>(); //blocking queue storing how many planes are grounded and ready to fly
    private static int poison = -1; 

    private Thread generator; //thread to generate destination airports
    private Thread producer; //thread to read from generator and put into blocking queue
    private Thread dispatcher; //thread to read from blocking que and dispatch flights

    private ExecutorService threadPool; //thread pool for servicing



    public Airport(int id, double x, double y)
    {
        this.id = id; 
        this.x = x; 
        this.y = y; 
    }

    public void addPlane(Plane p)
    {
        availPlanes.offer(p);
    }

    //starts Airport Funcitons
    public void start(int nAirports, ExecutorService threadPool)
    {
        this.threadPool = threadPool; 
        
        StandardFlightRequests sfr = new StandardFlightRequests(nAirports, id);

        //get destination airports thread
        generator = new Thread(() -> {
            try
                {
                    sfr.go();
                }
                catch(InterruptedException e)
                {
                    LOG.fine(() -> "SFR.go() interrupted for airport " + id);
                }
        }, "SFR Generator " + id);
        generator.start();

        //reads SFR output and puts into blockingqueue
        producer = new Thread(() -> {
            try (BufferedReader br = sfr.getBufferedReader())
            {
                String input;
                while((input = br.readLine()) != null)
                {
                    destinationQ.put(Integer.parseInt(input));
                }
            }
            catch (IOException e) {
                LOG.fine(() -> "Reader IO ended for airport " + id);
            } catch (InterruptedException ie) {
                LOG.fine(() -> "Reader interrupted for airport " + id);
                Thread.currentThread().interrupt();
            }
        }, "SFR Reader " + id);
        producer.start();

        //reads blockingqueue and dispatches available flights
        dispatcher = new Thread(() -> {
            try
            {
                while(true)
                {
                    int dest = destinationQ.take();
                    if(dest == poison)
                    {
                        break; 
                    }
                    Plane plane = availPlanes.take();
                    App.requestStartFlight(plane, id, dest); //Hit simulation to send a flight 
                }
            }
            catch(InterruptedException e)
            {
                LOG.fine(() -> "Dispatcher interrupted for airport " + id);
                Thread.currentThread().interrupt();
            }
        }, "SFR Dispatcher " + id);
        dispatcher.start();
    }

    //services the plane using threadpool
    public void servicePlane(Plane p)
    {
        p.setState(Plane.SERVICING);
        threadPool.submit(() -> {
            try
            {
                StandardPlaneServicing.service(id, p.getId());
                p.setCurrentAirportId(id);
                p.setState(Plane.ON_GROUND);
                availPlanes.offer(p); 
                App.notifyServiceComplete(p, id);
            }
            catch(InterruptedException e)
            {
                System.err.printf(e.getMessage());
            }
        });
    }

    //interrupts all threads to stop them and offers poison to break out of loop
    public void stop()
    {
        destinationQ.offer(poison);
        if(generator != null)
        {
            generator.interrupt();
        } 
        if(producer != null)
        {
            producer.interrupt();
        } 
        if(dispatcher != null)
        {
            dispatcher.interrupt();
        } 
    }    

    public int getId()
    {
        return id; 
    }

    public double getX()
    {
        return x; 
    }

    public double getY()
    {
        return y; 
    }
    
}
