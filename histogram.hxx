#pragma once

/*
 *  Implementation of a histogram which uses vector operations to speedup
 *  merging and unmerging two histograms.
 *
 */


#ifndef CBINS
    #define CBINS 64
#endif



//=============================================================================
template<typename T> union vector_storage
//=============================================================================
{
    typedef T value_t;
    typedef value_t vector_value_t
                __attribute__(( vector_size(CBINS) ));
    
    vector_value_t chunk;
    value_t data[CBINS];
};


//=============================================================================
class Histogram
//=============================================================================
// Histograms of byte valued integers. Since 64 bytes can fit into one MMX
// register of an Intel processor, using a Histogram with 64 bins gives single
// instruction merges and unmerges.
//
// Smaller number of bins do not improve performance because the cost is still
// a single instruction. Larger bins linearly degrade performance
{
private:
    vector_storage<unsigned char> m_vec;
    mutable int m_max;

public:
    Histogram()
    {
        clear();
    }
    
    void clear()
    {
        m_max = -1;
        memset(m_vec.data, 0, sizeof(m_vec.data));
    }

    void merge(const Histogram& that)
    {
#ifdef IMPLICIT_MMX
        for (int i=0; i<CBINS; ++i)
            m_vec.data[i] += that.m_vec.data[i];
#else
        m_vec.chunk += that.m_vec.chunk;
#endif

        m_max = std::max(m_max, that.m_max);
    }
    
    void unmerge(const Histogram& that)
    {
#ifdef IMPLICIT_MMX
        for (int i=0; i<CBINS; ++i)
            m_vec.data[i] -= that.m_vec.data[i];
#else
        m_vec.chunk -= that.m_vec.chunk;
#endif
    }
       
    void add(int v)
    {
        assert (v >=0 && v < CBINS);

        ++m_vec.data[v];
        m_max = v > m_max ? v : m_max;
    }
    
    void remove(int v)
    {
        assert (v >=0 && v < CBINS);
        
        --m_vec.data[v];
    }
    
    int max() const
    {
        assert (m_max != -1);

        while (m_vec.data[m_max] == 0)
            --m_max;
            
        return m_max;
    }
};

