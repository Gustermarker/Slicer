package slicer;

import Jama.Matrix;

public class JamaTest {
    public static void main(String[] args) {
        Matrix A = new Matrix(new double[][] { {-1, -1, 0 },
                                               {3, 0, -1},
                                               {-5, 0, 0 } });

        Matrix b = new Matrix(new double[][] { {-1},
                                               {-2},
                                               {-5} });
        Matrix x = A.solve(b);
        Matrix residual = A.times(x).minus(b);
       // double rnorm = residual.normInf();

        System.out.println("A");
        A.print(9, 6);                // printf("%9.6f");

        System.out.println("b");
        b.print(9, 6);

        System.out.println("x");
        x.print(9, 6);
        System.out.println(x);

        System.out.println("residual");
        residual.print(9, 6);

    }

}