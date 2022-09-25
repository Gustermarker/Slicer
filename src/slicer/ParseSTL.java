package slicer;

import javax.swing.*;
import java.awt.*;
import java.io.File;
import java.io.FileNotFoundException;
import java.math.BigDecimal;
import java.util.*;
import java.util.List;

/*
 * This file is responsible for all parsing and calculations that are necessary
 * for path finding algorithms and such.
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
    static List<Point3d> perimeter_list = new ArrayList<Point3d>();
    static List<Point2d> perimeter_intersection_points = new ArrayList<Point2d>();
    static List<Point2d> path_list = new ArrayList<Point2d>();
    static List<Point2d> temp = new ArrayList<Point2d>();
    static List<Point2d> test_list = new ArrayList<Point2d>();

    static double LAYER_HEIGHT = 0.2;
    static double Y_MIN = 100000, Y_MAX = -100000; // the max and min Y value for all intersection points

    public static void main(String[] args) {
        List<Triangle> list = new ArrayList<Triangle>();
        readSTL(list);// parses STL and fills 'list'
        Collections.sort(list);
        sliceSTL(list);
        crossSectionIntersections(perimeter_list);
        sortPoints(perimeter_intersection_points);

        int j = 0;
        for (Point2d p : perimeter_intersection_points) {
            //System.out.println("index: " + j + ", x: " + p.getX() + ", y: " + p.getY());
            j++;
        }

        temp.addAll(perimeter_intersection_points);
        GeneratePath generate = new GeneratePath();
        path_list = generate.GeneratePerims(perimeter_list);

        System.out.println("perimeter list size: " + perimeter_list.size());
        System.out.println("ordered perimeter size: " + path_list.size());

        ParseSTL panel = new ParseSTL();
        JFrame frame = new JFrame("Draw Points");
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        frame.setBackground(Color.white);
        frame.setSize(1200, 1200);
        frame.add(panel);
        frame.setVisible(true);
    }

    /**
     * Parses the STL file and creates a list of faces (an X, Y, Z plane)
     */
    public static List<Triangle> readSTL(List<Triangle> faceList) {
        try {
            Scanner scanner = new Scanner(new File("/Users/gustavoestermarker/IdeaProjects/Slicer/circle.stl"));
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

    /**
     * Slices the 3D model horizonally, one time for each layer.
     * The points of intersection between the horizonal plane and the 3D model's 'triangle faces'
     * form a 2d view of the walls of
     * 3 possibilities: AB/BC, AB/AC, AC/BC
     * uses: twoLines(), linePlaneIntersection()
     */
    public static void sliceSTL(List<Triangle> list) {
        double current_Z_height = 2;
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
                } else {
                    whichTwoLines(list.get(i).A, list.get(i).B, list.get(i).C, current_Z_height);
                }
//                if (list.get(i).Z_max > current_Z_height) {
//                    whichTwoLines(list.get(i).A, list.get(i).B, list.get(i).C, current_Z_height);
//                }
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
    public static void whichTwoLines(Point3d A, Point3d B, Point3d C, double layerHeight) {
        if ((A.getZ() <= layerHeight && B.getZ() >= layerHeight) || (A.getZ() >= layerHeight && B.getZ() <= layerHeight)) {
            if ((B.getZ() <= layerHeight && C.getZ() >= layerHeight) || (B.getZ() >= layerHeight && C.getZ() <= layerHeight)) {
                linePlaneIntersection(A, B, layerHeight);
                linePlaneIntersection(B, C, layerHeight);
            } else {
                linePlaneIntersection(A, B, layerHeight);
                linePlaneIntersection(A, C, layerHeight);
            }
        } else {
            linePlaneIntersection(A, C, layerHeight);
            linePlaneIntersection(B, C, layerHeight);
        }
    }

    /**
     * Calculates the intersection points between a line and a plane of height 'd' and
     * adds this point to a list
     */
    public static void linePlaneIntersection(Point3d A, Point3d B, double d) {
//        System.out.println("A.getX(): " + A.getX() + ", A.getY(): " + A.getY());
//        System.out.println("B.getX(): " + B.getX() + ", B.getY(): " + B.getY());
        double px = A.getX(), py = A.getY(), pz = A.getZ();
        double qx = B.getX(), qy = B.getY(), qz = B.getZ();
        double tDenom = (qz - pz);
        double layerHeight = d * -1;

        if (tDenom == 0) { // both points have same Z value
            if (qz == layerHeight) { // line is inside of the plane
                perimeter_list.add(new Point3d(A.getX(), A.getY(), A.getZ()));
                perimeter_list.add(new Point3d(B.getX(), B.getY(), B.getZ()));
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

        //System.out.println("x: " + x + ", y: " + y + ", z: " + z);
        perimeter_list.add(new Point3d(x, y, z));
    }

    /**
     * Slices the 2d cross section from Y_MIN to Y_MAX, incrementing by line width (0.6mm)
     * This will give the point of intersection of the 2d cross section and the slice
     */
    public static void crossSectionIntersections(List<Point3d> perimeter_list) {
        BigDecimal currentHeight = new BigDecimal(Y_MIN);

        while (currentHeight.compareTo(new BigDecimal(Y_MAX)) < 0) {
            for (int i = 0; i < perimeter_list.size() - 1; i += 2) {
                if (currentHeight.compareTo(BigDecimal.valueOf(perimeter_list.get(i).getY())) > 0 && currentHeight.compareTo(BigDecimal.valueOf(perimeter_list.get(i + 1).getY())) < 0 ||
                        currentHeight.compareTo(BigDecimal.valueOf(perimeter_list.get(i).getY())) < 0 && currentHeight.compareTo(BigDecimal.valueOf(perimeter_list.get(i + 1).getY())) > 0) {
                    pathLineIntersection(perimeter_list.get(i), perimeter_list.get(i + 1), currentHeight); // where on the line does it intersect
                }
            }
            currentHeight = currentHeight.add(new BigDecimal("0.6"));
        }
    }

    /**
     * Given a line (Point A, Point B), find where the intersection point is given
     * a certain height. Simply uses the slope to calculate X given height (Y)
     */
    public static void pathLineIntersection(Point3d A, Point3d B, BigDecimal height) {
        //System.out.println("pathLineIntersection()");
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

//        if (height.compareTo(new BigDecimal(35.5)) > 0 && height.compareTo(new BigDecimal(36.5)) < 0) {
//            System.out.println("in");
//            System.out.println("x1: " + x1);
//            System.out.println("x2: " + x2);
//            System.out.println("y1: " + y1);
//            System.out.println("y2: " + y2);
//            System.out.println("height: " + height.doubleValue() + "\n");
//           // path_intersection_points.add(new Point2d(x1, height_d));
//           // path_intersection_points.add(new Point2d(25.01612225144019, 35.4));
////            path_intersection_points.add(new Point2d(30.74150611617486, 35.4));
////            path_intersection_points.add(new Point2d(54.24898614182858, 35.4));
////            path_intersection_points.add(new Point2d(65.12122408430801, 35.4));
////            path_intersection_points.add(new Point2d(84.9790, 35.4));
////            path_intersection_points.add(new Point2d(76.26759921024, 35.4));
//
//        }
//
        double slope = (x1 - x2) / (y1 - y2);
        double x = (height_d - y2) * slope + x2;
        perimeter_intersection_points.add(new Point2d(x, height_d));
        // System.out.println("size path intersection: " + path_intersection_points.size());

//        if (height.compareTo(new BigDecimal(35)) > 0 && height.compareTo(new BigDecimal(36)) < 0) {
//            System.out.println("x: " + x);
//        }

    }

    /**
     * Takes in the cross section intersectin points. Right now they are sorted by Y_value,
     * but the X_value is unsorted. This method sorts the X_value within the list
     */
    public static void sortPoints(List<Point2d> list) {
        List<Point2d> temp = new ArrayList<Point2d>();
        BigDecimal Y_value = new BigDecimal(Y_MIN);
        BigDecimal increment = new BigDecimal("0.6");
        Y_value = Y_value.add(increment);

        for (int i = 0; i < list.size(); i++) {
//            System.out.println("size list: " + list.size());
//            System.out.println("loop");
            double d = list.get(i).getY();
            double y = Y_value.doubleValue();
            int size = temp.size();
            if (Y_value.doubleValue() == list.get(i).getY()) {
                temp.add(list.get(i));
            } else {
                sortAndReplace(temp, i - temp.size());
                Y_value = Y_value.add(increment);
                temp.clear();
                i--; // this is creating the issue ************************************************************
            }
        }
    }

    /**
     * Takes in a temp list subsection of path_intersection_list (list of points with same Y values)
     * and sorts it by X value, and places back into path_intersection_list
     */
    public static void sortAndReplace(List<Point2d> list, int index) {
        Collections.sort(list);
        for (int i = 0; i < list.size(); i++) {
            perimeter_intersection_points.set(index + i, list.get(i));
        }
    }

    public void paintComponent(Graphics g) {
        for (int i = 0; i < 1220; i += 2) {
            g.drawLine((int) (path_list.get(i).getX() * 8 + 450), (int) (path_list.get(i).getY() * 8 + 50), (int) (path_list.get(i + 1).getX() * 8 + 450), (int) (path_list.get(i + 1).getY() * 8 + 50));
        }
    }
}