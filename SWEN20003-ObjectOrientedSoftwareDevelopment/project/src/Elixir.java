import bagel.Image;
import bagel.util.Point;
import bagel.util.Rectangle;

public class Elixir extends Stationary{

    // image and attributes of Elixir
    private final static Image ELIXIR_IMAGE = new Image("res/items/elixir.png");
    private final static String NAME = "ELIXIR";
    private final static int INCREASE_POINTS = 35;

    /**
     * constructor of Elixir
     * @param x initial x-position of Elixir
     * @param y initial y-position of Elixir
     */
    public Elixir(double x, double y) {
        super(x, y);
    }

    /**
     * draw elixir image
     */
    public void render() {
        ELIXIR_IMAGE.drawFromTopLeft(x,y);
    }

    /**
     * update Elixir's state
     */
    @Override
    public void update() {
        render();
    }

    /**
     * get Elixir's name
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
     * get Elixir's bounding box
     * @return elixir's bounding box
     */
    @Override
    public Rectangle getBoundingBox() {
        return ELIXIR_IMAGE.getBoundingBoxAt(new Point(x, y));
    }
}
