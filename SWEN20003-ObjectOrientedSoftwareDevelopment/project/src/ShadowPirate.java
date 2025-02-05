import bagel.*;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;

/**
 * Skeleton Code for SWEN20003 Project 2, Semester 1, 2022
 *
 * Please fill your name below
 * @author Zhuoyang Hao
 */
public class ShadowPirate extends AbstractGame {
    // size of the window
    private final static int WINDOW_WIDTH = 1024;
    private final static int WINDOW_HEIGHT = 768;
    private final static String GAME_TITLE = "ShadowPirate";

    // Image and World file
    private final Image SHIP_IMAGE = new Image("res/background0.png");
    private final Image ISLAND_IMAGE = new Image("res/background1.png");
    private final static String SHIP_FILE = "res/level0.csv";
    private final static String ISLAND_FILE = "res/level1.csv";

    // message used to render
    private final static String START_MESSAGE = "PRESS SPACE TO START";
    private final static String ARROW_MESSAGE = "USE ARROW KEYS TO FIND LADDER";
    private final static String ATTACK_MESSAGE = "PRESS S TO ATTACK";
    private final static String TREASURE_MESSAGE = "FIND THE TREASURE";
    private final static String COMPLETE_MESSAGE = "LEVEL COMPLETE!";
    private final static String CONGRATULATIONS = "CONGRATULATIONS!";
    private final static String LOSE_MESSAGE = "GAME OVER";

    // message rendering position and font size
    private final static int MESSAGE_OFFSET = 70;
    private final static int MESSAGE_FONT_SIZE = 55;
    private final static int MESSAGE_Y_POS = 402;
    private final Font FONT = new Font("res/wheaton.otf", MESSAGE_FONT_SIZE);

    // four boolean value used to control game state
    private boolean gameOn;
    private boolean gameEnd;
    private boolean gameWin;
    private boolean isLevelUp;

    // arraylist used to store different entities
    private final ArrayList<Stationary> stationaries = new ArrayList<>();
    private final ArrayList<Enemy> enemies = new ArrayList<>();
    public static ArrayList<Projectile> projectiles = new ArrayList<>();
    private Sailor sailor;

    // boundary
    private static double top;
    private static double left;
    private static double bottom;
    private static double right;

    // refresh rate
    private final static int REFRESH_RATE = 60;

    // render time
    private int renderFrame = 0;
    private final static int RENDER_TIME = 3000;

    /**
     * The constructor for ShadowPirate class
     */
    public ShadowPirate() {
        super(WINDOW_WIDTH, WINDOW_HEIGHT, GAME_TITLE);
        readCSV(SHIP_FILE);
        gameWin = false;
        gameEnd = false;
        gameOn = false;
    }

    /**
     * The entry point for the program.
     */
    public static void main(String[] args) {
        ShadowPirate game = new ShadowPirate();
        game.run();
    }

    /**
     * Method used to read file and create objects
     */
    private void readCSV(String fileName){
        try (BufferedReader reader = new BufferedReader(new FileReader(fileName))){

            String line;
            while ((line = reader.readLine()) != null) {
                String[] sections = line.split(",");
                String type = sections[0];
                double xCor = Double.parseDouble(sections[1]);
                double yCor = Double.parseDouble(sections[2]);
                if (type.equals("Sailor")) {
                    sailor = new Sailor(xCor, yCor);
                }
                if (type.equals("TopLeft")) {
                    top = yCor;
                    left = xCor;
                }
                if (type.equals("BottomRight")) {
                    bottom = yCor;
                    right = xCor;
                }
                if (type.equals("Block")) {
                    if (!isLevelUp) {
                        stationaries.add(new Block(xCor, yCor));
                    } else {
                        stationaries.add(new Bomb(xCor, yCor));
                    }
                }
                if (type.equals("Sword")) {
                    stationaries.add(new Sword(xCor, yCor));
                }
                if (type.equals("Potion")) {
                    stationaries.add(new Potion(xCor, yCor));
                }
                if (type.equals("Elixir")) {
                    stationaries.add(new Elixir(xCor, yCor));
                }
                if (type.equals("Treasure")) {
                    stationaries.add(new Treasure(xCor, yCor));
                }
                if (type.equals("Pirate")) {
                    enemies.add(new Pirate(xCor, yCor));
                }
                if (type.equals("Blackbeard")) {
                    enemies.add(new Blackbeard(xCor, yCor));
                }
            }

        } catch (IOException e){
            e.printStackTrace();
            System.exit(-1);
        }
    }

    /**
     * Performs a state update.
     * allows the game to exit when the escape key is pressed.
     */
    @Override
    public void update(Input input) {

        // when game is win
        if (sailor.getFindTreasure()) {
            gameWin = true;
        }

        // when sailor lost the game
        if (sailor.getPercentage(sailor.getMaxHealthPoints()) <= 0) {
            gameEnd = true;
        }

        if (gameEnd) {
            drawLoseString();
        }

        if (gameWin) {
            drawEndString();
        }

        // draw start screen message and level complete message
        if (!gameWin && !gameEnd) {
            if (!isLevelUp) {
                if (!gameOn) {
                    drawStartScreen(input);
                }
            } else {
                if ((renderFrame * 1000 / REFRESH_RATE) >= RENDER_TIME) {
                    drawStartScreen(input);
                } else {
                    drawLevelComplete();
                }
            }
        }

        // when game is running
        if (gameOn && !gameEnd && !gameWin) {
            if (!isLevelUp) {
                SHIP_IMAGE.draw(Window.getWidth() / 2.0, Window.getHeight() / 2.0);
            } else {
                ISLAND_IMAGE.draw(Window.getWidth() / 2.0, Window.getHeight() / 2.0);
            }
            for (Stationary stationary: stationaries) {
                stationary.update();
            }
            for (Enemy enemy: enemies) {
                enemy.update(stationaries, sailor);
                enemy.render();
            }
            for (Projectile projectile: projectiles) {
                projectile.update(sailor);
                projectile.render();
            }

            if (isLevelUp) {
                stationaries.removeIf(Stationary::isEaten);
            }

            enemies.removeIf(enemy -> enemy.getHealthPoints() <= 0);
            projectiles.removeIf(Projectile::stopRender);
            sailor.update(input, stationaries, enemies);
            sailor.render();
        }

        if (input.wasPressed(Keys.ESCAPE)) {
            Window.close();
        }

        // clear all entities when the level is up
        if (!isLevelUp && sailor.reachLadder()) {
            isLevelUp = true;
            gameOn = false;
            stationaries.clear();
            enemies.clear();
            projectiles.clear();
            readCSV(ISLAND_FILE);
        }

        // plus the renderframe
        if (isLevelUp && !gameOn) {
            renderFrame += 1;
        }
    }

    /**
     * Method used to draw the start screen instructions
     */
    private void drawStartScreen(Input input){
        FONT.drawString(START_MESSAGE, (Window.getWidth()/2.0 - (FONT.getWidth(START_MESSAGE)/2.0)),
                MESSAGE_Y_POS);
        FONT.drawString(ATTACK_MESSAGE, (Window.getWidth()/2.0 - (FONT.getWidth(ATTACK_MESSAGE)/2.0)),
                (MESSAGE_Y_POS + MESSAGE_OFFSET));
        if (!isLevelUp) {
            FONT.drawString(ARROW_MESSAGE, (Window.getWidth() / 2.0 - (FONT.getWidth(ARROW_MESSAGE) / 2.0)),
                    (MESSAGE_Y_POS + 2 * MESSAGE_OFFSET));
        } else {
            FONT.drawString(TREASURE_MESSAGE, (Window.getWidth() / 2.0 - (FONT.getWidth(TREASURE_MESSAGE) / 2.0)),
                    (MESSAGE_Y_POS + 2 * MESSAGE_OFFSET));

        }
        if (input.wasPressed(Keys.SPACE)){
            gameOn = true;
        }
    }

    /**
     * Method used to draw level complete message
     */
    private void drawLevelComplete() {
        FONT.drawString(COMPLETE_MESSAGE,
                (Window.getWidth() / 2.0 - (FONT.getWidth(COMPLETE_MESSAGE) / 2.0)), MESSAGE_Y_POS);
    }

    /**
     * Method used to draw congratulations message
     */
    private void drawEndString() {
        FONT.drawString(CONGRATULATIONS,
                (Window.getWidth() / 2.0 - (FONT.getWidth(CONGRATULATIONS) / 2.0)), MESSAGE_Y_POS);
    }

    /**
     * Method used to draw lose message
     */
    public void drawLoseString() {
        FONT.drawString(LOSE_MESSAGE,
                (Window.getWidth() / 2.0 - (FONT.getWidth(LOSE_MESSAGE) / 2.0)), MESSAGE_Y_POS);
    }

    /**
     * get bottom boundary
     * @return bottom
     */
    public static double getBottom() {
        return bottom;
    }

    /**
     * get left boundary
     * @return left
     */
    public static double getLeft() {
        return left;
    }

    /**
     * get top boundary
     * @return top
     */
    public static double getTop() {
        return top;
    }

    /**
     * get right boundary
     * @return right
     */
    public static double getRight() {
        return right;
    }

    /**
     * get refresh rate
     * @return REFRESH_RATE
     */
    public static int getRefreshRate() {
        return REFRESH_RATE;
    }
}
