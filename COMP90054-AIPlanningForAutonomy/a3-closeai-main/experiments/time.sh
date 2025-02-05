#!/bin/bash

# Define the output file
output_file="minimax_output.txt"

# Run the game 100 times
for i in $(seq 1 50)
do
    echo "Running game $i..." >> "$output_file"
    python general_game_runner.py -g Splendor -a agents.generic.random,minimax-advanced.minimax -q -m 1 -p | grep 'Time' >> "$output_file"
    echo "Game $i completed and output saved in $output_file"
done

echo "All games have been run and saved in $output_file"
