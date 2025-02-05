package src.utility;

import src.AbstractTetris;
import src.blocks.*;

import java.util.Random;

public class RandomBlock {
    /*if (blockPreview != null)
            blockPreview.removeSelf();

    // If the game is in auto test mode, then the block will be moved according to the blockActions
    String currentBlockMove = "";
        if (blockActions.length > blockActionIndex) {
        currentBlockMove = blockActions[blockActionIndex];
    }*/
    private static final String[] shapes = new String[]{ "I", "J", "L", "O", "S", "T", "Z",  "P", "Q", "Plus"};
    private static final int pivot = 7;


    public static Block[] getRandomBlocks(AbstractTetris.GameDifficulty diff, Random rad, AbstractTetris abstractTetris){
        // return two blocks, one for scoring, one for preview
        Block[] blocks = new Block[2];

        int i = -1;
        String name = null;
        if (diff == AbstractTetris.GameDifficulty.EASY){
            i = rad.nextInt(pivot);
        }
        else {
            i = rad.nextInt(shapes.length);
        }
        name = shapes[i];
        blocks[0] = getBlock(name, abstractTetris);
        blocks[1] = getBlock(name, abstractTetris);
        return blocks;
    }

    private static Block getBlock(String name, AbstractTetris abstractTetris) {

        return switch (name) {
            case "J" -> new J(abstractTetris);
            case "L" -> new L(abstractTetris);
            case "O" -> new O(abstractTetris);
            case "S" -> new S(abstractTetris);
            case "T" -> new T(abstractTetris);
            case "Z" -> new Z(abstractTetris);
            case "P" -> new P(abstractTetris);
            case "Q" -> new Q(abstractTetris);
            case "Plus" -> new Plus(abstractTetris);
            default -> new I(abstractTetris);
        };
    }
}
