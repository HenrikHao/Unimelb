import random
import json
from collections import defaultdict
from .agent_util import *

class Policy:
    def select_action(self, state, action):
        pass


class DeterministicPolicy(Policy):
    def update(self, state, action):
        pass


class StochasticPolicy(Policy):
    def update(self, states, actions, rewards):
        pass

    def get_probability(self, state, action):
        pass

class TabularPolicy(DeterministicPolicy):
    def __init__(self, default_action=None):
        self.policy_table = defaultdict(lambda: default_action)

    def select_action(self, state, actions):
        return self.policy_table[state]

    def update(self, state, action):
        self.policy_table[state] = action

class ValueFunction():

    def update(self, state, value):
        pass

    def merge(self, value_table):
        pass

    def get_value(self, state):
        pass

    """ Return the Q-value of action in state """
    def get_q_value(self, mdp, state, action):
        q_value = 0.0
        for new_state in mdp.get_transitions(state, action):
            reward = mdp.get_reward(state, action, new_state)
            q_value += 0.2 * (
                    reward
                    + (mdp.get_discount_factor() * self.get_value(new_state))
            )

        return q_value

    """ Return a policy from this value function """

    def extract_policy(self, mdp):
        policy = TabularPolicy()
        for state in mdp.get_states():
            max_q = float("-inf")
            for action in mdp.get_actions(state):
                q_value = self.get_q_value(mdp, state, action)

                # If this is the maximum Q-value so far,
                # set the policy for this state
                if q_value > max_q:
                    policy.update(state, action)
                    max_q = q_value

        return policy

class QTable():
    def __init__(self, alpha=0.1, default_q_value=0.0):
        self.qtable = defaultdict(lambda: default_q_value)
        self.alpha = alpha

    def update(self, state, action, delta):
        self.qtable[(unpack(state), translateAction(action))] = self.qtable[(unpack(state), translateAction(action))] + self.alpha * delta

    def get_q_value(self, state, action):
        return self.qtable[(unpack(state), translateAction(action))]

    def save(self, filename):
        with open(filename, "w") as file:
            serialised = {str(key): value for key, value in self.qtable.items()}
            json.dump(serialised, file)

    def load(self, filename, default=0.0):
        with open(filename, "r") as file:
            serialised = json.load(file)
            self.qtable = defaultdict(
                lambda: default,
                {tuple(eval(key)): value for key, value in serialised.items()},
            )

class LinearQFunction(QTable):
    def __init__(self, features, alpha=0.1, weights=None, default_q_value=0.0):
        self.features = features
        self.alpha = alpha
        if weights == None:
            self.weights = [
                default_q_value
                for _ in range(0, features.num_actions())
                for _ in range(0, features.num_features())
            ]

    def update(self, state, action, delta):
        # update the weights
        feature_values = self.features.extract(state, action)
        for i in range(len(self.weights)):
            self.weights[i] = self.weights[i] + (self.alpha * delta * feature_values[i])

    def get_q_value(self, state, action):
        q_value = 0.0
        feature_values = self.features.extract(state, action)
        for i in range(len(feature_values)):
            q_value += feature_values[i] * self.weights[i]
        return q_value

class FeatureExtractor:
    def extract_features(self, state, action):
        pass