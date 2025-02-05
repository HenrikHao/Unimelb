from template import Agent
from Splendor import splendor_utils
from Splendor import splendor_model
from copy import deepcopy
import copy
import random
import time
from .alpha_beta_pruning_utils.MDP import *
from .alpha_beta_pruning_utils.MRB import *
from .alpha_beta_pruning_utils.Policy import *
from .alpha_beta_pruning_utils.Learners import *
from .alpha_beta_pruning_utils.agent_util import *

class myAgent(Agent):
    def __init__(self,_id):
        super().__init__(_id)
        self.id = _id
        self.turn = 0
        self.color_list=['red','black','blue','green','white']
        self.rule = splendor_model.SplendorGameRule(2)
        self.p = SplendorPotentialFunction(self.id)

    # Method to select an action based on the current game state.
    def SelectAction(self,actions,game_state:splendor_model.SplendorState):
        self.turn=self.turn+1
        return self.selecAction(actions,game_state)

    # Method to select an action based on the current game state.
    def selecAction(self,actions,game_state:splendor_model.SplendorState):
        start = time.time()
        max = float('-inf')
        alpha = float('-inf')
        beta = float('inf')
        act = None
        abp = AlphaBetapruning(self.id)
        test = 0
        for i in actions:
            test +=1
            t = deepcopy(game_state)
            x = self.rule.generateSuccessor(t,i,self.id)
            value = abp.alpha_beta(x, alpha, beta, False,1)
            if value > max:
                max = value
                act = i
            if time.time()-start > 0.90:
                break
        return act

class AlphaBetapruning():
    def __init__(self,id):
        self.id = id
        self.rule =  splendor_model.SplendorGameRule(2)
        self.p = SplendorPotentialFunction(self.id)

    def alpha_beta(self, state, alpha, beta, maximizing_player,depth):
        if depth == 1 or gameEnd(state):
            return self.p.get_potential(state)

        if maximizing_player:
            max_val = float('-inf')
            actions = self.rule.getLegalActions(state, self.id)
            for action in actions:
                state_copy = copy.deepcopy(state)
                successor_state = self.rule.generateSuccessor(state_copy, action, self.id)
                max_val = max(max_val, self.alpha_beta(successor_state, alpha, beta, False,depth-1))
                alpha = max(alpha, max_val)
                if beta <= alpha:
                    break  # Beta cut-off
            return max_val
        else:
            min_val = float('inf')
            opponent_id = (self.id + 1) % 2
            actions = self.rule.getLegalActions(state, opponent_id)
            for action in actions:
                state_copy = copy.deepcopy(state)
                successor_state = self.rule.generateSuccessor(state_copy, action, opponent_id)
                min_val = min(min_val, self.alpha_beta(successor_state, alpha, beta, True,depth-1))
                beta = min(beta, min_val)
                if beta <= alpha:
                    break  # Alpha cut-off
            return min_val

def gameEnd(game_state:splendor_model.SplendorState):
    for agent in game_state.agents:
        if agent.score >= 15:
            return True
    return False

class SplendorPotentialFunction():
    def __init__(self,id):
        self.id = id

    def get_potential(self,game_state:splendor_model.SplendorState):
        #Initial weight for different gem color
        current_weight = [0,0,0,0,0,0]
        available_cards,available_gems,available_nobels,agents,turn=unpack_u(game_state)
        value = calculate_deck(available_cards,available_nobels)
        #get self-information and opponent information
        this_agent = agents[self.id]
        other_agent = agents[(self.id+1)%2]
        this_agent_hand = [this_agent[1][i] + this_agent[2][i] for i in range(5)]
        this_agent_hand.append(this_agent[2][5])

        #Indec tracking the progress of the game, bounded by ~15
        index = max(this_agent[0],other_agent[0]*3/4.0+this_agent[0]/4.0)

        #Importance of level3 card gems
        level2Importance = 0.2+0.06*index-0.0053*index*index
        #Importance of level2 card gems
        level3Importance = 0.1+0.003*index*index
        #Importance of nobles card gems
        nobleImportance = 0.1+0.0016*index*index


        #Parameter for adjustment
        score_ratio = 2.0
        permant_gem_ratio = 4.0
        enemy_ratio = 0.2

        for i in range(5):
            current_weight[i]+= value[0][i]*(1-level2Importance-level3Importance-nobleImportance)+ \
                                value[1][i]*level2Importance+value[2][i]*level3Importance+value[3][i]*nobleImportance
        current_weight[5] = max(current_weight)
        value2 = calculate_deck_crd(available_cards,available_nobels,this_agent_hand,turn,current_weight,0.2+pow(turn,3)/1000,0.9)
        #---------------------------------------------------------------------------------------
        weight1 = [0,0,0,0,0,0]
        for i in range(5):
            weight1[i]+= value2[0][i]*(1-level2Importance-level3Importance-nobleImportance)+ \
                         value2[1][i]*level2Importance+value2[2][i]*level3Importance+value2[3][i]*nobleImportance
        weight1[5] = max(weight1)

        # Calculate score contribution from the current agent's resources and achievements
        this_agent_score = 0
        for i in range(5):
            this_agent_score += weight1[i]*this_agent[2][i]+permant_gem_ratio*(15.0-index) \
                                *0.0666*weight1[i]*this_agent[1][i]
        this_agent_score+= this_agent[0]*score_ratio*(index*index/100.0+1)
        this_agent_score+=this_agent[2][5]*weight1[5]


        return this_agent_score

# Function to unpack the game state into individual components.
def unpack_u(game_state):
    color_list=['red','black','blue','green','white']
    #[(card color, card cost in color list [0,0,0,0,0], points of card)]
    #[('green', [0,0,0,4,2], 0)]
    available_cards=[[card.colour,translateColor(card.cost),card.points,card.code]
                     for card in game_state.board.dealt_list()]

    #{'black': 4, 'red': 4, 'yellow': 4, 'green': 4, 'blue': 4, 'white': 4}
    available_gems=game_state.board.gems

    #All has 3 points, [('4b4g', {'blue': 4, 'green': 4})]
    available_nobels=game_state.board.nobles

    #(score,number of permanent color in hand in the form [0,0,0,0,0], gems in the form (0,0,0,0,0,0))
    agents = [(agent.score,[len(agent.cards[i]) for i
                            in color_list],translateColor1(agent.gems))
              for agent in game_state.agents]
    turn = len(game_state.agents[0].agent_trace.action_reward)
    return available_cards,available_gems,available_nobels,agents,turn



#Calculate sum of the cost of each gem color at each level (and all nobles)
def calculate_deck(available_cards,available_nobels):
    color_list=['red','black','blue','green','white']
    color1=[0,0,0,0,0]
    color2=[0,0,0,0,0]
    color3=[0,0,0,0,0]
    nobels=[0,0,0,0,0]
    for card in available_cards:
        cost = sum(card[1])
        if cost >8 or card[2] >3:
            for i in range(5):
                color3[i] += card[1][i]
        elif cost > 5 or card[2] > 1:
            for i in range(5):
                color2[i] += card[1][i]
        else:
            for i in range(5):
                color1[i] += card[1][i]
    for nobel in available_nobels:
        for i in range(5):
            if color_list[i] in nobel[1].keys():
                nobels[i] += nobel[1][color_list[i]]
    return [[i/sum(color1) for i in color1],[i/sum(color2) for i in color2],[i/sum(color3) for i in color3],[i/sum(nobels) for i in nobels]]


def distance_to_card(hand,card,alpha=0.47):
    card_cost = card[1]
    left = []
    cost = sum(card[1])

    for i in range(len(card_cost)):
        left.append(max(0,card_cost[i]-hand[i]))
    left=sorted(left,reverse=True)
    left = [pow(alpha,i)*left[i] for i in range(5)]
    if cost >8 or card[2] >3:
        return [(sum(left)-hand[5])/14,3]
    elif cost > 5 or card[2] > 1:
        return [(sum(left)-hand[5])/8,2]
    else:
        return [(sum(left)-hand[5])/5,1]

def card_value(card,current_weight,score_ratio):
    color_list=['red','black','blue','green','white']
    gem = card[0]
    score = card[2]
    for i in range(len(color_list)):
        if color_list[i] == gem:
            return (current_weight[i]+score_ratio*score)/(1+score_ratio*5)

def calculate_deck_crd(available_cards,available_nobels,hand,turn,current_weight,score_ratio,beta):
    color_list=['red','black','blue','green','white']
    color1=[0,0,0,0,0]
    color2=[0,0,0,0,0]
    color3=[0,0,0,0,0]
    nobels=[0,0,0,0,0]
    for card in available_cards:
        closeness = distance_to_card(hand,card)
        value = card_value(card,current_weight,score_ratio)
        weight = 1/(1+math.exp(16*closeness[0]-4.5))
        utility = beta*value+(1-beta)*weight
        if closeness[1] == 3:
            for i in range(5):
                color3[i] += card[1][i]*utility
        elif closeness[1] == 2:
            for i in range(5):
                color2[i] += card[1][i]*utility
        else:
            for i in range(5):
                color1[i] += card[1][i]*utility
    for nobel in available_nobels:
        for i in range(5):
            if color_list[i] in nobel[1].keys():
                nobels[i] += nobel[1][color_list[i]]
    return [[i/(sum(color1)+0.01) for i in color1],[i/(sum(color2)+0.01) for i in color2],[i/(sum(color3)+0.01) for i in color3],[i/(sum(nobels)+0.01) for i in nobels]]

def simulate(game_state:splendor_model.SplendorState,agent_index):
    current=agent_index
    current_state = game_state
    rule = splendor_model.SplendorGameRule(2)
    i=0
    states = [copy.deepcopy(current_state)]
    agent=[myAgent(0),myAgent(1)]
    turn = 0
    while not gameEnd(current_state) and turn <= 100:
        actions = rule.getLegalActions(current_state,current)
        action = agent[i].SelectAction(actions,current_state)
        current_state = rule.generateSuccessor(current_state,action,current)
        states.append(copy.deepcopy(current_state))
        current = (1+current)%2
        turn +=1
    return [rule.calScore(current_state,0),rule.calScore(current_state,1),states]

def lowerBound(listNum):
    x = 0
    l=listNum
    k=1
    while k > 0:
        l = sorted(l)
        k=min(l[len(listNum)-3:len(listNum)])
        x += k
        l[-1]-=k
        l[-2]-=k
        l[-3]-=k
    return x,sum(l)

class Splendor(MDP):
    def __init__(self,id):
        self.id = id
        self.episode_reward = []
        self.discount_factor=self.get_discount_factor()
        self.rule = splendor_model.SplendorGameRule(2)

    def get_actions(self, state):
        return self.rule.getLegalActions(state,self.id)

    def get_transitions(self, state, action):
        statec = copy.deepcopy(state)
        state1 = self.rule.generateSuccessor(statec,action,self.id)
        actions = self.rule.getLegalActions(state1,(1+self.id)%2)
        action1 = random.choice(actions)   #Replace
        return self.rule.generateSuccessor(state1,action1,(1+self.id)%2)

    def get_reward(self, state, action, next_state):
        reward = 0.0
        if self.is_terminal(state):
            reward=self.rule.calScore(state,self.id)
        self.episode_reward += [reward* (self.discount_factor ** len(self.episode_reward))]
        return reward

    def get_discount_factor(self):
        return 0.9

    def is_terminal(self, state):
        for agent in state.agents:
            if agent.score >= 15:
                return True
        return False

    def get_initial_state(self):
        return self.rule.initialGameState()

    def execute(self,state, action):
        next_state = self.get_transitions(state,action)
        return next_state,self.get_reward(state,action,next_state)

if False:
    rule = splendor_model.SplendorGameRule(2)
    simulate(rule.initialGameState(),0)