#pragma once
/*
 *  Implementation of maxconvolution
 */



#include <cassert>
#include <cstdio>
#include <cstdlib>
#include <cstring>
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
    ParallelMaxConvolve()
        : m_barrierReady(6),
          m_barrierDone(6),
          m_thread1(&maxconv_rect, this,  5, 15),
          m_thread2(&maxconv_rect, this,  9, 13),
          m_thread3(&maxconv_rect, this, 11, 11),
          m_thread4(&maxconv_rect, this, 13,  9),
          m_thread5(&maxconv_rect, this, 15,  5)
    {}
    
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
        m_thread1.interrupt();
        m_thread2.interrupt();
        m_thread3.interrupt();
        m_thread4.interrupt();
        m_thread5.interrupt();
        
        m_thread1.join();
        m_thread2.join();
        m_thread3.join();
        m_thread4.join();
        m_thread5.join();
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

    boost::thread m_thread1;
    boost::thread m_thread2;
    boost::thread m_thread3;
    boost::thread m_thread4;
    boost::thread m_thread5;

    int *m_in;
    int m_width;
    int m_height;
    int *m_out;
};

