#include <stdlib.h>
#include "memory_block.h"
#include <limits.h>
#include <string.h>
#include <stdio.h>

// initialise the memory 
void init_memory(MemoryBlock **memory) {
    *memory = malloc(sizeof(MemoryBlock));
    (*memory)->start = 0;
    (*memory)->end = 2047;
    (*memory)->is_allocated = false;
    (*memory)->next = NULL;
}

// allocate the memory given the size of process
int allocate_memory(MemoryBlock *memory, int size) {
    MemoryBlock *best_fit = NULL;
    int best_fit_size = INT_MAX;
    
    // Find the best fit block
    for (MemoryBlock *block = memory; block != NULL; block = block->next) {
        int free_space = block->end - block->start + 1;

        if (!block->is_allocated && free_space >= size && free_space < best_fit_size) {
            best_fit = block;
            best_fit_size = free_space;
        }
    }

    // If no suitable block was found, return false (allocation unsuccessful)
    if (best_fit == NULL) {
        return -1;
    }

    // If the best fit block has exactly the required size, mark it as allocated
    if (best_fit_size == size) {
        best_fit->is_allocated = true;
    }
    // If the best fit block is larger than the required size, split it
    else {
        MemoryBlock *new_block = malloc(sizeof(MemoryBlock));
        new_block->start = best_fit->start + size;
        new_block->end = best_fit->end;
        new_block->is_allocated = false;
        new_block->next = best_fit->next;

        best_fit->end = best_fit->start + size - 1;
        best_fit->is_allocated = true;
        best_fit->next = new_block;
    }

    return best_fit->start;
}

// free all memory used
void free_all_memory(MemoryBlock *memory) {
    MemoryBlock *current_block = memory;
    while (current_block != NULL) {
        MemoryBlock *next_block = current_block->next;
        free(current_block);
        current_block = next_block;
    }
}

// free the memory block given the start index
void free_given_memory(MemoryBlock *memory, int start) {
    MemoryBlock *block;
    MemoryBlock *previous = NULL;

    for (block = memory; block != NULL; block = block->next) {
        if (block->start == start) {
            break;
        }
        previous = block;
    }

    if (block == NULL) {
        // Block not found
        return;
    }

    block->is_allocated = false;

    // Merge with previous block if it's free
    if (previous != NULL && !previous->is_allocated) {
        previous->end = block->end;
        previous->next = block->next;
        free(block);
        block = previous;
    }

    // Merge with the next block if it's free
    if (block->next != NULL && !block->next->is_allocated) {
        MemoryBlock *next_block = block->next;
        block->end = next_block->end;
        block->next = next_block->next;
        free(next_block);
    }
}

