#ifndef MEMORY_BLOCK_H
#define MEMORY_BLOCK_H

#include <stdbool.h>
#include "utils.h"

typedef struct MemoryBlock {
    int start;
    int end;
    bool is_allocated;
    struct MemoryBlock *next;
    struct MemoryBlock *prev;
} MemoryBlock;

void init_memory(MemoryBlock **memory);
int allocate_memory(MemoryBlock *memory, int size);
void free_all_memory(MemoryBlock *block);
void free_given_memory(MemoryBlock *memory, int start);
#endif // MEMORY_BLOCK_H
