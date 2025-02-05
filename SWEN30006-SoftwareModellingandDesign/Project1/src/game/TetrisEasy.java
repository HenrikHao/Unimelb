package src.game;

import src.utility.RandomBlock;
import src.AbstractTetris;
import src.TetrisGameCallback;
import src.blocks.Block;

import java.util.Properties;

import static src.AbstractTetris.GameDifficulty.EASY;

public class TetrisEasy extends AbstractTetris {
    public static final GameDifficulty diff = EASY;
    public TetrisEasy(TetrisGameCallback Callback, Properties properties) {
        super(Callback, properties);
    }

    @Override
    public Block createRandomTetrisBlock() {
        Block[] blocks = RandomBlock.getRandomBlocks(diff, getRandom(), this);
        roundUpdate(blocks[0], blocks[1]);
        return blocks[0];
    }
}
