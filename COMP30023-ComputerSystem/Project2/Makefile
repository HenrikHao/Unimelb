CC=gcc
LDFLAGS=-pthread
CFLAGS=-Wall
RPC_SYSTEM=rpc.o

.PHONY: all clean

all: $(RPC_SYSTEM)

$(RPC_SYSTEM): rpc.c rpc.h
	$(CC) $(CFLAGS) -c -o $@ $< $(LDFLAGS)

clean:
	rm -f *.o

