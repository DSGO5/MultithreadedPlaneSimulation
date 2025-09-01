package edu.curtin.saed.assignment1;

import java.awt.*;
import java.awt.event.*;
import javax.swing.*;


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
    public static void main(String[] args) 
    {
        SwingUtilities.invokeLater(App::start);  // Equivalent to JavaFX's Platform.runLater().
    }

    public static void start()
    {
        var window = new JFrame("Air Traffic Simulator");

        // Set up the main "top-down" display area. This is an example only, and you should
        // change this to set it up as you require.

        GridArea area = new GridArea(10, 10);
        // area.setGridLines(false); // If desired
        area.setBackground(new Color(0, 0x60, 0));

        area.getIcons().add(new GridAreaIcon(
            1, // x
            1, // y
            0.0, // degrees rotation (clockwise)
            1.0, // scale
            App.class.getClassLoader().getResource("airport.png"), // URL for image resource
            "Airport 1")); // caption

        area.getIcons().add(new GridAreaIcon(
            5, 3, 45.0, 1.0,
            App.class.getClassLoader().getResource("plane.png"),
            "Plane 1"));


        // Set up other key parts of the user interface. You'll also want to adjust this.

        var startBtn = new JButton("Start");
        var endBtn = new JButton("End");

        startBtn.addActionListener((event) ->
        {
            System.out.println("Start button pressed");
        });
        endBtn.addActionListener((event) ->
        {
            System.out.println("End button pressed");
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

        var statusText = new JLabel("Label Text");
        var textArea = new JTextArea();
        textArea.append("Sidebar\n");
        textArea.append("Text\n");


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
}
