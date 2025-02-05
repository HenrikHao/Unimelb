.PHONY: all clean

CC = gcc
CFLAGS = -Wall -Werror -g

OBJS = allocate.o utils.o queue.o memory_block.o

all: allocate

allocate: $(OBJS)
	$(CC) $(CFLAGS) -o allocate $(OBJS) -lm

allocate.o: allocate.c utils.h queue.h memory_block.h
	$(CC) $(CFLAGS) -c allocate.c

utils.o: utils.c utils.h queue.h
	$(CC) $(CFLAGS) -c utils.c

queue.o: queue.c queue.h
	$(CC) $(CFLAGS) -c queue.c

memory_block.o: memory_block.c memory_block.h utils.h
	$(CC) $(CFLAGS) -c memory_block.c

clean:
	rm -f allocate *.o
