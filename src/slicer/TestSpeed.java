package slicer;

public class TestSpeed {

    public static void main(String[] args) {

        long startTime = System.nanoTime();


        for (long i = 0; i < (long) 1000000; i++) {
            int x = 0, y = 0;
            x += 7;
            y = x / 7;
            for (int j = 0; j < 1000; j++) {
            }
        }

        long endTime   = System.nanoTime();
        long totalTime = endTime - startTime;
        System.out.println((double) totalTime / 1000000000);
    }

}
