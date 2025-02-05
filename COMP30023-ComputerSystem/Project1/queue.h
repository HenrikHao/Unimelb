#ifndef QUEUE_H
#define QUEUE_H

#include <stdbool.h>

typedef enum {
    READY,
    RUNNING,
    FINISHED,
    NEW
} state;

typedef struct {
    int arrival_time;
    char name[4];
    int time_to_finish;
    int remaining_time;
    int memory;
    int finished_time;
    state current_state;
    int assigned_at; 
    int pipe1[2];
    int pipe2[2];
    int pid; 
    int running_num;
} Process;

typedef struct ProcessNode {
    Process process;
    struct ProcessNode *next;
} ProcessNode;

typedef struct {
    ProcessNode *head;
    ProcessNode *tail;
} ProcessQueue;

void process_queue_init(ProcessQueue *queue);
void enqueue(ProcessQueue *queue, Process process);
void move_ready_processes(ProcessQueue *input_queue, ProcessQueue *ready_queue);
int isEmpty(ProcessQueue *queue);
Process dequeue(ProcessQueue *queue);
int get_size(ProcessQueue *queue);
Process simple_dequeue(ProcessQueue *queue);
void process_queue_free(ProcessQueue *queue);

#endif