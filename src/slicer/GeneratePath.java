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
        boolean right = true; // is direction of path left or right
        boolean reset = false; // if this condition is true, we restart the while loop from the top
        int index;

        while (list.size() > 0) {
            Y_curr = list.get(0).getY();
            X_left = list.get(0).getX();
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
                        break;
                    }
                }

                reset = true; // default true, will change if reset condition met

                // this loops through all points at layer Y_curr
                for (int i = index; list.get(i).getY() == Y_curr; i += 2) {
                    // if this condition is never met, we will reset
                    if (list.get(i).getX() < X_left && list.get(i + 1).getX() > X_left) {
                        if (right) {
                            path_list.add(list.get(i));
                            path_list.add(list.get(i + 1));
                            X_left = list.get(i).getX();
                            right = false;
                        } else { // left
                            path_list.add(list.get(i + 1));
                            path_list.add(list.get(i));
                            X_left = list.get(i + 1).getX();
                            right = true;
                        }
                        list.remove(i);
                        list.remove(i);
                        reset = false;
                        break;
                    }
                    if  (list.size() == 2)
                        break;
                }
            } while (!reset);

        }

        return path_list;

    }

}
