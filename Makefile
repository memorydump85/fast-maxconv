all: perf test

CFLAGS = -lrt -lboost_thread -D IMPLICIT_MMX
OFLAGS = -D NDEBUG -O3 -march=native

perf: *.hxx perf.cxx
	g++ $(CFLAGS) $(DEFS) $(OFLAGS) -o perf perf.cxx
    
perf-checked: *.hxx perf.cxx
	g++ $(CFLAGS) $(DEFS) -g -o perf-checked perf.cxx
	
perf-debug: *.hxx perf.cxx
	g++ $(CFLAGS) $(DEFS) -g -o perf-debug perf.cxx
	
perf-disasm: *.hxx perf.cxx
	g++ $(CFLAGS) $(DEFS) -c -g $(OFLAGS) -Wa,-ahl=perf-implicit.s perf.cxx
	g++ $(CFLAGS) $(DEFS) -U IMPLICIT_MMX -c -g $(OFLAGS) -Wa,-ahl=perf-explicit.s perf.cxx
		
test: *.hxx test.cxx
	g++ $(CFLAGS) $(DEFS) -D NDEBUG -O3 -mtune=core2 -mmmx -msse -o test test.cxx
	
clean:
	rm test perf perf-* *.s *.o *.pgm

