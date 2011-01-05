package maxconv;

import java.util.*;


/**
 * An efficient heap backed by a histogram. The values in
 * the heap must be between 0 and 255.
 */
public class CountingHeap
{
    int[] bins = new int[256];
    int max = -1;

    public void clear()
    {
        Arrays.fill(bins, (byte)0);
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
