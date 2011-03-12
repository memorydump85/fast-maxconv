package test;

import java.awt.*;
import java.util.*;

import javax.swing.*;

import april.util.*;
import april.vis.*;

import maxconv.*;
import maxconv.RectCovering.*;


public class Covering
{
    public static void main(String[] args)
    {
        JFrame jf = new JFrame();
        jf.setLayout(new BorderLayout());

        VisCanvas vc = new VisCanvas(new VisWorld());
        vc.setBackground(Color.black);
        jf.add(vc, BorderLayout.CENTER);

        ParameterGUI pg = new ParameterGUI();
        pg.addIntSlider("r", "Radius", 1, 49, 5);
        jf.add(pg, BorderLayout.SOUTH);

        final VisWorld.Buffer vb = vc.getWorld().getBuffer("rects");

        pg.addListener(new ParameterListener() {
            public void parameterChanged(ParameterGUI pg, String name)
            {
                VisDataGrid vdg = new VisDataGrid(-50*0.05, -50*0.05, 100, 100, 0.05, 0.05, false);
                ArrayList<Offsets> rects = RectCovering.getForDisc(pg.gi("r"));
                for (int i=rects.size()-1; i>=0; --i) {
                    Offsets off = rects.get(i);
                    final Color c = ColorUtil.setAlpha(Color.green.darker(), 32);

                    for (int y=50+off.top; y<=50+off.bottom; ++y)
                        for (int x=50+off.left; x<=50+off.right; ++x) {
                            vdg.set(x, y, 0.01, Color.magenta);
                        }

                    VisData vrect = new VisRectangle(
                            off.getHorizSpan(), off.getVertSpan(), new VisDataLineStyle(c, 2));
                    vrect.add(new VisDataFillStyle(c));
                    vb.addBuffered(vrect);
                }
                System.out.println();

                vb.addBuffered(vdg);
                vb.switchBuffer();
            }
        });

        jf.setSize(800, 800);
        jf.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        jf.setVisible(true);
    }
}
