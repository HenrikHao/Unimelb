#include <stdio.h>
#include <stdlib.h>
#include <getopt.h>
#include <string.h>
#include "utils.h"
#include "queue.h"
#define MAX_PROCESSES 1024
#define IMPLEMENTS_REAL_PROCESS
int main(int argc, char **argv) {
    // options which include all input
    allocate_options options = read_input(argc, argv);

    // an array of processes, to store all processes
    ProcessQueue input_queue;
    process_queue_init(&input_queue);
    // num of processes
    int process_num;
    // simulation time
    int simulation_time = 0;
    // read the file and store them into processes
    read_file(options.filename, &input_queue, &process_num);

    // process manager
    if (strcmp(options.memory_strategy, "best-fit") == 0) {
        scheduling_best_fit(&input_queue, options.scheduler, options.quantum, &simulation_time, process_num);
    } else {
        scheduling(&input_queue, options.scheduler, options.quantum, &simulation_time, process_num);
    }
    return 0;
}