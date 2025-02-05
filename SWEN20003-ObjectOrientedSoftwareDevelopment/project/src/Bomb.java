import bagel.Image;
import bagel.util.Point;
import bagel.util.Rectangle;

public class Bomb extends Stationary{
    // Bomb's Image and attributes
    private final static Image BOMB = new Image("res/bomb.png");
    private final static Image EXPLOSION = new Image("res/explosion.png");
    private final static String NAME = "BOMB";
    private final static int DAMAGE = 10;
    private boolean exploded = false;
    private final static int EXPLOSION_TIME = 500;
    private Image currentImage;

    // Bomb's explosion frame
    private int frame = 0;

    /**
     * constructor of Bomb
     * @param x initial x-position of bomb
     * @param y initial y-position of bomb
     */
    public Bomb(double x, double y) {
        super(x, y);
        this.currentImage = BOMB;
    }

    /**
     * draw bomb image
     */
    @Override
    public void render() {
        currentImage.drawFromTopLeft(x, y);
    }

    /**
     * update bomb's state if exploded
     */
    @Override
    public void update() {
        if (exploded) {
            frame += 1;
        }
        if (frame * 1000 / ShadowPirate.getRefreshRate() == EXPLOSION_TIME) {
            isEaten = true;
        }
        render();
    }

    /**
     * method used to perform bomb's explosion
     */
    @Override
    public void explode() {
        currentImage = EXPLOSION;
        exploded = true;
    }

    /**
     * method return if this bomb is collided
     * @return isEaten
     */
    @Override
    public boolean isEaten() {
        return isEaten;
    }

    /**
     * method return if this bomb is exploded
     * @return exploded
     */
    @Override
    public boolean exploded() {
        return exploded;
    }

    /**
     * get Name
     * @return NAME
     */
    public String getName() {
        return NAME;
    }

    /**
     * get bomb's damage
     * @return DAMAGE
     */
    @Override
    public int getDamage() {
        return DAMAGE;
    }

    /**
     * get bomb's bounding box
     * @return bomb's bounding rectangle
     */
    @Override
    public Rectangle getBoundingBox() {
        return currentImage.getBoundingBoxAt(new Point(x, y));
    }

}
