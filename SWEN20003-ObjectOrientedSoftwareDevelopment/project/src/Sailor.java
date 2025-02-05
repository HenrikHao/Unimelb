import bagel.Image;
import bagel.Input;
import bagel.Keys;
import bagel.util.Rectangle;

import java.util.ArrayList;

public class Sailor extends Character {

    // sailor's image
    private final static Image SAILOR_LEFT = new Image("res/sailor/sailorLeft.png");
    private final static Image SAILOR_RIGHT = new Image("res/sailor/sailorRight.png");
    private final static Image SAILOR_HIT_LEFT = new Image("res/sailor/sailorHitLeft.png");
    private final static Image SAILOR_HIT_RIGHT = new Image("res/sailor/sailorHitRight.png");

    // Icon of picked items
    private final static Image ELIXIR_ICON = new Image("res/items/elixirIcon.png");
    private final static Image POTION_ICON = new Image("res/items/potionIcon.png");
    private final static Image SWORD_ICON = new Image("res/items/swordIcon.png");

    // sailor's state
    private final static String ATTACK = "ATTACK";
    private final static String IDLE = "IDLE";
    private final static String COOL_DOWN = "COOL_DOWN";

    // icon position
    private int iconY = 50;
    private final static int ICON_X = 10;
    private final static int ICON_OFFSET = 40;

    // arraylist of picked items
    ArrayList<Image> pickedItems = new ArrayList<>();

    // attack_time and cool_down_time
    private final static int ATTACK_TIME = 1000;
    private final static int COOL_DOWN_TIME = 2000;

    // LADDER POSITION of level0
    private final static int LADDER_X = 990;
    private final static int LADDER_Y = 630;

    private double maxHealthPoints = 100;
    private int damagePoints = 15;
    private final static double MOVE_SPEED = 1;

    private boolean findTreasure = false;

    /**
     * constructor of sailor
     * @param startX initial x-position
     * @param startY initial y-position
     */
    public Sailor(double startX, double startY) {
        super(startX, startY);
        super.currentImage = SAILOR_RIGHT;
        super.direction = RIGHT;
        status = IDLE;
        healthPoints = maxHealthPoints;
    }

    /**
     * Method that performs state update
     */
    public void update(Input input, ArrayList<Stationary> stationaries, ArrayList<Enemy> enemies){
        // attack enemy
        if (status.equals(ATTACK)) {
            attackFrame += 1;
            for (Enemy enemy: enemies) {
                if (!enemy.getInvincibleStatus()) {
                    if (attack(getBoundingBox(), enemy.getBoundingBox())) {
                        enemy.getAttacked(damagePoints);
                        enemy.changeInvincible();
                        System.out.format("Sailor inflicts %d damage points on %s. %s's current health: %d/%d\n",
                                damagePoints, enemy.getName(), enemy.getName(), (int)enemy.getHealthPoints(),
                                (int)enemy.getMaxHealthPoints());
                    }
                }
            }
        }

        // change the status to COOL_DOWN
        if (status.equals(ATTACK) && (attackFrame*1000 / ShadowPirate.getRefreshRate()) == ATTACK_TIME) {
            attackFrame = 0;
            status = COOL_DOWN;
            if (direction == RIGHT) {
                currentImage = SAILOR_RIGHT;
            } else{
                currentImage = SAILOR_LEFT;
            }
        }

        if (status.equals(COOL_DOWN)) {
            attackFrame += 1;
        }

        // change the status to IDLE
        if (status.equals(COOL_DOWN) && (attackFrame*1000 / ShadowPirate.getRefreshRate()) == COOL_DOWN_TIME) {
            attackFrame = 0;
            status = IDLE;
        }

        // moving of the sailor
        if (input.isDown(Keys.UP)) {
            setOldPoints();
            move(0, -MOVE_SPEED);
        } else if (input.isDown(Keys.DOWN)) {
            setOldPoints();
            move(0, MOVE_SPEED);
        } else if (input.isDown(Keys.LEFT)) {
            setOldPoints();
            move(-MOVE_SPEED, 0);
            direction = LEFT;
            if (status.equals(ATTACK)) {
                super.currentImage = SAILOR_HIT_LEFT;
            } else {
                super.currentImage = SAILOR_LEFT;
            }
        } else if (input.isDown(Keys.RIGHT)) {
            setOldPoints();
            move(MOVE_SPEED, 0);
            direction = RIGHT;
            if (status.equals(ATTACK)) {
                super.currentImage = SAILOR_HIT_RIGHT;
            } else {
                super.currentImage = SAILOR_RIGHT;
            }
        }

        // change the status to ATTACK
        if (input.wasPressed(Keys.S) && status.equals(IDLE)) {
            status = ATTACK;
            if (direction == RIGHT) {
                currentImage = SAILOR_HIT_RIGHT;
            } else {
                currentImage = SAILOR_HIT_LEFT;
            }
        }

        // test if sailor reaches the boundary
        if (y < ShadowPirate.getTop() || y > ShadowPirate.getBottom() ||
                x < ShadowPirate.getLeft() || x > ShadowPirate.getRight()) {
            moveBack();
        }

        // check collisions with the stationary
        checkCollisions(stationaries);
        healthbar.render(getPercentage(maxHealthPoints));

        // draw icon if pick items
        for (Image pickedItem: pickedItems) {
            pickedItem.drawFromTopLeft(ICON_X, iconY);
            iconY += ICON_OFFSET;
        }

        // reset icon position
        iconY = 50;
    }

    /**
     * method used to test collisions with stationary
     * @param stationaries arraylist of stationary
     */
    private void checkCollisions(ArrayList<Stationary> stationaries){
        // check collisions and print log
        Rectangle sailorBox = getBoundingBox();
        for (Stationary stationary : stationaries) {
            Rectangle stationaryBox = stationary.getBoundingBox();
            if (sailorBox.intersects(stationaryBox)) {

                // moveBack if collides with BLOCK
                if (stationary.getName().equals("BLOCK")) {
                    moveBack();
                }

                // moveBack and get attacked if collides with BOMB and print the log
                if (stationary.getName().equals("BOMB")) {
                    moveBack();
                    if (!stationary.exploded()) {
                        stationary.explode();
                        healthPoints -= stationary.getDamage();
                        System.out.format("Bomb inflicts 10 damage points on Sailor. Sailor's current health: %d/%d\n"
                        , (int)healthPoints, (int)maxHealthPoints);
                    }
                }

                // if collides with SWORD
                if (stationary.getName().equals("SWORD")) {
                    damagePoints += stationary.getIncreasePoints();
                    stationary.eat();
                    pickedItems.add(SWORD_ICON);
                    System.out.format("Sailor finds Sword. Sailor’s damage points increased to %d\n", damagePoints);
                }

                // if collides with POTION
                if (stationary.getName().equals("POTION")) {
                    healthPoints += stationary.getIncreasePoints();
                    if (healthPoints > maxHealthPoints) {
                        healthPoints = maxHealthPoints;
                    }
                    System.out.format("Sailor finds Potion. Sailor’s current health: %d/%d\n",
                            (int)healthPoints, (int)maxHealthPoints);
                    stationary.eat();
                    pickedItems.add(POTION_ICON);
                }

                // if collides with ELIXIR
                if (stationary.getName().equals("ELIXIR")) {
                    maxHealthPoints += stationary.getIncreasePoints();
                    healthPoints = maxHealthPoints;
                    System.out.format("Sailor finds Potion. Sailor’s current health: %d/%d\n",
                            (int)healthPoints, (int)maxHealthPoints);
                    stationary.eat();
                    pickedItems.add(ELIXIR_ICON);
                }

                // if collides with TREASURE
                if (stationary.getName().equals("TREASURE")) {
                    stationary.eat();
                    findTreasure = true;
                }
            }
        }
    }

    public boolean attack(Rectangle sailorBox, Rectangle enemyBox) {
        return sailorBox.intersects(enemyBox);
    }

    /**
     * get x-pos of sailor
     * @return x
     */
    public double getX() {
        return x;
    }

    /**
     * get y-pos of sailor
     * @return y
     */
    public double getY() {
        return y;
    }

    /**
     * get sailor's maxHealthPoints
     * @return maxHealthPoints
     */
    public double getMaxHealthPoints() {
        return maxHealthPoints;
    }

    /**
     * get current healthPoints
     * @return healthPoints
     */
    public double getHealthPoints() {return healthPoints;}

    /**
     * get findTreasure
     * @return findTreasure
     */
    public boolean getFindTreasure() {
        return findTreasure;
    }

    /**
     * test if the sailor reached the ladder
     * @return true if reached otherwise false
     */
    public boolean reachLadder() {
        return (x >= LADDER_X) && (y > LADDER_Y);
    }



}
