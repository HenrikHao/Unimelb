package thrones.game.utils;

import java.util.Random;

import ch.aplu.jcardgame.Card;
import ch.aplu.jcardgame.Hand;
import thrones.game.GameOfThrones;

public final class RandomUtil {

    private static int randomSeed = Integer.parseInt(GameOfThrones.DEFAULT_SEED);

    private static RandomUtil instance;
    private Random random;
    
    private RandomUtil(int seed) {
        this.random = new Random(seed);
    }

    // return random Card from Hand
    public Card randomCard(Hand hand) {
        assert !hand.isEmpty() : " random card from empty hand.";
        int x = random.nextInt(hand.getNumberOfCards());
        return hand.get(x);
    }

    public int nextInt(int range) {
        return random.nextInt(range);
    }

    public static void setSeed(int seed) {
        randomSeed = seed;
    }

    public static RandomUtil getInstance() {
        if (instance == null) {
            instance = new RandomUtil(randomSeed);
        }
        return instance;
    }
    
}
