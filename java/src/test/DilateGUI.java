package test;

import java.awt.*;
import java.awt.event.*;
import java.awt.image.*;
import java.util.*;

import javax.swing.*;
import javax.swing.event.*;

import maxconv.*;

public class DilateGUI
{
    private static final class ParamChangeListener implements ChangeListener, ActionListener
    {
        private final int mAPSIZE;
        private final JSlider js;
        private final int[] map;
        private final JFrame jf;
        private final JComboBox jc;
        private final ImagePanel jim;

        private ParamChangeListener(int MAPSIZE, JSlider js, int[] map, JFrame jf, JComboBox jc, ImagePanel jim)
        {
            this.mAPSIZE = MAPSIZE;
            this.js = js;
            this.map = map;
            this.jf = jf;
            this.jc = jc;
            this.jim = jim;
        }

        @Override
        public void stateChanged(ChangeEvent ce)
        {
            redo();
        }

        private void redo()
        {
            int[] dilated = null;
            long start = System.currentTimeMillis();

            switch (jc.getSelectedIndex()) {
            case 0:
                dilated = MaxConv.block(map, mAPSIZE, mAPSIZE, js.getValue(), new int[mAPSIZE*mAPSIZE]);
                break;
            case 1:
                dilated = MaxConv.blockQuick(map, mAPSIZE, mAPSIZE, js.getValue(), new int[mAPSIZE*mAPSIZE]);
                break;
            case 2:
                dilated = MaxConv.blockBinary(map, mAPSIZE, mAPSIZE, js.getValue(), new int[mAPSIZE*mAPSIZE]);
                break;
            case 3:
                dilated = MaxConv.disc(map, mAPSIZE, mAPSIZE, js.getValue(), new int[mAPSIZE*mAPSIZE]);
                break;
            case 4:
                dilated = MaxConv.discBinary(map, mAPSIZE, mAPSIZE, js.getValue(), new int[mAPSIZE*mAPSIZE]);
                break;
            }

            long elapsed = System.currentTimeMillis() - start;

            jf.setTitle("Dilate: " + elapsed + " ms");

            jim.setImage(mergedImageFromMaps(map, dilated, mAPSIZE, mAPSIZE));
            jim.repaint();
        }

        @Override
        public void actionPerformed(ActionEvent ae)
        {
            redo();
        }
    }

    public static void main(String args[])
    {
        final JFrame jf = new JFrame("Dilate");
        jf.setLayout(new BorderLayout());

        final int MAPSIZE = 400;
        final int[] map = new int[MAPSIZE*MAPSIZE];

        Random r = new Random();
        for (int i=0; i<map.length; ++i) {
            map[i] = r.nextDouble() > 0.999 ? 1 : 0;
        }

        int[] dilated = MaxConv.blockBinary(map, MAPSIZE, MAPSIZE, 5, new int[MAPSIZE*MAPSIZE]);
        final ImagePanel jim = new ImagePanel(mergedImageFromMaps(map, dilated, MAPSIZE, MAPSIZE));

        jf.add(jim, BorderLayout.CENTER);

        final JComboBox jc = new JComboBox(new String[] {"Dilate Block", "Dilate Block Quick", "Dilate Block Binary", "Dilate Disc", "Dilate Disc Binary"});
        jf.add(jc, BorderLayout.NORTH);

        JPanel jp = new JPanel(new BorderLayout());
        jf.add(jp, BorderLayout.SOUTH);

        jp.add(new JLabel("Kernel Size"), BorderLayout.NORTH);

        final JSlider js = new JSlider(0, 100, 5);
        js.setMajorTickSpacing(10);
        js.setMinorTickSpacing(1);
        js.setPaintTicks(true);
        js.setPaintLabels(true);
        jp.add(js, BorderLayout.SOUTH);

        ParamChangeListener pcl = new ParamChangeListener(MAPSIZE, js, map, jf, jc, jim);
        jc.addActionListener(pcl);
        js.addChangeListener(pcl);

        jf.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        jf.setSize(600,600);
        jf.setVisible(true);
    }

    static BufferedImage imageFromBinaryMap(int[] map, int W, int H)
    {
        BufferedImage im = new BufferedImage(W, H, BufferedImage.TYPE_INT_RGB);
        final int out[] = ((DataBufferInt) (im.getRaster().getDataBuffer())).getData();

        for (int j=0; j<H; ++j)
            for (int i=0; i<W; ++i)
                out[W*j+i] = map[W*j+i]==0 ? 0 : 0xFFFFFF;

        return im;
    }

    static BufferedImage mergedImageFromMaps(int[] map1, int[] map2, int W, int H)
    {
        BufferedImage im = new BufferedImage(W, H, BufferedImage.TYPE_INT_RGB);
        final int out[] = ((DataBufferInt) (im.getRaster().getDataBuffer())).getData();

        for (int j=0; j<H; ++j)
            for (int i=0; i<W; ++i) {
                out[W*j+i] = map2[W*j+i]==0 ? 0x001111 : 0x008888;
                if (map1[W*j+i]!=0)
                    out[W*j+i] = 0xFFFFFF;
            }

        return im;
    }
}


class ImagePanel extends JPanel{

    BufferedImage im;

    public ImagePanel()
    {}

    public ImagePanel(BufferedImage im)
    {
        this.im = im;
    }

    public void setImage(BufferedImage im)
    {
        this.im = im;
    }

    @Override
    public Dimension getPreferredSize()
    {
        return new Dimension(im.getWidth(), im.getHeight());
    }

    @Override
    public void paintComponent(Graphics g)
    {
        int x = this.getWidth()/2 - im.getWidth() / 2;
        int y = this.getHeight()/2 - im.getHeight() / 2;

        g.drawImage(im, x, y, null);
    }
}
