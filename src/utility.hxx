#pragma once
/*
 *  Utility functions and classes
 */

#include <cstdio>



//------------------------------------------------------------------------------
//  Writes a 2D array as a portable gray map file
//------------------------------------------------------------------------------
int write_pgm(int data[], int W, int H, const char* filename)
{
    FILE* fp = fopen(filename, "wb");

    if (fp == NULL)
        return 1;
        
    fprintf(fp, "P5 %d %d %d ", W, H, CBINS-1);

    for (int i=0; i<W*H; ++i) {
        unsigned char byte = std::min(CBINS-1, data[i]);
        fwrite(&byte, 1, 1, fp);
    }

    fclose(fp);
}


//==============================================================================
class TicToc
//==============================================================================
// Utility class for measuring time intervals
{
    double start;
   
    static double nanotime()
    {
        timespec ts;
        clock_gettime(CLOCK_REALTIME, &ts);
       
        return ts.tv_sec + ts.tv_nsec/1000000000.0;
    }
   
public:
    TicToc()
    {
        tic();
    }
   
    double toc() const
    {
        return nanotime()-start;
    }
    
    double tic()
    {
        start = nanotime();
    }
    
    double toctic()
    {
        double diff = toc();
        tic();
        
        return diff;
    }
};

