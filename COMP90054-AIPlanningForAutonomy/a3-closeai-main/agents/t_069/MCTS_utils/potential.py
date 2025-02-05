from Splendor import splendor_model


def get_potential(game_state: splendor_model.SplendorState, acted_agent_id):
    # Initial weight for different gem color
    current_weight = [0, 0, 0, 0, 0, 0]
    available_cards, available_gems, available_nobels, agents = unpack(game_state)
    value = calculate_deck(available_cards, available_nobels)
    # get self-information and opponent information
    this_agent = agents[acted_agent_id]
    other_agent = agents[(acted_agent_id + 1) % 2]
    # Indec tracking the progress of the game, bounded by ~15
    index = max(this_agent[0], other_agent[0] / 2.0 + this_agent[0] / 2.0)

    # Importance of level3 card gems
    level2Importance = 0.2 + 0.06 * index - 0.0053 * index * index
    # Importance of level2 card gems
    level3Importance = 0.1 + 0.003 * index * index
    # Importance of nobles card gems
    nobleImportance = 0.1 + 0.0016 * index * index

    # Parameter for adjustment
    score_ratio = 1.7
    permant_gem_ratio = 4.2
    enemy_ratio = 0.2
    for i in range(5):
        current_weight[i] += (
            value[0][i] * (1 - level2Importance - level3Importance - nobleImportance)
            + value[1][i] * level2Importance
            + value[2][i] * level3Importance
            + value[3][i] * nobleImportance
        )
    current_weight[5] = max(current_weight)

    # Calculate score contribution from the current agent's resources and achievements
    this_agent_score = 0
    for i in range(5):
        this_agent_score += (
            current_weight[i] * this_agent[2][i]
            + permant_gem_ratio * (15.0 - index) * 0.0666 * current_weight[i] * this_agent[1][i]
        )
    this_agent_score += this_agent[0] * score_ratio * (index * index / 100.0 + 1)
    this_agent_score += this_agent[2][5] * current_weight[5]

    # Calculate score contribution from the opponent's resources and achievements
    other_agent_score = 0
    for i in range(5):
        other_agent_score += (
            current_weight[i] * other_agent[2][i]
            + permant_gem_ratio * (15.0 - index) * 0.0666 * current_weight[i] * other_agent[1][i]
        )
    other_agent_score += other_agent[0] * score_ratio * (index * index / 100.0 + 1)
    other_agent_score += other_agent[2][5] * current_weight[5]

    # return this_agent_score

    return this_agent_score - enemy_ratio * other_agent_score


# Function to unpack the game state into individual components.
def unpack(game_state):
    color_list = ['red', 'black', 'blue', 'green', 'white']
    # [(card color, card cost in color list [0,0,0,0,0], points of card)]
    # [('green', [0,0,0,4,2], 0)]
    available_cards = [
        [card.colour, translateColor(card.cost), card.points, card.code] for card in game_state.board.dealt_list()
    ]

    # {'black': 4, 'red': 4, 'yellow': 4, 'green': 4, 'blue': 4, 'white': 4}
    available_gems = game_state.board.gems

    # All has 3 points, [('4b4g', {'blue': 4, 'green': 4})]
    available_nobels = game_state.board.nobles

    # (score,number of permanent color in hand in the form [0,0,0,0,0], gems in the form (0,0,0,0,0,0))
    agents = [
        (agent.score, [len(agent.cards[i]) for i in color_list], translateColor1(agent.gems))
        for agent in game_state.agents
    ]
    return available_cards, available_gems, available_nobels, agents


# Calculate sum of the cost of each gem color at each level (and all nobles)
def calculate_deck(available_cards, available_nobels):
    color_list = ['red', 'black', 'blue', 'green', 'white']
    color1 = [0, 0, 0, 0, 0]
    color2 = [0, 0, 0, 0, 0]
    color3 = [0, 0, 0, 0, 0]
    nobels = [0, 0, 0, 0, 0]
    for card in available_cards:
        cost = sum(card[1])
        if cost > 8 or card[2] > 3:
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
    return [
        [i / 12.0 for i in color1],
        [i / 20.0 for i in color2],
        [i / 25.0 for i in color3],
        [i / 11.0 for i in nobels],
    ]


def translateColor(cost):
    colors = [0, 0, 0, 0, 0]
    color_list = ['red', 'black', 'blue', 'green', 'white']
    for i in range(len(color_list)):
        if color_list[i] in list(cost.keys()):
            colors[i] = cost[color_list[i]]
    return tuple(colors)


def translateColor1(cost):
    colors = [0, 0, 0, 0, 0, 0]
    color_list = ['red', 'black', 'blue', 'green', 'white', 'yellow']
    for i in range(len(color_list)):
        if color_list[i] in list(cost.keys()):
            colors[i] = cost[color_list[i]]
    return tuple(colors)
