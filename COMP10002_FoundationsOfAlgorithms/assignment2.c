/* Program to print and play checker games.
Skeleton program written by Artem Polyvyanyy, artem.polyvyanyy@unimelb.edu.au,
September 2021, with the intention that it be modified by students
to add functionality, as required by the assignment specification.
Student Authorship Declaration:
(1) I certify that except for the code provided in the initial skeleton file,
the program contained in this submission is completely my own individual
work, except where explicitly noted by further comments that provide details
otherwise. I understand that work that has been developed by another student,
or by me in collaboration with other students, or by non-students as a result
of request, solicitation, or payment, may not be submitted for assessment in
this subject. I understand that submitting for assessment work developed by
or in collaboration with other students or non-students constitutes Academic
Misconduct, and may be penalized by mark deductions, or by other penalties
determined via the University of Melbourne Academic Honesty Policy, as
described at https://academicintegrity.unimelb.edu.au.
(2) I also certify that I have not provided a copy of this work in either
softcopy or hardcopy or any other form to any other student, and nor will I
do so until after the marks are released. I understand that providing my work
to other students, regardless of my intention or any undertakings made to me
by that other student, is also Academic Misconduct.
(3) I further understand that providing a copy of the assignment specification
to any form of code authoring or assignment tutoring service, or drawing the
attention of others to such services and code that may have been made
available via such a service, may be regarded as Student General Misconduct
(interfering with the teaching activities of the University and/or inciting
others to commit Academic Misconduct). I understand that an allegation of
Student General Misconduct may arise regardless of whether or not I personally
make use of such solutions or sought benefit from such actions.
Signed by: Zhuoyang Hao
Dated: 2021-10-04
*/
#include <stdlib.h>
#include <stdio.h>
#include <limits.h>
#include <assert.h>
#include <math.h>
/* some #define's from my sample solution ------------------------------------*/
#define BOARD_SIZE 8       // board size
#define ROWS_WITH_PIECES 3 // number of initial rows with pieces
#define CELL_EMPTY '.'     // empty cell character
#define CELL_BPIECE 'b'    // black piece character
#define CELL_WPIECE 'w'    // white piece character
#define CELL_BTOWER 'B'    // black tower character
#define CELL_WTOWER 'W'    // white tower character
#define COST_PIECE 1       // one piece cost
#define COST_TOWER 3       // one tower cost
#define TREE_DEPTH 3       // minimax tree depth
#define COMP_ACTIONS 10    // number of computed actions
#define LEGAL_MOVE 0       // legal action
#define ERROR_ONE 1        // error type 1
#define ERROR_TWO 2        // error type 2
#define ERROR_THREE 3      // error type 3
#define ERROR_FOUR 4       // error type 4
#define ERROR_FIVE 5       // error type 5
#define ERROR_SIX 6        // error type 6
#define MAX_CHILDREN 48    // num of children a node can have at most
#define MAX_MOVE 4         // num of moves a piece can make at most
#define CONTINUE 1         // stage 1 can make next move, continue
#define END 0              // game end
/* one type definition from my sample solution -------------------------------*/
typedef unsigned char board_t[BOARD_SIZE][BOARD_SIZE]; // board type
// action
typedef struct
{
    int row1;
    char col1;
    int row2;
    char col2;
} action_t;
// node in minimax tree
typedef struct node node_t;
struct node
{
    board_t board;                  // board condition
    int cost;                       // cost of board
    node_t *parent;                 // pointer to the parent of node
    node_t *children[MAX_CHILDREN]; // pointer to the children array of node
    int children_num;               // children num
    action_t action_now;            // action that get to this node
    action_t action_next;           // action need to do in next move
};
/* my function prototypes ----------------------------------------------------*/
void initial_setup(board_t);
void board_print(board_t);
void read_and_act(board_t);
void make_move(char, char, int, int, board_t);
int detect_illegal(char, char, int, int, int, board_t);
void print_error(int);
int max(int, int);
int board_cost(board_t);
int all_move(int, int, int, board_t, action_t actions[]);
void distroy_tree(node_t *);
node_t *make_node(node_t *, action_t);
void make_tree(node_t *, int, int);
int stage_one(board_t, int);
void stage_one_print(int, char, int, char, int, int, board_t);
int main(int argc, char *argv[])
{
    // YOUR IMPLEMENTATION OF STAGES 0-2
    board_t board;        // define a board
    initial_setup(board); // initially fill the board
    read_and_act(board);
    printf("\n");
    return EXIT_SUCCESS; // exit program with the success code
}
// Filled in the board to initial state
void initial_setup(board_t board)
{
    int row, column;
    // traverse each cell in the board and fill it in
    for (row = 0; row < BOARD_SIZE; row++)
    {
        for (column = 0; column < BOARD_SIZE; column++)
        {
            if (row == 3 || row == 4)
            {
                board[row][column] = CELL_EMPTY;
            }
            else if (row % 2 == 0 && column % 2 != 0)
            {
                if (row < 3)
                {
                    board[row][column] = CELL_WPIECE;
                }
                else
                {
                    board[row][column] = CELL_BPIECE;
                }
            }
            else if (row % 2 != 0 && column % 2 == 0)
            {
                if (row < 3)
                {
                    board[row][column] = CELL_WPIECE;
                }
                else
                {
                    board[row][column] = CELL_BPIECE;
                }
            }
            else
            {
                board[row][column] = CELL_EMPTY;
            }
        }
    }
}
// Print out the board
void board_print(board_t board)
{
    int row, column;
    printf(" A B C D E F G H\n");
    printf(" +---+---+---+---+---+---+---+---+");
    // traverse the board and print out each piece in the cell
    for (row = 0; row < BOARD_SIZE; row++)
    {
        printf("\n");
        printf("%2d |", row + 1);
        for (column = 0; column < BOARD_SIZE; column++)
        {
            printf("%2c |", board[row][column]);
        }
        printf("\n");
        printf(" +---+---+---+---+---+---+---+---+");
    }
}
// Read input and get to stage1 or stage2
void read_and_act(board_t board)
{
    char col1, col2;
    int rownum1, rownum2, action = 0;
    while (scanf("%c%d-%c%d\n", &col1, &rownum1, &col2, &rownum2) == 4)
    {
        if (action == 0)
        {
            printf("BOARD SIZE: 8x8\n");
            printf("#BLACK PIECES: 12\n");
            printf("#WHITE PIECES: 12\n");
            board_print(board);
        }
        action += 1;
        // determine if this move is illegal
        int error = detect_illegal(col1, col2, rownum1, rownum2, action, board);
        if (error)
        {
            print_error(error);
            break;
        }
        // if not illegal then make a move
        make_move(col1, col2, rownum1, rownum2, board);
        printf("\n=====================================\n");
        // determine if it is a white move or black move
        if (action % 2 != 0)
        {
            printf("BLACK ACTION #%d: %c%d-%c%d\n",
                   action, col1, rownum1, col2, rownum2);
        }
        else
        {
            printf("WHITE ACTION #%d: %c%d-%c%d\n",
                   action, col1, rownum1, col2, rownum2);
        }
        // print out the cost and the board
        printf("BOARD COST: %d\n", board_cost(board));
        board_print(board);
    }
    // if scanf reads a 'A' then it will be stored in col1, go to stage 1
    if (col1 == 'A')
    {
        stage_one(board, action);
    }
    // if scanf reads a 'A' then it will be stored in col1, go to stage 2
    if (col1 == 'P')
    {
        for (int i = 0; i < 10; i++)
        {
            if (stage_one(board, action) == 0)
            {
                break;
            }
            action += 1;
        }
    }
}
// Detect illegal move
int detect_illegal(char col1, char col2, int rownum1, int rownum2,
                   int action, board_t board)
{
    int colnum1 = col1 - 'A', colnum2 = col2 - 'A'; // covert col char to int
    int bigger_column = max(colnum1, colnum2);      // get the bigger column
    // multiple if statements to determine if there is a error
    if (colnum1 > 7 || rownum1 > 8 || colnum1 < 0 || rownum1 < 1)
    {
        return ERROR_ONE;
    }
    if (colnum2 > 7 || rownum2 > 8 || colnum2 < 0 || rownum2 < 1)
    {
        return ERROR_TWO;
    }
    if (board[rownum1 - 1][colnum1] == CELL_EMPTY)
    {
        return ERROR_THREE;
    }
    if (board[rownum2 - 1][colnum2] != CELL_EMPTY)
    {
        return ERROR_FOUR;
    }
    if ((action % 2 != 0 && (board[rownum1 - 1][colnum1] == CELL_WPIECE ||
                             board[rownum1 - 1][colnum1] == CELL_WTOWER)) ||
        (action % 2 == 0 && (board[rownum1 - 1][colnum1] == CELL_BPIECE ||
                             board[rownum1 - 1][colnum1] == CELL_BTOWER)))
    {
        return ERROR_FIVE;
    }
    if (board[rownum1 - 1][colnum1] == CELL_WPIECE)
    {
        if (rownum2 == rownum1 + 1 && abs(colnum1 - colnum2) == 1)
        {
            return LEGAL_MOVE;
        }
        if (rownum2 == rownum1 + 2 &&
            (board[rownum1][bigger_column - 1] == CELL_BPIECE ||
             board[rownum1][bigger_column - 1] == CELL_BTOWER))
        {
            return LEGAL_MOVE;
        }
        return ERROR_SIX;
    }
    if (board[rownum1 - 1][colnum1] == CELL_BPIECE)
    {
        if (rownum2 == rownum1 - 1 && abs(colnum1 - colnum2) == 1)
        {
            return LEGAL_MOVE;
        }
        if (rownum2 == rownum1 - 2 &&
            (board[rownum2][bigger_column - 1] == CELL_WPIECE ||
             board[rownum2][bigger_column - 1] == CELL_WTOWER))
        {
            return LEGAL_MOVE;
        }
        return ERROR_SIX;
    }
    if (board[rownum1 - 1][colnum1] == CELL_BTOWER ||
        board[rownum1 - 1][colnum1] == CELL_WTOWER)
    {
        if (abs(rownum2 - rownum1) == 1 && abs(colnum2 - colnum1) == 1)
        {
            return LEGAL_MOVE;
        }
        if (board[rownum1 - 1][colnum1] == CELL_BTOWER &&
            (board[max(rownum1, rownum2) - 2][bigger_column - 1] == CELL_WPIECE ||
             board[max(rownum1, rownum2) - 2][bigger_column - 1] == CELL_WTOWER))
        {
            return LEGAL_MOVE;
        }
        if (board[rownum1 - 1][colnum1] == CELL_WTOWER &&
            (board[max(rownum1, rownum2) - 2][bigger_column - 1] == CELL_BPIECE ||
             board[max(rownum1, rownum2) - 2][bigger_column - 1] == CELL_BTOWER))
        {
            return LEGAL_MOVE;
        }
        return ERROR_SIX;
    }
    return LEGAL_MOVE; // nothing happens, return legal_move
}
// Print out error message
void print_error(int error)
{
    if (error == ERROR_ONE)
    {
        printf("\nERROR: Source cell is outside of the board.");
    }
    if (error == ERROR_TWO)
    {
        printf("\nERROR: Target cell is outside of the board.");
    }
    if (error == ERROR_THREE)
    {
        printf("\nERROR: Source cell is empty.");
    }
    if (error == ERROR_FOUR)
    {
        printf("\nERROR: Target cell is not empty.");
    }
    if (error == ERROR_FIVE)
    {
        printf("\nERROR: Source cell holds opponent's piece/tower.");
    }
    if (error == ERROR_SIX)
    {
        printf("\nERROR: Illegal action.");
    }
}
// Find the bigger value among two nums, being used in detect illegal moves
int max(int a, int b)
{
    if (a > b)
    {
        return a;
    }
    else
    {
        return b;
    }
}
// Make a move
void make_move(char col1, char col2, int rownum1, int rownum2, board_t board)
{
    int colnum1 = col1 - 'A', colnum2 = col2 - 'A';
    int bigger_column = max(colnum1, colnum2), bigger_row = max(rownum1, rownum2);
    if (abs(rownum1 - rownum2) == 1)
    {
        board[rownum2 - 1][colnum2] = board[rownum1 - 1][colnum1];
        board[rownum1 - 1][colnum1] = CELL_EMPTY;
    }
    if (abs(rownum1 - rownum2) == 2)
    {
        board[rownum2 - 1][colnum2] = board[rownum1 - 1][colnum1];
        board[rownum1 - 1][colnum1] = CELL_EMPTY;
        board[bigger_row - 2][bigger_column - 1] = CELL_EMPTY;
    }
    if (board[rownum2 - 1][colnum2] == CELL_WPIECE && rownum2 == 8)
    {
        board[rownum2 - 1][colnum2] = CELL_WTOWER;
    }
    if (board[rownum2 - 1][colnum2] == CELL_BPIECE && rownum2 == 1)
    {
        board[rownum2 - 1][colnum2] = CELL_BTOWER;
    }
}
// Calculate the board cost
int board_cost(board_t board)
{
    int b = 0, B = 0, w = 0, W = 0;
    int row, column;
    for (row = 0; row < BOARD_SIZE; row++)
    {
        for (column = 0; column < BOARD_SIZE; column++)
        {
            if (board[row][column] == CELL_BPIECE)
            {
                b += 1;
            }
            if (board[row][column] == CELL_BTOWER)
            {
                B += 1;
            }
            if (board[row][column] == CELL_WPIECE)
            {
                w += 1;
            }
            if (board[row][column] == CELL_WTOWER)
            {
                W += 1;
            }
        }
    }
    int cost = b + COST_TOWER * B - w - COST_TOWER * W;
    return cost;
}
// Make minimax tree
int stage_one(board_t board, int action)
{
    node_t *root = (node_t *)malloc(sizeof(node_t)); // define a root
    int row, column, cost;
    int depth = 0; // depth of tree
    // traverse the original board and copy its information to board in root
    for (row = 0; row < BOARD_SIZE; row++)
    {
        for (column = 0; column < BOARD_SIZE; column++)
        {
            root->board[row][column] = board[row][column];
        }
    }
    // root does not have parent and initially does not have children
    root->parent = NULL;
    root->children_num = 0;
    // make a tree
    make_tree(root, action, depth);
    // determine if next move can be made according to minimax tree
    if (root->cost == INT_MAX)
    {
        printf("\nBLACK WIN!\n");
        return END;
    }
    else if (root->cost == INT_MIN)
    {
        printf("\nWHITE WIN!\n");
        return END;
    }
    else
    {
        action += 1;
        make_move(root->action_next.col1, root->action_next.col2,
                  root->action_next.row1, root->action_next.row2, board);
        cost = board_cost(board);
        // print out the board after make a stage-1 move
        stage_one_print(action, root->action_next.col1, root->action_next.row1,
                        root->action_next.col2, root->action_next.row2, cost, root->board);
        board_print(board);
    }
    distroy_tree(root);
    return CONTINUE;
}
// Explore the tree, make children node
void make_tree(node_t *root, int action, int depth)
{
    // since it comes to the children so action and depth shoud += 1
    action += 1;
    depth += 1;
    int row, column, i;
    char move_piece, move_tower; // piece and tower can move in this action
    int actions_all;             // total num of actions this node can make
    action_t actions[MAX_MOVE];  // array that stores actions
    // if depth > 3, return, this is the end condition of recusion
    if (depth > TREE_DEPTH)
    {
        return;
    }
    // if action is even, white piece and white tower can move
    // else black piece and black tower can move
    if (action % 2 == 0)
    {
        move_piece = CELL_WPIECE;
        move_tower = CELL_WTOWER;
    }
    else
    {
        move_piece = CELL_BPIECE;
        move_tower = CELL_BTOWER;
    }
    // traverse the board and find all children of this node
    for (row = 0; row < BOARD_SIZE; row++)
    {
        for (column = 0; column < BOARD_SIZE; column++)
        {
            if (root->board[row][column] == move_piece ||
                root->board[row][column] == move_tower)
            {
                actions_all = all_move(row, column, action, root->board, actions);
                for (i = 0; i < actions_all; i++)
                {
                    node_t *new = make_node(root, actions[i]);
                    root->children[root->children_num] = new;
                    root->children_num += 1;
                }
            }
        }
    }
    // for each children, recursion to make its children till depth 3
    for (i = 0; i < root->children_num; i++)
    {
        make_tree(root->children[i], action, depth);
    }
    // compute minimax algorithm, find next move
    if (action % 2 != 0)
    {
        int curr_cost = INT_MIN;
        for (i = 0; i < root->children_num; i++)
        {
            if (root->children[i]->cost > curr_cost)
            {
                curr_cost = root->children[i]->cost;
                root->action_next = root->children[i]->action_now;
            }
        }
        root->cost = curr_cost;
    }
    else
    {
        int curr_cost = INT_MAX;
        for (i = 0; i < root->children_num; i++)
        {
            if (root->children[i]->cost < curr_cost)
            {
                curr_cost = root->children[i]->cost;
                root->action_next = root->children[i]->action_now;
            }
        }
        root->cost = curr_cost;
    }
}
// ALL possible moves of a piece
int all_move(int row, int column, int action, board_t board, action_t actions[])
{
    int actions_num = 0;
    char col1 = column + 'A';
    int rownum1 = row + 1;
    // use detect illegal function and struct action_t to find all possible
    // move and store them in clockwise direction
    // everytime it finds one, actions_num+=1
    if (!detect_illegal(col1, col1 + 1, rownum1, rownum1 - 1, action, board))
    {
        actions[actions_num].row1 = rownum1;
        actions[actions_num].col1 = col1;
        actions[actions_num].row2 = rownum1 - 1;
        actions[actions_num].col2 = col1 + 1;
        actions_num += 1;
    }
    if (!detect_illegal(col1, col1 + 2, rownum1, rownum1 - 2, action, board))
    {
        actions[actions_num].row1 = rownum1;
        actions[actions_num].col1 = col1;
        actions[actions_num].row2 = rownum1 - 2;
        actions[actions_num].col2 = col1 + 2;
        actions_num += 1;
    }
    if (!detect_illegal(col1, col1 + 1, rownum1, rownum1 + 1, action, board))
    {
        actions[actions_num].row1 = rownum1;
        actions[actions_num].col1 = col1;
        actions[actions_num].row2 = rownum1 + 1;
        actions[actions_num].col2 = col1 + 1;
        actions_num += 1;
    }
    if (!detect_illegal(col1, col1 + 2, rownum1, rownum1 + 2, action, board))
    {
        actions[actions_num].row1 = rownum1;
        actions[actions_num].col1 = col1;
        actions[actions_num].row2 = rownum1 + 2;
        actions[actions_num].col2 = col1 + 2;
        actions_num += 1;
    }
    if (!detect_illegal(col1, col1 - 1, rownum1, rownum1 + 1, action, board))
    {
        actions[actions_num].row1 = rownum1;
        actions[actions_num].col1 = col1;
        actions[actions_num].row2 = rownum1 + 1;
        actions[actions_num].col2 = col1 - 1;
        actions_num += 1;
    }
    if (!detect_illegal(col1, col1 - 2, rownum1, rownum1 + 2, action, board))
    {
        actions[actions_num].row1 = rownum1;
        actions[actions_num].col1 = col1;
        actions[actions_num].row2 = rownum1 + 2;
        actions[actions_num].col2 = col1 - 2;
        actions_num += 1;
    }
    if (!detect_illegal(col1, col1 - 1, rownum1, rownum1 - 1, action, board))
    {
        actions[actions_num].row1 = rownum1;
        actions[actions_num].col1 = col1;
        actions[actions_num].row2 = rownum1 - 1;
        actions[actions_num].col2 = col1 - 1;
        actions_num += 1;
    }
    if (!detect_illegal(col1, col1 - 2, rownum1, rownum1 - 2, action, board))
    {
        actions[actions_num].row1 = rownum1;
        actions[actions_num].col1 = col1;
        actions[actions_num].row2 = rownum1 - 2;
        actions[actions_num].col2 = col1 - 2;
        actions_num += 1;
    }
    return actions_num;
}
// Make a node
node_t *
make_node(node_t *parent, action_t action)
{
    int row, column;
    char col1 = action.col1, col2 = action.col2;
    int rownum1 = action.row1, rownum2 = action.row2;
    // make a node and store information into it
    node_t *node = (node_t *)malloc(sizeof(*node));
    node->parent = parent;
    node->children_num = 0;
    node->action_now = action;
    for (row = 0; row < BOARD_SIZE; row++)
    {
        for (column = 0; column < BOARD_SIZE; column++)
        {
            node->board[row][column] = parent->board[row][column];
        }
    }
    make_move(col1, col2, rownum1, rownum2, node->board);
    node->cost = board_cost(node->board);
    return node;
}
// Distroy a tree
void distroy_tree(node_t *root)
{
    int i;
    if (root == NULL)
    {
        return;
    }
    // iterate each depth and free it
    for (i = 0; i < root->children_num; i++)
    {
        distroy_tree(root->children[i]);
    }
    free(root);
}
// Stage 1 print
void stage_one_print(int action, char col1, int row1, char col2, int row2,
                     int cost, board_t board)
{
    printf("\n=====================================\n");
    // print out information
    if (action % 2 != 0)
    {
        printf("*** BLACK ACTION #%d: %c%d-%c%d\n",
               action, col1, row1, col2, row2);
        printf("BOARD COST: %d\n", cost);
    }
    else
    {
        printf("*** WHITE ACTION #%d: %c%d-%c%d\n",
               action, col1, row1, col2, row2);
        printf("BOARD COST: %d\n", cost);
    }
}
/* THE END -------------------------------------------------------------------*/
/* algorithms are fun */