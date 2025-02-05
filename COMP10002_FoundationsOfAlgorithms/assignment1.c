/* Program to assist in the challenge of solving sudoku puzzles.
Skeleton program written by Alistair Moffat, ammoffat@unimelb.edu.au,
August 2021, with the intention that it be modified by students
to add functionality, as required by the assignment specification.
Student Authorship Declaration:
(1) I certify that except for the code provided in the initial skeleton
file, the program contained in this submission is completely my own
individual work, except where explicitly noted by further comments that
provide details otherwise. I understand that work that has been developed
by another student, or by me in collaboration with other students, or by
non-students as a result of request, solicitation, or payment, may not be
submitted for assessment in this subject. I understand that submitting for
assessment work developed by or in collaboration with other students or
non-students constitutes Academic Misconduct, and may be penalized by mark
deductions, or by other penalties determined via the University of
Melbourne Academic Honesty Policy, as described at
https://academicintegrity.unimelb.edu.au.
(2) I also certify that I have not provided a copy of this work in either
softcopy or hardcopy or any other form to any other student, and nor will I
do so until after the marks are released. I understand that providing my
work to other students, regardless of my intention or any undertakings made
to me by that other student, is also Academic Misconduct.
(3) I further understand that providing a copy of the assignment
specification to any form of code authoring or assignment tutoring service,
or drawing the attention of others to such services and code that may have
been made available via such a service, may be regarded as Student General
Misconduct (interfering with the teaching activities of the University
and/or inciting others to commit Academic Misconduct). I understand that
an allegation of Student General Misconduct may arise regardless of whether
or not I personally make use of such solutions or sought benefit from such
actions.
Signed by: Zhuoyang Hao
Dated: 2021/9/17
*/
#include <stdio.h>
#include <stdlib.h>
#include <assert.h>
/* these #defines provided as part of the initial skeleton */
#define NDIM 3 /* sudoku dimension, size of each inner square */
#define NDIG (NDIM * NDIM)
/* total number of values in each row */
#define NGRP 3 /* number of sets each cell is a member of */
#define NSET (NGRP * NDIG)
/* total number of sets in the sudoku */
#define NCLL (NDIG * NDIG)
/* total number of cells in the sudoku */
#define ERROR (-1) /* error return value from some functions */
/* these global constant arrays provided as part of the initial skeleton,
you may use them in your code but must not alter them in any way,
regard them as being completely fixed. They describe the relationships
between the cells in the sudoku and provide a basis for all of the
sudoku processing loops */
/* there are 27 different different sets of elements that need to be
checked against each other, this array converts set numbers to cells,
that's why its called s2c */
int s2c[NSET][NDIM * NDIM] = {
    /* the first group of nine sets describe the sudoku's rows */
    {0, 1, 2, 3, 4, 5, 6, 7, 8},
    {9, 10, 11, 12, 13, 14, 15, 16, 17},
    {18, 19, 20, 21, 22, 23, 24, 25, 26},
    {27, 28, 29, 30, 31, 32, 33, 34, 35},
    {36, 37, 38, 39, 40, 41, 42, 43, 44},
    {45, 46, 47, 48, 49, 50, 51, 52, 53},
    {54, 55, 56, 57, 58, 59, 60, 61, 62},
    {63, 64, 65, 66, 67, 68, 69, 70, 71},
    {72, 73, 74, 75, 76, 77, 78, 79, 80},
    /* the second group of nine sets describes the sudoku's columns */
    {0, 9, 18, 27, 36, 45, 54, 63, 72},
    {1, 10, 19, 28, 37, 46, 55, 64, 73},
    {2, 11, 20, 29, 38, 47, 56, 65, 74},
    {3, 12, 21, 30, 39, 48, 57, 66, 75},
    {4, 13, 22, 31, 40, 49, 58, 67, 76},
    {5, 14, 23, 32, 41, 50, 59, 68, 77},
    {6, 15, 24, 33, 42, 51, 60, 69, 78},
    {7, 16, 25, 34, 43, 52, 61, 70, 79},
    {8, 17, 26, 35, 44, 53, 62, 71, 80},
    /* the last group of nine sets describes the inner squares */
    {0, 1, 2, 9, 10, 11, 18, 19, 20},
    {3, 4, 5, 12, 13, 14, 21, 22, 23},
    {6, 7, 8, 15, 16, 17, 24, 25, 26},
    {27, 28, 29, 36, 37, 38, 45, 46, 47},
    {30, 31, 32, 39, 40, 41, 48, 49, 50},
    {33, 34, 35, 42, 43, 44, 51, 52, 53},
    {54, 55, 56, 63, 64, 65, 72, 73, 74},
    {57, 58, 59, 66, 67, 68, 75, 76, 77},
    {60, 61, 62, 69, 70, 71, 78, 79, 80},
};
/* there are 81 cells in a dimension-3 sudoku, and each cell is a
member of three sets, this array gets filled by the function
fill_c2s(), based on the defined contents of the array s2c[][] */
int c2s[NCLL][NGRP];
void fill_c2s()
{
    int s = 0, d = 0, c;
    for (; s < NSET; s++)
    {
        /* record the first set number each cell is part of */
        for (c = 0; c < NDIM * NDIM; c++)
        {
            c2s[s2c[s][c]][d] = s;
        }
        if ((s + 1) % (NGRP * NDIM) == 0)
        {
            d++;
        }
    }
#if 0
/* this code available here if you want to see the array
cs2[][] that gets created, just change that 0 two lines back
to a 1 and recompile */
for (c=0; c<NCLL; c++) {
printf("cell %2d: sets ", c);
for (s=0; s<NGRP; s++) {
printf("%3d", c2s[c][s]);
}
printf("\n");
}
printf("\n");
#endif
    return;
}
/* find the row number a cell is in, counting from 1
 */
int rownum(int c)
{
    return 1 + (c / (NDIM * NDIM));
}
/* find the column number a cell is in, counting from 1
 */
int colnum(int c)
{
    return 1 + (c % (NDIM * NDIM));
}
/* find the minor square number a cell is in, counting from 1
 */
int sqrnum(int c)
{
    return 1 + 3 * (c / NSET) + (c / NDIM) % NDIM;
}
/* If you wish to add further #defines, put them below this comment,
then prototypes for the functions that you add
The only thing you should alter above this line is to complete the
Authorship Declaration
*/
void stage_one_print(int A[]);
int stage_two_print(int A[]);
void stage_three_print(int A[]);
/****************************************************************/
/****************************************************************/
/* main program controls all the action
 */
int main(int argc, char *argv[])
{
    int sudoku[NCLL]; /* one dimention array of sudoku */
    int ta_daa = 1;   /* a flag variable to determine if all unknowns are solved */
    /* read input into sudoku array */
    for (int i = 0; i < NCLL; i++)
    {
        scanf("%d", &sudoku[i]);
    }
    fill_c2s();
    printf("\n");
    stage_one_print(sudoku);
    /* if there are no violations, solve the sudoku */
    if (stage_two_print(sudoku))
    {
        stage_three_print(sudoku);
        stage_one_print(sudoku);
    }
    /* determine if all unknowns are solved */
    for (int j = 0; j < NCLL; j++)
    {
        if (sudoku[j] == 0)
        {
            ta_daa = 0;
            break;
        }
    }
    if (ta_daa)
        printf("\nta daa!!!");
    printf("\n");
    /* all done, so pack up bat and ball and head home */
    return 0;
}
/* stage one print out
 */
void stage_one_print(int A[])
{
    int unknowns = 0;    /* count number of unknowns */
    int num_printed = 0; /* number of cells that have been printed */
    for (int i = 0; i < NCLL; i++)
    {
        num_printed++;
        if ((A[i] == 0) && (num_printed % 9 != 0))
        {
            printf(". ");
            unknowns += 1;
        }
        else if ((A[i] == 0) && (num_printed % 9 == 0))
        {
            printf(".");
            unknowns += 1;
        }
        else if (num_printed % 9 != 0)
        {
            printf("%d ", A[i]);
        }
        else
        {
            printf("%d", A[i]);
        }
        if ((num_printed % 3 == 0) && (num_printed % 9 != 0))
            printf("| ");
        if (num_printed % 9 == 0)
            printf("\n");
        if ((num_printed == 27) || (num_printed == 54))
            printf("------+-------+------\n");
    }
    printf("\n%2d cells are unknown\n", unknowns);
}
/* stage two print out, if there are violations, return 0 and print them out, else
return 1
*/
int stage_two_print(int A[])
{
    printf("\n");
    int set[NSET][NDIG + 1] = {0}; /* A 2d array which used to indicate which set has
      violation */
    int cell;                      /* number in each cell */
    int vio = 0;                   /* a flag variable to determine if there exists violations */
    int violations = 0;            /* to count how many sets have violations */
    int total_violations = 0;      /* violations in total */
    int violation_set[NSET] = {0}; /* an array to record how many violations in
    each set */
    /* read each cell of sudoku into <set> by the set it is in */
    for (int i = 0; i < NCLL; i++)
    {
        cell = A[i];
        for (int j = 0; j < NGRP; j++)
        {
            set[c2s[i][j]][cell] += 1;
        }
    }
    /* iterate <set> to see if there exists violations in each set */
    for (int set_num = 0; set_num < NSET; set_num++)
    {
        for (int num = 1; num < NDIG + 1; num++)
        {
            int instances = set[set_num][num];
            if (instances > 1)
            {
                vio = 1;
                total_violations += 1;
                violation_set[set_num] += 1;
                if (set_num <= 8)
                {
                    printf("set%3d (row%2d):%2d instances of %d\n", set_num,
                           set_num + 1, instances, num);
                }
                else if ((set_num > 8) && (set_num <= 17))
                {
                    printf("set%3d (col%2d):%2d instances of %d\n", set_num,
                           set_num - 8, instances, num);
                }
                else
                {
                    printf("set%3d (sqr%2d):%2d instances of %d\n", set_num,
                           set_num - 17, instances, num);
                }
            }
        }
    }
    /* print out violations */
    if (vio)
    {
        for (int j = 0; j < NSET; j++)
        {
            if (violation_set[j] != 0)
                violations += 1;
        }
        printf("\n%d different sets have violations\n", violations);
        printf("%d violations in total", total_violations);
        return 0;
    }
    return 1;
}
/* stage three print out
 */
void stage_three_print(int A[])
{
    int have_zero = 1; /* to count how many unknowns(zero) in sudoku */
    int cell;          /* num in cell */
    while (have_zero)
    {
        int filled_in = 0;   /* a flag variable to determine if strategy one is
            available to sudoku */
        int tmp[NCLL] = {0}; /* a temporary array to record which cell is unknown
                              */
        have_zero = 0;       /* initialize have_zero to 0 each time */
        for (int i = 0; i < NCLL; i++)
        {
            if (A[i] == 0)
            {
                have_zero += 1;
                tmp[i] = -1;              /* label all empty cells in tmp */
                int all_number[10] = {0}; /* an array to record how many
                times each number appears in a set */
                /* based on three sets an unknown cell is in, calculate
                number of times each number appears
                if there is only one number have not appeared in three
                sets, then it must be the answer */
                for (int k = 0; k < NGRP; k++)
                {
                    for (int j = 0; j < NDIG; j++)
                    {
                        cell = A[s2c[c2s[i][k]][j]];
                        all_number[cell] += 1;
                    }
                }
                int zero_num = 0;  /* how many numbers have not appeared in
                   three sets that an unknown is in */
                int empty_num = 0; /* the solution */
                for (int m = 1; m < 10; m++)
                {
                    if (all_number[m] == 0)
                    {
                        zero_num += 1;
                        empty_num = m;
                    }
                }
                /* replace the unknown in tmp with the answer */
                if (zero_num == 1)
                {
                    have_zero -= 1;
                    tmp[i] = empty_num;
                }
            }
        }
        /* to determine if some unknowns can be solved */
        for (int y = 0; y < NCLL; y++)
        {
            if ((tmp[y] != 0) && (tmp[y] != -1))
            {
                filled_in = 1;
                break;
            }
        }
        /* if a solution can be generated, print them out and fill them into
        the original sudoku array
        else, break this while loop and end this function*/
        if (filled_in)
        {
            printf("strategy one\n");
            for (int z = 0; z < NCLL; z++)
            {
                if ((tmp[z] != 0) && (tmp[z] != -1))
                {
                    A[z] = tmp[z];
                    printf("row %d col %d must be %d\n", rownum(z),
                           colnum(z), A[z]);
                }
            }
        }
        else
        {
            break;
        }
        printf("\n");
    }
}
/****************************************************************/
/* algorithms are fun */