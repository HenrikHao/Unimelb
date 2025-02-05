import bagel.util.Rectangle;

public abstract class Stationary {
    protected double x;
    protected double y;
    protected boolean isEaten = false;
    protected Stationary(double x, double y) {
        this.x = x;
        this.y = y;
    }
    protected void render() { }

    protected void update() {
        render();
    }

    protected abstract String getName();

    protected abstract Rectangle getBoundingBox();

    protected void explode() {}

    protected boolean exploded() {return false;}

    protected boolean isEaten() {return isEaten;}

    protected int getDamage() {return 0;}

    protected int getIncreasePoints() {return 0;}

    protected void eat() {isEaten = true;}
}
