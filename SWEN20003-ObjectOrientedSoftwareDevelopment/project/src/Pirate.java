import bagel.Image;
import java.util.ArrayList;

public class Pirate extends Enemy{
    // Pirate's attributes and Image
    private final static double MAX_HEALTH_POINTS = 45;
    private final static int COOL_DOWN_TIME = 3000;
    private final static int INVINCIBLE_TIME = 1500;
    private final static double DAMAGE = 10;
    private final static double SPEED_MIN = 0.2;
    private final static double SPEED_MAX = 0.7;
    private final static Image PIRATE_LEFT = new Image("res/pirate/pirateLeft.png");
    private final static Image PIRATE_RIGHT = new Image("res/pirate/pirateRight.png");
    private final static Image PIRATE_HIT_LEFT = new Image("res/pirate/pirateHitLeft.png");
    private final static Image PIRATE_HIT_RIGHT = new Image("res/pirate/pirateHitRight.png");
    private final static Image PIRATE_PROJECTILE = new Image("res/pirate/pirateProjectile.png");
    private final static String NAME = "Pirate";
    private final static double ATTACK_RANGE = 100;
    private final static double PROJECTILE_SPEED = 0.4;
    private final double MOVE_SPEED = Math.random()*(SPEED_MAX-SPEED_MIN+0.01)+SPEED_MIN;


    /**
     * constructor of Pirate
     * @param x initial x-position of Pirate
     * @param y initial y-position of Pirate
     */
    public Pirate(double x, double y) {
        super(x, y);
        super.direction = (int)(Math.random()*(DOWN-LEFT+1)+LEFT);
        if (super.direction == 0) {
            super.currentImage = PIRATE_LEFT;
        } else {
            super.currentImage = PIRATE_RIGHT;
        }

        status = READY_TO_ATTACK;
        healthPoints = MAX_HEALTH_POINTS;
    }

    /**
     * update Pirate's state
     * @param stationaries arraylist of stationary, used to test collision
     * @param sailor sailor in the game
     */
    public void update(ArrayList<Stationary> stationaries, Sailor sailor) {
        attackSailor(sailor, ATTACK_RANGE, COOL_DOWN_TIME, INVINCIBLE_TIME, PIRATE_LEFT, PIRATE_RIGHT,
                PIRATE_HIT_LEFT, PIRATE_HIT_RIGHT, PROJECTILE_SPEED, PIRATE_PROJECTILE, DAMAGE);
        moving(MOVE_SPEED);
        healthbar.update(getPercentage(MAX_HEALTH_POINTS), x, y);
        checkCollisions(stationaries, PIRATE_LEFT, PIRATE_RIGHT);
    }

    /**
     * getName
     * @return NAME
     */
    @Override
    public String getName() {
        return NAME;
    }

    /**
     * getMaxHealthPoints
     * @return MAX_HEALTH_POINTS
     */
    @Override
    protected double getMaxHealthPoints() {
        return MAX_HEALTH_POINTS;
    }
}
