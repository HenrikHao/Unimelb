import bagel.DrawOptions;
import bagel.Image;
import bagel.util.Rectangle;

public class Projectile implements Movable, Attackable{

    // projectile's attribute
    private final Image currentImage;
    private final double damage;
    private boolean attacked = false;
    private double x;
    private double y;
    private final double speedX;
    private final double speedY;
    private final static int UPPER_LEFT = 0;
    private final static int UPPER_RIGHT = 1;
    private final static int LOWER_RIGHT = 2;
    private final static int LOWER_LEFT = 3;
    private final DrawOptions drawOptions;
    private final int direction;
    private final String name;

    /**
     * constructor of projectile
     * @param x initial x-position
     * @param y initial y-position
     * @param speed speed of projectile
     * @param projectileImage Image of projectile, depends on the shooter
     * @param damage damage, depends on the shooter
     * @param radian rotate radians
     * @param direction projectile's moving direction
     * @param name shooter's name
     */
    public Projectile(double x, double y, double speed, Image projectileImage, double damage, double radian,
                      int direction, String name) {
        this.x = x;
        this.y = y;
        this.currentImage = projectileImage;
        this.direction = direction;
        this.damage = damage;
        this.name = name;
        if (direction == UPPER_LEFT || direction == LOWER_RIGHT) {
            drawOptions = new DrawOptions().setRotation(radian);
        } else {
            drawOptions = new DrawOptions().setRotation(-radian);
        }
        speedX = speed * Math.cos(radian);
        speedY = speed * Math.sin(radian);
    }

    @Override
    public void move(double xMove, double yMove) {
        x += xMove;
        y += yMove;
    }

    /**
     * update projectile's state in the game
     * @param sailor sailor in the game
     */
    public void update(Sailor sailor) {

        // if attacking the sailor, print out the log
        if (attack(getBoundingBox(), sailor.getBoundingBox())) {
            attacked = true;
            sailor.getAttacked(damage);
            System.out.format("%s inflicts %d damage points on Sailor. Sailor's current health: %d/%d\n",
                    name, (int)damage, (int)sailor.getHealthPoints(), (int)sailor.getMaxHealthPoints());
        }

        // moving
        if (direction == UPPER_LEFT) {
            move(-speedX, -speedY);
        } else if (direction == UPPER_RIGHT) {
            move(speedX, -speedY);
        } else if (direction == LOWER_RIGHT) {
            move(speedX, speedY);
        } else if (direction == LOWER_LEFT) {
            move(-speedX, speedY);
        }
    }

    public boolean attack(Rectangle projectileBox, Rectangle sailorBox) {
        return projectileBox.intersects(sailorBox);
    }

    /**
     * method used to test if the projectile out of bound
     * @return true if out of bound, otherwise false
     */
    public boolean stopRender() {
        return y < ShadowPirate.getTop() || y > ShadowPirate.getBottom() ||
                x < ShadowPirate.getLeft() || x > ShadowPirate.getRight() || attacked;
    }

    private Rectangle getBoundingBox() {
        return new Rectangle(x, y, 1, 1);
    }

    /**
     * draw projectile
     */
    public void render() {
        currentImage.drawFromTopLeft(x, y, drawOptions);
    }
}
