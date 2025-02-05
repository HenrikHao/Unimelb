from Splendor import splendor_utils
def unpack(game_state):
    color = ['red','black','blue','green','white']
    #(card color thing in string, card cost in dict{color string:number}, points of card)
    #[('green', {'red': 2, 'black': 2, 'blue': 1}, 0)]
    cards=[(card.colour,translateColor(card.cost),card.points,card.code) for card in game_state.board.dealt_list()]
    cards = sorted(cards,key=lambda x:x[-1])
    string = ""
    for i in cards:
        string+=i[3]

    #{'black': 4, 'red': 4, 'yellow': 4, 'green': 4, 'blue': 4, 'white': 4}
    available_gems=str(translateColor1(game_state.board.gems))
    string+=available_gems

    #All has 3 points, [('4b4g', {'blue': 4, 'green': 4})]
    available_nobels=((i[0],translateColor(i[1])) for i in game_state.board.nobles)
    available_nobels= sorted(available_nobels,key=lambda x:x[0])

    for i in available_nobels:
        string+=i[0]
    #(id,score,dict of cards in hand {color:[cards]}), gems, nobels)
    agent0 = [str(game_state.agents[0].score),[len(game_state.agents[0].cards[i]) for i
                                              in color],translateColor(game_state.agents[0].gems),sorted([j.code for j in game_state.agents[0].cards['yellow']])]
    agent1= [str(game_state.agents[1].score),[len(game_state.agents[1].cards[i]) for i
                                              in color],translateColor(game_state.agents[1].gems),sorted([j.code for j in game_state.agents[1].cards['yellow']])]

    string+=str(agent0[0])+str(agent0[1])+str(agent0[2])+str(agent0[3])
    string+=str(agent1[0])+str(agent1[1])+str(agent1[2])+str(agent1[3])
    return string


def unpack_ml(game_state,agent_index):
    color = ['red','black','blue','green','white']
    #(card color thing in string, card cost in dict{color string:number}, points of card)
    #[('green', {'red': 2, 'black': 2, 'blue': 1}, 0)]
    cards=[(card.colour,translateColor(card.cost),card.points,card.code) for card in game_state.board.dealt_list()]
    cards = sorted(cards,key=lambda x:x[-1])
    string = one_hot_card([card[3] for card in cards])


    #{'black': 4, 'red': 4, 'yellow': 4, 'green': 4, 'blue': 4, 'white': 4}
    available_gems=translateColor1(game_state.board.gems)
    string+=list(available_gems)

    #All has 3 points, [('4b4g', {'blue': 4, 'green': 4})]
    available_nobels=((i[0],translateColor(i[1])) for i in game_state.board.nobles)
    available_nobels= [n[0] for n in sorted(available_nobels,key=lambda x:x[0])]

    string+=one_hot_nobels(available_nobels)
    #(id,score,dict of cards in hand {color:[cards]}), gems, nobels)
    agent0 = [game_state.agents[agent_index].score,[len(game_state.agents[agent_index].cards[i]) for i
                                               in color],translateColor(game_state.agents[agent_index].gems),[sorted([j.code for j in game_state.agents[agent_index].cards['yellow']])]]
    agent1= [game_state.agents[(1+agent_index)%2].score,[len(game_state.agents[(1+agent_index)%2].cards[i]) for i
                                              in color],translateColor(game_state.agents[(1+agent_index)%2].gems),[sorted([j.code for j in game_state.agents[(1+agent_index)%2].cards['yellow']])]]

    string+=[agent0[0]]+agent0[1]+list(agent0[2])+one_hot_card(agent0[3])
    string+=[agent1[0]]+agent1[1]+list(agent1[2])+one_hot_card(agent1[3])
    return string


def translateAction(action):
    if action['type'] == 'collect_diff' or action['type'] == 'collect_same':
        if action['noble'] is None:
            return action['type']+str(translateColor1(action['collected_gems']))+str(translateColor1(action['returned_gems']))
        return action['type']+str(translateColor1(action['collected_gems']))+str(translateColor1(action['returned_gems']))+action['noble'][0]
    elif action['type'] == 'reserve':
        if action['noble'] is None:
            return action['type']+action['card'].code+str(translateColor1(action['returned_gems']))
        return action['type']+action['card'].code+str(translateColor1(action['returned_gems']))+action['noble'][0]
    else:
        if action['noble'] is None:
            return action['type']+action['card'].code+str(translateColor1(action['returned_gems']))
        return action['type']+action['card'].code+str(translateColor1(action['returned_gems']))+action['noble'][0]

def translateColor(cost):
    colors = [0,0,0,0,0]
    color_list=['red','black','blue','green','white']
    for i in range(len(color_list)):
        if color_list[i] in list(cost.keys()):
            colors[i] = cost[color_list[i]]
    return tuple(colors)

def translateColor1(cost):
    colors = [0,0,0,0,0,0]
    color_list=['red','black','blue','green','white','yellow']
    for i in range(len(color_list)):
        if color_list[i] in list(cost.keys()):
            colors[i] = cost[color_list[i]]
    return tuple(colors)

def one_hot_card(cards):
    names = list(splendor_utils.CARDS.keys())
    result = []
    for i in names:
        if i in cards:
            result.append(1)
        else:
            result.append(0)
    return result

def one_hot_nobels(nobels):
    names = splendor_utils.NOBLES
    result = []
    for i in names:
        if i in nobels:
            result.append(1)
        else:
            result.append(0)
    return result
