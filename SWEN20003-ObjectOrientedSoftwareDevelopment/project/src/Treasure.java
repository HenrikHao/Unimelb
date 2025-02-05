import bagel.Image;
import bagel.util.Point;
import bagel.util.Rectangle;

public class Treasure extends Stationary{
    // Treasure's attributes and image
    private final static Image TREASURE_IMAGE = new Image("res/treasure.png");
    private final static String NAME = "TREASURE";

    /**
     * constructor of Treasure
     * @param x initial x-position
     * @param y initial y-position
     */
    public Treasure(double x, double y) {
        super(x, y);
    }

    /**
     * draw TREASURE Image
     */
    public void render() {
        TREASURE_IMAGE.drawFromTopLeft(x,y);
    }

    /**
     * update treasure's state in the game
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
     * get the bounding box of the treasure
     * @return treasure's bounding rectangle
     */
    @Override
    public Rectangle getBoundingBox() {
        return TREASURE_IMAGE.getBoundingBoxAt(new Point(x, y));
    }
}
