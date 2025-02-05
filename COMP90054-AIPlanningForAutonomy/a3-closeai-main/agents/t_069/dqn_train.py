import random
import torch
import torch.nn as nn
import torch.optim as optim
from collections import deque
from copy import deepcopy
from Splendor.splendor_model import SplendorGameRule
from template import Agent
import pickle
import itertools
from Splendor.splendor_utils import COLOURS, CARDS, NOBLES
# Constants
DISCOUNT_FACTOR = 0.8
WEIGHT_FILE_PATH = "agents/t_069/dqn_weights"
LEARNING_RATE = 0.001
EPSILON = 0.15
EPSILON_DECAY_RATE = 0.995
LEARNING_DECAY_RATE = 0.995
MIN_EPSILON = 0.01
MIN_LEARNING_RATE = 0.01
BATCH_SIZE = 32
MEMORY_SIZE = 2000
TARGET_UPDATE = 10  # How often to update the target network
SAVE_INTERVAL = 50  # How often to save the model weights
UPDATE_FREQUENCY = 10
ACTION_DICT = {'collect_same': 0, 'collect_diff': 1, 'reserve': 2, 'buy_available': 3, 'buy_reserve': 4, "pass": 5}
class QNetwork(nn.Module):
    def __init__(self, input_size, output_size):
        super(QNetwork, self).__init__()
        self.fc1 = nn.Linear(input_size, 256)
        self.dropout1 = nn.Dropout(0.2)
        self.fc2 = nn.Linear(256, 256)  # Keep consistent size through the network
        self.dropout2 = nn.Dropout(0.2)
        self.fc3 = nn.Linear(256, output_size)

    def forward(self, x):
        x = torch.relu(self.fc1(x))
        x = self.dropout1(x)
        x = torch.relu(self.fc2(x))
        x = self.dropout2(x)
        x = self.fc3(x)
        return x

class myAgent(Agent):
    def __init__(self, _id):
        super().__init__(_id)
        self.game_rule = SplendorGameRule(2)
        self.epsilon = EPSILON
        self.learning_rate = LEARNING_RATE
        self.memory = deque(maxlen=MEMORY_SIZE)
        self.device = torch.device("cuda" if torch.cuda.is_available() else "cpu")
        # Define the Q-Network and Target Network
        self.state_size = 242 # Number of features in the state + action
        self.output_size = 1
        self.q_network = QNetwork(self.state_size, self.output_size).to(self.device)
        self.target_network = QNetwork(self.state_size, self.output_size).to(self.device)
        self.optimizer = optim.Adam(self.q_network.parameters(), lr=self.learning_rate, weight_decay=1e-5)
        self.turn_counter = 0
        self.save_counter = 0
        self.update_count = 0

        # Load weights if they exist
        self.load_weights(self.id)

    def load_weights(self, id):
        try:
            state_dict = torch.load(WEIGHT_FILE_PATH + f'weights-{id}.pth')
            self.q_network.load_state_dict(state_dict)
            self.target_network.load_state_dict(state_dict)
            print("Weights loaded successfully.")
        except FileNotFoundError:
            print("Weight file not found, starting with random weights.")
        except Exception as e:
            print(f"Error loading weights: {e}")

    def save_weights(self):
        try:
            torch.save(self.target_network.state_dict(), WEIGHT_FILE_PATH + f"weights-256-{self.id}-{self.save_counter}.pth")
            print("Target network weights saved successfully.")
        except Exception as e:
            print(f"Error saving weights: {e}")

    def update_target_network(self):
        self.target_network.load_state_dict(self.q_network.state_dict())

    def SelectAction(self, actions, game_state):
        state = self.extract_state_features(game_state)
        q_values = []
        if random.uniform(0, 1) < self.epsilon:
            action= random.choice(actions) 
        else:
            with torch.no_grad():
                for action in actions:
                    encoded_action = self.encoding_action(action)
                    state_action = state + encoded_action
                    state_action_tensor = torch.tensor(state_action, dtype=torch.float32).to(self.device)
                    q = self.q_network(state_action_tensor).cpu().numpy()
                    q_values.append(q)
                max_q = max(q_values)
                max_q_index = q_values.index(max_q)
                action = actions[max_q_index]

        self.epsilon = max(MIN_EPSILON, self.epsilon * EPSILON_DECAY_RATE)
        #print(action)
        next_state = self.game_rule.generateSuccessor(deepcopy(game_state), action, self.id)
        reward = self.reward_shaping(game_state, action, self.id) + self.get_reward(game_state, action, next_state, self.id)
        #reward = self.p.get_potential(next_state) - self.p.get_potential(game_state)
        self.memory.append((state, action, reward, next_state, self.isTerminal(next_state)))

        if len(self.memory) > BATCH_SIZE:
            print("replaying")
            self.replay(BATCH_SIZE)
            self.memory.clear()

        if len(self.memory) == 0:  # Implies a replay just happened
            self.update_count += 1
            self.save_counter += 1
            if self.update_count >= TARGET_UPDATE:
                print("updating target work")
                self.update_target_network()
                self.update_count = 0  # Reset the counter
            if self.save_counter % SAVE_INTERVAL == 0:
                print("saving weights")
                self.save_weights()

        return action

    def replay(self, batch_size):
        minibatch = random.sample(self.memory, batch_size)
        for state, action, reward, next_state, done in minibatch:
            # Prepare current state-action pair
            encoded_action = self.encoding_action(action)
            current_state_action = state + encoded_action
            current_state_action_tensor = torch.tensor(current_state_action, dtype=torch.float32).to(self.device)

            # Prepare next state-action pair
            opponent_action = self.select_best_action(next_state, 1 - self.id) 
            opponent_state = self.game_rule.generateSuccessor(next_state, opponent_action, 1 - self.id)
            next_action = self.select_best_action(opponent_state, self.id)
            encoded_next_action = self.encoding_action(next_action)
            next_state_action = self.extract_state_features(opponent_state) + encoded_next_action
            next_state_action_tensor = torch.tensor(next_state_action, dtype=torch.float32).to(self.device)

            # Compute Q-values
            current_q = self.q_network(current_state_action_tensor.unsqueeze(0))
            next_q = self.target_network(next_state_action_tensor.unsqueeze(0)).detach()  # Using target network to stabilize training

            if done:
                target_q = torch.tensor([reward], device=self.device)
            else:
                target_q = reward + DISCOUNT_FACTOR * next_q
                target_q = torch.tensor([target_q], device=self.device)  # Ensuring it's a tensor


            # Compute loss
            loss = nn.SmoothL1Loss()(current_q, target_q.unsqueeze(0))

            # Backpropagation
            self.optimizer.zero_grad()
            loss.backward()
            self.optimizer.step()

    def select_best_action(self, state, id):
        actions = self.get_abstracted_actions(state, id)
        best_action = None
        max_p = -10000
        for action in actions:
            tmp_state = deepcopy(state)
            next_state = self.game_rule.generateSuccessor(tmp_state, action, id)
            next_p = get_potential(next_state, id)
            if next_p > max_p:
                max_p = next_p
                best_action = action

        return best_action


    def extract_state_features(self, game_state):
        our_id = self.id
        gem_types = ['green', 'black', 'red', 'yellow', 'blue', 'white']
        board_state = game_state.board
        our_state = game_state.agents[our_id]
        opponent_state = game_state.agents[1-our_id]
        card_types = ['green', 'black', 'red', 'blue', 'white']
        score = our_state.score
        opponent_score = opponent_state.score
        
        feature = []

        # adding score into feature
        feature.append(score / 15)
        feature.append(opponent_score / 15)
        feature.append(score - opponent_score)

        # One-hot encoding for nobles on the board
        noble_indices = [NOBLES.index(noble) for noble in board_state.nobles]
        noble_one_hot = [0] * len(NOBLES)
        for index in noble_indices:
            noble_one_hot[index] = 1
        feature.extend(noble_one_hot)

        # our gems and features
        feature.append(sum(our_state.gems.values()) / 10)  # total gems
        feature.append(len(our_state.nobles) / 3)  # total nobles

        feature.append(sum(opponent_state.gems.values()) / 10)  # total gems
        feature.append(len(opponent_state.nobles) / 3)  # total nobles

        all_cards = list(CARDS.keys())
        card_one_hot = [0] * len(CARDS)
        for cards in board_state.dealt:
            for card in cards:
                if card:
                    card_one_hot[all_cards.index(card.code)] = 1

        if len(our_state.cards['yellow']):
            for card in our_state.cards['yellow']:
                card_one_hot[all_cards.index(card.code)] = 2
        if len(opponent_state.cards['yellow']):
            for card in opponent_state.cards['yellow']:
                card_one_hot[all_cards.index(card.code)] = 3
        feature.extend(card_one_hot)
        # adding number of cards owned of each color
        feature.extend([len(our_state.cards[color]) for color in card_types])

        feature.extend([len(opponent_state.cards[color]) for color in card_types])

        # adding number of gems owned of each color
        for color in gem_types:
            feature.append(our_state.gems[color] / 4)  # Normalize by max_gems

        for color in gem_types:
            feature.append(opponent_state.gems[color] / 4)  # Normalize by max_gems

        return feature
    
    def isTerminal(self, state):
        for agent in state.agents:
            if agent.score >= 15:
                return True
        return False
    
    def encoding_action(self, action):
        encoded_action = []

        
        all_cards = list(CARDS.keys())
        gem_types = ['green', 'black', 'red', 'yellow', 'blue', 'white']

        card_one_hot = [0] * len(CARDS)
        collected_gems = [0] * len(gem_types)
        returned_gems = [0] * len(gem_types)
        noble_one_hot = [0] * len(NOBLES)

        action_type = ACTION_DICT[action['type']]
        encoded_action.append(action_type)

        if 'card' in action:
            card = action['card']
            card_one_hot[all_cards.index(card.code)] = 1
        encoded_action.extend(card_one_hot)
        
        if 'collected_gems' in action:
            gems = action['collected_gems']
            for key, value in gems.items():
                collected_gems[gem_types.index(key)] += value
        encoded_action.extend(collected_gems)

        if 'returned_gems' in action:
            gems = action['returned_gems']
            for key, value in gems.items():
                returned_gems[gem_types.index(key)] += value
        encoded_action.extend(returned_gems)

        if 'noble' in action:
            if action['noble']:
                noble_indices = [NOBLES.index(action['noble'])]
                for index in noble_indices:
                    noble_one_hot[index] = 1
        encoded_action.extend(noble_one_hot)

        return encoded_action
    

    def get_abstracted_actions(self, game_state, agent_id):
        actions = []
        agent, board = game_state.agents[agent_id], game_state.board

        potential_nobles = []
        for noble in board.nobles:
            if self.game_rule.noble_visit(agent, noble):
                potential_nobles.append(noble)
        if len(potential_nobles) == 0:
            potential_nobles = [None]

        # Generate actions (collect up to 3 different gems). Work out all legal combinations. Theoretical max is 10.
        available_colours = [colour for colour, number in board.gems.items() if colour != 'yellow' and number > 0]
        num_holding_gem = sum(agent.gems.values())
        if num_holding_gem <= 7:
            min_comb_len = min(3, len(available_colours))

        elif num_holding_gem == 8:
            min_comb_len = min(2, len(available_colours))
        else:
            min_comb_len = min(1, len(available_colours))

        for combo_length in range(min_comb_len, min(len(available_colours), 3) + 1):
            for combo in itertools.combinations(available_colours, combo_length):
                collected_gems = {colour: 1 for colour in combo}
                # make sure there is no action that collect empty gem
                if not collected_gems == {}:
                    return_combos = self.generate_return_combos_for_abstracted_actions(agent.gems, collected_gems)
                    for returned_gems in return_combos:
                        for noble in potential_nobles:
                            actions.append(
                                {
                                    'type': 'collect_diff',
                                    'collected_gems': collected_gems,
                                    'returned_gems': returned_gems,
                                    'noble': noble,
                                }
                            )

        # Generate actions (collect 2 identical gems). Theoretical max is 5.
        available_colours = [colour for colour, number in board.gems.items() if colour != 'yellow' and number >= 4]
        for colour in available_colours:
            collected_gems = {colour: 2}

            # Like before, find combos to return, if any. Since the max to be returned is now 2, theoretical max
            # combinations will be 21, and max actions generated here will be 105.
            return_combos = self.generate_return_combos_for_abstracted_actions(agent.gems, collected_gems)
            for returned_gems in return_combos:
                for noble in potential_nobles:
                    actions.append(
                        {
                            'type': 'collect_same',
                            'collected_gems': collected_gems,
                            'returned_gems': returned_gems,
                            'noble': noble,
                        }
                    )

        # if reserve cards num < 2 only, we can reserve cards
        if len(agent.cards['yellow']) < 2 or (len(agent.cards['yellow']) < 3 and board.gems['yellow'] > 0):
            collected_gems = {'yellow': 1} if board.gems['yellow'] > 0 else {}
            return_combos = self.generate_return_combos_for_abstracted_actions(agent.gems, collected_gems)
            for returned_gems in return_combos:
                for card in board.dealt_list():
                    if card:
                        for noble in potential_nobles:
                            actions.append(
                                {
                                    'type': 'reserve',
                                    'card': card,
                                    'collected_gems': collected_gems,
                                    'returned_gems': returned_gems,
                                    'noble': noble,
                                }
                            )

        for card in board.dealt_list() + agent.cards['yellow']:
            if not card or len(agent.cards[card.colour]) == 7:
                continue
            returned_gems = self.game_rule.resources_sufficient(agent, card.cost)  # Check if this card is affordable.
            if (
                type(returned_gems) == dict
            ):  # If a dict was returned, this means the agent possesses sufficient resources.
                # Check to see if the acquisition of a new card has meant new nobles becoming candidates to visit.
                new_nobles = []
                for noble in board.nobles:
                    agent_post_action = pickle.loads(pickle.dumps(agent, -1))
                    # Give the card featured in this action to a copy of the agent.
                    agent_post_action.cards[card.colour].append(card)
                    # Use this copied agent to check whether this noble can visit.
                    if self.game_rule.noble_visit(agent_post_action, noble):
                        new_nobles.append(noble)  # If so, add noble to the new list.
                if not new_nobles:
                    new_nobles = [None]
                for noble in new_nobles:
                    actions.append(
                        {
                            'type': 'buy_reserve' if card in agent.cards['yellow'] else 'buy_available',
                            'card': card,
                            'returned_gems': returned_gems,
                            'noble': noble,
                        }
                    )

        # Return list of actions. If there are no actions (almost impossible), all this player can do is pass.
        # A noble is still permitted to visit if conditions are met.
        if not actions:
            for noble in potential_nobles:
                actions.append({'type': 'pass', 'noble': noble})

        return actions
    
    def generate_return_combos_for_abstracted_actions(self, current_gems, collected_gems):
        total_gem_count = sum(current_gems.values()) + sum(collected_gems.values())
        if total_gem_count > 10:
            return_combos = []
            num_return = total_gem_count - 10
            # Combine current and collected gems. Screen out gem colours that were just collected.
            total_gems = {i: current_gems.get(i, 0) + collected_gems.get(i, 0) for i in set(current_gems)}
            total_gems = {i[0]: i[1] for i in total_gems.items() if i[0] not in collected_gems.keys()}.items()
            # Form a total gems list (with elements == gem colours, and len == number of gems).
            total_gems_list = []
            for colour, count in total_gems:
                for _ in range(count):
                    total_gems_list.append(colour)
            # If, after screening, there aren't enough gems that can be returned, return an empty list, indicating that
            # the collected_gems combination is not viable.
            if len(total_gems_list) < num_return:
                return []

            # *****Else, find one combinations of gems to return.*****
            for combo in [random.sample(total_gems_list, num_return)]:
                returned_gems = {c: 0 for c in COLOURS.values()}
                for colour in combo:
                    returned_gems[colour] += 1
                # Filter out colours with zero gems, and append.
                return_combos.append(dict({i for i in returned_gems.items() if i[-1] > 0}))

            return return_combos

        return [{}]  # If no gems need to be returned, return a list comprised of one empty combo.

    def get_reward(self, state, action, new_state, agent_id):
        return new_state.agents[agent_id].score - state.agents[agent_id].score
    
    def reward_shaping(self, state, action, agent_id):
        agent = state.agents[agent_id]
        reserved_cards = agent.cards['yellow']
        if 'buy' in action['type']:
            extra_reward = 0
            nobles = state.board.nobles
            card_color = action['card'].colour
            my_cards = agent.cards
            for noble in nobles:
                if card_color in noble[1].keys() and len(my_cards[card_color]) < noble[1][card_color]:
                    extra_reward += 0.3 # 0-point card's max: 0.3 * 3 = 0.9
            return extra_reward
        
        return 0
    
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