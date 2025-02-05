#!/bin/bash
#SBATCH --nodes=1
#SBATCH --ntasks=8
#SBATCH --time=0-12:00:00
##change the partition to sapphire if needed
#SBATCH --partition=cascade

module load mpi4py/3.1.4
module load Python/3.10.4
time mpiexec -n 8 python3 main.py 
my-job-stats -a -n -s
