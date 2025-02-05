#include <stdio.h>
#include <stdlib.h>
#include <getopt.h>
#include "utils.h"
#include <string.h>
#include "queue.h"
#include "memory_block.h"
#include <math.h>
#include <unistd.h>
#include <sys/types.h>
#include <sys/wait.h>
#include <signal.h>

#define MAX_PROCESSES 1024

// read input and store them
allocate_options read_input(int argc, char **argv) {
    const char *optstring = "f:s:m:q:";
    const struct option long_options[] = {
        {"filename", required_argument, NULL, 'f'},
        {"scheduler", required_argument, NULL, 's'}, 
        {"memory-strategy", required_argument, NULL, 'm'},
        {"quantum", required_argument, NULL, 'q'},
        {NULL, 0, NULL, 0}
    };

    allocate_options options = {NULL, NULL, NULL, 0};

    int opt;
    while ((opt = getopt_long(argc, argv, optstring, long_options, NULL)) != -1) {
        switch (opt) {
            case 'f':
                options.filename = optarg;
                break;
            case 's':
                options.scheduler = optarg;
                break;
            case 'm':
                options.memory_strategy = optarg;
                break;
            case 'q':
                options.quantum = atoi(optarg);
                break;
            default:
                exit(EXIT_FAILURE);
        }
    }

    return options;
}

// open the file and read process
void read_file(const char *filename, ProcessQueue *processes, int *process_num) {
    FILE *file = fopen(filename, "r");
    if (file == NULL) {
        exit(EXIT_FAILURE);
    }

    *process_num = 0;
    Process temp_process;
    while (fscanf(file, "%d %s %d %d", &(temp_process.arrival_time), temp_process.name, &(temp_process.time_to_finish), &(temp_process.memory)) == 4) {
        temp_process.remaining_time = temp_process.time_to_finish;
        temp_process.current_state = NEW;
        temp_process.assigned_at = 0;
        temp_process.finished_time = 0;
        temp_process.running_num = 0;
        enqueue(processes, temp_process);
        (*process_num)++;
    }

    fclose(file);
}

// allocate the memory and print the required output
void allocate_and_print(ProcessQueue *input_queue, int *simulation_time, MemoryBlock *memory) {
    for (ProcessNode *node = input_queue->head; node != NULL; node = node->next) {
        if (node->process.current_state == READY) {
            int assigned_at = allocate_memory(memory, node->process.memory);
            if (assigned_at != -1) {
                node->process.assigned_at = assigned_at;
                print_output(node->process, simulation_time, assigned_at);
            } else {
                node->process.current_state = NEW;
            }
        }    
    }
}

// process manager for best_fit
void scheduling_best_fit(ProcessQueue *input_queue, char scheduler[], int quantum, int *simulation_time, int process_num) {
    ProcessQueue ready_queue, running_queue, finished_queue;
    process_queue_init(&ready_queue);
    process_queue_init(&running_queue);
    process_queue_init(&finished_queue);
    Process running_process;   
    MemoryBlock *memory;
    init_memory(&memory);

    // if the scheduler is SJF
    if (strcmp(scheduler, "SJF") == 0) {
        while (get_size(&finished_queue) != process_num) {
            change_state(input_queue, simulation_time, READY);
            allocate_and_print(input_queue, simulation_time, memory);
            move_ready_processes(input_queue, &ready_queue);

            // pick one process to run or continue it
            if (!isEmpty(&ready_queue) && isEmpty(&running_queue)) {
                running_process = dequeue(&ready_queue);
                running_process.current_state = RUNNING;
                running_process.running_num += 1;
                if (running_process.running_num == 1) {
                    create_process(&running_process, *simulation_time);
                } else {
                    resume_process(&running_process, *simulation_time);
                }
                enqueue(&running_queue, running_process);
                print_output(running_process, simulation_time, 0);
            }
            *simulation_time += quantum;

            // if the running queue is not empty, check if it is finished, if not continue it
            if (!isEmpty(&running_queue)) {
                running_process = dequeue(&running_queue);
                running_process.remaining_time -= quantum;
                running_process.running_num += 1;
                if (running_process.remaining_time <= 0) {
                    running_process.current_state = FINISHED;
                    running_process.finished_time = *simulation_time;
                    print_output(running_process, simulation_time, get_size(&ready_queue));
                    terminate_process(&running_process, *simulation_time);
                    free_given_memory(memory, running_process.assigned_at);
                    enqueue(&finished_queue, running_process);
                } else {
                    enqueue(&running_queue, running_process);
                    resume_process(&running_process, *simulation_time);
                }
            }
        }
        print_performance_statistics(&finished_queue, process_num, *simulation_time);
    }

    // if the scheduler is RR
    if (strcmp(scheduler, "RR") == 0) {
        while (get_size(&finished_queue) != process_num) {

            change_state(input_queue, simulation_time, READY);
            allocate_and_print(input_queue, simulation_time, memory);
            move_ready_processes(input_queue, &ready_queue);
            
            // if there are ready processes
            if (get_size(&ready_queue) >= 1) {
                
                // if there is a process running
                if (!isEmpty(&running_queue)) {
                    running_process = dequeue(&running_queue);
                    running_process.remaining_time -= quantum;
                    running_process.running_num += 1;

                    // check if it is finished
                    if (running_process.remaining_time <= 0) {
                        running_process.current_state = FINISHED;
                        running_process.finished_time = *simulation_time;
                        enqueue(&finished_queue, running_process);
                        free_given_memory(memory, running_process.assigned_at);
                        print_output(running_process, simulation_time, get_size(&ready_queue) + get_size(input_queue));
                        terminate_process(&running_process, *simulation_time);

                        // if a process finished, free its memory and check if other processes can be allocated
                        change_state(input_queue, simulation_time, READY);
                        allocate_and_print(input_queue, simulation_time, memory);
                        move_ready_processes(input_queue, &ready_queue);

                    // if not, suspend it
                    } else {
                        suspend_process(&running_process, *simulation_time);
                        running_process.current_state = READY;
                        enqueue(&ready_queue, running_process);
                    }

                    // pick a new process to run
                    running_process = simple_dequeue(&ready_queue);
                    running_process.current_state = RUNNING;
                    running_process.running_num += 1;
                    if (running_process.running_num == 1) {
                        create_process(&running_process, *simulation_time);
                    } else {
                        resume_process(&running_process, *simulation_time);
                    }
                    enqueue(&running_queue, running_process); 
                    print_output(running_process, simulation_time, 0);   

                // if there are no processes running
                } else {
                    
                    // pick one process to run
                    running_process = simple_dequeue(&ready_queue);
                    running_process.current_state = RUNNING;
                    running_process.running_num += 1;
                    if (running_process.running_num == 1) {
                        create_process(&running_process, *simulation_time);
                    } else {
                        resume_process(&running_process, *simulation_time);
                    }
                    enqueue(&running_queue, running_process);
                    print_output(running_process, simulation_time, 0);
                }
            
            // if there are no ready processes
            } else {

                // if there is a process running, check if it is finished
                if (!isEmpty(&running_queue)) {
                    running_process = dequeue(&running_queue);
                    running_process.remaining_time -= quantum;
                    running_process.running_num += 1;
                    if (running_process.remaining_time <= 0) {
                        running_process.current_state = FINISHED;
                        running_process.finished_time = *simulation_time;
                        enqueue(&finished_queue, running_process);
                        free_given_memory(memory, running_process.assigned_at);
                        print_output(running_process, simulation_time, get_size(&ready_queue) + get_size(input_queue));
                        terminate_process(&running_process, *simulation_time);
                        change_state(input_queue, simulation_time, READY);
                        allocate_and_print(input_queue, simulation_time, memory);
                        move_ready_processes(input_queue, &ready_queue);
                    
                    // if not, continue it
                    } else {
                        resume_process(&running_process, *simulation_time);
                        enqueue(&running_queue, running_process);
                    }    
                }
            }
            
            *simulation_time += quantum;
        } 
        print_performance_statistics(&finished_queue, process_num, *simulation_time - quantum);
    }

    // free all memory
    free_all_memory(memory);   
    process_queue_free(input_queue);
    process_queue_free(&ready_queue);
    process_queue_free(&running_queue);
    process_queue_free(&finished_queue);
    
}

// process manager for inifinite
void scheduling(ProcessQueue *input_queue, char scheduler[], int quantum, int *simulation_time, int process_num) {
    ProcessQueue ready_queue, running_queue, finished_queue;
    process_queue_init(&ready_queue);
    process_queue_init(&running_queue);
    process_queue_init(&finished_queue);
    Process running_process, finished_process;    

    // if the scheduler is SJF
    if (strcmp(scheduler, "SJF") == 0) {
        
        while (get_size(&finished_queue) != process_num) {
            change_state(input_queue, simulation_time, READY);
            move_ready_processes(input_queue, &ready_queue);
            
            // pick one process to run
            if (!isEmpty(&ready_queue) && isEmpty(&running_queue)) {

                running_process = dequeue(&ready_queue);
                running_process.current_state = RUNNING;
                running_process.running_num += 1;
                if (running_process.running_num == 1) {
                    create_process(&running_process, *simulation_time);
                } else {
                    resume_process(&running_process, *simulation_time);
                }
                enqueue(&running_queue, running_process);
                print_output(running_process, simulation_time, 0);
            }
            
            // let the running process run until it finished
            if (!isEmpty(&running_queue)) {
                running_process = dequeue(&running_queue);
                while (running_process.remaining_time > 0) {
                    change_state(input_queue, simulation_time, READY);
                    move_ready_processes(input_queue, &ready_queue);
                    
                    *simulation_time += quantum;
                    running_process.remaining_time -= quantum;
                    if (running_process.remaining_time <= 0) {
                        break;
                    }
                    resume_process(&running_process, *simulation_time);
                }
                enqueue(&running_queue, running_process);
                finished_process = dequeue(&running_queue);
                finished_process.current_state = FINISHED;
                finished_process.finished_time = *simulation_time;
                print_output(finished_process, simulation_time, get_size(&ready_queue));
                terminate_process(&finished_process, *simulation_time);
                enqueue(&finished_queue, finished_process);

                continue;
            }
            *simulation_time += quantum;
        }
        print_performance_statistics(&finished_queue, process_num, *simulation_time);
    }

    // if the scheduler is RR
    if (strcmp(scheduler, "RR") == 0) {
        
        while (get_size(&finished_queue) != process_num) {
            change_state(input_queue, simulation_time, READY);
            move_ready_processes(input_queue, &ready_queue);

            // if there are ready processes
            if (get_size(&ready_queue) >= 1) {
                
                // if there is a process running
                if (!isEmpty(&running_queue)) {
                    running_process = dequeue(&running_queue);
                    running_process.remaining_time -= quantum;
                    running_process.running_num += 1;

                    // check if it is finished
                    if (running_process.remaining_time <= 0) {
                        running_process.current_state = FINISHED;
                        running_process.finished_time = *simulation_time;
                        enqueue(&finished_queue, running_process);
                        print_output(running_process, simulation_time, get_size(&ready_queue) + get_size(input_queue));
                        terminate_process(&running_process, *simulation_time);

                    // if not, suspend it
                    } else {
                        suspend_process(&running_process, *simulation_time);
                        running_process.current_state = READY;
                        enqueue(&ready_queue, running_process);
                    }

                    // pick a process to run
                    running_process = simple_dequeue(&ready_queue);
                    running_process.current_state = RUNNING;
                    running_process.running_num += 1;
                    if (running_process.running_num == 1) {
                        create_process(&running_process, *simulation_time);
                    } else {
                        resume_process(&running_process, *simulation_time);
                    }
                    enqueue(&running_queue, running_process); 
                    print_output(running_process, simulation_time, 0);  

                // if there are no processes running 
                } else {
                    // create the process
                    running_process = simple_dequeue(&ready_queue);
                    running_process.current_state = RUNNING;
                    running_process.running_num += 1;
                    if (running_process.running_num == 1) {
                        create_process(&running_process, *simulation_time);
                    } else {
                        resume_process(&running_process, *simulation_time);
                    }
                    enqueue(&running_queue, running_process);
                    print_output(running_process, simulation_time, 0);
                }
            
            // if there are no ready processes
            } else {

                // if there is a process running, check if it is finished
                if (!isEmpty(&running_queue)) {
                    running_process = dequeue(&running_queue);
                    running_process.remaining_time -= quantum;
                    running_process.running_num += 1;
                    if (running_process.remaining_time <= 0) {
                        running_process.current_state = FINISHED;
                        running_process.finished_time = *simulation_time;
                        enqueue(&finished_queue, running_process);
                        print_output(running_process, simulation_time, process_num - get_size(&finished_queue));
                        terminate_process(&running_process, *simulation_time);

                    // if not, continue it
                    } else {
                        resume_process(&running_process, *simulation_time);
                        enqueue(&running_queue, running_process);
                    }    
                }
            }
            *simulation_time += quantum;
        }
    print_performance_statistics(&finished_queue, process_num, *simulation_time - quantum);
    }

    // free all memory used
    process_queue_free(input_queue);
    process_queue_free(&ready_queue);
    process_queue_free(&running_queue);
    process_queue_free(&finished_queue);
    
}

// change the state to ready if the process arrived
void change_state(ProcessQueue *processes, int *simulation_time, state process_state) {
    ProcessNode *current_node = processes->head;

    while (current_node != NULL) {
        if (process_state == READY) {
            Process *process = &current_node->process;
            if (*simulation_time >= process->arrival_time) {
                process->current_state = READY;
            }
        }
        current_node = current_node->next;
    }
}

// print the expected output
void print_output(Process process, int *simulation_time, int data) {
    if (process.current_state == READY) {
        printf("%d,READY,process_name=%s,assigned_at=%d\n", *simulation_time, process.name, data);
    }
    if (process.current_state == RUNNING) {
        printf("%d,RUNNING,process_name=%s,remaining_time=%d\n", *simulation_time, process.name, process.remaining_time);
    }
    if (process.current_state == FINISHED) {
        printf("%d,FINISHED,process_name=%s,proc_remaining=%d\n", *simulation_time, process.name, data);
    }
}

// print performance statistics
void print_performance_statistics(ProcessQueue *finished_queue, int process_num, int simulation_time) {
    double total_turnaround_time = 0;
    double total_overhead = 0;
    double max_overhead = 0;

    // Calculate performance statistics
    for (ProcessNode *node = finished_queue->head; node != NULL; node = node->next) {
        int turnaround_time = node->process.finished_time - node->process.arrival_time;
        double overhead = (double)turnaround_time / node->process.time_to_finish;

        total_turnaround_time += turnaround_time;
        total_overhead += overhead;

        if (overhead > max_overhead) {
            max_overhead = overhead;
        }
    }

    int avg_turnaround_time = (int)ceil(total_turnaround_time / process_num);
    double avg_overhead = total_overhead / process_num;

    // Print performance statistics
    printf("Turnaround time %d\n", avg_turnaround_time);
    printf("Time overhead %.2f %.2f\n", max_overhead, avg_overhead);
    printf("Makespan %d\n", simulation_time);
}

// create process
void create_process(Process *process, int time) {
    pid_t pid;

    if (pipe(process->pipe1) == -1) {
        exit(EXIT_FAILURE);
    }

    if (pipe(process->pipe2) == -1) {
        exit(EXIT_FAILURE);
    }

    // create the child process
    pid = fork();
    if (pid == -1) {
        exit(EXIT_FAILURE);
    } else if (pid == 0) {
        close(process->pipe1[1]);
        close(process->pipe2[0]);

        dup2(process->pipe1[0], STDIN_FILENO);

        dup2(process->pipe2[1], STDOUT_FILENO);
        char* args[] = {"-v", process->name, NULL};
        execvp("./process", args);
    } else {
        process->pid = pid;
        close(process->pipe1[0]);
        close(process->pipe2[1]);

        unsigned char arr[4];
        arr[0] = (time >> 24) & 0xff;
        arr[1] = (time >> 16) & 0xff;
        arr[2] = (time >> 8) & 0xff;
        arr[3] = time & 0xff;

        write(process->pipe1[1], arr, sizeof(arr));

        unsigned char c;

        read(process->pipe2[0], &c, sizeof(c));

        if (arr[3] != c) {
            printf("OK"); 
        }
    }
}

// resuming or continue the process
void resume_process(Process *process, int time) {
    unsigned char arr[4];
    arr[0] = (time >> 24) & 0xff;
    arr[1] = (time >> 16) & 0xff;
    arr[2] = (time >> 8) & 0xff;
    arr[3] = time & 0xff;

    write(process->pipe1[1], arr, sizeof(arr));

    kill(process->pid, SIGCONT);

    unsigned char c;

    read(process->pipe2[0], &c, 1);
    if (arr[3] != c) {
        printf("OK");
    }
}

// suspend the process
void suspend_process(Process *process, int time) {
    unsigned char arr[4];
    arr[0] = (time >> 24) & 0xff;
    arr[1] = (time >> 16) & 0xff;
    arr[2] = (time >> 8) & 0xff;   
    arr[3] = time & 0xff;

    write(process->pipe1[1], arr, sizeof(arr));
    int wstatus;
    kill(process->pid, SIGTSTP);
    waitpid(process->pid, &wstatus, WUNTRACED);

    while (!WIFSTOPPED(wstatus)) {}
}

// terminate the process
void terminate_process(Process *process, int time) {
    unsigned char arr[4];
    arr[0] = (time >> 24) & 0xff;
    arr[1] = (time >> 16) & 0xff;
    arr[2] = (time >> 8) & 0xff;   
    arr[3] = time & 0xff;
 
    write(process->pipe1[1], arr, sizeof(arr));
    
    kill(process->pid, SIGTERM);

    char SHA[64];
    SHA[64] = '\0';
    read(process->pipe2[0], &SHA, sizeof(SHA));

    printf("%d,FINISHED-PROCESS,process_name=%s,sha=%s\n", time, process->name, SHA);
    close(process->pipe1[1]);
    close(process->pipe2[0]);
}