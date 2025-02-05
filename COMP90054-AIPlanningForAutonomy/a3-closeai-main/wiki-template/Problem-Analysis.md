# Problem Analysis
The project aims at building an AI agent to play against another agent in the Splendor game, where players are trying to be the first player with a score greater or equal to 15 with the fewest cards by collecting gems and buying affordable cards to earn the points. We can model the problem as a Markov Decision Process(MDP) with 2 agents and here is the state model.

## MDP State Model

### State space
A state consists of 2 parts: the board and the list of agents.

The board contains the deck(a 2-D list, each sublist represents a tier), the dealt(a 2-D list, each sublist represents a tier with a size of 4), the gems(a dictionary to record the number of gems for each colour) and the nobles(list of noble cards with a size of 3 in a 2-player game). 

For each agent, the state includes the id(0 or 1 in a 2-player game), the score, the gems(a dictionary to record the number of gems for each colour), the cards(a dictionary to record the cards of each colour, yellow cards are the reserved cards), the nobles that the agent has visited, the passed flag to indicate whether the agent has any available actions in his turn, the agent trace to trace all actions performed before with the associated rewards and the last action.

Given the large number of elements in a state, the state space is huge and it is very unlikely to return to the same state throughout a game.

### Initial state
The initial state of the game is random due to the random cards shuffling for the deck and nobles on the board. The dealt on the board is also different in every game since it is initialised by randomly selecting 4 cards from the deck for each tier.

### Goal states
The goal state of the model is where any agent achieves a score greater or equal to 15. The game will only find the winner(if any) and terminate after all agents have finished their turns.

### Actions
Each action consists of 5 elements:
- type: 5 types of actions: collect_diff, collect_same, reserve, buy_available and buy_reserve.
- collected_gems(if applicable): the gems to be collected
- returned_gems: if the agent is holding exceeding gems(every agent can only hold 10 gems of any colour in total) or buying a card to pay the cost, he has to return the gems to the board.
- card(if applicable): the card to buy or reserve (the agent can only earn the points of the card after purchase)
- noble: the noble that is eligible to visit, which is worth 3 points

In case of exceeding the gems count limit, the game will generate all possible combinations of return gems under the game rule. There may also be multiple nobles eligible to visit even though this is rare. Therefore, the action space is huge.

### Transition probabilities
The actions of type collect_diff and collect_same are deterministic because we will collect and return some gems, which are explicitly declared in the action. However, actions of type reserve, buy_available involve taking a card from the dealt on the board and the board has to randomly select a card from the deck to fill in the vacancy in the dealt. Thus, the successor state is random and the action is probabilistic. We can model the transition of these actions using a uniform distribution, assuming every card has equal chances to be selected and fill in the space. For actions of type buy_reserve, it is deterministic as well because it buys an agent's reserved cards without changing the cards in the dealt of the board.

### Action cost
We model the action cost of every action as 1. (a uniform-cost problem)

