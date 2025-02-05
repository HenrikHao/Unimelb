/* 
leap.c

Implementations for leap list construction and manipulation.

Skeleton written by Grady Fitzaptrick for COMP20007 Assignment 1 2022
*/
#include <stdio.h>
#include <stdlib.h>
#include <assert.h>
#include "leap.h"
#include "utils.h"

struct node {
    int baseAccess; // base access of this node
    int data; // data
    struct node *next; // next node in the same level
    struct node *nextHeight; // pointer to the node in next level
    struct node *prevHeight; // pointer to the node in previous level
    int height; // the height of this node
    int requiredAccess; // requiredAccess to reach this node
};

struct leapList {
    /* IMPLEMENT: Fill in structure. */
    struct node **heads; // a dynamic array of heads
};

/* create a node */
struct node *createNode(){
    struct node *newNode = malloc(sizeof(struct node));
    newNode->next = NULL;
    return newNode;
}

struct leapList *newList(int maxHeight, double p, enum problemPart part){
    /* IMPLEMENT: Set up list */
    struct leapList *newList; 
    newList = malloc(sizeof(struct leapList));
    assert(newList);
    newList->heads = malloc(sizeof(struct node *) * maxHeight);
    assert(newList->heads);
    
    // create head nodes
    for (int i=0; i<maxHeight; i++) {
        newList->heads[i] = createNode();
        newList->heads[i]->height = i;
        newList->heads[i]->next = NULL;
        if (i==0) {
            newList->heads[i]->nextHeight = newList->heads[i+1];
            newList->heads[i]->prevHeight = NULL;
        } else if (i==maxHeight-1) {
            newList->heads[i]->nextHeight = NULL;
            newList->heads[i]->prevHeight = newList->heads[i-1];
        } else {
            newList->heads[i]->nextHeight = newList->heads[i+1];
            newList->heads[i]->prevHeight = newList->heads[i-1];
        }
        
    }
    return newList;
}

void printLevel(struct leapList *list, int level){
    assert(list);
    struct node *temp = list->heads[level]->next;
    if(! list){
        printf("\n");
        return;
    }
    /* IMPLEMENT (Part B): loop over list at given level, printing out each value. */
    /* Note: while additional next elements, print a space after the key. If no additional next elements, 
    print a new line and then return. */
    while (temp != NULL) {
        printf("%d", temp->data);
        if (temp->next != NULL) {
            printf(" ");
        }
        temp = temp->next;
    }
    printf("\n");
    return;
}

void insertKey(int key, struct leapList *list, int maxHeight, double p){
    /* IMPLEMENT: Insert the key into the given leap list. */
    assert(list);
    int height = 1;
    struct node *firstNode = createNode();
    struct node *newNode;
    firstNode->data = key;
    sortedInsert(&(list->heads[0]->next), firstNode);

    /* Insert the key to the upper level */
    while((height < maxHeight) && ((double) rand()/RAND_MAX < p)) {
        newNode = createNode();
        newNode->data = key;
        sortedInsert(&(list->heads[height]->next), newNode);
        firstNode->nextHeight = newNode;
        newNode->prevHeight = firstNode;
        firstNode = newNode;
        height++;
    }  
}

/* Insert the key in an ascending order */
void sortedInsert(struct node **head, struct node *new) {
    struct node *current;

    if (*head == NULL || (*head)->data >= new->data) {
        new->next = *head;
        *head = new;
    } else {
        current = *head;
        while (current->next != NULL && current->next->data < new->data) {
            current = current->next;
        }
        new->next = current->next;
        current->next = new;
    }
}

/* update the baseAccess of data in the first level */
void baseAccess(struct leapList *list) {
    struct node *tmpNode = list->heads[0];
    int baseAccess = 0;
    while (tmpNode != NULL) {
        tmpNode->baseAccess = baseAccess;
        baseAccess++;
        tmpNode = tmpNode->next;
    }
}

/* find the node where the key in */
struct node *findingNode(struct node *startnode, int element, int requiredAccesses, struct node *comparedNode) {
    int data;
    struct node *nextNode = startnode->next;

    /* if the finding process reached the first level, then check if next node
    of the start is NULL or larger than the element, if so, return the last 
    visited node */
    if (!(startnode->prevHeight)) {
        if (nextNode != NULL) {
            if (nextNode->data > element) {
                nextNode->requiredAccess = requiredAccesses;
                return nextNode;
            }
        } else {
            startnode->requiredAccess = requiredAccesses;
            return startnode;
        }
    }

    /* During the descent of leap list, if the two values being compared are 
    the same as last comparation, requiredAccess plus 1 */
    if (nextNode != NULL) {
        data = nextNode->data;
        if (comparedNode != NULL) {
            if (data != comparedNode->data) {
                requiredAccesses += 1;
            }
        } else {
            requiredAccesses += 1;
        }
        if (data == element) {
            nextNode->requiredAccess = requiredAccesses;
            return nextNode;
        } else if (data > element) {
            return findingNode(startnode->prevHeight, element, requiredAccesses, nextNode);
        } else {
            return findingNode(nextNode, element, requiredAccesses, nextNode);
        }
    } else {
        return findingNode(startnode->prevHeight, element, requiredAccesses, nextNode);
    }
    return NULL;
}

/* Queries the leap list for the given key and places the result in the solution structure. */
int findKey(int key, struct leapList *list, enum problemPart part, struct solution *solution, int maxHeight){
    int found = NOTFOUND;
    int element = key;
    int baseAccesses = 0;
    int requiredAccesses = 0;
    struct node * finalNode;
    struct node * startNode;
    struct node *comparedNode = NULL;
    //list->heads[maxHeight-1]->next;
    assert(solution);
    /* IMPLEMENT: Find the given key in the leap list. */
    startNode = list->heads[maxHeight-1];
    finalNode = findingNode(startNode, element, requiredAccesses, comparedNode);
    baseAccess(list);    
    if (finalNode->data == element) {
        struct node *tmpNode = list->heads[0];
        /* find the baseAccess  */
        while (tmpNode != NULL) {
            if (tmpNode->data == finalNode->data) {
                baseAccesses = tmpNode->baseAccess;
                break;
            }
            tmpNode = tmpNode->next;
        }
        found = PRESENT;
        requiredAccesses = finalNode->requiredAccess;
    } else {
        requiredAccesses = finalNode->requiredAccess;
        baseAccesses = finalNode->baseAccess;
    }

    /* Insert result into solution. */
    (solution->queries)++;
    solution->queryResults = (int *) realloc(solution->queryResults, sizeof(int) * solution->queries);
    assert(solution->queryResults);
    (solution->queryResults)[solution->queries - 1] = found;
    solution->queryElements = (int *) realloc(solution->queryElements, sizeof(int) * solution->queries);
    assert(solution->queryElements);
    solution->queryElements[solution->queries - 1] = element;
    solution->baseAccesses = (int *) realloc(solution->baseAccesses, sizeof(int) * solution->queries);
    assert(solution->baseAccesses);
    solution->baseAccesses[solution->queries - 1] = baseAccesses;
    solution->requiredAccesses = (int *) realloc(solution->requiredAccesses, sizeof(int) * solution->queries);
    assert(solution->requiredAccesses);
    solution->requiredAccesses[solution->queries - 1] = requiredAccesses;
    return found;
}

void deleteKey(int key, struct leapList *list, enum problemPart part, int maxHeight){
    /* IMPLEMENT: Remove the given key from the leap list. */
    /* Traverse the leaplist and delete */
    for (int height=maxHeight-1; height>=0; height--) {
        struct node *prev = list->heads[height];
        struct node *tmpNode = list->heads[height]->next;
        while (tmpNode != NULL) {
            if (tmpNode->data == key) {
                if (tmpNode->next != NULL) {
                    prev->next = tmpNode->next;
                } else{
                    prev->next = NULL;
                }
                free(tmpNode);
                break;
            }
            prev = tmpNode;
            tmpNode = tmpNode->next;
        }
    }
}

void freeList(struct leapList *list){
    /* IMPLEMENT: Free all memory used by the list. */
    struct node *heads = list->heads[0];
    while (heads != NULL) {
        struct node *listHead = heads;
        struct node *tmp;
        while (listHead != NULL) {
            tmp = listHead;
            listHead = listHead->next;
            free(tmp);
        }
        heads = heads->nextHeight;
    }
    free(list->heads);
    free(list);

}

void freeSolution(struct solution *solution){
    if(! solution){
        return;
    }
    freeList(solution->list);
    if(solution->queries > 0){
        free(solution->queryResults);
        free(solution->queryElements);
        free(solution->baseAccesses);
        free(solution->requiredAccesses);
    }
    free(solution);
}

