package edu.curtin.saed.assignment1;
import edu.curtin.saed.assignment1.objects.*;

import java.awt.*;
import java.awt.event.*;
import javax.swing.*;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;


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
    private static final int gridX = 12; 
    private static final int gridY = 12; 
    private static int nA = 10; // number of airports
    private static int nP = 10; //number of planes per airport

    private static Random rng = new Random(); 
    private static Boolean started = false; 
    private static int planeID = 0; 
    
    private static List<Airport> airports = new ArrayList<>(); 
    private static List<Plane> planes = new ArrayList<>();

    private static List<GridAreaIcon> airportIcons = new ArrayList<>(); 
    private static List<GridAreaIcon> planeIcons = new ArrayList<>(); 

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

        //initalise window components
        var startBtn = new JButton("Start");
        var endBtn = new JButton("End");
        var statusText = new JLabel("Label Text");
        var textArea = new JTextArea();
        textArea.append("::::::::::::::::::::::::::::::::: AirPlane Event Log ::::::::::::::::::::::::::::::::::");

        startBtn.addActionListener((event) ->
        {
            //skip if clicked after sim started
            if(started == true)
            {
                return; 
            }

            started = true; 
            statusText.setText("Starting sim... Spawning Airports and Planes");
            spawnAirports(area);
            spawnPlanes(area);
            area.repaint();
            statusText.setText("Spawned 10 airports and 100 planes");
        });
        
        endBtn.addActionListener((event) ->
        {
            statusText.setText("Stopped");
        });

        window.setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
        window.addWindowListener(new WindowAdapter()
        {
            @Override
            public void windowClosed(WindowEvent e)
            {
                System.out.println("Window closed");
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


    private static void spawnAirports(GridArea area)
    {
        for(int i = 0; i < nA; i++)
        {
            int x = getRandomWBounds(gridX, 0);
            int y = getRandomWBounds(gridY, 0);

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
}
