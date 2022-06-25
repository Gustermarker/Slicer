package slicer;

import javax.swing.*;
import java.awt.*;
import java.io.File;
import java.io.FileNotFoundException;
import java.math.BigDecimal;
import java.util.*;
import java.util.List;

/*
    This file is responsible for all parsing and calculations that are necessary
    for path finding algorithms and such.
 */

/* ascii STL code format
   facet normal x y z
      outer loop
         vertex x y z
         vertex x y z
         vertex x y z
      end loop
   end facet
 */


public class ParseSTL extends JPanel {
    static Map<Double, Point3d> map = new HashMap<Double, Point3d>(); // currently not used
    static List<Point3d> intersection_list = new ArrayList<Point3d>();
    static List<Point3d> intersections_cross = new ArrayList<Point3d>(); // cross section intersection points

    static double LAYER_HEIGHT = 0.2;
    static double Y_MIN = 100000, Y_MAX = -100000; // the max and min Y value for all intersection points

    public static void main(String[] args) {
        List<Triangle> list = new ArrayList<Triangle>();
        //List<Point3d> intersection_list = new ArrayList<Point3d>();
        list = readSTL(list);
        Point3d p1 = new Point3d(0, .6, 0);
        Point3d p2 = new Point3d(10, .6, 0);
        Point3d p3 = new Point3d(5, .6, 0);
        Collections.sort(list);
        findIntersections(list);
        crossSectionIntersections(intersection_list);
        //sortPoints(intersections_cross);

        ParseSTL panel = new ParseSTL();
        JFrame.setDefaultLookAndFeelDecorated(true);
        JFrame frame = new JFrame("Draw Points");
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        frame.setBackground(Color.white);
        frame.setSize(600, 600);
        frame.add(panel);
        frame.setVisible(true);

        System.out.println("Y_MIN = " + Y_MIN);
        System.out.println("Total lines: " + intersection_list.size() / 2);
        System.out.println("Y_MAX = " + Y_MAX);

//        for (Point3d p : intersection_list) {
//            System.out.println(p.getX() + ", " + p.getY());
//        }
        for (Point3d p : intersections_cross) {
            System.out.println("x: " + p.getX() + ", y: " + p.getY());
        }

//        for (Triangle l : list) {
//            System.out.println("(" + l.A.getX() + ", " + l.A.getY() + ", " + l.A.getZ() + ")" + ", (" + l.B.getX() + ", " + l.B.getY() + ", " + l.B.getZ() + ")" + ", (" + l.C.getX() + ", " + l.C.getY() + ", " + l.C.getZ() + ")");
//        }
    }

    public static List<Triangle> readSTL(List<Triangle> faceList) {
        try {
            Scanner scanner = new Scanner(new File("/Users/gustavoestermarker/IdeaProjects/Slicer/Chisel.stl"));
            while (scanner.hasNextLine()) {
                Point3d[] points = new Point3d[3];
                if (scanner.nextLine().contains("outer")) { // 3 vertex lines follow "outer loop" line
                    for (int i = 0; i < 3; i++) {
                        String vertex = scanner.nextLine(); // vertex x y z
                        String[] xyz = vertex.split("\\s+"); // xyz of one point
                        points[i] = new Point3d(Double.parseDouble(xyz[2]), Double.parseDouble(xyz[3]), Double.parseDouble(xyz[4]));
                    }
                    faceList.add(new Triangle(points[0], points[1], points[2]));
                }
            }
            scanner.close();
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        }
        return faceList;
    }

    /*
    3 possibilities:
    AB BC
    AB AC
    AC BC

    if (AB)
        if (BC)
            intersection(AB, BC)
        else (AC)
            intersection(AB, AC)
    else
        intersection(AC, BC)
     */

    public static void findIntersections(List<Triangle> list) {
        double current_Z_height = 5;
        double temp_Z_height;
        int index = 0;
        int startIndex = 0;
        int i = 0;

        // computes intersections for each layer, one at a time
        // NOTE: increase efficiency of the for loop. it's not perfect, repeats and such
        while (true) {
            // go through all faces at that Z height
            for (i = startIndex; i < list.size() - 1; i++) {
                if (list.get(i).Z_min > current_Z_height) {
                    startIndex = i;
                    break;
                }
                if (list.get(i).Z_max > current_Z_height) {
                    twoLines(list.get(i).A, list.get(i).B, list.get(i).C, current_Z_height);
                }
            }
            //current_Z_height += LAYER_HEIGHT;
            if (current_Z_height > 100) {
                break;
            }
            break;
        }
    }

    /**
     * determine which two lines the plane intersects
     * 3 possibilities: 1) AB and BC, 2) AB and AC, 3) AC and BC
     */
    public static void twoLines(Point3d A, Point3d B, Point3d C, double layerHeight) {
        if ((A.getZ() <= layerHeight && B.getZ() >= layerHeight) | (A.getZ() >= layerHeight && B.getZ() <= layerHeight)) {
            if ((B.getZ() <= layerHeight && C.getZ() >= layerHeight) | (B.getZ() >= layerHeight && C.getZ() <= layerHeight)) {
                findLinePlaneIntersectionCoords(A, B, layerHeight);
                findLinePlaneIntersectionCoords(B, C, layerHeight);
            } else {
                findLinePlaneIntersectionCoords(A, B, layerHeight);
                findLinePlaneIntersectionCoords(A, C, layerHeight);
            }
        } else {
            findLinePlaneIntersectionCoords(A, C, layerHeight);
            findLinePlaneIntersectionCoords(B, C, layerHeight);
        }
    }

    // finds the coordinates of the intersection between the line and the horizontal plane Z = curr_Z_height
    public static void findLinePlaneIntersectionCoords(Point3d A, Point3d B, double d) {
        double px = A.getX(), py = A.getY(), pz = A.getZ();
        double qx = B.getX(), qy = B.getY(), qz = B.getZ();
        double tDenom = (qz - pz);
        double layerHeight = d * -1;

        if (tDenom == 0) { // both points have same Z value
            if (qz == layerHeight) { // line is inside of the plane
                intersection_list.add(new Point3d(A.getX(), A.getY(), A.getZ()));
                intersection_list.add(new Point3d(B.getX(), B.getY(), B.getZ()));
            }
            return;
        }

        // the intersection point
        double t = -(pz + layerHeight) / tDenom;
        double x = (px + t * (qx - px));
        double y = (py + t * (qy - py));
        double z = (pz + t * (qz - pz));

        if (z == 0)
            return;

        if (y > Y_MAX)
            Y_MAX = y;
        if (y < Y_MIN)
            Y_MIN = y;

        intersection_list.add(new Point3d(x, y, z));
    }

    /**
     * Slices the 2d cross section from Y_MIN to Y_MAX, incrementing by line width (0.6mm)
     * This will give the point of intersection of the 2d cross section and the slice
     */
    public static void crossSectionIntersections(List<Point3d> list) {
        BigDecimal currentHeight = new BigDecimal(Y_MIN);

        while (currentHeight.compareTo(new BigDecimal(Y_MAX)) < 0) {
            for (int i = 0; i < list.size() - 1; i += 2) {
                if (currentHeight.compareTo(new BigDecimal(list.get(i).getY())) > 0 && currentHeight.compareTo(new BigDecimal(list.get(i + 1).getY())) < 0 ||
                        currentHeight.compareTo(new BigDecimal(list.get(i).getY())) < 0 && currentHeight.compareTo(new BigDecimal(list.get(i + 1).getY())) > 0) {
                    pointOnALine(list.get(i), list.get(i + 1), currentHeight); // where on the line does it intersect
                }
            }
            currentHeight = currentHeight.add(new BigDecimal("0.6"));
        }
    }


    public static void pointOnALine(Point3d A, Point3d B, BigDecimal height) {
        double x1, y1, x2, y2;
        double height_d = height.doubleValue();
        if (A.getY() > B.getY()) {
            x1 = A.getX();
            y1 = A.getY();
            x2 = B.getX();
            y2 = B.getY();
        } else {
            x1 = B.getX();
            y1 = B.getY();
            x2 = A.getX();
            y2 = A.getY();
        }

        double slope = (x1 - x2) / (y1 - y2);
        double x = (height_d - y2) * slope + x2;
        intersections_cross.add(new Point3d(x, height_d, 0)); // doesn't use Z, maybe make a Point2d
    }


    /**
     * Given a line (Point A, Point B), find where the intersection point is given
     * a certain height. Simply uses the slope to calculate X given height (Y)
     */
//    public static void pointOnALine(Point3d A, Point3d B, double height) {
//        double x1, y1, x2, y2;
//
//        if (A.getY() > B.getY()) {
//            x1 = A.getX();
//            y1 = A.getY();
//            x2 = B.getX();
//            y2 = B.getY();
//        } else {
//            x1 = B.getX();
//            y1 = B.getY();
//            x2 = A.getX();
//            y2 = A.getY();
//        }
//
//        double slope = (x1 - x2) / (y1 - y2);
//        double x = (height - y2) * slope + x2;
//        intersections_cross.add(new Point3d(x, height, 0)); // doesn't use Z, maybe make a Point2d
//    }

    /**
     * Takes in the cross section intersectin points. Right now they are sorted by Y_value,
     * but the X_value is unsorted. This method sorts the X_value within the list
     */
    public static void sortPoints(List<Point3d> list) {
        List<Point3d> temp = new ArrayList<Point3d>();
        double Y_value = list.get(0).getY();
        int index = 0;

        for (int i = 0; i < list.size(); i++) {
            if (list.get(i).getY() == Y_value) {
                temp.add(list.get(i));
            } else {
                for (Point3d p : temp) {
                    System.out.println("x: " + p.getX() + ", y: " + p.getY());
                }
                Y_value += 0.6;

                if (temp.size() > 2) {
                    sortAndReplace(list, temp, index);
                }

                temp.clear();
                System.out.println();
                index = i;
            }
        }
//        for (int i = 0; i < list.size(); i++) {
//            System.out.println("Y_value: " + Y_value);
//            if (list.get(i).getY() == Y_value) {
//                temp.add(list.get(i));
//            } else if (temp.size() <= 2) { // if only two elements, swap order if necessary
//                System.out.println("temp size");
//                if (temp.size() == 2 && temp.get(0).getX() > temp.get(1).getX()) {
//                    System.out.println("temp.size() <= 2");
//                    intersections_cross.set(i, temp.get(1));
//                    intersections_cross.set(i + 1, temp.get(0));
//                    temp.clear();
//                    index = i;
//                    Y_value += 0.6;
//                } else {
//                    Y_value += 0.6;
//                }
//            } else {
//                System.out.println("else");
//                sortAndReplace(list, temp, index);
//                temp.clear();
//                index = i;
//                Y_value += 0.6;
//            }
//        }
    }


    public static void sortAndReplace(List<Point3d> list, List<Point3d> temp_list, int index) {
        //System.out.println("\nsize:" + temp_list.size());
        //System.out.println("Before sort:");
        for (Point3d p : temp_list) {
            System.out.println("x: " + p.getX() + ", y: " + p.getY());
        }
        for (int j = 1; j < temp_list.size(); j++) {
            //double current = temp_list.get(j).getX();
            Point3d current = temp_list.get(j);
            int i = j - 1;
            while ((i > -1) && (temp_list.get(i).getX() > current.getX())) {
                temp_list.set(i + i, temp_list.get(i));
                i--;
            }
            temp_list.set(i + 1, current);
        }
        for (int i = 0; i < temp_list.size(); i++) {
            intersections_cross.set(index + i, temp_list.get(i));
        }

        //System.out.println("After sort:");
        for (Point3d p : temp_list) {
            //  System.out.println("x: " + p.getX() + ", y: " + p.getY());
        }
    }

    /**
     * Sorts the temporary list and replaces the values in 'list'
     * Uses insertion sort since the temp list is very small
     * <p>
     * //* @param index starting index in 'list'
     */
//    public static void sortAndReplace(List<Point3d> list, List<Point3d> temp_list, int index) {
//        //System.out.println("\nsize:" + temp_list.size());
//        //System.out.println"Before sort:");
//        for (Point3d p : temp_list) {
//            System.out.println("x: " + p.getX() + ", y: " + p.getY());
//        }
//        for (int j = 1; j < temp_list.size(); j++) {
//            //double current = temp_list.get(j).getX();
//            Point3d current = temp_list.get(j);
//            int i = j - 1;
//            while ((i > -1) && (temp_list.get(i).getX() > current.getX())) {
//                temp_list.set(i + i, temp_list.get(i));
//                i--;
//            }
//            temp_list.set(i + 1, current);
//        }
//        for (int i = 0; i < temp_list.size(); i++) {
//            intersections_cross.set(index + i, temp_list.get(i));
//        }
//
//        //System.out.println("After sort:");
//        for (Point3d p : temp_list) {
//            //  System.out.println("x: " + p.getX() + ", y: " + p.getY());
//        }
//    }
    public void paintComponent(Graphics g) {
        for (int i = 0; i < intersection_list.size() - 1; i += 2) {
            g.drawLine((int) (intersection_list.get(i).getX() * 10), (int) (intersection_list.get(i).getY() * 10), (int) (intersection_list.get(i + 1).getX() * 10), (int) (intersection_list.get(i + 1).getY() * 10));
        }

        for (Point3d p : intersections_cross) {
            g.drawOval((int) (p.getX() * 10), (int) (p.getY() * 10), 3, 3);
        }
    }
}