import bagel.Image;
import bagel.util.Point;
import bagel.util.Rectangle;

public class Block extends Stationary {

    // Block's image and name
    private final static Image BLOCK = new Image("res/block.png");
    private final static String NAME = "BLOCK";

    /**
     * constructor
     * @param x initial x-position of block
     * @param y initial y-position of block
     */
    public Block(double x, double y) {
        super(x, y);
    }

    /**
     * draw block
     */
    @Override
    public void render() {
        BLOCK.drawFromTopLeft(x, y);
    }

    /**
     * update block's state
     */
    @Override
    public void update() {
        render();
    }

    /**
     * get block's name
     * @return NAME
     */
    public String getName() {
        return NAME;
    }

    /**
     * get block's bounding box
     * @return block's Rectangle
     */
    public Rectangle getBoundingBox(){
        return BLOCK.getBoundingBoxAt(new Point(x, y));
    }


}
