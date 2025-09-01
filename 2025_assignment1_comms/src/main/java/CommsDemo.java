/**
 * EXAMPLE CODE. This shows how to use StandardFlightRequests and StandardPlaneServicing.
 */

package edu.curtin.saed.assignment1;
import java.io.*;
import java.util.logging.Logger;

public class CommsDemo
{
    private static final Logger logger = Logger.getLogger(Demo.class.getName());

    public static void main(String[] args)
    {
        // A single "StandardFlightRequests" object will provide flight requests for _one_ airport (in this case, airport #5 out of 10).
        var flightRequests = new StandardFlightRequests(10, 5);

        // You must call the "go()" method in a separate thread (for each StandardFlightRequests object).
        Thread thread = new Thread(
            () -> {
                try
                {
                    flightRequests.go();
                }
                catch(InterruptedException e)
                {
                    logger.info("Flight request thread ended");
                }
            },
            "flight requests");

        logger.info("Starting flight request thread");
        thread.start();

        // StandardFlightRequests will provide data that you can read line-by-line, like reading from a file, but indefinitely:
        try(BufferedReader br = flightRequests.getBufferedReader())
        {
            long startTime = System.currentTimeMillis();
            while(System.currentTimeMillis() - startTime < 10000) // Just an example that runs for 10 seconds
            {
                String line = br.readLine();
                System.out.println("-> " + line);
            }
        }
        catch(IOException e)
        {
            System.err.printf("Could not run command: %s: %s", e.getClass(), e.getMessage());
        }
        finally
        {
            // Shut down _each_ flight requests thread when you're finished.
            thread.interrupt();
        }

        // ----------

        // Servicing a plane
        logger.info("Servicing a plane");
        try
        {
            // Service plane #77 at airport #5.
            StandardPlaneServicing.service(5, 77);
        }
        catch(InterruptedException e)
        {
            logger.warning("Plane servicing interrupted!");
        }
    }
}
