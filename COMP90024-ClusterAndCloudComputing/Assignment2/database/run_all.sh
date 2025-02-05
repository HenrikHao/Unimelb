#!/bin/bash

for file in *.py
do 
  echo "Running $file..."
  python "$file"
done

echo "All scripts executed."
