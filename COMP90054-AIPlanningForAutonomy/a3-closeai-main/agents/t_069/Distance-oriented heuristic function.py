from template import Agent
from Splendor import splendor_utils
from Splendor import splendor_model
from copy import deepcopy
import copy
import random
import time
from .MRB import *
from .Policy import *
from .agent_util import *

class myAgent(Agent):
    def __init__(self,_id):
        super().__init__(_id)
        self.id = _id
        self.turn = 0
        self.color_list=['red','black','blue','green','white']
        self.rule = splendor_model.SplendorGameRule(2)
        self.p = SplendorPotentialFunction1(self.id)

    # Method to select an action based on the current game state.
    def SelectAction(self,actions,game_state:splendor_model.SplendorState):
        self.turn=self.turn+1
        return self.selecAction(actions,game_state)

    # Method to select an action based on the current game state.
    def selecAction(self,actions,game_state:splendor_model.SplendorState):
        s = time.time()
        max = 0
        act = None
        for i in actions:
            t = deepcopy(game_state)
            state = self.rule.generateSuccessor(t,i,self.id)
            x = self.p.get_potential(state)
            if x > max:
                max = x
                act = i
            if time.time()-s>0.9:
                break
        return act



class SplendorPotentialFunction1():
    def __init__(self,id):
        self.id = id
        self.weight = {0: 0.9853785093759472, 1: -0.7675253802002668, 2: -0.4850341242463197, 3: -0.07915404457531344, 4: 0.8036802322027493, 5: 0.19294131395789704, 6: 1.306354679802648, 7: 9.095009128093604, 8: 0, 9: 0, 10: 0, 11: 0, 12: 0, 13: 0, 14: 0, 15: 0, 16: 0, 17: 0, 18: 0, 19: 0}


    def get_potential(self,game_state:splendor_model.SplendorState):
        feature = pig_pig_pig(game_state,self.id)+ [game_state.agents[self.id].score/20]
        val = sum([self.weight[i]*feature[i] for i in range(len(feature))])
        available_cards,available_gems,available_nobels,agents,turn=unpack_u(game_state)
        this_agent = agents[self.id]
        other_agent = agents[(self.id+1)%2]
        #Indec tracking the progress of the game, bounded by ~15
        index = max(this_agent[0],other_agent[0]*3/4.0+this_agent[0]/4.0)
        #Importance of level3 card gems
        level2Importance = 0.2+0.06*index-0.0053*index*index
        #Importance of level2 card gems
        level3Importance = 0.1+0.003*index*index
        #Importance of nobles card gems
        nobleImportance = 0.1+0.0016*index*index


        #Parameter for adjustment
        score_ratio = 3.0*(index*index/100.0+1)
        individual_ratio = 3.0

        return 0-(1-level2Importance-level3Importance-nobleImportance)*feature[0]-\
               level2Importance*feature[1]-level3Importance*feature[2]-nobleImportance*feature[3]\
               +feature[4]+individual_ratio*feature[5]+individual_ratio*feature[6]+score_ratio*feature[7]

class LinearApprox():
    def __init__(self):
        self.weight = {i:0 for i in range(20)}
        self.lr=0.01

    def update(self, actual_value, state:splendor_model.SplendorState, idd):
        feature = pig_pig_pig(state, idd) + [state.agents[idd].score/20]
        print(feature)
        val = sum([self.weight[i]*feature[i] for i in range(len(feature))])
        print(val,actual_value)


        for i in range(len(feature)):
            self.weight[i] += 0.01*(actual_value-val)*feature[i]
        print(self.weight)



def pig_pig_pig(game_state:splendor_model.SplendorState,id):
    current_weight = [0,0,0,0,0,0]
    available_cards,available_gems,available_nobels,agents,turn=unpack_u(game_state)

    #get self-information and opponent information
    this_agent = agents[id]
    other_agent = agents[(id+1)%2]
    this_agent_hand = [this_agent[1][i] + this_agent[2][i] for i in range(5)]
    this_agent_hand.append(this_agent[2][5])
    value = calculate_deck(available_cards,available_nobels,this_agent[3])

    #Indec tracking the progress of the game, bounded by ~15
    index = max(this_agent[0],other_agent[0]*3/4.0+this_agent[0]/4.0,turn*15/29)

    #Importance of level3 card gems
    level2Importance = 0.2+0.06*index-0.0053*index*index
    #Importance of level2 card gems
    level3Importance = 0.1+0.003*index*index
    #Importance of nobles card gems
    nobleImportance = 0.1+0.0016*index*index

    #Parameter
    k=index/24.0+index*index/288.0

    for i in range(5):
        current_weight[i]+= value[0][i]*(1-level2Importance-level3Importance-nobleImportance)+ \
                            value[1][i]*level2Importance+value[2][i]*level3Importance+value[3][i]*nobleImportance
    current_weight[5] = max(current_weight)

    return calculate_card(available_cards,available_nobels,this_agent[3],this_agent[1],this_agent[2],current_weight,k,turn)

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
                            in color_list],translateColor1(agent.gems),[[card.colour,translateColor(card.cost),card.points] for card in agent.cards['yellow']])
              for agent in game_state.agents]
    turn = len(game_state.agents[0].agent_trace.action_reward)
    return available_cards,available_gems,available_nobels,agents,turn



#Calculate sum of the cost of each gem color at each level (and all nobles)
def calculate_deck(available_cards,available_nobels,card_in_hand):
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
    for card in card_in_hand:
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
    return [[i/(sum(color1)+0.01) for i in color1],[(i/sum(color2)+0.01) for i in color2],[i/(sum(color3)+0.01) for i in color3],[i/(sum(nobels)+0.01) for i in nobels]]


def calculate_card(available_cards,available_nobels,card_in_hand,per_hand,gem_hand,gem_val,P,turn):
    color_list=['red','black','blue','green','white']
    l1=[]
    l2=[]
    l3=[]
    ln=[]
    vl=[]
    vh=[]
    for card in available_cards:
        cost = sum(card[1])
        if cost >8 or card[2] >3:
            fix_cost=distance_to_card_de(per_hand,card)
            var_cost=distance_to_card_var(per_hand,gem_hand,card)
            value = P*card[2]+gem_val[color_list.index(card[0])]-(var_cost[0]-var_cost[0]/3.0)
            l3.append((fix_cost[0]-fix_cost[1]/3.0))
            vl.append(value)
        elif cost > 5 or card[2] > 1:
            fix_cost=distance_to_card_de(per_hand,card)
            var_cost=distance_to_card_var(per_hand,gem_hand,card)
            value = P*card[2]+gem_val[color_list.index(card[0])]-(var_cost[0]-var_cost[0]/3.0)
            l2.append(fix_cost[0]-fix_cost[1]/3.0)
            vl.append(value)
        else:
            fix_cost=distance_to_card_de(per_hand,card)
            var_cost=distance_to_card_var(per_hand,gem_hand,card)
            value = P*card[2]+gem_val[color_list.index(card[0])]-(var_cost[0]-var_cost[0]/3.0)
            l1.append(fix_cost[0]-fix_cost[1]/3.0)
            vl.append(value)
    for card in card_in_hand:
        cost = sum(card[1])
        if cost >8 or card[2] >3:
            fix_cost=distance_to_card_de(per_hand,card)
            var_cost=distance_to_card_var(per_hand,gem_hand,card)
            value = P*card[2]+gem_val[color_list.index(card[0])]-(var_cost[0]-var_cost[0]/3.0)
            l3.append((fix_cost[0]-fix_cost[1])/3.0)
            vh.append(value)
        elif cost > 5 or card[2] > 1:
            fix_cost=distance_to_card_de(per_hand,card)
            var_cost=distance_to_card_var(per_hand,gem_hand,card)
            value = P*card[2]+gem_val[color_list.index(card[0])]-(var_cost[0]-var_cost[0]/3.0)
            l2.append(fix_cost[0]-fix_cost[1]/3.0)
            vh.append(value)
        else:
            fix_cost=distance_to_card_de(per_hand,card)
            var_cost=distance_to_card_var(per_hand,gem_hand,card)
            value = P*card[2]+gem_val[color_list.index(card[0])]-(var_cost[0]-var_cost[0]/3.0)
            l1.append(fix_cost[0]-fix_cost[1]/3.0)
            vh.append(value)
    for nobel in available_nobels:
        color =translateColor(nobel[1])
        ln.append(sum([max(0,color[i]-per_hand[i]) for i in range(5)]))
    return [sum(l1) / (2.0*len(l1)+0.01), sum(l2) / (3.0*len(l2)+0.01), sum(l3) / (5.0*len(l3)+0.01), sum(ln) / (9.0*len(ln)+0.01), sum(vh)/(11*len(vh)+0.01)]+[i/11.0 for i in sorted(vl, reverse=True)[:2]]
    #return [vec[0],turn*vec[0]/35,turn*turn*vec[0]/1225,vec[1],turn*vec[1]/35,turn*turn*vec[1]/1225,vec[2],turn*vec[2]/35,turn*turn*vec[2]/1225,vec[3],turn*vec[3]/35,turn*turn*vec[3]/1225,vec[4],vec[5],vec[6],turn*vec[4]/35,turn*vec[5]/35,turn*vec[6]/35,turn/35]

def distance_to_card(hand,card,alpha=0.47):
    card_cost = card[1]
    left = []
    cost = sum(card[1])

    for i in range(len(card_cost)):
        left.append(max(0,card_cost[i]-hand[i]))
    left=sorted(left,reverse=True)
    left = [pow(alpha,i)*left[i] for i in range(5)]
    if cost >8 or card[2] >3:
        return [(14-(sum(left)-hand[5]))/14,3]
    elif cost > 5 or card[2] > 1:
        return [(14-(sum(left)-hand[5]))/8,2]
    else:
        return [(14-(sum(left)-hand[5]))/5,1]

def lowerBound(listNum):
    x = 0
    l=listNum
    k=1
    gem = 0
    while k > 0:
        l = sorted(l)
        k=min(l[len(listNum)-3:len(listNum)])
        x += k
        l[-1]-=k
        l[-2]-=k
        l[-3]-=k
    k=1
    while k > 0:
        l = sorted(l)
        k=min(l[len(listNum)-2:len(listNum)])
        x += k
        l[-1]-=k
        l[-2]-=k
        gem+=k
    for i in l:
        x+=(i-(i%2))/2
        if i%2 != 0:
            gem+=2
            x+=1
    return int(x),gem


def distance_to_card_de(hand_per,card):
    card_cost = card[1]
    left = []

    for i in range(len(card_cost)):
        left.append(max(0,card_cost[i]-hand_per[i]))

    return lowerBound(left)

def distance_to_card_var(hand_per,hand_gem,card):
    card_cost = card[1]
    left = []

    for i in range(len(card_cost)):
        left.append(max(0,card_cost[i]-hand_per[i]-hand_gem[i]))
    for i in range(hand_gem[5]):
        left = sorted(left)
        left[-1]=max(0,left[-1]-1)
    return lowerBound(left)



def card_cost(card,perm_gem):
    color_list=['red','black','blue','green','white']
    gem = card[0]
    result = []
    for i in range(5):
        result.append(max(0,gem[i]-perm_gem[i]))
    return result


def gameEnd(game_state:splendor_model.SplendorState,agent_index):
    for agent in game_state.agents:
        if agent.score >= 15:
            return True
    return False

class Simluator():
    def __init__(self,seed):
        random.seed(seed)

    def simulate(self,game_state:splendor_model.SplendorState,agent_index):
        current=agent_index
        current_state = copy.deepcopy(game_state)
        rule = splendor_model.SplendorGameRule(2)
        i=0
        states = [(copy.deepcopy(current_state),agent_index)]
        agent=[myAgent(0),myAgent(1)]
        turn = 0
        while not gameEnd(current_state,current) and turn <= 100:
            actions = rule.getLegalActions(current_state,current)
            action = agent[current].selecAction(actions,current_state)
            current_state = rule.generateSuccessor(current_state,action,current)
            current = (1+current)%2
            states.append((copy.deepcopy(current_state),current))
            turn +=1
        return [rule.calScore(current_state,0),rule.calScore(current_state,1),states]
