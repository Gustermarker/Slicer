package slicer;

public class Point2d implements Comparable<Point2d> {

    Double x, y;

    public Point2d(double x, double y) {
        this.x = x;
        this.y = y;
    }

    public double getX() {
        return x;
    }

    public double getY() {
        return y;
    }

    @Override
    public int compareTo(Point2d p) {
        return this.x.compareTo(p.x);
    }
}