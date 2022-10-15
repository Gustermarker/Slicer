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
    static List<Point2d> one_layer_path = new ArrayList<Point2d>();
    static List<List<Point2d>> all_layers_path = new ArrayList<List<Point2d>>();
    static List<Triangle> triangleList = new ArrayList<Triangle>();

    static double LAYER_HEIGHT = 0.2;
    static double Y_MIN = 100000, Y_MAX = -100000, X_MIN = 100000, X_MAX = -100000; // the max and min Y value for all intersection points
    boolean horizontal_slice = true;

    public static void main(String[] args) {
        readSTL(triangleList); // parses STL and fills 'list'
        Collections.sort(triangleList);
        sliceSTL(triangleList); // generates perimeter_list

//        X_MIN = perimeter_list.get(0).getX();
//        X_MAX = perimeter_list.get(0).getY();

//        for (int i = 0; i < perimeter_list.size(); i++) {
//            double x = perimeter_list.get(i).getX();
//            double y = perimeter_list.get(i).getY();
//            System.out.println("x: " + x + ", y: " + y);
//            System.out.println();
//            if (y > Y_MAX)
//                Y_MAX = y;
//            if (y < Y_MIN)
//                Y_MIN = y;
//            if (x > X_MAX)
//                X_MAX = x;
//            if (x < X_MIN)
//                X_MIN = x;
//        }

        System.out.println("Y_min: " + Y_MIN + ", Y_max: " + Y_MAX + ", X_min" + X_MIN + ", X_max: " + X_MAX);

        //crossSectionIntersectionsHorizontal(perimeter_list);
        crossSectionIntersectionsVertical(perimeter_list);

        //sortPointsHorizontal(perimeter_intersection_points);
        sortPointsVertical(perimeter_intersection_points);


        int j = 0;
        for (Point2d p : perimeter_intersection_points) {
            System.out.println("index: " + j + ", x: " + p.getX() + ", y: " + p.getY());
            j++;
        }

        System.out.println("perimeter intersection size: " + perimeter_intersection_points.size());

        GeneratePath generate = new GeneratePath();
        //one_layer_path = generate.GeneratePath(perimeter_intersection_points, perimeter_list);
        one_layer_path = generate.GeneratePathVertical(perimeter_intersection_points, perimeter_list);

        System.out.println("perimeter list size: " + perimeter_list.size());
        System.out.println("ordered perimeter size: " + one_layer_path.size());

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
            Scanner scanner = new Scanner(new File("/Users/gustavoestermarker/IdeaProjects/Slicer 2.0/circle.stl"));
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
                } else {
                    whichTwoLinesIntersectsPlane(list.get(i).A, list.get(i).B, list.get(i).C, current_Z_height);
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
    public static void whichTwoLinesIntersectsPlane(Point3d A, Point3d B, Point3d C, double layerHeight) {

        if ((A.getZ() <= layerHeight && B.getZ() >= layerHeight) || (A.getZ() >= layerHeight && B.getZ() <= layerHeight)) {
            if ((B.getZ() <= layerHeight && C.getZ() >= layerHeight) || (B.getZ() >= layerHeight && C.getZ() <= layerHeight)) {
                calculateLinePlaneIntersection(A, B, layerHeight);
                calculateLinePlaneIntersection(B, C, layerHeight);
            } else {
                calculateLinePlaneIntersection(A, B, layerHeight);
                calculateLinePlaneIntersection(A, C, layerHeight);
            }
        } else {
            calculateLinePlaneIntersection(A, C, layerHeight);
            calculateLinePlaneIntersection(B, C, layerHeight);
        }
    }

    /**
     * Calculates the intersection points between a line and a plane of height 'd' and
     * adds this point to a list
     */
    public static void calculateLinePlaneIntersection(Point3d A, Point3d B, double d) {
        double px = A.getX(), py = A.getY(), pz = A.getZ();
        double qx = B.getX(), qy = B.getY(), qz = B.getZ();
        double tDenom = (qz - pz);
        double layerHeight = d * -1;

        if (tDenom == 0) { // both points have same Z value
            if (qz == layerHeight) { // line is inside the plane
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
        if (x > X_MAX)
            X_MAX = x;
        if (x < X_MIN)
            X_MIN = x;

        perimeter_list.add(new Point3d(x, y, z));
    }

    /**
     * Slices the 2d cross section from Y_MIN to Y_MAX, incrementing by line width (0.6mm)
     * This will give the point of intersection of the 2d cross section and the slice
     */
    public static void crossSectionIntersectionsHorizontal(List<Point3d> perimeter_list) {
        //BigDecimal currentHeight = new BigDecimal(Y_MIN);
        // alternates between a horizontal and vertical slice

        BigDecimal currentHeight = new BigDecimal(Y_MIN);
        // alternates between a horizontal and vertical slice


        while (currentHeight.compareTo(new BigDecimal(Y_MAX)) < 0) {
            for (int i = 0; i < perimeter_list.size() - 1; i += 2) {
                if (currentHeight.compareTo(BigDecimal.valueOf(perimeter_list.get(i).getY())) > 0 && currentHeight.compareTo(BigDecimal.valueOf(perimeter_list.get(i + 1).getY())) < 0 ||
                        currentHeight.compareTo(BigDecimal.valueOf(perimeter_list.get(i).getY())) < 0 && currentHeight.compareTo(BigDecimal.valueOf(perimeter_list.get(i + 1).getY())) > 0) {
                    pathLineIntersection(perimeter_list.get(i), perimeter_list.get(i + 1), currentHeight, true); // where on the line does it intersect
                }
            }
            currentHeight = currentHeight.add(new BigDecimal("0.6"));
            boolean horizontal_slice = false;
        }
    }

    public static void crossSectionIntersectionsVertical(List<Point3d> perimeter_list) {
        BigDecimal currentHeight = new BigDecimal(X_MIN);
        // alternates between a horizontal and vertical slice

        while (currentHeight.compareTo(new BigDecimal(X_MAX)) < 0) {
            for (int i = 0; i < perimeter_list.size() - 1; i += 2) {
                if (currentHeight.compareTo(BigDecimal.valueOf(perimeter_list.get(i).getX())) > 0 && currentHeight.compareTo(BigDecimal.valueOf(perimeter_list.get(i + 1).getX())) < 0 ||
                        currentHeight.compareTo(BigDecimal.valueOf(perimeter_list.get(i).getX())) < 0 && currentHeight.compareTo(BigDecimal.valueOf(perimeter_list.get(i + 1).getX())) > 0) {
                    pathLineIntersection(perimeter_list.get(i), perimeter_list.get(i + 1), currentHeight, false); // where on the line does it intersect
                }
            }
            currentHeight = currentHeight.add(new BigDecimal("0.6"));
            boolean horizontal_slice = false;
        }
    }

    /**
     * Given a line (Point A, Point B), find where the intersection point is given
     * a certain height. Simply uses the slope to calculate X given height (Y)
     */
    // this function works
    public static void pathLineIntersection(Point3d A, Point3d B, BigDecimal height, boolean horizontal) {
        double x1, y1, x2, y2;
        double height_d = height.doubleValue();
        if (horizontal) {
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
            perimeter_intersection_points.add(new Point2d(x, height_d));
        } else {
            if (A.getX() > B.getX()) {
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
            double slope = (y1 - y2) / (x1 - x2);
            double y = (height_d - x2) * slope + y2;
            perimeter_intersection_points.add(new Point2d(height_d, y));
        }
    }

    /**
     * Takes in the cross-section intersection points. Right now they are sorted by Y_value,
     * but the X_value is unsorted. This method sorts the X_value within the list
     */
    public static void sortPointsHorizontal(List<Point2d> list) {
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
                sortAndReplace(temp, i - temp.size(), false);
                Y_value = Y_value.add(increment);
                temp.clear();
                i--; // this is creating the issue ************************************************************
            }
        }
    }

    /**
     * Takes in the cross-section intersection points. Right now they are sorted by X_value,
     * but the Y_value is unsorted. This method sorts the Y_value within the list
     */
    public static void sortPointsVertical(List<Point2d> list) {
        List<Point2d> temp = new ArrayList<Point2d>();
        BigDecimal X_value = new BigDecimal(X_MIN);
        BigDecimal increment = new BigDecimal("0.6");
        X_value = X_value.add(increment);

        for (int i = 0; i < list.size(); i++) {
            double d = list.get(i).getX();
            double x = X_value.doubleValue();
            int size = temp.size();
            if (X_value.doubleValue() == list.get(i).getX()) {
                temp.add(list.get(i));
            } else {
                sortAndReplace(temp, i - temp.size(), true);
                X_value = X_value.add(increment);
                temp.clear();
                i--; // this is creating the issue ************************************************************
            }
        }
    }

    /**
     * Takes in a temp list subsection of path_intersection_list (list of points with same Y values)
     * and sorts it by X value, and places back into path_intersection_list
     */
    public static void sortAndReplace(List<Point2d> list, int index, boolean vertical) {
        if (vertical) {
            for (Point2d perimeter_intersection_point : perimeter_intersection_points) {
                double temp = perimeter_intersection_point.getX();
                perimeter_intersection_point.setX(perimeter_intersection_point.getY());
                perimeter_intersection_point.setY(temp);
            }
            Collections.sort(list);
            for (Point2d perimeter_intersection_point : perimeter_intersection_points) {
                double temp = perimeter_intersection_point.getX();
                perimeter_intersection_point.setX(perimeter_intersection_point.getY());
                perimeter_intersection_point.setY(temp);
            }
        } else {
            Collections.sort(list);
        }

        for (int i = 0; i < list.size(); i++) {
            perimeter_intersection_points.set(index + i, list.get(i));
        }
    }

    public void paintComponent(Graphics g) {
        for (int i = 0; i < 1550; i += 2) {
            g.drawLine((int) (one_layer_path.get(i).getX() * 8 + 50), (int) (one_layer_path.get(i).getY() * 8 + 50), (int) (one_layer_path.get(i + 1).getX() * 8 + 50), (int) (one_layer_path.get(i + 1).getY() * 8 + 50));
        }
    }
}