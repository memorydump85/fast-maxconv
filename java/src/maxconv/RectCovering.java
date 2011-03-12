package maxconv;

import java.util.*;

public class RectCovering
{
    /**
     * Returns a set of rectangles that cover a circle of given radius.
     * The circle is discretized using Bresenham's midpoint circle algorithm.
     */
    public static ArrayList<Offsets> getForDisc(int radius)
    {
        ArrayList<Offsets> rects = new ArrayList<Offsets>();

        int f = 1 - radius;
        int ddF_x = 1;
        int ddF_y = -2 * radius;
        int x = 0;
        int y = radius;

        while (x <= y) {
            if (f >= 0) {
                rects.add(new Offsets(-x, -y, x, y));
                if (x!=y) {
                    rects.add(new Offsets(-y, -x, y, x));
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

        public int getHorizSpan()
        {
            return right - left + 1;
        }

        public int getVertSpan()
        {
            return bottom - top + 1;
        }
    }
}
