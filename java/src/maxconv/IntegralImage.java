package maxconv;


public class IntegralImage
{
    int W;
    int H;
    int[] sums;

    public IntegralImage(int width, int height, int[] pixels)
    {
        W = width+1;
        H = height+1;
        sums = new int[W*H];

        sums[W+1] = pixels[0];

        for (int j=1; j<H; ++j)
            for (int i=1; i<W; ++i)
                sums[j*W+i] = pixels[(j-1)*(W-1)+(i-1)] + sums[(j-1)*W+i] + sums[j*W+(i-1)] - sums[(j-1)*W+(i-1)];
    }

    /**
     * Sum of pixel values from x0,y0 (inclusive) to x1,y1 (inclusive)
     */
    public long sum(int x0, int y0, int x1, int y1)
    {
        ++x1; ++y1;
        return sums[y0*W+x0] + sums[y1*W+x1] - (sums[y1*W+x0] + sums[y0*W+x1]);
    }

    public static void main(String args[])
    {
        int W = 8;
        int H = 8;
        int[] pixels = new int[W*H];

        for (int j=0; j<H; ++j)
            for (int i=0; i<W; ++i)
                pixels[j*W+i] = i+1;

        IntegralImage iim = new IntegralImage(W, H, pixels);
        for (int j=0; j<iim.H; ++j) {
            for (int i=0; i<iim.W; ++i)
                System.out.printf("%3d ", iim.sums[j*iim.W+i]);
            System.out.println();
        }
    }
}

