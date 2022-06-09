package slicer;

import javax.swing.*;
import java.awt.*;
import java.io.File;
import java.io.FileNotFoundException;
import java.util.*;
import java.util.List;

/*
    this class reads in data i

    take the list of 3d face plane. for each plane, calculate the intersection
    points for all possible layer heights in that face. ie. if Z_min is
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

// hashmap(key, <point 1, point2>)

public class ParseSTL extends JPanel {

    //    static Map<Double, List<Point3d>> map = new HashMap<Double, List<Point3d>>();
    static Map<Double, Point3d> map = new HashMap<Double, Point3d>(); // currently not used
    static List<Point3d> intersection_list = new ArrayList<Point3d>();

    static double LAYER_HEIGHT = 0.2;

    public static void main(String[] args) {
        List<Triangle> list = new ArrayList<Triangle>();
        //List<Point3d> intersection_list = new ArrayList<Point3d>();
        list = readSTL(list);

        Collections.sort(list);
        findIntersections(list);


        JFrame.setDefaultLookAndFeelDecorated(true);
        JFrame frame = new JFrame("Draw Points");
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        frame.setBackground(Color.white);
        frame.setSize(1000, 1000);

        ParseSTL panel = new ParseSTL();

        frame.add(panel);

        frame.setVisible(true);


        for (Point3d p : intersection_list) {
            System.out.println("x: " + p.getX() + ", y: " + p.getY() + ", z: " + p.getZ());
        }

        for (Triangle l : list) {
            System.out.println("(" + l.A.getX() + ", " + l.A.getY() + ", " + l.A.getZ() + ")" + ", (" + l.B.getX() + ", " + l.B.getY() + ", " + l.B.getZ() + ")" + ", (" + l.C.getX() + ", " + l.C.getY() + ", " + l.C.getZ() + ")");
        }
    }

    public static List<Triangle> readSTL(List<Triangle> faceList) {
        try {
            Scanner scanner = new Scanner(new File("/Users/gustavoestermarker/IdeaProjects/Slicer/testSTL.stl"));
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


    if !(new Hashmap(key,
     */

    public static void findIntersections(List<Triangle> list) {
        double current_Z_height = 2.5;
        double temp_Z_height;
        int index = 0;
        int startIndex = 0;
        int i = 0;

        // computes intersections for each layer, one at a time
        // NOTE: increase efficiency of the for loop. it's not perfect, repeats and such
        while (true) {
            // go through all faces at that Z height
            for (i = 0; i < list.size() - 1; i++) {
                if (list.get(i).Z_min > current_Z_height) {
                    //startIndex = i;
                    break;
                }
                if (list.get(i).Z_max > current_Z_height) {
                    twoLines(list.get(i).A, list.get(i).B, list.get(i).C, current_Z_height);
                }
            }

            break;
        }
    }

    // determine which two lines the plane intersects
    // 3 possibilities: 1) AB and BC, 2) AB and AC, 3) AC and BC
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

        double t = -(pz + layerHeight) / tDenom;
        double x = (px + t * (qx - px));
        double y = (py + t * (qy - py));
        double z = (pz + t * (qz - pz));

        if (z == 0)
            return;
        intersection_list.add(new Point3d(x, y, z));
    }


    public void paintComponent(Graphics g) {

        for (int i = 0; i < intersection_list.size(); i++) {

        }

        for (Point3d p : intersection_list) {
            g.drawLine((int)(p.getX() * 10) , (int)(p.getY() * 10) , (int)(p.getX() * 10) , (int)(p.getY() * 10) );
        }

    }
}