package edu.curtin.saed.assignment1.objects;

public class Plane 
{
    public static final String ON_GROUND = "ON_GROUND";
    public static final String IN_FLIGHT = "IN_FLIGHT";
    public static final String SERVICING = "SERVICING";

    private int id;
    private double x;
    private double y;
    private int currentAirportId;
    private String state;

    public Plane(int id, double startX, double startY, int atAirportId)
    {
        this.id = id;
        this.x = startX;
        this.y = startY;
        this.currentAirportId = atAirportId;
        this.state = ON_GROUND; // planes start on the ground
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

    public int getCurrentAirportId()
    {
        return currentAirportId;
    }

    public String getState()
    {
        return state;
    }

    
    public void setState(String newState)
    {
        this.state = newState;
    }

    public void setPosition(double x, double y)
    {
        this.x = x;
        this.y = y;
    }

    public void setCurrentAirportId(int airportId)
    {
        this.currentAirportId = airportId;
    }

}


