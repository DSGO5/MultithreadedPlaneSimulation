package edu.curtin.saed.assignment1;
import edu.curtin.saed.assignment1.objects.*;

import java.awt.*;
import java.awt.event.*;
import javax.swing.*;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Random;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;


/**
 * This is demonstration code intended for you to modify. Currently, it sets up a rudimentary
 * Swing GUI with the basic elements required for the assignment.
 *
 * (There is an equivalent JavaFX version of this, if you'd prefer.)
 *
 * You will need to use the GridArea object, and create various GridAreaIcon objects, to represent
 * the on-screen map.
 *
 * Use the startBtn, endBtn, statusText and textArea objects for the other input/output required by
 * the assignment specification.
 *
 * Break this up into multiple methods and/or classes if it seems appropriate. Promote some of the
 * local variables to fields if needed.
 */
public class App
{
    //sim config vars
    private static int gridX = 12; 
    private static int gridY = 12; 
    private static int nA = 10; // number of airports
    private static int nP = 10; //number of planes per airport
    private static double speed = 1.2;
    private static int ticks = 50; 

    private static Random rng = new Random(); 
    private static Boolean started = false; 
    private static int planeID = 1; 
    
    //lists to hold all airport and plane info
    private static List<Airport> airports = new ArrayList<>(); 
    private static List<Plane> planes = new ArrayList<>();
    private static List<GridAreaIcon> airportIcons = new ArrayList<>(); 
    private static List<GridAreaIcon> planeIcons = new ArrayList<>(); 

    //lookup hashmaps
    private static Map<Integer, Airport> airportsMap = new HashMap<>();
    private static Map<Integer, GridAreaIcon> planeIconsMap = new HashMap<>();
    
    //thread pool used for servicing planes
    private static ExecutorService threadPool = Executors.newCachedThreadPool();

    //list of active flights
    private static List<Flight> activeFlights = new ArrayList<>();

    //logging info
    private static int inFlight = 0, servicing = 0, trips = 0; 

    //references so these can be altered by other methods
    private static GridArea gridArea; 
    private static JLabel status; 
    private static JTextArea log; 

    //UI elements
    private static Timer uiTimer; 
    private static long lastTick; 


    public static void main(String[] args) 
    {
        SwingUtilities.invokeLater(App::start);  // Equivalent to JavaFX's Platform.runLater().
    }

    public static void start()
    {
        var window = new JFrame("Air Traffic Simulator");

        //initalise grid
        GridArea area = new GridArea(gridX, gridY);
        area.setBackground(new Color(0, 0x60, 0));
        gridArea = area; 

        //initalise window components
        var startBtn = new JButton("Start");
        var endBtn = new JButton("End");
        var statusText = new JLabel("Ready");
        status = statusText;
        var textArea = new JTextArea();
        textArea.setEditable(false);
        textArea.append("::::::::::::::::::::::::::::::::: AirPlane Event Log ::::::::::::::::::::::::::::::::::");
        log = textArea;

        startBtn.addActionListener((event) ->
        {
            //skip if clicked after sim started
            if(started == true)
            {
                return; 
            }
            started = true; 

            //spawn the airports and planes
            statusText.setText("Starting sim... Spawning Airports and Planes");
            spawnAirports(area);
            spawnPlanes(area);

            //add airports into lookup table
            airportsMap.clear();
            for(Airport a : airports)
            {
                airportsMap.put(a.getId(), a);
            }

            //add planes into lookup table
            planeIconsMap.clear();
            for(int i = 0; i < planes.size(); i++)
            {
                planeIconsMap.put(planes.get(i).getId(), planeIcons.get(i));
            }

            //add planes to airports in the lookup table
            for(Plane p : planes)
            {
                airportsMap.get(p.getCurrentAirportId()).addPlane(p);
            }

            //start Ui Timer for animation
            startUiTimer();

            //start each of the airports threads
            for(Airport a : airports)
            {
                a.start(nA, threadPool);
            }

            //repain grid
            area.repaint();
            statusText.setText("Spawned " + nA + "Airports and " + (nA * nP) + " planes");
        });
        
        endBtn.addActionListener((event) ->
        {
            stopSim(statusText);
        });

        window.setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
        window.addWindowListener(new WindowAdapter()
        {
            @Override
            public void windowClosed(WindowEvent e)
            {
                stopSim(statusText);
            }
        });
        
        // Below is basically just the GUI "plumbing" (connecting things together).
        var toolbar = new JToolBar();
        toolbar.add(startBtn);
        toolbar.add(endBtn);
        toolbar.addSeparator();
        toolbar.add(statusText);

        var scrollingTextArea = new JScrollPane(textArea);
        scrollingTextArea.setBorder(BorderFactory.createEtchedBorder());

        var splitPane = new JSplitPane(
            JSplitPane.HORIZONTAL_SPLIT, area, scrollingTextArea);

        Container contentPane = window.getContentPane();
        contentPane.setLayout(new BorderLayout());
        contentPane.add(toolbar, BorderLayout.NORTH);
        contentPane.add(splitPane, BorderLayout.CENTER);

        window.setPreferredSize(new Dimension(1200, 1000));
        window.pack();
        splitPane.setDividerLocation(0.75);
        window.setVisible(true);
    }

    //spawn airports
    private static void spawnAirports(GridArea area)
    {
        for(int i = 0; i < nA; i++)
        {
            int x = getRandomWBounds(gridX, 1);
            int y = getRandomWBounds(gridY, 1);

            Airport airport = new Airport(i, x, y);
            airports.add(airport);

            GridAreaIcon icon = new GridAreaIcon(
                airport.getX(),
                airport.getY(),
                0.0,                 
                1.0,                 
                App.class.getClassLoader().getResource("airport.png"),
                "Airport " + airport.getId()
            );

            area.getIcons().add(icon);
            airportIcons.add(icon);
        }
    }

    //spawn planes
    private static void spawnPlanes(GridArea area)
    {
        for (Airport ap : airports) {
            for (int j = 0; j < nP; j++) {
                Plane p = new Plane(planeID++, ap.getX(), ap.getY(), ap.getId());
                planes.add(p);

                GridAreaIcon pIcon = new GridAreaIcon(
                    p.getX(),
                    p.getY(),
                    0.0,                 // rotation
                    1.0,                 // scale
                    App.class.getClassLoader().getResource("plane.png"),
                    "Plane " + p.getId()
                );
                pIcon.setShown(false);
                
                area.getIcons().add(pIcon);
                planeIcons.add(pIcon);
            }
        }
    }

    private static int getRandomWBounds(int max, int min)
    {
        return rng.nextInt(max - min) + min;
    }

    private static void startUiTimer()
    {
        lastTick = System.nanoTime();

        uiTimer = new Timer(ticks, event -> {
            long now = System.nanoTime();
            double dt = (now -  lastTick) / 1000000000.0;
            lastTick = now; 

            stepMovement(dt);
        });

        uiTimer.start();
    }
    
    private static void stepMovement(double dt)
    {
        if (dt <= 0)
        {
            return;
        } 

        double step = speed * dt; //distance to move this tick
        boolean anyMoved = false;

        //iterator instead of for each so it will keep going and can remove when landed
        var it = activeFlights.iterator();
        while (it.hasNext()) {
            Flight flight = it.next();

            // advance along path for this flight
            double moved = flight.advance(step);
            double ratio = flight.ratio();
            double angle = Math.toDegrees(Math.atan2(flight.getDy(), flight.getDx())) + 90.0;
            //calculate next x,y positions for next move
            double x = flight.getOrigin().getX() + flight.getDx() * ratio;
            double y = flight.getOrigin().getY() + flight.getDy() * ratio;


            // update icon with new position
            flight.getPlane().setPosition(x, y);
            GridAreaIcon icon = planeIconsMap.get(flight.getPlane().getId());
            if (icon != null)
            {
                icon.setPosition(x, y);
                icon.setRotation(angle);
            } 
            anyMoved = true;

            // check for landed
            if (Math.abs(moved - step) > 1e-12 && ratio >= 1.0 || flight.getDist() == 0.0) {
                it.remove();
                inFlight--;
                trips++;
                servicing++;
                hidePlaneIcon(flight.getPlane().getId());
                appendLog("Landing: Plane " + flight.getPlane().getId() + " at Airport " + flight.getDest().getId());
                updateStatus();

                // start servicing at landed airport
                flight.getDest().servicePlane(flight.getPlane());
            }
        }

        if (anyMoved)
        {
            gridArea.repaint(); //if a move was made then repaint the 
        } 
    }

    //Start a flight (origin -> dest)
    public static void requestStartFlight(Plane p, int originId, int destId)
    {
        SwingUtilities.invokeLater(() -> {
            Airport origin = airportsMap.get(originId);
            Airport dest   = airportsMap.get(destId);
            Flight flight = new Flight(p, origin, dest);
            activeFlights.add(flight);
            inFlight++;
            showPlaneIcon(p.getId()); // make the plane visible
            appendLog("Departure: Plane " + p.getId() + " Airport " + originId + " TO Airport " + destId);
            updateStatus();
        });
    }

    //After StandardPlaneServicing completes
    public static void notifyServiceComplete(Plane p, int atAirportId)
    {
        SwingUtilities.invokeLater(() -> {
            servicing--;
            Airport a = airportsMap.get(atAirportId);
            GridAreaIcon icon = planeIconsMap.get(p.getId());
            if (icon != null) {
                icon.setPosition(a.getX(), a.getY()); // keep ground coords accurate
                gridArea.repaint();
            }
            updateStatus();
        });
    }

    private static void showPlaneIcon(int id) {
        GridAreaIcon icon = planeIconsMap.get(id);
        if (icon != null) 
        {
            icon.setShown(true); gridArea.repaint(); 
        }
    }

    private static void hidePlaneIcon(int id) {
        GridAreaIcon icon = planeIconsMap.get(id);
        if (icon != null) 
        { 
            icon.setShown(false); gridArea.repaint(); 
        }
    }

    private static void appendLog(String msg) {
        log.append(msg + "\n");
        log.setCaretPosition(log.getDocument().getLength());
    }

    private static void updateStatus() {
        status.setText("In-flight: " + inFlight + "  | Servicing: " + servicing + " | Trips: " + trips);
    }

    private static void stopSim(JLabel statusText)
    {
        // safely end all airport threads
        for (Airport a : airports)
        {
            a.stop();   
        }
        //stop timer
        if (uiTimer != null)
        {
            uiTimer.stop();
        } 
        // end thread pool
        threadPool.shutdownNow();          
        statusText.setText("Stopped");
    }

}
