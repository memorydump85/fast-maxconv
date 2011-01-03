echo Bins R FastMax TrivMax

for B in 8 16 32 64 128 256; do
    for K in 1 2 3 4 5 6 7; do
        make clean &> /dev/null
        make DEFS="-D CBINS=$B -D KERNEL_SIZE=$K -D DISC_KERNEL=DISC_KERNEL_$K" &> /dev/null
        ./perf
    done
    echo
done

