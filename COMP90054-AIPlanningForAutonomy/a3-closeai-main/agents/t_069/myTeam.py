from template import Agent
import random
import math
import itertools
import traceback
import time
from collections import defaultdict
import pickle
from Splendor.splendor_model import SplendorGameRule
from Splendor.splendor_utils import COLOURS
from .MCTS_utils.potential import get_potential


def state_to_key(game_state, agent_id):
    agent, board = game_state.agents[agent_id], game_state.board
    dealt = [card.code for cards in board.dealt for card in cards if card is not None]

    # tiers = []
    # for i in range(3):
    #     tiers.append({'black': 0, 'red': 0, 'green': 0, 'blue': 0, 'white': 0})
    # for tier, cards in enumerate(board.dealt):
    #     for card in cards:
    #         if card is not None:
    #             for c, cost in card.cost.items():
    #                 tiers[tier][c] += cost
    # tiers_key = frozenset([str(tier) for tier in tiers])

    # cards_cost = []
    # cards_cost = {'black': 0, 'red': 0, 'green': 0, 'blue': 0, 'white': 0}
    # for cards in board.dealt:
    #     for card in cards:
    #         if card is not None:
    #             for c, cost in card.cost.items():
    #                 cards_cost[c] += cost

    # board_nobles = frozenset([noble[0] for noble in board.nobles])

    # agent_gems_cp = pickle.loads(pickle.dumps(agent.gems, -1))

    agent_cards = {}
    for c, cards in agent.cards.items():
        agent_cards[c] = frozenset([card.code for card in cards])
    # agent_nobles = frozenset([noble[0] for noble in agent.nobles])

    key = (
        frozenset(dealt),
        # tiers_key,
        # str(cards_cost),
        # board_nobles,
        str(board.gems),
        # agent.score,
        str(agent.gems),
        str(agent_cards)
        # agent_nobles,
    )

    return key

class QTable:
    def __init__(self):
        self.table = {}
        self.action_lookup = {}

    def update(self, game_state, action, delta, agent_id):
        state_key = state_to_key(game_state, agent_id)
        action_str = str(action)
        if state_key not in self.table:
            self.table[state_key] = {}
        if action_str not in self.table[state_key]:
            self.table[state_key][action_str] = 0
            self.action_lookup[action_str] = action
        self.table[state_key][action_str] = self.table[state_key][action_str] + delta

    def get_q_value(self, game_state, action, agent_id):
        state_key = state_to_key(game_state, agent_id)
        action_str = str(action)
        if state_key not in self.table or action_str not in self.table[state_key]:
            return 0
        return self.table[state_key][action_str]

    # def get_max_q(self, game_state, agent_id):
    #     state_key = state_to_key(game_state, agent_id)
    #     if state_key not in self.table:
    #         return 0
    #     return max(self.table[state_key].values())

    def get_best_action(self, game_state, agent_id):
        state_key = state_to_key(game_state, agent_id)
        if state_key not in self.table:
            return None, 0
        best_action_str = max(self.table[state_key], key=self.table[state_key].get)
        return self.action_lookup[best_action_str], self.table[state_key][best_action_str]


class UpperConfidenceBounds:
    def __init__(self):
        self.Cp = 0.8

    def select(self, game_state, actions, qtable, agent_id):
        max_actions = []
        max_value = float("-inf")
        state_key = state_to_key(game_state, agent_id)

        # for action in actions:
        #     action_str = str(action)
        #     if Node.visits[(state_key, action_str)] == 0:
        #         # print('UCB not visited:', action)
        #         return action

        for action in actions:
            action_str = str(action)

            value = qtable.get_q_value(game_state, action, agent_id) + 2 * self.Cp * math.sqrt(
                (2 * math.log(Node.visits[state_key])) / Node.visits[(state_key, action_str)]
            )
            if value > max_value:
                max_actions = [action]
                max_value = value
            elif value == max_value:
                max_actions += [action]

        # if there are multiple actions with the highest value
        # choose one randomly
        result = random.choice(max_actions)

        return result


class MDP:
    def __init__(self):
        self.game_rule = SplendorGameRule(2)
        self.discount_factor = 0.7

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

        if len(agent.cards['yellow']) < 3 and board.gems['yellow'] > 0:
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
                # agent_post_action = pickle.loads(pickle.dumps(agent, -1))
                # Give the card featured in this action to a copy of the agent.
                agent.cards[card.colour].append(card)
                
                new_nobles = []
                for noble in board.nobles:
                    # Use this copied agent to check whether this noble can visit.
                    if self.game_rule.noble_visit(agent, noble):
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
                agent.cards[card.colour].remove(card)

        # Return list of actions. If there are no actions (almost impossible), all this player can do is pass.
        # A noble is still permitted to visit if conditions are met.
        if not actions:
            for noble in potential_nobles:
                actions.append({'type': 'pass', 'noble': noble})

        return actions

    def get_pass_actions_only(self, potential_nobles):
        actions = []

        for noble in potential_nobles:
            actions.append({'type': 'pass', 'noble': noble})

        return actions

    def get_collect_actions_only(self, game_state, agent_id):
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
        return actions, potential_nobles

    def get_reserve_actions_only(self, game_state, agent_id):
        actions = []
        agent, board = game_state.agents[agent_id], game_state.board

        potential_nobles = []
        for noble in board.nobles:
            if self.game_rule.noble_visit(agent, noble):
                potential_nobles.append(noble)
        if len(potential_nobles) == 0:
            potential_nobles = [None]

        if len(agent.cards['yellow']) < 3:
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
        return actions

    def get_buy_actions_only(self, game_state, agent_id):
        actions = []
        agent, board = game_state.agents[agent_id], game_state.board

        for card in board.dealt_list() + agent.cards['yellow']:
            if not card or len(agent.cards[card.colour]) == 7:
                continue
            returned_gems = self.game_rule.resources_sufficient(agent, card.cost)  # Check if this card is affordable.
            if (
                type(returned_gems) == dict
            ):  # If a dict was returned, this means the agent possesses sufficient resources.
                # Check to see if the acquisition of a new card has meant new nobles becoming candidates to visit.
                
                # agent_post_action = pickle.loads(pickle.dumps(agent, -1))
                # Give the card featured in this action to a copy of the agent.
                agent.cards[card.colour].append(card)
                
                new_nobles = []
                for noble in board.nobles:
                    # Use this copied agent to check whether this noble can visit.
                    if self.game_rule.noble_visit(agent, noble):
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
                # Recover
                agent.cards[card.colour].remove(card)
        return actions

    def get_discount_factor(self):
        return self.discount_factor
    
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
            # for card in reserved_cards:
            #     if action['card'].colour in card.cost.keys():
            #         extra_reward += 0.2 # 0-point's max: 0.2 * 3 = 0.6
            return extra_reward
        
        # if action['type'] == 'reserve' and action['collected_gems'] and len(agent.cards['yellow']) < 2:
        #     return action['card'].points * 0.2 # max: 5 * 0.2 = 1.0
        
        # if 'collect' in action['type']:
        #     extra_reward = 0 
        #     for color, count in action['collected_gems'].items():
        #         for card in reserved_cards:
        #             if color in card.cost.keys():
        #                 extra_reward += count * 0.15
        #     return extra_reward
        
        return 0
        
    def execute(self, state, action, agent_id):
        state_cp = pickle.loads(pickle.dumps(state, -1))
        next_state = self.game_rule.generateSuccessor(state_cp, action, agent_id)
        total_reward = self.get_reward(state, action, next_state, agent_id) + self.reward_shaping(state, action, agent_id)
        return (next_state, total_reward)

    def execute_without_copy_state(self, state, action, agent_id):
        old_score = state.agents[agent_id].score
        next_state = self.game_rule.generateSuccessor(state, action, agent_id)
        reward = next_state.agents[agent_id].score - old_score
        total_reward = reward + self.reward_shaping(state, action, agent_id)
        return (next_state, total_reward)

    def get_reward(self, state, action, new_state, agent_id):
        return new_state.agents[agent_id].score - state.agents[agent_id].score

    def is_terminal(self, state, agent_id):
        return state.agents[agent_id].score >= 15


class MCTS:
    def __init__(self, mdp, qfunction, bandit):
        self.mdp = mdp
        self.qfunction = qfunction
        self.bandit = bandit

    """
    Execute the MCTS algorithm from the initial state given, with timeout in seconds
    """

    def mcts(self, root_node, timeout=1):
        start_time = time.time()
        simulation_num = 0
        total_loop_num = 0
        while time.time() < start_time + timeout:

            # Find a state node to expand
            selected_node = root_node.select()
            if time.time() >= start_time + timeout:
                break
            
            if not self.mdp.is_terminal(selected_node.state, selected_node.agent_id):
                
                child = selected_node.expand()
                if time.time() >= start_time + timeout:
                    break
                
                reward, first_action = self.simulate(child)
                if time.time() >= start_time + timeout:
                    break

                child.back_propagate(reward, first_action)

                simulation_num += 1
            total_loop_num += 1

        print('Simulation num:', simulation_num)
        print('Loop num', total_loop_num)

    def choose_fast(self, state, agent_id):
        buy_actions = self.mdp.get_buy_actions_only(state, agent_id)
        nobles = state.board.nobles
        best_buy_action = None
        best_buy_action_reward = 0
        agent = state.agents[agent_id]
        print('buy actions len', len(buy_actions))
        for action in buy_actions:
            reward = 0
            reward += action['card'].points
            if action['noble']:
                reward += 3
            card_color = action['card'].colour
            my_cards = agent.cards
            for noble in nobles:
                if card_color in noble[1].keys() and len(my_cards[card_color]) < noble[1][card_color]:
                    reward += 0.4
            if reward > best_buy_action_reward:
                best_buy_action_reward = reward
                best_buy_action = action
        if best_buy_action is not None:
            return best_buy_action
        
        if state.board.gems['yellow'] > 0:
            reserve_actions = self.mdp.get_reserve_actions_only(state, agent_id)
            print('reserve actions len', len(reserve_actions))
            if reserve_actions and reserve_actions[0]['collected_gems']:
                max_card_points_action = None
                max_card_points = 0
                for action in reserve_actions:
                    if action['card'].points > max_card_points:
                        max_card_points = action['card'].points
                        max_card_points_action = action
                if max_card_points_action:
                    return action
        
        collect_actions, potential_nobles = self.mdp.get_collect_actions_only(state, agent_id)
        if collect_actions:
            print('Collect actinos len', len(collect_actions))
            max_action = None
            max_action_potential = 0
            reserved_cards = agent.cards['yellow']
            for action in collect_actions:
                potential = 0
                for color, count in action['collected_gems'].items():
                    for card in reserved_cards:
                        if color in card.cost.keys():
                            potential += count
                if potential > max_action_potential:
                    max_action_potential = potential
                    max_action = action
                
            if max_action:
                return max_action
            return random.choice(collect_actions)
        
        return random.choice(self.mdp.get_pass_actions_only(potential_nobles))

    """ Simulate until a terminal state """

    def simulate(self, node):
        state = pickle.loads(pickle.dumps(node.state, -1))
        cumulative_reward = 0.0
        depth = 0
        first_action = None
        while not self.mdp.is_terminal(state, node.agent_id) and depth < 3:
            # Choose an action to execute
            choose_start = time.time()
            action = self.choose_fast(state, node.agent_id)
            print('Choose fast used', time.time() - choose_start)

            # Execute the action
            (next_state, reward) = self.mdp.execute_without_copy_state(state, action, node.agent_id)

            if first_action is None:
                first_action = action

            # Discount the reward
            cumulative_reward += pow(self.mdp.get_discount_factor(), depth) * reward

            depth += 1
            state = next_state

        return cumulative_reward, first_action


class Node:
    next_node_id = 0

    visits = defaultdict(lambda: 0)

    def __init__(self, mdp, parent, state, qfunction, bandit, agent_id, reward=0.0, action=None):
        self.mdp = mdp
        self.parent = parent
        self.state = state
        self.id = Node.next_node_id
        Node.next_node_id += 1

        self.qfunction = qfunction
        self.bandit = bandit
        self.reward = reward
        self.action = action

        # A dictionary from actions to a set of node
        self.children = {}
        self.abstracted_avail_actions = self.mdp.get_abstracted_actions(self.state, agent_id)

        self.abstracted_avail_actions_str = set([str(action) for action in self.abstracted_avail_actions])

        self.agent_id = agent_id

    """ Return true if and only if all child actions have been expanded """

    def is_fully_expanded(self):
        valid_actions = self.abstracted_avail_actions
        if len(valid_actions) == len(self.children):
            return True
        else:
            return False

    """ Select a node that is not fully expanded """

    def select(self):
        if not self.is_fully_expanded() or self.mdp.is_terminal(self.state, self.agent_id):
            return self
        else:
            abstracted_action = self.bandit.select(
                self.state, self.abstracted_avail_actions, self.qfunction, self.agent_id
            )
            return self.get_outcome_child(abstracted_action).select()

    """ Expand a node if it is not a terminal node """

    def expand(self):
        if not self.mdp.is_terminal(self.state, self.agent_id):
            # Randomly select an unexpanded action to expand
            actions_str = self.abstracted_avail_actions_str - self.children.keys()
            target_action_str = random.choice(list(actions_str))

            target_action = None
            for action in self.abstracted_avail_actions:
                if str(action) == target_action_str:
                    target_action = action

            self.children[target_action_str] = []
            return self.get_outcome_child(target_action)
        return self

    """ Backpropogate the reward back to the parent node """

    def back_propagate(self, reward, action):
        if action is not None:
            # self is not a terminal state
            action_str = str(action)
            state_key = state_to_key(self.state, self.agent_id)
            Node.visits[state_key] += 1
            Node.visits[(state_key, action_str)] += 1
            # print('Back propagate: ', action_str, Node.visits[(state_key, action_str)])

            delta = (1 / (Node.visits[(state_key, action_str)])) * (
                reward - self.qfunction.get_q_value(self.state, action, self.agent_id)
            )
            self.qfunction.update(self.state, action, delta, self.agent_id)

        if self.parent != None:
            self.parent.back_propagate(self.reward + self.mdp.get_discount_factor() * reward, self.action)

    """ Simulate the outcome of an action, and return the child node """

    def get_outcome_child(self, abstracted_action):
        # Choose one outcome based on transition probabilities
        (next_state, reward) = self.mdp.execute(self.state, abstracted_action, self.agent_id)

        action_str = str(abstracted_action)
        # Find the corresponding state and return if this already exists
        for child in self.children[action_str]:
            if state_to_key(next_state, self.agent_id) == state_to_key(child.state, self.agent_id):
                return child

        # This outcome has not occured from this state-action pair previously
        new_child = Node(
            self.mdp, self, next_state, self.qfunction, self.bandit, self.agent_id, reward, abstracted_action
        )

        # Find the probability of this outcome (only possible for model-based) for visualising tree
        self.children[action_str] += [new_child]
        return new_child


class myAgent(Agent):
    def __init__(self, _id):
        super().__init__(_id)
        self.qfunction = QTable()
        self.bandit = UpperConfidenceBounds()
        self.mdp = MDP()
        self.mcts = MCTS(self.mdp, self.qfunction, self.bandit)

        self.opponent_id = abs(_id - 1)

    def choose_best_action_by_potential(self, game_state, actions):
        max_action = None
        max_potential = float('-inf')
        for action in actions:
            next_state, reward = self.mdp.execute(game_state, action, self.id)
            potential = get_potential(next_state, self.id)
            if potential > max_potential:
                max_potential = potential
                max_action = action
        return max_action

    def SelectAction(self, actions, game_state):
        try:
            my_timeout = 0.93
            start_time = time.time()
            node = Node(self.mdp, None, game_state, self.qfunction, self.bandit, self.id, reward=0.0, action=None)
            self.mcts.mcts(node, timeout=my_timeout)
            print("Time: ", time.time() - start_time)
            best_action, best_action_q = self.qfunction.get_best_action(game_state, self.id)
            print('My turn')

            if best_action is None or best_action_q == 0:
                print('Choose best action by potential', best_action, best_action_q)
                return self.choose_best_action_by_potential(game_state, actions)
            else:
                # for action in actions:
                #     if str(action) == best_action_str:
                #         print('HIT!!')
                #         print('BEST ACTION', best_action_str, best_action_q)

                #         return action

                # print('Cannot hit')
                # return self.choose_best_action_by_potential(game_state, actions)
                print('BEST ACTION', best_action, best_action_q)
                return best_action

        except Exception as e:
            traceback.print_exc()
