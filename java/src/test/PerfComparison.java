package test;

import java.util.*;

import maxconv.*;

public class PerfComparison
{
    static final int SAMPLES = 50;

    static interface Dilator
    {
        public int[] dilate(int[] map, int width, int height, int radius);
    }

    static double lowerMedian(List<Double> values)
    {
        Collections.sort(values);
        return values.get(values.size()/2);
    }

    static double evalPerf(Dilator d, int[] map, int width, int height, int radius)
    {
        ArrayList<Double> times = new ArrayList<Double>();

        for (int i=0; i<SAMPLES; ++i) {
            long tic = System.nanoTime();
            d.dilate(map, width, height, radius);
            long toc = System.nanoTime();

            times.add((toc - tic)/10.0e6);
        }

        return lowerMedian(times);
    }

    public static HashMap<String, ArrayList<double[]>> getTimes(int W, int H)
    {
        /* Dilators */
        HashMap<String, Dilator> dilators = new HashMap<String, Dilator>();

        dilators.put("Naive-Block", new Dilator() {
            @Override
            public int[] dilate(int[] map, int width, int height, int radius)
            {
                return MaxConv.block(map, width, height, radius, new int[map.length]);
            }
        });

        dilators.put("Quick-Block", new Dilator() {
            @Override
            public int[] dilate(int[] map, int width, int height, int radius)
            {
                return MaxConv.blockQuick(map, width, height, radius, new int[map.length]);
            }
        });

        dilators.put("Binary-Block", new Dilator() {
            @Override
            public int[] dilate(int[] map, int width, int height, int radius)
            {
                return MaxConv.blockBinary(map, width, height, radius, new int[map.length]);
            }
        });

        dilators.put("Naive-Disc", new Dilator() {
            @Override
            public int[] dilate(int[] map, int width, int height, int radius)
            {
                return MaxConv.disc(map, width, height, radius, new int[map.length]);
            }
        });

        dilators.put("Binary-Disc", new Dilator() {
            @Override
            public int[] dilate(int[] map, int width, int height, int radius)
            {
                return MaxConv.discBinary(map, width, height, radius, new int[map.length]);
            }
        });

        /* Performance Data */
        HashMap<String, ArrayList<double[]>> times = new HashMap<String, ArrayList<double[]>>();
        times.put("Naive-Block", new ArrayList<double[]>());
        times.put("Quick-Block", new ArrayList<double[]>());
        times.put("Binary-Block", new ArrayList<double[]>());
        times.put("Naive-Disc", new ArrayList<double[]>());
        times.put("Binary-Disc", new ArrayList<double[]>());

        /* Random map */
        int[] map = new int[W*H];

        Random r = new Random();
        for (int i=0; i<map.length; ++i) {
            map[i] = r.nextDouble() > 0.999 ? 1 : 0;
        }

        for (int radius=0; radius<=30; radius+=3) {
            System.out.println("Evaluating for radius=" + radius);

            for (String method : times.keySet()) {
                double elapsed = evalPerf(dilators.get(method), map, W, H, radius);
                times.get(method).add(new double[] {radius, elapsed});
            }
        }

        return times;
    }

    public static void main(String[] args)
    {
        HashMap<String, ArrayList<double[]>> times = getTimes(400, 400);

        for (String method: times.keySet()) {
            System.out.println(method);

            ArrayList<double[]> data = times.get(method);
            for (double[] d : data) {
                System.out.printf("%2d: %.2f, ", (int)d[0], d[1]);
            }
            System.out.println();
        }
    }
}
