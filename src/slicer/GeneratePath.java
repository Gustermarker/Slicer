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
    public List<Point2d> GeneratePath(List<Point2d> input) {
        List<Point2d> temp = new ArrayList<Point2d>();
        List<Point2d> list = new ArrayList<Point2d>();
        list = input;

        int currIndex = 0;

        double Y_curr;
        double X_left;
        double prev_X_left, prev_X_right;
        boolean right = true; // is direction of path left or right
        boolean reset = false; // if this condition is true, we restart the while loop from the top
        int index;

        while (list.size() > 0) {
            Y_curr = list.get(0).getY();
            X_left = list.get(0).getX();
            prev_X_left = list.get(0).getX();
            prev_X_right = list.get(1).getX();
            reset = true;
            index = 0;

            // always start by adding the bottom left line to 'path_list', then remove those lines from 'list'
            if (right) {
                path_list.add(list.get(0));
                path_list.add(list.get(1));
                right = false;
            } else { // left
                path_list.add(list.get(1));
                path_list.add(list.get(0));
                right = true;
            }
            list.remove(0);
            list.remove(0);

            if (list.size() == 0) {
                System.out.println("breaqk;");
                break;
            }

            do {
                // move to next layer
                for (int j = index; j < list.size(); j += 2) {
                    if (list.get(j).getY() > Y_curr) {
                        index = j;
                        Y_curr = list.get(j).getY();
                        //System.out.println("Y_curr: " + Y_curr);
                        break;
                    }
                }

                //System.out.println("Y_curr: " + Y_curr);

                reset = true; // default true, will change if reset condition met

                // this loops through all points at layer Y_curr
                //for (int i = index; list.get(i).getY() == Y_curr || i < list.size() - 1; i += 2) {
                for (int i = index; i < list.size() - 1; i += 2) {
                    if (list.get(i).getY() > Y_curr) {
                        break;
                    }

                    // if this condition is never met, we will reset
                    //if (list.get(i).getX() < X_left && list.get(i + 1).getX() > X_left) {
//                    if (list.get(i).getX() < X_left && list.get(i + 1).getX() > X_left) {
                    //if (prev_X_left < X_left && prev_X_right > X_left) {
                    if (list.get(i).getX() >= prev_X_left && list.get(i).getX() <= prev_X_right ||
                        list.get(i).getX() <= prev_X_left && list.get(i).getX() <= prev_X_right) {
                        if (right) {
                            path_list.add(list.get(i));
                            path_list.add(list.get(i + 1));
                            prev_X_left = list.get(i).getX();
                            prev_X_right = list.get(i + 1).getX();
                            right = false;
                        } else { // left
                            path_list.add(list.get(i + 1));
                            path_list.add(list.get(i));
                            prev_X_left = list.get(i).getX();
                            prev_X_right = list.get(i + 1).getX();
                            right = true;
                        }
                        list.remove(i);
                        list.remove(i);
                        index = i;
                        reset = false;
                        break;
                    }
                    if (i > list.size() - 1) {
                        break;
                    }

                }
            } while (!reset);

        }

        return path_list;

    }

}
