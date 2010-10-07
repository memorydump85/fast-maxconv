#pragma once
/*
 *  Implementation of maxconvolution
 */



#include <cassert>
#include <cstdio>
#include <cstdlib>
#include <cstring>
#include <cstdarg>
#include <algorithm>
#include <time.h>

#include <boost/thread/thread.hpp>
#include <boost/thread/barrier.hpp>

#include "histogram.hxx"



static inline bool inBounds(int v, int vMax)
{
    return (v>=0) && (v<vMax);
}

static int* maxconv_impl(
    const int in[],     /* input data*/
    const int xstart,   /* starting column of stripe in input data */
    const int xend,     /* ending column of stripe in input data */
    const int W,        /* width of input data */
    const int H,        /* height of input data */
    const int KW,       /* kernel block width */
    const int KH,       /* kernel block height */
    int out[]           /* max convolved output data */
    )
{
    assert (KW%2 != 0);
    assert (KH%2 != 0);
    assert (H > KH);
    assert (W > KW);

    const int Q = KH/2;
    const int R = KW/2;
    
    /* Initialize column histograms */
    Histogram col_hist[W];
    
    for (int y = 0; y < Q; ++y)
        for (int x = xstart; x < xend; ++x)
            col_hist[x].add(in[y*W+x]);
    
    /* Initialize histogram */
    Histogram hist;
    

    for (int y = 0; y < H; ++y) {
        /* prepare initial kernel histogram for this row*/
        hist.clear();
    
        for (int x = xstart; x < xstart+R; ++x) {
            if (inBounds(y-Q-1, H))
                col_hist[x].remove(in[(y-Q-1)*W+x]);
                    
            if (inBounds(y+Q, H))
                col_hist[x].add(in[(y+Q)*W+x]);

            hist.merge(col_hist[x]);
        }

        for (int x = xstart; x < xend; ++x) {
            /* remove old left column */
            if (inBounds(x-R-1, xend))
                hist.unmerge(col_hist[x-R-1]);
                
            /* prepare new right column and merge */
            if (inBounds(x+R, xend)) {
                if (inBounds(y-Q-1, H))
                    col_hist[x+R].remove(in[(y-Q-1)*W+(x+R)]);
                    
                if (inBounds(y+Q, H))
                    col_hist[x+R].add(in[(y+Q)*W+(x+R)]);

                hist.merge(col_hist[x+R]);
            }
            
            /* get the max at this position of the kernel.
               The compare-and-swap lets multiple threads operate on the same
               input data */
            while (true) {
                int oldVal = out[y*W+x];
                int newVal = std::max(oldVal, hist.max());
                
                if (__sync_bool_compare_and_swap(out+y*W+x, oldVal, newVal))
                    break;
            }
        }
    }

    return out;
}


//==============================================================================
class ParallelMaxConvolve
//==============================================================================
//  Performs circular max convolution by decomposing a circular kernel into a
//  superposition of rectangular kernels and invoking each rectangular kernel
//  convolution in a separate thread.
//
//  Uses thread pools to avoid the overhead of creating threads for each
//  invocation of convolve.
//
//  This implementation is hardcoded to use a circular disc kernel with radius 7
//  (width=15px, height=15px).
{
public:
    ParallelMaxConvolve(int cRects, ...)
        : m_barrierReady(cRects+1),
          m_barrierDone(cRects+1)
    {
        va_list vl;
        va_start(vl, cRects);
        
        for (int i=0; i<cRects; ++i) {
            int w = va_arg(vl, int);
            int h = va_arg(vl, int);
            
            m_threadGroup.add_thread(new boost::thread(&maxconv_rect, this, w, h));
        }
        
        va_end(vl);
    }
    
    void convolve(int input[], int width, int height, int output[])
    {       
        // Set up member variables
        m_in = input;
        m_width = width;
        m_height = height;
        m_out = output;
    
        // Release the threads ...
        m_barrierReady.wait();
        
        // And wait till they complete their work
        m_barrierDone.wait();
    }
    
    ~ParallelMaxConvolve()
    {
        m_threadGroup.interrupt_all();
        m_threadGroup.join_all();
    }
    
private:
    static void maxconv_rect(ParallelMaxConvolve* _this, int kernel_width, int kernel_height)
    {
        while (true) {
            _this->m_barrierReady.wait();

            maxconv_impl(_this->m_in, 0, _this->m_width, _this->m_width,
                _this->m_height, kernel_width, kernel_height, _this->m_out);
                
            _this->m_barrierDone.wait();
        }
    }

    boost::barrier m_barrierReady;    
    boost::barrier m_barrierDone;

    boost::thread_group m_threadGroup;

    int *m_in;
    int m_width;
    int m_height;
    int *m_out;
};

#define DISC_KERNEL_1   2,   1, 3,   3, 1
#define DISC_KERNEL_2   2,   3, 5,   5, 3
#define DISC_KERNEL_3   3,   3, 7,   5, 5,   7, 3
#define DISC_KERNEL_4   3,   3, 9,   7, 7,   9, 3
#define DISC_KERNEL_5   4,   5,11,   7, 9,   9, 7,   11, 5
#define DISC_KERNEL_6   5,   5,13,   7,11,   9, 9,   11, 7,   13, 5
#define DISC_KERNEL_7   5,   5,15,   9,13,  11,11,   13, 9,   15, 5

/*
 * Donot define more: bigger kernel sizes might require more than 255 pixels.
 * If you need bigger kernels, upgrade to histograms with 16-bit buckets
 */
