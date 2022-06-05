package slicer;

import java.util.*;

public class Main {

    public static void main(String[] args) {
        Point3d p1 = new Point3d(.5, .5, .5);
        Point3d p2 = new Point3d(2, 2, 2);
        Point3d p3 = new Point3d(3, 3, 3);
        Point3d p4 = new Point3d(2, 3, 1);

        Triangle tri = new Triangle(p1, p2, p3);
        Triangle tri2 = new Triangle(p3, p2, p4);

//        List<Triangle> list =  new ArrayList<Triangle>();
//        list.add(tri2);
//        list.add(tri);
//        System.out.println("Z_min before sort: " + list.get(0).Z_min);
//
//        Collections.sort(list);
//        System.out.println("Z_min after sort: " + list.get(0).Z_min);
//
//
//        String s = "vertex 3.1521 7.935733e+01 5.69";
//        String[] xyz = s.split(" ");
//        System.out.println(xyz[0]);
//        System.out.println(xyz[1]);
//        System.out.println(xyz[2]);
//        System.out.println(xyz[3]);
//
//        Double d =  Double.parseDouble(xyz[2]);
//        System.out.println("d = " + d);



    }

}