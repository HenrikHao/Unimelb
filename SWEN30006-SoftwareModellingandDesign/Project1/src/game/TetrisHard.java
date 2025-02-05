package src.game;

import src.utility.RandomBlock;
import src.AbstractTetris;
import src.TetrisGameCallback;
import src.blocks.Block;

import java.util.Properties;
import java.util.Random;


import static src.AbstractTetris.GameDifficulty.HARD;

public class TetrisHard extends AbstractTetris {
    public static final GameDifficulty diff = HARD;
    public TetrisHard(TetrisGameCallback gameCallback, Properties properties) {
        super(gameCallback, properties);
    }

    @Override
    public Block createRandomTetrisBlock() {

        Block[] blocks = RandomBlock.getRandomBlocks(diff, getRandom(), this);
        roundUpdate(blocks[0], blocks[1]);
        super.setSimulationTime(this.getSimulationTime());
        return blocks[0];
    }

    @Override
    public boolean canRotate() {
        return false;
    }

    @Override
    public int getSimulationTime() {
        if (super.isAuto()) {
            return DEFAULT_SPEED_AUTO;
        } else {
            Random random = super.getRandom();
            int speed = getSpeed();
            if (speed < 1) return 1;
            return random.nextInt(speed / 2, speed);
        }
    }

}
