package maxconv;

import java.util.*;

import maxconv.RectCovering.*;

public class MaxConv
{
    /**
     * Naive max-convolution/dilation using a block(square) kernel
     *
     * @param im
     *            input gray-level image
     * @param width
     *            width of the input image
     * @param height
     *            height of the input image
     * @param radius
     *            radius of the block kernel
     * @param imout
     *            gray-level image for storing the result
     * @return resulting dilated image (same as the <code>imout</code> parameter)
     */
    public static int[] block(int[] im, int width, int height, int radius, int[] imout)
    {
        for (int p=-radius; p <= radius; p++) {
            for (int q=-radius; q <= radius; q++) {

                int ymin = Math.max(0, 0-p);
                int ymax = Math.min(height, height - p);
                int xmin = Math.max(0, 0-q);
                int xmax = Math.min(width, width - q);

                for (int y = ymin; y < ymax; ++y) {
                    int i = (y+p)*width + (xmin+q);
                    int j = y*width + xmin;

                    for (int x = xmin; x < xmax; ++x, ++i, ++j)
                        imout[j] = (int) Math.max(imout[j], im[i]);
                }
            }
        }

        return imout;
    }

    /**
     * Fast max-convolution/dilation using a block(square) kernel
     *
     * @param im
     *            input gray-level image
     * @param width
     *            width of the input image
     * @param height
     *            height of the input image
     * @param radius
     *            radius of the block kernel
     * @param imout
     *            gray-level image for storing the result
     * @return resulting dilated image (same as the <code>imout</code> parameter)
     */
    public static int[] blockQuick(int[] im, int width, int height, int radius, int[] imout)
    {
        int dia = radius*2+1;
        HeapConvolve.convolve(im, 0, width, width, height, dia, dia, imout);

        return imout;
    }

    /**
     * Fast binary dilation using a block(square) kernel
     *
     * @param im
     *            input binary image
     * @param width
     *            width of the input image
     * @param height
     *            height of the input image
     * @param radius
     *            radius of the block kernel
     * @param imout
     *            binary image for storing the result
     * @return resulting dilated image (same as the <code>imout</code> parameter)
     */
    public static int[] blockBinary(int[] im, int width, int height, int radius, int[] imout)
    {
        return blockBinary(im, width, height, radius, radius, imout);
    }

    static int[] blockBinary(int[] im, int width, int height, int rH, int rW, int[] imout)
    {
        IntegralImage iim = new IntegralImage(width, height, im);

        for (int j=0; j<height; ++j) {
            for (int i=0; i<width; ++i) {
                int xMin = Math.max(0, i-rW);
                int xMax = Math.min(width-1, i+rW);
                int yMin = Math.max(0, j-rH);
                int yMax = Math.min(height-1, j+rH);

                int sum = (int) iim.sum(xMin, yMin, xMax, yMax);
                imout[width*j + i] = Math.max(imout[width*j + i], sum);
            }
        }

        return imout;
    }

    /**
     * Naive max-convolution/dilation using a disc kernel. This algorithm produces
     * circles that are different from circles obtained through Bresenham's midpoint
     * circle algorithm
     *
     * @param im
     *            input gray-level image
     * @param width
     *            width of the input image
     * @param height
     *            height of the input image
     * @param radius
     *            radius of the disc kernel
     * @param imout
     *            gray-level image for storing the result
     * @return resulting dilated image (same as the <code>imout</code> parameter)
     */
    public static int[] disc(int[] im, int width, int height, int radius, int[] imout)
    {
        for (int p=-radius; p <= radius; p++) {
            for (int q=-radius; q <= radius; q++) {

                if (p*p + q*q >= (radius+1)*(radius+1))
                    continue;

                int ymin = Math.max(0, 0-p);
                int ymax = Math.min(height, height - p);
                int xmin = Math.max(0, 0-q);
                int xmax = Math.min(width, width - q);

                for (int y = ymin; y < ymax; ++y) {
                    int i = (y+p)*width + (xmin+q);
                    int j = y*width + xmin;

                    for (int x = xmin; x < xmax; ++x, ++i, ++j)
                        imout[j] = (int) Math.max(imout[j], im[i]);
                }
            }
        }

        return imout;
    }

    /**
     * Fast binary dilation using a disc kernel
     *
     * @param im
     *            input binary image
     * @param width
     *            width of the input image
     * @param height
     *            height of the input image
     * @param radius
     *            radius of the disc kernel
     * @param imout
     *            binary image for storing the result
     * @return resulting dilated image (same as the <code>imout</code> parameter)
     */
    public static int[] discBinary(int[] im, int width, int height, int radius, int[] imout)
    {
        ArrayList<Offsets> offs = RectCovering.getForDisc(radius);
        for (Offsets off : offs) {
            blockBinary(im, width, height, off.right, off.bottom, imout);
        }

        return imout;
    }

    public static int[] octBinary(int[] im, int width, int height, int radius, int[] imout)
    {
        int[] rim = rotate45(im, width, height);

        IntegralImage iim = new IntegralImage(width, height, im);
        IntegralImage iimr = new IntegralImage(2*width, 2*height, rim);

        ArrayList<Offsets> offs = RectCovering.getForOct(radius);
        for (int n=0; n<2; ++n) {
            Offsets f  = offs.get(n);
            for (int j=0; j<height; ++j) {
                for (int i=0; i<width; ++i) {
                    final int x0 = i+f.left;
                    final int y0 = j+f.top;
                    final int x1 = i+f.right;
                    final int y1 = j+f.bottom;

                    int xMin = Math.max(0, x0);
                    int yMin = Math.max(0, y0);
                    int xMax = Math.min(width-1, x1);
                    int yMax = Math.min(height-1, y1);
                    int sum = (int) iim.sum(xMin, yMin, xMax, yMax);

                    imout[width*j + i] = Math.max(imout[width*j + i], sum);
                }
            }
        }

        for (int n=2; n<4; ++n) {
            Offsets f  = offs.get(n);
            for (int j=0; j<height; ++j) {
                for (int i=0; i<width; ++i) {
                    final int x0 = i+f.left;
                    final int y0 = j+f.top;
                    final int x1 = i+f.right;
                    final int y1 = j+f.bottom;

                    int xr0 = width-y0+x0;
                    int yr0 = x0+y0;
                    int xr1 = width-y1+x1;
                    int yr1 = x1+y1;

                    int xMin = Math.max(0, xr0);
                    int yMin = Math.max(0, yr0);
                    int xMax = Math.min(2*width-1, xr1);
                    int yMax = Math.min(2*height-1, yr1);
                    int sum = (int) iimr.sum(xMin, yMin, xMax, yMax);

                    imout[width*j + i] = Math.max(imout[width*j + i], sum);
                }
            }
        }

        return imout;
    }

    static int[] rotate45(int[] x, int W, int H)
    {
        int[] y = new int[(2*W)*(2*H)];

        for(int j=0; j<H; ++j)
            for (int i=0; i<W; ++i)
                y[(i+j)*2*W + (W-j+i)] = x[j*W+i];

        return y;
    }

}


class HeapConvolve
{
    static final boolean isInBounds(int v, int vMax)
    {
        return (v>=0) && (v<vMax);
    }

    static final int[] convolve(
        final int in[],     /* input data*/
        final int xstart,   /* starting column of stripe in input data */
        final int xend,     /* ending column of stripe in input data */
        final int W,        /* width of input data */
        final int H,        /* height of input data */
        final int KW,       /* kernel block width */
        final int KH,       /* kernel block height */
        int out[]           /* convolved output data */
        )
    {
        assert (KW%2 != 0);
        assert (KH%2 != 0);
        assert (H > KH);
        assert (W > KW);

        final int Q = KH/2;
        final int R = KW/2;

        /* Initialize column histograms */
        CountingHeap colHeap[] = new CountingHeap[W];

        for (int n=0; n<colHeap.length; ++n)
            colHeap[n] = new CountingHeap(256);

        for (int y = 0; y < Q; ++y)
            for (int x = xstart; x < xend; ++x)
                colHeap[x].add(in[y*W+x]);

        /* Initialize histogram */
        CountingHeap heap = new CountingHeap(256);


        for (int y = 0; y < H; ++y) {
            /* prepare initial kernel histogram for this row*/
            heap.clear();

            for (int x = xstart; x < xstart+R; ++x) {
                if (isInBounds(y-Q-1, H))
                    colHeap[x].remove(in[(y-Q-1)*W+x]);

                if (isInBounds(y+Q, H))
                    colHeap[x].add(in[(y+Q)*W+x]);

                heap.merge(colHeap[x]);
            }

            for (int x = xstart; x < xend; ++x) {
                /* remove old left column */
                if (isInBounds(x-R-1, xend))
                    heap.unmerge(colHeap[x-R-1]);

                /* prepare new right column and merge */
                if (isInBounds(x+R, xend)) {
                    if (isInBounds(y-Q-1, H))
                        colHeap[x+R].remove(in[(y-Q-1)*W+(x+R)]);

                    if (isInBounds(y+Q, H))
                        colHeap[x+R].add(in[(y+Q)*W+(x+R)]);

                    heap.merge(colHeap[x+R]);
                }

                out[y*W+x] = Math.max(out[y*W+x], heap.getMax());
            }
        }

        return out;
    }
}
