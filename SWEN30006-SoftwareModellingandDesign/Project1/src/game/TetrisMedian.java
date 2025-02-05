package src.game;

import src.utility.RandomBlock;
import src.AbstractTetris;
import src.TetrisGameCallback;
import src.blocks.Block;

import java.util.Properties;

import static src.AbstractTetris.GameDifficulty.MEDIAN;

public class TetrisMedian extends AbstractTetris {
    public static final GameDifficulty diff = MEDIAN;
    public TetrisMedian(TetrisGameCallback gameCallback, Properties properties) {
        super(gameCallback, properties);
    }

    @Override
    public Block createRandomTetrisBlock() {
        Block[] blocks = RandomBlock.getRandomBlocks(diff, getRandom(), this);
        roundUpdate(blocks[0], blocks[1]);
        return blocks[0];
    }

    @Override
    public int getSimulationTime() {
        if (isAuto()) {
            return DEFAULT_SPEED_AUTO;
        } else {
            return (int) (getSpeed() * 0.8);
        }
    }
}
