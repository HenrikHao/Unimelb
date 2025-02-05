class ModelFreeLearner:
    def execute(self, episodes=2000):
        pass

class TemporalDifferenceLearner(ModelFreeLearner):
    def __init__(self, mdp, bandit, qfunction):
        self.mdp = mdp
        self.bandit = bandit
        self.qfunction = qfunction
        self.qfunction.load("save.txt")

    def execute(self, episodes=2000):
        print("start")
        rewards = []
        for i in range(episodes):
            #print(i)
            state = self.mdp.get_initial_state()
            actions = self.mdp.get_actions(state)
            action = self.bandit.select(state, actions, self.qfunction)
            episode_reward = 0.0
            step = 0
            while not self.mdp.is_terminal(state):
                print(state)
                print("-----------------------------------------------------------")
                print("-----------------------------------------------------------")
                print("-----------------------------------------------------------")
                print("-----------------------------------------------------------")
                print("-----------------------------------------------------------")

                next_state, reward = self.mdp.execute(state, action)
                actions = self.mdp.get_actions(next_state)
                next_action = self.bandit.select(next_state, actions, self.qfunction)

                delta = self.get_delta(reward, state, action, next_state, next_action)
                self.qfunction.update(state, action, delta)

                state = next_state
                action = next_action
                episode_reward += reward * (self.mdp.discount_factor ** step)
                step += 1

            rewards.append(episode_reward)
            self.qfunction.save("save.txt")
        return rewards

    """ Calculate the delta for the update """

    def get_delta(self, reward, state, action, next_state, next_action):
        q_value = self.qfunction.get_q_value(state, action)
        next_state_value = self.state_value(next_state, next_action)
        delta = reward + self.mdp.discount_factor * next_state_value - q_value
        return delta

    """ Get the value of a state """

    def state_value(self, state, action):
        pass

class QLearning(TemporalDifferenceLearner):
    def state_value(self, state, action):
        max_q_value = max([self.qfunction.get_q_value(state,i) for i in self.mdp.get_actions(state)])
        return max_q_value

class RewardShapedQLearning(QLearning):
    def __init__(self, mdp, bandit, potential, qfunction):
        super().__init__(mdp, bandit, qfunction=qfunction)
        self.potential = potential

    def get_delta(self, reward, state, action, next_state, next_action):
        q_value = self.qfunction.get_q_value(state, action)
        next_state_value = self.state_value(next_state, next_action)
        state_potential = self.potential.get_potential(state)
        next_state_potential = self.potential.get_potential(next_state)
        potential = self.mdp.discount_factor * next_state_potential - state_potential
        delta = reward + potential + self.mdp.discount_factor * next_state_value - q_value
        return delta