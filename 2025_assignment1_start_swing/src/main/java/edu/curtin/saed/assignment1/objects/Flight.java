package edu.curtin.saed.assignment1.objects;

public class Flight {
    private Plane p;
    private Airport origin;
    private Airport dest;
    private double dx, dy, dist; //displacement x, displacement y and distance
    private double traveled = 0.0;

    public Flight(Plane p, Airport origin, Airport dest) {
        this.p = p;
        this.origin = origin;
        this.dest = dest;
        this.dx = dest.getX() - origin.getX();
        this.dy = dest.getY() - origin.getY();
        this.dist = Math.hypot(dx, dy);

    }

    // returns how much we actually moved. 
    public double advance(double step) {
        double remaining = dist - traveled;
        double move = Math.min(step, remaining);
        traveled += move;
        return move;
    }


    //return the ratio 
    public double ratio() {
        if (dist == 0.0) 
        {
            return 1.0;
        } 
        else 
        {
            return traveled / dist;
        }
    }

    public Plane getPlane() 
    { 
        return p; 
    }
    public Airport getOrigin() 
    { 
        return origin; 
    }
    public Airport getDest() 
    { 
        return dest; 
    }
    public double getDx() 
    { 
        return dx; 
    }
    public double getDy() 
    { 
        return dy; 
    }
    public double getDist() 
    { 
        return dist; 
    }
}
