import bagel.Image;

import java.util.ArrayList;

public class Blackbeard extends Enemy {
    // blackbeard's attributes
    private final static double MAX_HEALTH_POINTS = 90;
    private final static int COOL_DOWN_TIME = 1500;
    private final static int INVINCIBLE_TIME = 1500;
    private final static double DAMAGE = 20;
    private final static double SPEED_MIN = 0.4;
    private final static double SPEED_MAX = 1.4;
    private final static String NAME = "Blackbeard";
    private final static double ATTACK_RANGE = 200;
    private final static double PROJECTILE_SPEED = 0.8;
    private final double MOVE_SPEED = Math.random() * (SPEED_MAX - SPEED_MIN + 0.01) + SPEED_MIN;

    // blackbeard's image
    private final static Image BLACKBEARD_LEFT = new Image("res/blackbeard/blackbeardLeft.png");
    private final static Image BLACKBEARD_RIGHT = new Image("res/blackbeard/blackbeardRight.png");
    private final static Image BLACKBEARD_HIT_LEFT = new Image("res/blackbeard/blackbeardHitLeft.png");
    private final static Image BLACKBEARD_HIT_RIGHT = new Image("res/blackbeard/blackbeardHitRight.png");
    private final static Image BLACKBEARD_PROJECTILE = new Image("res/blackbeard/blackbeardProjectile.png");

    /**
     * constructor of Blackbeard
     * @param x initial x position
     * @param y initial y position
     */
    public Blackbeard(double x, double y) {
        super(x, y);
        super.direction = (int)(Math.random()*(DOWN-LEFT+1)+LEFT);
        if (direction == LEFT) {
            super.currentImage = BLACKBEARD_LEFT;
        } else {
            super.currentImage = BLACKBEARD_RIGHT;
        }
        status = READY_TO_ATTACK;
        healthPoints = MAX_HEALTH_POINTS;
    }

    /**
     * perform blackbeard's update
     * @param stationaries arraylist of all stationary, used to test collision
     * @param sailor sailor in the game
     */
    public void update(ArrayList<Stationary> stationaries, Sailor sailor) {
        attackSailor(sailor, ATTACK_RANGE, COOL_DOWN_TIME, INVINCIBLE_TIME, BLACKBEARD_LEFT, BLACKBEARD_RIGHT,
                BLACKBEARD_HIT_LEFT, BLACKBEARD_HIT_RIGHT, PROJECTILE_SPEED, BLACKBEARD_PROJECTILE, DAMAGE);
        moving(MOVE_SPEED);
        healthbar.update(getPercentage(MAX_HEALTH_POINTS), x, y);
        checkCollisions(stationaries, BLACKBEARD_LEFT, BLACKBEARD_RIGHT);
    }

    /**
     * get blackbeard's name
     * @return NAME
     */
    @Override
    protected String getName() {
        return NAME;
    }

    /**
     * get maxHealthPoints
     * @return MAX_HEALTH_POINTS
     */
    @Override
    protected double getMaxHealthPoints() {
        return MAX_HEALTH_POINTS;
    }
}