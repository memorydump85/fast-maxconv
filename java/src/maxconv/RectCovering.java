package maxconv;

import java.awt.*;
import java.util.*;

public class RectCovering
{
    /**
     * Returns a set of rectangles that cover a circle of given radius.
     * The circle is discretized using Bresenham's midpoint circle algorithm.
     */
    public static ArrayList<Dimension> getForDisc(int radius)
    {
        ArrayList<Dimension> rects = new ArrayList<Dimension>();

        int f = 1 - radius;
        int ddF_x = 1;
        int ddF_y = -2 * radius;
        int x = 0;
        int y = radius;

        while (x <= y) {
            if (f >= 0) {
                rects.add(new Dimension(2*x+1, 2*y+1));
                if (x!=y) {
                    rects.add(new Dimension(2*y+1, 2*x+1));
                }

                y--;
                ddF_y += 2;
                f += ddF_y;
            }
            x++;
            ddF_x += 2;
            f += ddF_x;
        }

        return rects;
    }

    /** Ratio of half side-length to radius for a regular octagon */
    final static double RH_RATIO = 1 - 1.0/Math.sqrt(2);

    public static ArrayList<Offsets> getForOct(int radius)
    {
        int x = radius;
        int y = radius/2;

        ArrayList<Offsets> offs = new ArrayList<Offsets>();
        offs.add(new Offsets(-x, -y, x, y));
        offs.add(new Offsets(-y, -x, y, x));
        offs.add(new Offsets(-x, -y, x, y));
        offs.add(new Offsets(-x, y, x, -y));

        return offs;
    }

    public static class Offsets
    {
        public final int left;
        public final int top;
        public final int right;
        public final int bottom;

        public Offsets(int l, int t, int r, int b)
        {
            this.left = l;
            this.top = t;
            this.right = r;
            this.bottom = b;
        }
    }
}
