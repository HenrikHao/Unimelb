import bagel.Image;
import bagel.util.Point;
import bagel.util.Rectangle;

public class Sword extends Stationary{

    // sword's image and attributes
    private final static Image SWORD_IMAGE = new Image("res/items/sword.png");
    private final static String NAME = "SWORD";
    private final static int INCREASE_POINTS = 15;

    /**
     * constructor of Sword
     * @param x initial x-position
     * @param y initial y-position
     */
    public Sword(double x, double y) {
        super(x, y);
    }

    /**
     * draw Sword Image
     */
    public void render() {
        SWORD_IMAGE.drawFromTopLeft(x,y);
    }

    /**
     * change sword's state in the game
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
     * get the bounding box of the sword
     * @return SWORD_IMAGE's bounding rectangle
     */
    @Override
    public Rectangle getBoundingBox() {
        return SWORD_IMAGE.getBoundingBoxAt(new Point(x, y));
    }
}
