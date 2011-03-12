package maxconv;

import java.awt.*;
import java.util.*;

import javax.swing.*;

import april.util.*;
import april.vis.*;

public class OctDiscApproxTest
{
    public static void mai1n(String[] args)
    {
        JFrame jf = new JFrame();
        jf.setLayout(new BorderLayout());

        VisCanvas vc = new VisCanvas(new VisWorld());
        vc.setBackground(Color.black);
        jf.add(vc, BorderLayout.CENTER);

        final VisWorld.Buffer vb = vc.getWorld().getBuffer("rects");

        ParameterGUI pg = new ParameterGUI();
        pg.addIntSlider("discR", "Disc Radius", 3, 100, 3);
        pg.addIntSlider("octR", "Oct Radius", 3, 100, 3);
        jf.add(pg, BorderLayout.SOUTH);

        final int W = 100;
        final int H = 100;
        final int[] frame = new int[W*H];
        frame[H/2*W + W/2] = 1;

        pg.addListener(new ParameterListener() {
            public void parameterChanged(ParameterGUI pg, String name)
            {
                int[] map1 = Arrays.copyOf(frame, frame.length);
                MaxConv.discBinary(frame, W, H, pg.gi("discR"), map1);

                int[] map2 = Arrays.copyOf(frame, frame.length);
                MaxConv.octBinary(frame, W, H, pg.gi("octR"), map2);

                VisDataGrid vdg1 = new VisDataGrid(0, 0, W, H, 0.1, 0.1, true);
                VisDataGrid vdg2 = new VisDataGrid(0, 0, W, H, 0.1, 0.1, true);
                for (int j=0; j<H; ++j)
                    for (int i=0; i<W; ++i) {
                        if (map1[j*W+i]==1)
                            vdg1.set(i, j, 0, Color.green);
                        if (map2[j*W+i]==1) {
                            vdg2.set(i, j, 0, ColorUtil.setAlpha(Color.magenta, 64));
                        } else {
                            vdg2.set(i, j, 0, ColorUtil.setAlpha(Color.magenta, 0));
                        }
                    }

                vb.addBuffered(vdg1);
                vb.addBuffered(vdg2);
                vb.switchBuffer();
            }
        });

        jf.setSize(800, 800);
        jf.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        jf.setVisible(true);
    }

    public static void main(String[] args)
    {
        for (int discR=3; discR<100; ++discR) {
            int W = 250;
            int H = 250;
            int[] frame = new int[W*H];
            frame[H/2*W + W/2] = 1;

            int[] map1 = Arrays.copyOf(frame, frame.length);
            MaxConv.discBinary(frame, W, H, discR, map1);

            int lastCost = Integer.MAX_VALUE;
            for (int octR=3; octR<100; ++octR) {
                int[] map2 = Arrays.copyOf(frame, frame.length);
                MaxConv.octBinary(frame, W, H, octR, map2);
                int[] cost = compare(map1, map2);
                if (cost[0]==0) {
                    if (lastCost < cost[1]) {
                        map2 = Arrays.copyOf(frame, frame.length);
                        MaxConv.octBinary(frame, W, H, octR-1, map2);
                        int onesDisc = countOnes(map1);
                        int onesOct = countOnes(map2);
                        System.out.printf("%d,%d,%d,%f;\n", discR, octR-1, lastCost, (onesOct-onesDisc)/(float)onesDisc);
                        break;
                    }
                    lastCost = cost[1];
                }
            }
        }
    }

    static int countOnes(int[] map)
    {
        int count = 0;
        for (int m : map)
            count += m;

        return count;
    }

    static int[] compare(int[] map1, int[] map2)
    {
        assert (map1.length == map2.length);

        int negatives = 0;
        int positives = 0;
        for (int i=0; i<map1.length; ++i) {
            int diff =  map2[i] - map1[i];
            if (diff > 0)
                ++positives;
            else if (diff < 0)
                ++negatives;
        }

        return new int[] {negatives, positives};
    }
}
