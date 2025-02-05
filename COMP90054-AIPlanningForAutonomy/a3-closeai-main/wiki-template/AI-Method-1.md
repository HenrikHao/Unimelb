# AI Method 1 - Monte Carlo Tree Search (MCTS)


# Table of Contents
  * [Citation](#citation)
  * [Motivation](#motivation)
  * [Application](#application)
  * [Trade-offs](#trade-offs)     
     - [Advantages](#advantages)
     - [Disadvantages](#disadvantages)
  * [Challenges](#challenges)
  * [Future improvements](#future-improvements)

### Citation
The code of classes Node, MCTS, QTable and UpperConfidenceBounds was referenced from the subject’s online textbook, namely:
- Node, MCTS: https://gibberblot.github.io/rl-notes/single-agent/mcts.html
- QTable: https://gibberblot.github.io/rl-notes/single-agent/temporal-difference-learning.html
- UpperConfidenceBounds: https://gibberblot.github.io/rl-notes/single-agent/multi-armed-bandits.html

[Back to top](#table-of-contents)

### Motivation  
Considering the large scale of the model and the random initial state, we adopted MCTS with UCB1 as the multi-armed bandit algorithm to plan for the best next action for the agent in each turn. Initially, I tried to apply value iteration and Q-learning to extract a policy using the 15 seconds at the start. However, I realised that not only is the number of states and actions enormous but also the initial state of the game is random and we could not retrieve the game state inside the constructor of myAgent. It means even if we compute a policy for those states we encounter during the start-up computation, we are unlikely to utilise it because the initial states are different and so are the subsequent states. Thus, these 2 approaches were discarded.

[Back to top](#table-of-contents)

### Application  
We employed the MCTS algorithm from the subject's online textbook and enriched it in the following ways to better fit the game setting and improve its performance under the model.

- Actions Abstraction
Acknowledging the large original action space, we decided to abstract the actions by only considering one single possible combination of return gems if the action needs to return some gems, which can reduce a large number of actions needed to be considered for every state and hence, significantly cut down the branching factor of the MCT, especially for collect_diff and collect_same actions. The idea behind this was that the different combinations of return gems of action would not change the semantic meanings and effect for the performing agent a lot. By summarising similar actions, MCTS can focus on the set of available actions with enough differences among them and choose the best action in a relatively high-level manner. Particularly, with a smaller branching factor, MCTS and achieve a more reliable actions


- Heuristic
Another important factor affecting the performance of MCTS is the quality of the simulation. We developed a heuristic to choose the most promising action during the 3-step


- Reward Shaping
In order to guide the agent to favour buying low-point cards to receive more bonuses and race for potential nobles, additional rewards were added for buy action based on the number of . The values were set to be low to avoid affecting the original reward mechanism.

- Further Abstraction: 1-agent MCTS in the 2-agent Game
Notably, the MCTS implemented was a single-agent MCTS even though the game involved 2 agents. Abstracting the problem by ignoring the opponent enables us to focus on our own actions and explore and gather more information about them, leading to a more informed decision. However, to accommodate to this abstraction, we decided to set the discount factor to be 0.7, which was lower than the usual 0.9, to 

[Back to top](#table-of-contents)

### Trade-offs  
#### *Advantages*  
- Good Generalisability
- Model-Free

#### *Disadvantages*
- Slow to Respond in each Turn
Since MCTS is an online approach and has to make use of the entire 1-second allowance to simulate and choose the best action in every turn, it is slow to respond to the opponent compared to offline approaches. On the other hand, with this pure online strategy, the trade-off is 0 computation time before the game starts and the agent is ready right after the game loads.

- Unstable performance
The performance of MCTS would be impacted by the quality and number of simulations run during the time limit in each turn. Depending on the computer workload, the number of simulations always varies. In case the available computational power is restricted or reduced, we will arrive at an unreliable q-value approximation and the selected action will not be optimal even though its q-value is the highest among the choices.

[Back to top](#table-of-contents)

### Challenges
- Computational Overhead Reduction
In order to run as many simulations as possible within the strict time limit, we realised the importance of reducing computational overhead throughout the MCTS algorithm. Strategies include replacing deepcopy in the copy library with loads and dumps in the pickle library to enhance necessary object replication efficiency. Eliminating all unnecessary object replication can also further improve efficiency. For example, an execute_without_copy function was introduced in the MDP class to be used dedicatedly in the simulation step as opposed to the original execute function, which would copy the state before generating its successor. We also tried to apply the potential function from Yixiang in the heuristic of simulations. It was proved that the potential was able to provide useful insights and select strong actions for the agent in the preliminary submission, where it reached 9 wins out of 10 games. However, the inevitable expensive overhead of copying the state and generating the successor contributed to fewer simulations and hence, worse performance of MCTS. Therefore, we strived to come up with an effective and efficient heuristic to be used in the simulation in order to provide a reliable approximation of the q-values, which requires strong domain knowledge of the game. The implementation details of the chosen heuristic were described in the Application section.



[Back to top](#table-of-contents)

### Future improvements  

[Back to top](#table-of-contents)
