import bagel.Image;
import bagel.util.Point;
import bagel.util.Rectangle;

public class Character implements Movable, Attackable{

    // common attributes of characters
    protected Image currentImage;
    protected double x;
    protected double y;
    protected double oldX;
    protected double oldY;
    protected double healthPoints;
    protected final Healthbar healthbar = new Healthbar();
    protected String status;
    protected int attackFrame = 0;
    protected int direction;

    // direction
    protected final static int LEFT = 0;
    protected final static int RIGHT = 1;
    protected final static int UP = 2;
    protected final static int DOWN = 3;

    protected Character(double x, double y) {
        this.x = x;
        this.y = y;
    }

    public void move(double xMove, double yMove) {
        x += xMove;
        y += yMove;
    }

    // store old coordinates every time the character moves
    protected void setOldPoints(){
        oldX = x;
        oldY = y;
    }

    // reset the coordinates
    protected void moveBack(){
        x = oldX;
        y = oldY;
    }

    // draw character
    protected void render() {
        currentImage.drawFromTopLeft(x, y);
    }

    // decrease healthpoints is get attacked
    protected void getAttacked(double damage) {
        healthPoints -= damage;
    }

    // get the bounding box of character
    protected Rectangle getBoundingBox() {
        return currentImage.getBoundingBoxAt(new Point(x, y));
    }

    // get the middle point of this character
    protected Point getMidPoint() {
        return new Point(x - currentImage.getWidth()/2, y - currentImage.getHeight()/2);
    }

    // get healhpoints percentage
    protected int getPercentage(double maxHealthPoints) {
        return (int) Math.round((healthPoints / maxHealthPoints) * 100);
    }

    @Override
    public boolean attack(Rectangle attackRec, Rectangle damageRec) {
        return false;
    }
}
