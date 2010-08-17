#include <iostream>

#include "maxconv.hxx"
#include "utility.hxx"



#ifndef KERNEL_SIZE
    #define KERNEL_SIZE 7
#endif

#ifndef DISC_KERNEL
    #define DISC_KERNEL DISC_KERNEL_7
#endif



static void convolveCenteredDisc2DMax(int a[], int width, int height, int radius, int r[])
{
    for (int k=-radius; k <= radius; k++) {
        for (int l=-radius; l <= radius; l++) {
            // is it within the radius?
            if (k*k + l*l > radius*radius)
                continue;

            int ymin = std::max(0, 0-k);
            int ymax = std::min(height, height - k);
            int xmin = std::max(0, 0-l);
            int xmax = std::min(width, width - l);

            for (int y = ymin; y < ymax; y++) {
                int n = (y+k)*width + (xmin+l);
                int o = y*width + xmin;

                for (int x = xmin; x < xmax; x++) {
                    r[o] = std::max(r[o], a[n++]);
                    o++;
                }
            }
        }
    }
}


int main()
{
    const int SZ = 800;
    const int SAMPLES = 500;
    int* data = new int[SZ*SZ];
    int* filtered = new int[SZ*SZ];

    srand(37);
    for (int i=0; i<SZ*SZ; ++i) {
        data[i] = (rand()%500==0) ? rand()%CBINS : 0;
    } 


    ParallelMaxConvolve pmc(DISC_KERNEL);
    double sum_fast = 0;
    for (int i=0; i<SAMPLES; ++i) {
        TicToc tic;
        pmc.convolve(data, SZ, SZ, filtered);
        sum_fast += tic.toc();
        
//        putc('.', stderr);
//        fflush(stderr);
    }  
    

    double sum_triv = 0;
    for (int i=0; i<SAMPLES; ++i) {
        TicToc tic;
                convolveCenteredDisc2DMax(data, SZ, SZ, KERNEL_SIZE, filtered);
        sum_triv += tic.toc();

//        putc('.', stderr);
//        fflush(stderr);
    }
    
    printf("%d %d %.4f %.4f\n", CBINS, KERNEL_SIZE, sum_fast/SAMPLES, sum_triv/SAMPLES);
 
    delete filtered;
    delete data;
    return 0;       
}

