#include <stdio.h>
#include <stdlib.h>
#include <string.h>
#include "queue.h"
#include "utils.h"

// initialise the process queue
void process_queue_init(ProcessQueue *queue) {
    queue->head = NULL;
    queue->tail = NULL;
}

// enqueue the process into the queue
void enqueue(ProcessQueue *queue, Process process) {
    ProcessNode *new_node = (ProcessNode *)malloc(sizeof(ProcessNode));
    new_node -> process = process;
    new_node -> next = NULL;

    if (queue -> tail == NULL) {
        queue -> head = queue -> tail = new_node;
        return;
    }

    queue -> tail -> next = new_node;
    queue -> tail = new_node;
}

// move the processes in ready state from input queue to ready queue
void move_ready_processes(ProcessQueue *input_queue, ProcessQueue *ready_queue) {
    ProcessQueue temp_queue;
    process_queue_init(&temp_queue);

    // Iterate through the input_queue
    while (!isEmpty(input_queue)) {
        Process current_process = simple_dequeue(input_queue);

        if (current_process.current_state == READY) {
            enqueue(ready_queue, current_process);
        } else {
            enqueue(&temp_queue, current_process);
        }
    }

    // Swap the input_queue and temp_queue
    *input_queue = temp_queue;
}

// check if the queue is empty
int isEmpty(ProcessQueue *queue) {
    if (queue->head == NULL) {
        return 1;
    }
    return 0;
}

// dequeue the process with the smallest remaining time, 
//if there is a tie, dequeue the processes with the 'smallest name'
Process dequeue(ProcessQueue *queue) {
    if (isEmpty(queue)) {
        exit(EXIT_FAILURE);
    }

    ProcessNode *current_node = queue->head;
    ProcessNode *smallest_remaining_time_node = queue->head;
    ProcessNode *prev_node = NULL;
    ProcessNode *prev_smallest_node = NULL;
    int smallest_remaining_time = queue->head->process.remaining_time;

    while (current_node != NULL) {
        if (current_node->process.remaining_time < smallest_remaining_time) {
            smallest_remaining_time = current_node->process.remaining_time;
            prev_smallest_node = prev_node;
            smallest_remaining_time_node = current_node;
        } else if (current_node->process.remaining_time == smallest_remaining_time) {
            if (strcmp(current_node->process.name, smallest_remaining_time_node->process.name) < 0) {
                prev_smallest_node = prev_node;
                smallest_remaining_time_node = current_node;
            }
        }
        prev_node = current_node;
        current_node = current_node->next;
    }

    Process dequeued_process = smallest_remaining_time_node->process;

    // Remove the smallest_remaining_time_node from the queue
    if (prev_smallest_node != NULL) {
        prev_smallest_node->next = smallest_remaining_time_node->next;
    } else {
        queue->head = smallest_remaining_time_node->next;
    }
    
    if (smallest_remaining_time_node == queue->tail) {
        queue->tail = prev_smallest_node;
    }

    free(smallest_remaining_time_node);

    return dequeued_process;
}

// get the size of queue
int get_size(ProcessQueue *queue) {
    int count = 0;
    ProcessNode *current_node = queue->head;

    while (current_node != NULL) {
        count++;
        current_node = current_node->next;
    }

    return count;
}

// dequeue the first element in the queue
Process simple_dequeue(ProcessQueue *queue) {
    if (isEmpty(queue)) {
        fprintf(stderr, "Error: Cannot dequeue from an empty queue.\n");
        exit(EXIT_FAILURE);
    }

    ProcessNode *node_to_remove = queue->head;
    Process dequeued_process = node_to_remove->process;

    queue->head = queue->head->next;
    if (queue->head == NULL) {
        queue->tail = NULL;
    }

    free(node_to_remove);

    return dequeued_process;
}

// free the process queue
void process_queue_free(ProcessQueue *queue) {
    ProcessNode *current = queue->head;
    while (current != NULL) {
        ProcessNode *temp = current;
        current = current->next;
        free(temp);
    }
    queue->head = NULL;
    queue->tail = NULL;
}
