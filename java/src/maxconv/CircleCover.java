package maxconv;

import java.awt.*;
import java.util.*;

public class CircleCover
{
    public static ArrayList<Dimension> getRects(int radius)
    {
        ArrayList<Dimension> rects = new ArrayList<Dimension>();

        int f = 1 - radius;
        int ddF_x = 1;
        int ddF_y = -2 * radius;
        int x = 0;
        int y = radius;

        while (x < y) {
            if (f >= 0) {
                rects.add(new Dimension(2*(x+1)+1, 2*y+1));
                rects.add(new Dimension(2*y+1, 2*(x+1)+1));

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
}
