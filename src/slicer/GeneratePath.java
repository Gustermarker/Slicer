package slicer;

import java.util.ArrayList;
import java.util.List;

/**
 * This class generates that path that the nozzle will take each layer.
 */

public class GeneratePath {

    // list that contains the path that will turn into GCODE. represented by points
    List<Point2d> path_list = new ArrayList<Point2d>();


    /*
     *                  point 1       point 2       point3       point 4
     * input list = {(X1_a, Y1_a), (X1_b, X2_b), (X2_a, Y2_a), (X2_b, Y2_b), ... }
     */
    public List<Point2d> GeneratePath(List<Point2d> perim_intersections, List<Point3d> perimeter) {
        List<Point2d> temp = new ArrayList<Point2d>();
        List<Point2d> list = new ArrayList<Point2d>();
        // System.out.println("path_list before: " + path_list.size());
        System.out.println(perimeter.get(0).getX());
        //GeneratePerimiters(perimeter);
        //System.out.println("path_list after: " + path_list.size());
        list = perim_intersections;

//        int currIndex = 0;
//
//        double Y_curr;
//        double X_left;
//        double prev_X_left, prev_X_right;
//        boolean right = true; // is direction of path left or right
//        boolean reset = false; // if this condition is true, we restart the while loop from the top
//        int index;
//
//        while (list.size() > 0) {
//            Y_curr = list.get(0).getY();
//            X_left = list.get(0).getX();
//            prev_X_left = list.get(0).getX();
//            prev_X_right = list.get(1).getX();
//            reset = true;
//            index = 0;
//
//            // always start by adding the bottom left line to 'path_list', then remove those lines from 'list'
//            if (right) {
//                path_list.add(list.get(0));
//                path_list.add(list.get(1));
//                right = false;
//            } else { // left
//                path_list.add(list.get(1));
//                path_list.add(list.get(0));
//                right = true;
//            }
//            list.remove(0);
//            list.remove(0);
//
//            if (list.size() == 0) {
//                break;
//            }
//
//            do {
//                // move to next layer
//                for (int j = index; j < list.size(); j += 2) {
//                    if (list.get(j).getY() > Y_curr) {
//                        index = j;
//                        Y_curr = list.get(j).getY();
//                        break;
//                    }
//                }
//                reset = true; // default true, will change if reset condition met
//
//                // this loops through all points at layer Y_curr
//                //for (int i = index; list.get(i).getY() == Y_curr || i < list.size() - 1; i += 2) {
//                for (int i = index; i < list.size() - 1; i += 2) {
//                    //System.out.println("urmom");
//                    if (list.get(i).getY() > Y_curr) {
//                        break;
//                    }
//
//                    if (list.get(i).getX() >= prev_X_left && list.get(i).getX() <= prev_X_right ||
//                        list.get(i).getX() <= prev_X_left && list.get(i).getX() <= prev_X_right) {
//                        if (right) {
//                            path_list.add(list.get(i));
//                            path_list.add(list.get(i + 1));
//                            prev_X_left = list.get(i).getX();
//                            prev_X_right = list.get(i + 1).getX();
//                            right = false;
//                        } else { // left
//                            path_list.add(list.get(i + 1));
//                            path_list.add(list.get(i));
//                            prev_X_left = list.get(i).getX();
//                            prev_X_right = list.get(i + 1).getX();
//                            right = true;
//                        }
//                        list.remove(i);
//                        list.remove(i);
//                        index = i;
//                        reset = false; // never switches back to true  ???
//                        break;
//                    }
//                    if (i > list.size() - 1) {
//                        reset = true; // hmmm
//                        break;
//                    }
//
//                }
//            } while (!reset);
//        }

        return path_list;
    }

    /*
     * this generates the path for all of the perimeters. takes the unordered perimeter list and orders it
     * so that all lines are connected head to tails
     */
    public List<Point2d> GeneratePerims(List<Point3d> input) {
        List<Point2d> perimeter = new ArrayList<Point2d>();

        // turns it from a Point3d list into a Point2d list just to makes things cleaner
        for (int i = 0; i < input.size(); i++) {
            perimeter.add(new Point2d(input.get(i).getX(), input.get(i).getY()));
        }

        path_list.add(perimeter.get(0));
        path_list.add(perimeter.get(1));

        double endPointX = perimeter.get(1).getX();
        double endPointY = perimeter.get(1).getY();
        double firstPointX = perimeter.get(0).getX();
        double firstPointY = perimeter.get(0).getY();

        perimeter.remove(0);
        perimeter.remove(0);

        while (perimeter.size() > 0) {
            for (int i = 0; i < perimeter.size() - 1; i += 2) {
                if ((endPointX - 0.001 < perimeter.get(i).getX() && endPointX + 0.001 > perimeter.get(i).getX())
                 && (endPointY - 0.001 < perimeter.get(i).getY() && endPointY + 0.001 > perimeter.get(i).getY())) {

                    path_list.add(perimeter.get(i));
                    path_list.add(perimeter.get(i + 1));

                    endPointX = perimeter.get(i + 1).getX();
                    endPointY = perimeter.get(i + 1).getY();

                    perimeter.remove(i);
                    perimeter.remove(i);

                    if ((endPointX - 0.001 < firstPointX && endPointX + 0.001 > firstPointX)
                     && (endPointY + 0.001 > firstPointY && endPointY + 0.001 > firstPointY)) { // a perimeter loop has been completed

                        if (i + 2 < perimeter.size() - 1) {
                            firstPointX = perimeter.get(i).getX();
                            firstPointY = perimeter.get(i).getY();
                            endPointX = perimeter.get(i + 1).getX();
                            endPointY = perimeter.get(i + 1).getY();

                            path_list.add(perimeter.get(i));
                            path_list.add(perimeter.get(i + 1));
                            perimeter.remove(i);
                            perimeter.remove(i);
                        }
                    }
                    break;

                } else if ((endPointX - 0.001 < perimeter.get(i + 1).getX() && endPointX + 0.001 > perimeter.get(i + 1).getX())
                        && (endPointY - 0.001 < perimeter.get(i + 1).getY() && endPointY + 0.001 > perimeter.get(i + 1).getY())) {

                    path_list.add(perimeter.get(i + 1));
                    path_list.add(perimeter.get(i));

                    endPointX = perimeter.get(i).getX();
                    endPointY = perimeter.get(i).getY();

                    perimeter.remove(i);
                    perimeter.remove(i);

                    if ((endPointX - 0.001 < firstPointX && endPointX + 0.001 > firstPointX) && (endPointY + 0.001 > firstPointY && endPointY + 0.001 > firstPointY)) { // a perimeter loop has been completed
                        if (i + 2 < perimeter.size() - 1) {
                            firstPointX = perimeter.get(i).getX();
                            firstPointY = perimeter.get(i).getY();

                            endPointX = perimeter.get(i + 1).getX();
                            endPointY = perimeter.get(i + 1).getY();

                            path_list.add(perimeter.get(i));
                            path_list.add(perimeter.get(i + 1));
                            perimeter.remove(i);
                            perimeter.remove(i);
                        }
                    }
                    break;
                }
            }
        }

        return path_list;
    }

}