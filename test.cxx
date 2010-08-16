#include <iostream>

#include "maxconv.hxx"
#include "utility.hxx"



int main()
{
    const int SZ = 800;
    int* data = new int[SZ*SZ];
    int* filtered = new int[SZ*SZ];

    srand(37);
    for (int i=0; i<SZ*SZ; ++i) {
        data[i] = (rand()%500==0) ? rand()%CBINS : 0;
    } 


    ParallelMaxConvolve pmc;       
    pmc.convolve(data, SZ, SZ, filtered);
    
    write_pgm(data, SZ, SZ, "input.pgm");
    write_pgm(filtered, SZ, SZ, "filtered.pgm");
    
    puts("Input written to input.pgm, output to filtered.pgm");
    
    delete filtered;
    delete data;
    return 0;       
}

