package slicer;

import Jama.Matrix;

public class Test {

    public static void main(String[] args) {
        Point3d p1 = new Point3d(5, 7, 3);
        Point3d p2 = new Point3d(1, 3, 9);

        findLinePlaneIntersectionCoords(1, 3, 4, 3, -2, 5, 0, 0, 1, -5);
        //intersection(p1, p2, 3);
    }

    public static void findLinePlaneIntersectionCoords(double px, double py, double pz, double qx, double qy, double qz, double a, double b, double c, double d) {
        double tDenom = (qz - pz);
        if (tDenom == 0)
            System.out.println("Line is in plane");

        double t = -(pz + d) / tDenom;

        System.out.println("x: " + (px + t * (qx - px)));
        System.out.println("y: " + (py + t * (qy - py)));
        System.out.println("z: " + (pz + t * (qz - pz)));

    }

    static void intersection(Point3d a1, Point3d a2, double layerHeight) {
        // currently going till I hit z_max. this causes one problem. we are testing the intersection
        // of two lines. on will have a higher Z. need to make the lower one stop
        // or just have some conditional in this method.

        Point3d difference_a = new Point3d(a1.getX() - a2.getX(), a1.getY() - a2.getY(), a1.getZ() - a2.getZ());

        double x_a = a1.getX();
        double y_a = a1.getY();
        double z_a = a1.getZ() - layerHeight;
        double t_a = z_a / difference_a.getZ();

        x_a -= t_a * difference_a.getX();
        y_a -= t_a * difference_a.getY();
        z_a -= t_a * difference_a.getZ();

        //intersection_list.add(new Point3d(x_a, y_a, z_a));
        //intersection_list.add(new Point3d(x_b, y_b, z_b));

        // add to map (new Point3d(x, y, z)

    }

}
