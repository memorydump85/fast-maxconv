all: perf test

CFLAGS = -lrt -lboost_thread

perf: *.hxx perf.cxx
	g++ $(CFLAGS) $(DEFS) -D NDEBUG -O3 -mtune=core2 -mmmx -msse -o perf perf.cxx
    
perf-checked: *.hxx perf.cxx
	g++ $(CFLAGS) $(DEFS) -g -o perf-checked perf.cxx
	
perf-debug: *.hxx perf.cxx
	g++ $(CFLAGS) $(DEFS) -g -o perf-debug perf.cxx
	
test: *.hxx test.cxx
	g++ $(CFLAGS) $(DEFS) -D NDEBUG -O3 -mtune=core2 -mmmx -msse -o test test.cxx
	
clean:
	rm test perf perf-* *.pgm

