package slicer;

import java.util.*;

public class Main {

    public static void main(String[] args) {
        Point2d p1 = new Point2d(0, 5);
        Point2d p2 = new Point2d(10, 5);
        Point2d p3 = new Point2d(3, 5);
        Point2d p4 = new Point2d(2, 5);

        List<Point2d> list = new ArrayList<Point2d>();

        for (Point2d p : list) {
            System.out.println("x: " + p.getX() + ", y: " + p.getY());
        }
        System.out.println();

        list.add(p1);
        list.add(p2);
        list.add(p3);
        list.add(p4);
        Collections.sort(list);

        for (Point2d p : list) {
            System.out.println("x: " + p.getX() + ", y: " + p.getY());
        }
    }

}