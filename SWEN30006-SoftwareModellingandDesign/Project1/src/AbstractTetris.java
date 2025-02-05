package src;// Tetris.java

import ch.aplu.jgamegrid.*;
import src.blocks.*;
import src.utility.GameStat;

import java.io.IOException;
import java.util.*;
import java.awt.event.KeyEvent;
import java.awt.*;
import javax.swing.*;

public abstract class AbstractTetris extends JFrame implements GGActListener {

    protected static final int DEFAULT_SPEED_AUTO = 10;
    private static final int DEFAULT_SPEED = 100;
    protected Random getRandom() {
        return new Random();
    }
    public enum GameDifficulty {
        EASY, MEDIAN, HARD
    }
    private Block currentBlock;  // Currently active block
    private Block blockPreview = null;   // block in preview window
    private int score = 0;
    private int round = 0;
    private int slowDown = 5;
    private int speed = DEFAULT_SPEED;
    private Random random = new Random(0);

    private TetrisGameCallback gameCallback;

    private boolean isAuto = false;

    private int seed = 30006;
    // For testing mode, the block will be moved automatically based on the blockActions.
    // L is for Left, R is for Right, T is for turning (rotating), and D for down
    private String [] blockActions = new String[10];
    private int blockActionIndex = 0;
    private final String difficulty = this.getClass().getSimpleName();
    private GameStat gameStats = new GameStat(difficulty);

    // Initialise object
    private void initWithProperties(Properties properties) {
        this.seed = Integer.parseInt(properties.getProperty("seed", "30006"));
        random = new Random(seed);
        isAuto = Boolean.parseBoolean(properties.getProperty("isAuto"));
        String blockActionProperty = properties.getProperty("autoBlockActions", "");
        blockActions = blockActionProperty.split(",");
    }

    public AbstractTetris(TetrisGameCallback gameCallback, Properties properties) {
        // Initialise value
        initWithProperties(properties);
        this.gameCallback = gameCallback;
        blockActionIndex = 0;

        // Set up the UI components. No need to modify the UI Components
        tetrisComponents = new TetrisComponents();
        tetrisComponents.initComponents(this);
        gameGrid1.addActListener(this);
        gameGrid1.setSimulationPeriod(getSimulationTime());

        // Add the first block to start
        currentBlock = createRandomTetrisBlock();
        gameGrid1.addActor(currentBlock, new Location(6, 0));
        gameGrid1.doRun();

        // Do not lose keyboard focus when clicking this window
        gameGrid2.setFocusable(false);
        setTitle("SWEN30006 Tetris Madness");
        score = 0;
        showScore(score);
        setSpeed(score);
        slowDown = 5;
    }

    // create a block and assign to a preview mode
    public abstract Block createRandomTetrisBlock();


    public void setCurrentTetrisBlock(Block t) {
        gameCallback.changeOfBlock(currentBlock);
        currentBlock = t;
    }

    // Handle user input to move block. Arrow left to move left, Arrow right to move right, Arrow up to rotate and
    // Arrow down for going down
    public void moveBlock(int keyEvent) {
        switch (keyEvent) {
            case KeyEvent.VK_UP:
                currentBlock.rotate();
                break;
            case KeyEvent.VK_LEFT:
                currentBlock.left();
                break;
            case KeyEvent.VK_RIGHT:
                currentBlock.right();
                break;
            case KeyEvent.VK_DOWN:
                currentBlock.drop();
                break;
            default:
        }
    }
    public void roundUpdate(Block next, Block preview) {
    // set preview and block actions if auto
        String currentBlockMove = "";
        if (blockActions.length > blockActionIndex) {
            currentBlockMove = blockActions[blockActionIndex];
        }
        blockActionIndex++;

        if (isAuto) {
            next.setAutoBlockMove(currentBlockMove);
        }
        next.setSlowDown(slowDown);
        setSpeed(score);
        gameStats.addTetrisToStat(round, next.getClass().getSimpleName());

        if (blockPreview != null) {
            blockPreview.removeSelf();
        }
        preview.display(gameGrid2, new Location(2, 1));
        blockPreview = preview;
    }

    public void act() {
        removeFilledLine();
        moveBlock(gameGrid1.getKeyCode());
    }
    private void removeFilledLine() {
        for (int y = 0; y < gameGrid1.nbVertCells; y++) {
            boolean isLineComplete = true;
            TetroBlock[] blocks = new TetroBlock[gameGrid1.nbHorzCells];   // One line
            // Calculate if a line is complete
            for (int x = 0; x < gameGrid1.nbHorzCells; x++) {
                blocks[x] =
                        (TetroBlock) gameGrid1.getOneActorAt(new Location(x, y), TetroBlock.class);
                if (blocks[x] == null) {
                    isLineComplete = false;
                    break;
                }
            }
            if (isLineComplete) {
                // If a line is complete, we remove the component block of the shape that belongs to that line
                for (int x = 0; x < gameGrid1.nbHorzCells; x++)
                    gameGrid1.removeActor(blocks[x]);
                ArrayList<Actor> allBlocks = gameGrid1.getActors(TetroBlock.class);
                for (Actor a : allBlocks) {
                    int z = a.getY();
                    if (z < y)
                        a.setY(z + 1);
                }
                gameGrid1.refresh();
                score++;
                gameStats.addScore(round, score);
                gameCallback.changeOfScore(score);
                showScore(score);
                setSpeed(score);
            }
        }
    }

    private void setSpeed(int score) {
        // Set speed of tetrisBlocks
        if (score > 10)
            slowDown = 4;
        if (score > 20)
            slowDown = 3;
        if (score > 30)
            slowDown = 2;
        if (score > 40)
            slowDown = 1;
        if (score > 50)
            slowDown = 0;
    }

    // Show Score and Game Over

    private void showScore(final int score) {
        EventQueue.invokeLater(new Runnable() {
            public void run() {
                scoreText.setText(score + " points");
            }
        });

    }

    public void gameOver() throws IOException {
        gameStats.output();
        gameGrid1.addActor(new Actor("sprites/gameover.gif"), new Location(5, 5));
        gameGrid1.doPause();
        if (isAuto) {
            System.exit(0);
        }
    }

    // Start a new game
    public void startBtnActionPerformed(java.awt.event.ActionEvent evt)
    {
        gameGrid1.doPause();
        gameGrid1.removeAllActors();
        gameGrid2.removeAllActors();
        gameGrid1.refresh();
        gameGrid2.refresh();
        gameGrid2.delay(getDelayTime());
        blockActionIndex = 0;
        currentBlock = createRandomTetrisBlock();
        gameGrid1.addActor(currentBlock, new Location(6, 0));
        gameGrid1.doRun();
        gameGrid1.requestFocus();
        score = 0;
        showScore(score);
        slowDown = 5;
        resetSpeed();

        ++round;
    }

    private void resetSpeed() {
        if (isAuto) {
            speed = DEFAULT_SPEED_AUTO;
        } else {
            speed = DEFAULT_SPEED;
        }
    }

    // Different speed for manual and auto mode
    protected int getSimulationTime() {
        if (isAuto) {
            return DEFAULT_SPEED_AUTO;
        } else {
            return speed;
        }
    }

    public void setSimulationTime(int s) {
        speed = s;
        gameGrid1.setSimulationPeriod(speed);
    }

    private int getDelayTime() {
        if (isAuto) {
            return 200;
        } else {
            return 2000;
        }
    }
    public boolean canRotate() {
        return true;
    }

    public boolean isAuto() {
        return isAuto;
    }

    public int getSpeed() {
        return speed;
    }


    // AUTO GENERATED - do not modify//GEN-BEGIN:variables
    public ch.aplu.jgamegrid.GameGrid gameGrid1;
    public ch.aplu.jgamegrid.GameGrid gameGrid2;
    public javax.swing.JPanel jPanel1;
    public javax.swing.JPanel jPanel2;
    public javax.swing.JPanel jPanel3;
    public javax.swing.JPanel jPanel4;
    public javax.swing.JScrollPane jScrollPane1;
    public javax.swing.JTextArea jTextArea1;
    public javax.swing.JTextField scoreText;
    public javax.swing.JButton startBtn;
    private TetrisComponents tetrisComponents;
    // End of variables declaration//GEN-END:variables

}
