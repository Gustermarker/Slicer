package slicer;

public class Triangle implements Comparable<Triangle> {

    Point3d A, B, C;
    public Double Z_max, Z_min;

    public Triangle(Point3d A, Point3d B, Point3d C) {
        this.A = A;
        this.B = B;
        this.C = C;
        min_max();
    }

    // get Z range for each Triangle face
    void min_max() {
        Z_max = A.getZ();
        Z_min = A.getZ();

        if (B.getZ() > Z_max)
            Z_max = B.getZ();
        else if (B.getZ() < Z_min)
            Z_min = B.getZ();

        if (C.getZ() > Z_max)
            Z_max = C.getZ();
        else if (C.getZ() < Z_min)
            Z_min = C.getZ();
    }

    @Override
    public int compareTo(Triangle t) {
        return this.Z_min.compareTo(t.Z_min);
    }
}