import torch
import torch.nn as nn
from Splendor.splendor_model import SplendorGameRule
from template import Agent
from Splendor.splendor_utils import CARDS, NOBLES

# Constants
WEIGHT_FILE_PATH = "agents/t_069/dqn_weights/"
ACTION_DICT = {'collect_same': 0, 'collect_diff': 1, 'reserve': 2, 'buy_available': 3, 'buy_reserve': 4, "pass": 5}
class QNetwork(nn.Module):
    def __init__(self, input_size, output_size):
        super(QNetwork, self).__init__()
        self.fc1 = nn.Linear(input_size, 256) 
        self.fc2 = nn.Linear(256, 256)  
        self.fc3 = nn.Linear(256, output_size)

    def forward(self, x):
        x = torch.relu(self.fc1(x))
        x = torch.relu(self.fc2(x))
        x = self.fc3(x)
        return x



class myAgent(Agent):
    def __init__(self, _id):
        super().__init__(_id)
        self.game_rule = SplendorGameRule(2)
        # Define the Q-Network and Target Network
        self.state_size = 242 # Number of features in the state + action
        self.output_size = 1
        self.device = torch.device("cuda" if torch.cuda.is_available() else "cpu")
        self.q_network = QNetwork(self.state_size, self.output_size).to(self.device)
        # Load weights
        self.load_weights(self.id)

    def load_weights(self, id):
        print(id)
        state_dict = torch.load(WEIGHT_FILE_PATH + f"weights-{id}.pth", map_location=torch.device('cpu'))
        self.q_network.load_state_dict(state_dict)

    def SelectAction(self, actions, game_state):
        state = self.extract_state_features(game_state)
        q_values = []
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

        return action
    
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