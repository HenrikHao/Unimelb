import bagel.Image;
import bagel.util.Point;
import bagel.util.Rectangle;

public class Potion extends Stationary {
    // attributes and image of Potion
    private final static Image POTION_IMAGE = new Image("res/items/Potion.png");
    private final static String NAME = "POTION";
    private final static int INCREASE_POINTS = 25;

    /**
     * constructor of Potion
     * @param x initial x-position
     * @param y initial y-position
     */
    public Potion(double x, double y) {
        super(x, y);
    }

    /**
     * draw potion image
     */
    public void render() {
        POTION_IMAGE.drawFromTopLeft(x,y);
    }

    /**
     * update potion's state
     */
    @Override
    public void update() {
        render();
    }

    /**
     * get Name
     * @return NAME
     */
    @Override
    public String getName() {
        return NAME;
    }

    /**
     * get IncreasePoints
     * @return INCREASE_POINTS
     */
    public int getIncreasePoints() {
        return INCREASE_POINTS;
    }

    /**
     * get Potion Image's bounding box
     * @return potion Image's bounding rectangle
     */
    @Override
    public Rectangle getBoundingBox() {
        return POTION_IMAGE.getBoundingBoxAt(new Point(x, y));
    }
}
