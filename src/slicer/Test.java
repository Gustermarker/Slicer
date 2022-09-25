package slicer;

import java.util.ArrayList;
import java.util.List;

public class Test {

    static List<Point2d> path_list = new ArrayList<Point2d>();
    static List<Point2d> perimeter = new ArrayList<Point2d>();


    public static void main(String[] args) {
        test();
        System.out.println("(BEFORE) perimeter size: " + perimeter.size());
        System.out.println("(BEFORE) path_list size: " + path_list.size());

        path_list.add(perimeter.get(0));
        path_list.add(perimeter.get(1));
        perimeter.remove(0);
        perimeter.remove(0);

        System.out.println("(AFTER) perimeter size: " + perimeter.size());
        System.out.println("(AFTER) path_list size: " + path_list.size());
        System.out.println("X: " + path_list.get(0).getX() + ", Y: " + path_list.get(0).getY());
    }

    public static void test() {
        // A
        perimeter.add(new Point2d(0, 4));
        perimeter.add(new Point2d(2, 3));

        // F
        perimeter.add(new Point2d(4,2));
        perimeter.add(new Point2d(5,0));

        // B
        perimeter.add(new Point2d(2,3));
        perimeter.add(new Point2d(2,-1));

        // E
        perimeter.add(new Point2d(-2, 4));
        perimeter.add(new Point2d(0,4));

        // G
        perimeter.add(new Point2d(5,0));
        perimeter.add(new Point2d(3,-2));

        // C
        perimeter.add(new Point2d(2, -1));
        perimeter.add(new Point2d(-2,0));

        // D
        perimeter.add(new Point2d(-2,0));
        perimeter.add(new Point2d(-2, 4));

        // H
        perimeter.add(new Point2d(3,-2));
        perimeter.add(new Point2d(4, 2));
    }

}