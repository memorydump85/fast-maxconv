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
    static final int SIZE = 400;

    final JFrame jf = new JFrame();
    final JComboBox jcb = new JComboBox();
    final ImagePanel jimp = new ImagePanel();
    final JSlider js = new JSlider(0, 100, 5);
    final EventListener listener = new EventListener();

    final int[] map = new int[SIZE*SIZE];

    DilateGUI()
    {
        jf.setLayout(new BorderLayout());

        /* Combo box to allow algorithm selection */
        jcb.addItem("Block");
        jcb.addItem("Quick Block");
        jcb.addItem("Binary Block");
        jcb.addItem("--------------------");
        jcb.addItem("Disc");
        jcb.addItem("Binary Disc");

        jcb.addActionListener(listener);
        jf.add(jcb, BorderLayout.NORTH);

        /* Random map and image panel */
        Random r = new Random();
        for (int i=0; i<map.length; ++i) {
            map[i] = r.nextDouble() > 0.999 ? 1 : 0;
        }

        jf.add(jimp, BorderLayout.CENTER);

        /* Slider for kernel size */
        JPanel jp = new JPanel(new BorderLayout());
        jp.add(new JLabel("Kernel Size"), BorderLayout.NORTH);

        js.setMajorTickSpacing(10);
        js.setMinorTickSpacing(1);
        js.setPaintTicks(true);
        js.setPaintLabels(true);
        js.addChangeListener(listener);
        jp.add(js, BorderLayout.SOUTH);

        jf.add(jp, BorderLayout.SOUTH);

        /* Now display our GUI */
        listener.displayDilationResult();

        jf.setSize(600,600);
        jf.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        jf.setVisible(true);
    }

    class EventListener implements ChangeListener, ActionListener
    {
        @Override
        public void stateChanged(ChangeEvent arg0)
        {
            displayDilationResult();
        }

        @Override
        public void actionPerformed(ActionEvent arg0)
        {
            displayDilationResult();
        }

        public void displayDilationResult()
        {
            int[] result = new int[SIZE*SIZE];
            long start = System.currentTimeMillis();

            switch (jcb.getSelectedIndex()) {
            case 0:
                MaxConv.block(map, SIZE, SIZE, js.getValue(), result);
                break;
            case 1:
                MaxConv.blockQuick(map, SIZE, SIZE, js.getValue(), result);
                break;
            case 2:
                MaxConv.blockBinary(map, SIZE, SIZE, js.getValue(), result);
                break;
            case 3:
                /* Dummy */
                return;
            case 4:
                result = MaxConv.disc(map, SIZE, SIZE, js.getValue(), result);
                break;
            case 5:
                result = MaxConv.discBinary(map, SIZE, SIZE, js.getValue(), result);
                break;
            }

            long elapsed = System.currentTimeMillis() - start;

            jf.setTitle("Dilate: " + elapsed + " ms");

            jimp.setImage(imageFromMaps(map, result, SIZE, SIZE));
            jimp.repaint();
        }
    }

    static BufferedImage imageFromMaps(int[] map1, int[] map2, int W, int H)
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

    public static void main(String args[])
    {
        new DilateGUI();
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
