#ifndef UTILS_H
#define UTILS_H

#include "queue.h"

typedef struct {
    char *filename;
    char *scheduler;
    char *memory_strategy;
    int quantum;
} allocate_options;

allocate_options read_input(int argc, char *argv[]);
void read_file(const char *filename, ProcessQueue *processes, int *process_num);
Process sjf(Process *processes, int process_num);
int compare_process(const void* a, const void* b);
int find_all_ready_process(Process *processes, int process_num, Process *ready_processes);
void scheduling_best_fit(ProcessQueue *input_queue, char scheduler[], int quantum, int *simulation_time, int process_num);
void scheduling(ProcessQueue *input_queue, char scheduler[], int quantum, int *simulation_time, int process_num);
void change_state(ProcessQueue *processes, int *simulation_time, state process_state);
void print_output(Process process, int *simulation_time, int proc_remaining);
void print_performance_statistics(ProcessQueue *finished_queue, int process_num, int simulation_time);
void create_process(Process *process, int time);
void resume_process(Process *process, int time);
void suspend_process(Process *process, int time);
void terminate_process(Process *process, int time);
#endif