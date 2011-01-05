package maxconv;

import java.util.*;


/**
 * An efficient heap backed by a histogram.
 */
public class CountingHeap
{
    final int[] bins;
    int max = -1;

    public CountingHeap(int nBins)
    {
        bins = new int[nBins];
    }

    public void clear()
    {
        Arrays.fill(bins, 0);
        max = -1;
    }

    public void merge(CountingHeap h)
    {
        for(int i=0; i<bins.length; ++i)
            bins[i] += h.bins[i];

        max = Math.max(max, h.max);
    }

    public void unmerge(CountingHeap h)
    {
        for(int i=0; i<bins.length; ++i)
            bins[i] -= h.bins[i];
    }

    public void add(int v)
    {
        ++bins[v];
        max = Math.max(max, v);
    }

    public void remove(int v)
    {
        --bins[v];
    }

    public int getMax()
    {
        assert (max != -1);

        /* Compensate for laziness during deletion */
        while (bins[max] == 0)
            --max;

        return max;
    }
}
