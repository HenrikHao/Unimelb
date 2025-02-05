# search.py
# ---------
# Licensing Information:  You are free to use or extend these projects for
# educational purposes provided that (1) you do not distribute or publish
# solutions, (2) you retain this notice, and (3) you provide clear
# attribution to UC Berkeley, including a link to http://ai.berkeley.edu.
# 
# Attribution Information: The Pacman AI projects were developed at UC Berkeley.
# The core projects and autograders were primarily created by John DeNero
# (denero@cs.berkeley.edu) and Dan Klein (klein@cs.berkeley.edu).
# Student side autograding was added by Brad Miller, Nick Hay, and
# Pieter Abbeel (pabbeel@cs.berkeley.edu).


"""
In search.py, you will implement generic search algorithms which are called by
Pacman agents (in searchAgents.py).
"""

import util
from game import Actions
from game import Directions
from collections import defaultdict
import heapq


class SearchProblem:
    """
    This class outlines the structure of a search problem, but doesn't implement
    any of the methods (in object-oriented terminology: an abstract class).

    You do not need to change anything in this class, ever.
    """

    def getStartState(self):
        """
        Returns the start state for the search problem.
        """
        util.raiseNotDefined()

    def isGoalState(self, state):
        """
          state: Search state

        Returns True if and only if the state is a valid goal state.
        """
        util.raiseNotDefined()

    def getSuccessors(self, state):
        """
          state: Search state

        For a given state, this should return a list of triples, (successor,
        action, stepCost), where 'successor' is a successor to the current
        state, 'action' is the action required to get there, and 'stepCost' is
        the incremental cost of expanding to that successor.
        """
        util.raiseNotDefined()

    def getCostOfActions(self, actions):
        """
         actions: A list of actions to take

        This method returns the total cost of a particular sequence of actions.
        The sequence must be composed of legal moves.
        """
        util.raiseNotDefined()


def depthFirstSearch(problem):
    """
    Search the deepest nodes in the search tree first.

    Your search algorithm needs to return a list of actions that reaches the
    goal. Make sure to implement a graph search algorithm.

    To get started, you might want to try some of these simple commands to
    understand the search problem that is being passed in:

    print("Start:", problem.getStartState())
    print("Is the start a goal?", problem.isGoalState(problem.getStartState()))
    print("Start's successors:", problem.getSuccessors(problem.getStartState()))
    """
    "*** YOUR CODE HERE ***"

    util.raiseNotDefined()


def uniformCostSearch(problem):
    """Search the node of least total cost first."""
    "*** YOUR CODE HERE ***"
    util.raiseNotDefined()


def nullHeuristic(state, problem=None):
    """
    This heuristic is trivial.
    """
    return 0

def isValidMove(point, walls, visited):
    """
    This function check if next move is valid.
    """
    return point not in walls and point not in visited

def getNeighbors(point):
    """
    This function returns four adjacent cells coordinates
    """
    pointX, pointY = point
    return [(pointX - 1, pointY), (pointX + 1, pointY), (pointX, pointY - 1), (pointX, pointY + 1)]

def bfsMazeDistance(start, end, walls):
    """
    This function calculates the actual maze distance using BFS
    """
    if start == end:
        return 0
    
    visited = set([start])
    queue = util.Queue()
    queue.push((start, 0))  # Queue of (point, distance)
    
    while not queue.isEmpty():
        current_point, distance = queue.pop()
        
        for neighbor in getNeighbors(current_point):
            if neighbor == end:
                return distance + 1  # Found goal, return distance
            if isValidMove(neighbor, walls, visited):
                visited.add(neighbor)
                queue.push((neighbor, distance + 1))
    
    return -1  # Goal is not reachable

def precomputeDistances(points, walls):
    """
    This function precompute the distance given a set of points
    """
    distances = {}
    for i in range(len(points)):
        for j in range(i+1, len(points)):  # Avoid repeating pairs
            dist = bfsMazeDistance(points[i], points[j], walls)
            distances[(points[i], points[j])] = dist
            distances[(points[j], points[i])] = dist  # Ensure symmetry
    return distances

def foodHeuristic(state, problem):
    """
    Your heuristic for the FoodSearchProblem goes here.

    This heuristic must be consistent to ensure correctness.  First, try to come
    up with an admissible heuristic; almost all admissible heuristics will be
    consistent as well.

    If using A* ever finds a solution that is worse uniform cost search finds,
    your heuristic is *not* consistent, and probably not admissible!  On the
    other hand, inadmissible or inconsistent heuristics may find optimal
    solutions, so be careful.

    The state is a tuple ( pacmanPosition, foodGrid ) where foodGrid is a Grid
    (see game.py) of either True or False. You can call foodGrid.asList() to get
    a list of food coordinates instead.

    If you want access to info like walls, capsules, etc., you can query the
    problem.  For example, problem.walls gives you a Grid of where the walls
    are.

    If you want to *store* information to be reused in other calls to the
    heuristic, there is a dictionary called problem.heuristicInfo that you can
    use. For example, if you only want to count the walls once and store that
    value, try: problem.heuristicInfo['wallCount'] = problem.walls.count()
    Subsequent calls to this heuristic can access
    problem.heuristicInfo['wallCount']
    """
    "*** YOUR CODE HERE for task1 ***"
    pacmanPosition = state[0]
    foodGrid = state[1]
    foodCoordinates = foodGrid.asList()
    if not foodCoordinates:
        return 0
    
    # pre compute the distance of foods, can save a lot of time
    if not problem.heuristicInfo.keys():
        distance = precomputeDistances(foodCoordinates, problem.walls.asList())
        problem.heuristicInfo['distance'] = distance
    else:
        distance = problem.heuristicInfo['distance']

    return prim_mst(pacmanPosition, foodCoordinates, problem.walls.asList(), distance)
    # comment the below line after you implement the algorithm
    #util.raiseNotDefined()

def isValidMoveMAPF(nextPacmanPositions, currentPacmanPositions, walls):
    """
    This functions check for swapping collisions and vertex collisions
    """
    # Check for vertex collisions
    if len(nextPacmanPositions.values()) != len(set(nextPacmanPositions.values())):
        return False
    
    # Check for swapping collisions
    for pacman1, pos1 in nextPacmanPositions.items():
        for pacman2, pos2 in nextPacmanPositions.items():
            if pacman1 != pacman2 and pos1 == currentPacmanPositions[pacman2] and pos2 == currentPacmanPositions[pacman1]:
                return False
    
    # Check if any pacman is moving into a wall
    for pos in nextPacmanPositions.values():
        if walls[pos[0]][pos[1]]:
            return False

    return True

def generateActions(pacmanPositions, foodGrid, walls, currentActions={}, remainingPacmans=[]):
    """
    Generate all possible actions in MAPF problem
    """
    successors = []
    directions = [Directions.NORTH, Directions.SOUTH, Directions.EAST, Directions.WEST, Directions.STOP]

    if not remainingPacmans:
        nextPacmanPositions = {pacman: tuple(map(int, map(sum, zip(pacmanPositions[pacman], Actions.directionToVector(action)))))
                               for pacman, action in currentActions.items()}
        
        if isValidMoveMAPF(nextPacmanPositions, pacmanPositions, walls):
            nextFoodGrid = foodGrid.copy()
            for pacman, pos in nextPacmanPositions.items():
                if nextFoodGrid[pos[0]][pos[1]] == pacman:
                    nextFoodGrid[pos[0]][pos[1]] = False
            successors.append(((nextPacmanPositions, nextFoodGrid), currentActions, 1))
    else:
        for direction in directions:
            nextActions = currentActions.copy()
            nextActions[remainingPacmans[0]] = direction
            successors.extend(generateActions(pacmanPositions, foodGrid, walls, nextActions, remainingPacmans[1:]))

    return successors

def adaptedAstar(problem, heuristic, timeConstraint, agent, foodPosition):  
    """
    This function is the low-level search used in part3
    """
    myPQ = util.PriorityQueue()

    # start state here is just the starting coordinates of the given agent
    startState = problem.getStartState()[0][agent]
    walls = problem.walls

    #set the start time step to be -1
    startTimeStep = -1

    # this is the dictionary storing both path and positions of the agent
    pathPositionDict = defaultdict(list)
    startNode = (startState, 0, pathPositionDict, startTimeStep, True)
    myPQ.push(startNode, heuristic(startState, foodPosition, walls))
    best_g = dict()
    while not myPQ.isEmpty():
        node = myPQ.pop()
        state, cost, path, timeStep, stillFood = node

        # with the time constraint, time step when reaching a state should be considered
        stateTimeKey = (state, timeStep)

        # The goal state now consists of two conditions:
        # 1. eating its food
        # 2. the last position will not cause any conflict
        if (stateTimeKey not in best_g) or (cost < best_g[stateTimeKey]):
            best_g[stateTimeKey] = cost

            satisfyConstraint = True
            if not stillFood:
                satisfyConstraint = True
                for constraint in timeConstraint:
                    constraintAgent, constraintPosition, constraintTime = constraint
                    if agent == constraintAgent:
                        if constraintTime >= timeStep and state == constraintPosition:
                            satisfyConstraint = False
                            break
                if satisfyConstraint:
                    return path

            for succ in adaptedAstarGetSuccsssors(state, problem.walls):
                isTimeConstraint = False
                newTimeStep = timeStep + 1
                succPosition, succAction, succCost = succ
                succStillFood = True

                # check if this state still have food to eat
                if stillFood:
                    if succPosition == foodPosition:
                        succStillFood = False
                else:
                    succStillFood = False
                for constraint in timeConstraint:
                    constraintAgent, constraintPosition, constraintTime = constraint
                    if (agent == constraintAgent) and (constraintPosition == succPosition) and (newTimeStep == constraintTime):
                        isTimeConstraint = True
                        break

                # if the state position is in time constraint, skip this state
                if isTimeConstraint:
                    continue

                if path:
                    newPath = {key: value[:] for key, value in path.items()}
                else:
                    newPath = defaultdict(list)
                
                # appending actions and positions
                newPath['path'].append(succAction)
                newPath['position'].append(succPosition)
                newCost = cost + succCost

                newNode = (succPosition, newCost, newPath, newTimeStep, succStillFood)

                # if there is still food to eat, the heuristic still exist
                # otherwise the heuristic will just be zero, as long as the pacman is not in any conflicted position then it's fine
                if succStillFood:
                    myPQ.push(newNode, newCost + heuristic(succPosition, foodPosition, walls))
                else:
                    myPQ.push(newNode, newCost)

def adaptedAstarGetSuccsssors(position, walls):
    # get successors given a position, used in adapted A* function
    successors = []
    directions = [Directions.NORTH, Directions.SOUTH, Directions.EAST, Directions.WEST, Directions.STOP]
    for direction in directions:
        x, y = position
        dx, dy = Actions.directionToVector(direction)
        next_x, next_y = int(x + dx), int(y + dy)
        if not walls[next_x][next_y]:
            succPosition = (next_x, next_y)
            successors.append((succPosition, direction, 1))
    return successors

class MAPFProblem(SearchProblem):
    """
    A search problem associated with finding a path that collects all
    food (dots) in a Pacman game.

    A search state in this problem is a tuple ( pacmanPositions, foodGrid ) where
      pacmanPositions:  a dictionary {pacman_name: (x,y)} specifying Pacmans' positions
      foodGrid:         a Grid (see game.py) of either pacman_name or False, specifying the target food of that pacman_name. For example, foodGrid[x][y] == 'A' means pacman A's target food is at (x, y). Each pacman have exactly one target food at start
    """

    def __init__(self, startingGameState):
        "Initial function"
        "*** WARNING: DO NOT CHANGE!!! ***"
        self.start = (startingGameState.getPacmanPosition(), startingGameState.getFood())
        self.walls = startingGameState.getWalls()

    def getStartState(self):
        "Get start state"
        "*** WARNING: DO NOT CHANGE!!! ***"
        return self.start

    def isGoalState(self, state):
        "Return if the state is the goal state"
        "*** YOUR CODE HERE for task2 ***"

        # the goal state is when all the food are eaten
        foodGrid = state[1]
        for row in foodGrid:
            for cell in row:
                if cell:
                    return False
        return True
        
        # comment the below line after you implement the function
        #util.raiseNotDefined()

    def getSuccessors(self, state):
        """
            Returns successor states, the actions they require, and a cost of 1.
            Input: search_state
            Output: a list of tuples (next_search_state, action_dict, 1)

            A search_state in this problem is a tuple consists of two dictionaries ( pacmanPositions, foodGrid ) where
              pacmanPositions:  a dictionary {pacman_name: (x,y)} specifying Pacmans' positions
              foodGrid:    a Grid (see game.py) of either pacman_name or False, specifying the target food of each pacman.

            An action_dict is {pacman_name: direction} specifying each pacman's move direction, where direction could be one of 5 possible directions in Directions (i.e. Direction.SOUTH, Direction.STOP etc)


        """
        "*** YOUR CODE HERE for task2 ***"

        # recursively getting all actions
        pacmanPositions, foodGrid = state
        walls = self.walls
        successors = generateActions(pacmanPositions, foodGrid, walls, {}, list(pacmanPositions.keys()))
        return successors
        # comment the below line after you implement the function
        #util.raiseNotDefined()

def checkConflicts(pathPositions, startPosition):
    """
    iterate through positions and check conflicts
    """
    max_path_length = max(len(details['position']) for details in pathPositions.values())

    for time_step in range(max_path_length):
        positions_this_step = {}

        for pacman, details in pathPositions.items():
            if time_step < len(details['position']):
                current_position = details['position'][time_step]
            else:
                current_position = details['position'][-1]

            # Check for vertex collision
            if current_position in positions_this_step:
                other_pacman = positions_this_step[current_position]
                return (((pacman), current_position, time_step), 
                        ((other_pacman), current_position, time_step))
            
            positions_this_step[current_position] = pacman

            # Check for swapping collision
            if time_step > 0:
                previous_position = details['position'][time_step - 1] if time_step - 1 < len(details['position']) else details['position'][-1]
                for other_pacman, other_details in pathPositions.items():
                    if other_pacman == pacman:
                        continue
                    other_previous_position = other_details['position'][time_step - 1] if time_step - 1 < len(other_details['position']) else other_details['position'][-1]
                    other_current_position = other_details['position'][time_step] if time_step < len(other_details['position']) else other_details['position'][-1]
                    if current_position == other_previous_position and previous_position == other_current_position:
                        return (((pacman), (current_position), time_step), 
                                ((other_pacman), (previous_position), time_step), 
                                ((pacman), (previous_position), time_step - 1), 
                                ((other_pacman), (current_position), time_step - 1))
                    
            # check for swapping collision at the first step
            if time_step == 0:
                previous_position = startPosition[pacman]
                for other_pacman, other_details in pathPositions.items():
                    if other_pacman == pacman:
                        continue
                    other_previous_position = startPosition[other_pacman]
                    other_current_position = pathPositions[other_pacman]['position'][time_step]
                    if current_position == other_previous_position and previous_position == other_current_position:
                        return (((pacman), (current_position), time_step), 
                                ((other_pacman), (previous_position), time_step))


    return None

def conflictBasedSearch(problem: MAPFProblem):
    """
        Conflict-based search algorithm.
        Input: MAPFProblem
        Output(IMPORTANT!!!): A dictionary stores the path for each pacman as a list {pacman_name: [action1, action2, ...]}.

    """
    "*** YOUR CODE HERE for task3 ***"
    
    timeConstraints = set()
    notPossibleTimeConstraints = []
    startState = problem.getStartState()
    foodGrid = startState[1]
    startPosition = startState[0]
    foodPositions = {}
    for i in range(foodGrid.width):
        for j in range(foodGrid.height):
            if foodGrid[i][j]:
                foodPositions[foodGrid[i][j]] = (i, j)
    myPQ = util.PriorityQueue()
    pacmen = list(startPosition.keys())
    pathPositions = defaultdict(dict)
    cost = 0

    # getting shortest path for each agent
    for pacman in pacmen:
        singlePathPositions = adaptedAstar(problem, manhattan_distance, timeConstraints, pacman, foodPositions[pacman])
        pathPositions[pacman]['path'] = singlePathPositions['path']
        pathPositions[pacman]['position'] = singlePathPositions['position']
        cost += len(singlePathPositions['path'])

    startNode = (pathPositions, timeConstraints)
    myPQ.push(startNode, cost)

    while not myPQ.isEmpty():
        node = myPQ.pop()
        currentPathPositions, currentTimeConstraints = node

        # check conflicts
        conflicts = checkConflicts(currentPathPositions, startPosition)
        if not conflicts:
            solutionPaths = defaultdict(list)
            solutionPositions = defaultdict(list)
            for pacman, data in currentPathPositions.items():
                solutionPaths[pacman] = data['path']
                solutionPositions[pacman] = data['position']

            return solutionPaths

        # find a new solution if there is a conflict
        for conflict in conflicts:
            conflictedPacman, conflictedPosition, conflictedTimeStep = conflict
            newPathPositions = {key: value for key, value in currentPathPositions.items() if key != conflictedPacman}
            newTimeConstraints = set(currentTimeConstraints)
            newTimeConstraints.add(conflict)
            newPathPositions[conflictedPacman] = defaultdict(list)
            if newTimeConstraints in notPossibleTimeConstraints:
                continue
            newSolution = adaptedAstar(problem, manhattan_distance, newTimeConstraints, conflictedPacman, foodPositions[conflictedPacman])
            if not newSolution:
                notPossibleTimeConstraints.append(newTimeConstraints)
                continue
            
            newPathPositions[conflictedPacman]['path'] = newSolution['path']
            newPathPositions[conflictedPacman]['position'] = newSolution['position']

            newCost = sum(len(newPathPositions[pacman]['path']) for pacman in newPathPositions)
            newNode = (newPathPositions, newTimeConstraints)
            myPQ.push(newNode, newCost)
        
    # comment the below line after you implement the function
    #util.raiseNotDefined()

def prim_mst(pacmanPosition, foodPositions, walls, distances):
    """
    This function generates the minimum spanning tree connecting all
    foods and the pacman position, the weight of edges is used as the heuristic in part 1
    """

    nodes = [pacmanPosition] + foodPositions
    
    if not foodPositions:
        return 0
    
    # Initialize the MST weight.
    total_weight = 0
    
    # Use a set to keep track of nodes not in MST yet.
    nodes_not_in_mst = set(nodes)
    
    # Priority queue for edges, format: (weight, start_node, end_node).
    edges = [(0, pacmanPosition, pacmanPosition)]
    
    while nodes_not_in_mst and edges:
        weight, _, end_node = heapq.heappop(edges)  # Get edge with smallest weight.
        
        if end_node in nodes_not_in_mst:
            nodes_not_in_mst.remove(end_node)  # Add this node to the MST.
            total_weight += weight  # Add this edge's weight to the total MST weight.
            
            # Add all edges from the current node to nodes_not_in_mst to the priority queue.
            for next_node in nodes_not_in_mst:
                if end_node != next_node:
                    edge_distance = distances.get((end_node, next_node)) or distances.get((next_node, end_node))          
                    if edge_distance is None:
                        edge_distance = bfsMazeDistance(end_node, next_node, walls)
                        distances[(end_node, next_node)] = edge_distance
                        
                    heapq.heappush(edges, (edge_distance, end_node, next_node))
    
    # return the weight of edges
    return total_weight

def manhattan_distance(point1, point2, walls):
    """
    Calculate the Manhattan distance between two points.
    :param point1: The first point, represented as a tuple of coordinates.
    :param point2: The second point, represented as a tuple of coordinates.
    :return: The Manhattan distance between the two points.
    """
    return sum(abs(coord1 - coord2) for coord1, coord2 in zip(point1, point2))

"###WARNING: Altering the following functions is STRICTLY PROHIBITED. Failure to comply may result in a grade of 0 for Assignment 1.###"
"###WARNING: Altering the following functions is STRICTLY PROHIBITED. Failure to comply may result in a grade of 0 for Assignment 1.###"
"###WARNING: Altering the following functions is STRICTLY PROHIBITED. Failure to comply may result in a grade of 0 for Assignment 1.###"


class FoodSearchProblem(SearchProblem):
    """
    A search problem associated with finding a path that collects all
    food (dots) in a Pacman game.

    A search state in this problem is a tuple ( pacmanPosition, foodGrid ) where
      pacmanPosition: a tuple (x,y) of integers specifying Pacman's position
      foodGrid:       a Grid (see game.py) of either True or False, specifying remaining food
    """

    def __init__(self, startingGameState):
        self.start = (startingGameState.getPacmanPosition(), startingGameState.getFood())
        self.walls = startingGameState.getWalls()
        self._expanded = 0  # DO NOT CHANGE
        self.heuristicInfo = {}  # A optional dictionary for the heuristic to store information

    def getStartState(self):
        return self.start

    def isGoalState(self, state):
        return state[1].count() == 0

    def getSuccessors(self, state):
        "Returns successor states, the actions they require, and a cost of 1."
        successors = []
        self._expanded += 1  # DO NOT CHANGE
        for direction in [Directions.NORTH, Directions.SOUTH, Directions.EAST, Directions.WEST, Directions.STOP]:
            x, y = state[0]
            dx, dy = Actions.directionToVector(direction)
            next_x, next_y = int(x + dx), int(y + dy)
            if not self.walls[next_x][next_y]:
                nextFood = state[1].copy()
                nextFood[next_x][next_y] = False
                successors.append((((next_x, next_y), nextFood), direction, 1))
        return successors

    def getCostOfActions(self, actions):
        """Returns the cost of a particular sequence of actions.  If those actions
        include an illegal move, return 999999"""
        x, y = self.getStartState()[0]
        cost = 0
        for action in actions:
            # figure out the next state and see whether it's legal
            dx, dy = Actions.directionToVector(action)
            x, y = int(x + dx), int(y + dy)
            if self.walls[x][y]:
                return 999999
            cost += 1
        return cost


class SingleFoodSearchProblem(FoodSearchProblem):
    """
    A special food search problem with only one food and can be generated by passing pacman position, food grid (only one True value in the grid) and wall grid
    """

    def __init__(self, pos, food, walls):
        self.start = (pos, food)
        self.walls = walls
        self._expanded = 0  # DO NOT CHANGE
        self.heuristicInfo = {}  # A optional dictionary for the heuristic to store information


def breadthFirstSearch(problem):
    """Search the shallowest nodes in the search tree first."""
    Q = util.Queue()
    startState = problem.getStartState()
    startNode = (startState, 0, [])
    Q.push(startNode)
    while not Q.isEmpty():
        node = Q.pop()
        state, cost, path = node
        if problem.isGoalState(state):
            return path
        for succ in problem.getSuccessors(state):
            succState, succAction, succCost = succ
            new_cost = cost + succCost
            newNode = (succState, new_cost, path + [succAction])
            Q.push(newNode)

    return None  # Goal not found


def aStarSearch(problem, heuristic=nullHeuristic):
    """Search the node that has the lowest combined cost and heuristic first."""
    myPQ = util.PriorityQueue()
    startState = problem.getStartState()
    startNode = (startState, 0, [])
    myPQ.push(startNode, heuristic(startState, problem))
    best_g = dict()
    while not myPQ.isEmpty():
        node = myPQ.pop()
        state, cost, path = node
        if (not state in best_g) or (cost < best_g[state]):
            best_g[state] = cost
            if problem.isGoalState(state):
                return path
            for succ in problem.getSuccessors(state):
                succState, succAction, succCost = succ
                new_cost = cost + succCost
                newNode = (succState, new_cost, path + [succAction])
                myPQ.push(newNode, heuristic(succState, problem) + new_cost)

    return None  # Goal not found


# Abbreviations
bfs = breadthFirstSearch
dfs = depthFirstSearch
astar = aStarSearch
ucs = uniformCostSearch
cbs = conflictBasedSearch
