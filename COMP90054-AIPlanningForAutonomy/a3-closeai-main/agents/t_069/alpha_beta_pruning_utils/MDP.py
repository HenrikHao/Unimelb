class MDP:
    """ Return all states of this MDP """
    def get_states(self):
        pass

    """ Return all actions with non-zero probability from this state """
    def get_actions(self, state):
        pass

    """ Return all non-zero probability transitions for this action
        from this state, as a list of (state, probability) pairs
    """
    def get_transitions(self, state, action):
        pass

    """ Return the reward for transitioning from state to
        nextState via action
    """
    def get_reward(self, state, action, next_state):
        pass

    """ Return true if and only if state is a terminal state of this MDP """
    def is_terminal(self, state):
        pass

    """ Return the discount factor for this MDP """
    def get_discount_factor(self):
        pass

    """ Return the initial state of this MDP """
    def get_initial_state(self):
        pass

    """ Return all goal states of this MDP """
    def get_goal_states(self):
        pass