import bagel.Image;
import bagel.util.Point;
import bagel.util.Rectangle;

import java.util.ArrayList;

public abstract class Enemy extends Character{
    protected final static String COOL_DOWN = "COOL_DOWN";
    protected final static String READY_TO_ATTACK = "READY_TO_ATTACK";
    private final static int LOWER_RIGHT = 0;
    private final static int LOWER_LEFT = 1;
    private final static int UPPER_LEFT = 2;
    private final static int UPPER_RIGHT = 3;
    protected int invincibleFrame = 0;

    protected boolean invincible = false;
    protected Rectangle attackSquare;

    protected int relativePosition;
    protected Enemy(double x, double y) {
        super(x, y);
    }

    protected abstract void update(ArrayList<Stationary> stationaries, Sailor sailor);

    protected void changeInvincible() {
        invincible = !invincible;
    }

    protected void attackSailor(Sailor sailor, double attackRange, int coolDownTime, int invincibleTime,
                                Image left, Image right, Image hitLeft, Image hitRight, double projectileSpeed,
                                Image projectile, double damage) {
        // get the relative position of the enemy to the sailor and attackRange of enemy
        setRelativePosition(sailor);
        setAttackSquare(attackRange);

        // attack sailor and change status to COOL_DOWN
        if (status.equals(READY_TO_ATTACK) && attackSquare.intersects(sailor.getMidPoint())) {
            shoot(sailor, projectileSpeed, projectile, damage);
            status = COOL_DOWN;
        }

        // after COOL_DOWN change the status back to READY_TO_ATTACK
        if (status.equals(COOL_DOWN) && attackFrame*1000 / ShadowPirate.getRefreshRate() == coolDownTime) {
            attackFrame = 0;
            status = READY_TO_ATTACK;
        }

        if (status.equals(COOL_DOWN)) {
            attackFrame += 1;
        }

        // invincible status
        if (invincible) {
            invincibleFrame += 1;
            if (direction == LEFT) {
                super.currentImage = hitLeft;
            } else {
                super.currentImage = hitRight;
            }
        }

        // change the status to INVINCIBLE
        if (invincible && (invincibleFrame * 1000 / ShadowPirate.getRefreshRate()) == invincibleTime) {
            invincibleFrame = 0;
            changeInvincible();
            if (direction == LEFT) {
                super.currentImage = left;
            } else {
                super.currentImage = right;
            }
        }
    }

    protected void setAttackSquare(double attack_range) {
        attackSquare = new Rectangle(getTopLeftPoint(attack_range), attack_range, attack_range);
    }

    private Point getTopLeftPoint(double attack_range) {
        return new Point(getMidPoint().x - attack_range/2,
                getMidPoint().y - attack_range/2);
    }


    protected void changeDirection(Image left, Image right) {
        if (direction == LEFT) {
            direction = RIGHT;
            currentImage = right;
        } else if (direction == RIGHT) {
            direction = LEFT;
            currentImage = left;
        } else if (direction == UP) {
            direction = DOWN;
        } else {
            direction = UP;
        }
    }

    protected void checkCollisions(ArrayList<Stationary> stationaries, Image left, Image right){

        Rectangle pirateBox = currentImage.getBoundingBoxAt(new Point(x, y));
        for (Stationary stationary : stationaries) {
            Rectangle stationaryBox = stationary.getBoundingBox();
            if (pirateBox.intersects(stationaryBox)) {
                changeDirection(left, right);
                moveBack();
                break;
            }
        }
        if (y < ShadowPirate.getTop() || y > ShadowPirate.getBottom() ||
                x < ShadowPirate.getLeft() || x > ShadowPirate.getRight()) {
            changeDirection(left, right);
            moveBack();
        }
    }

    protected void moving(double move_speed) {
        if (direction == LEFT) {
            super.setOldPoints();
            super.move(-move_speed, 0);
        } else if (direction == RIGHT) {
            super.setOldPoints();
            super.move(move_speed, 0);
        } else if (direction == UP) {
            super.setOldPoints();
            super.move(0, -move_speed);
        } else {
            super.setOldPoints();
            super.move(0, move_speed);
        }
    }

    protected double getRadian(Sailor sailor) {
        double horizontalSide = Math.abs(sailor.getX() - x);
        double verticalSide = Math.abs(sailor.getY() - y);
        return Math.atan(verticalSide/horizontalSide);
    }

    protected void setRelativePosition(Sailor sailor) {
        if (sailor.getX() <= x && sailor.getY() < y) {
            relativePosition = LOWER_RIGHT;
        } else if (sailor.getX() > x && sailor.getY() <= y) {
            relativePosition = LOWER_LEFT;
        } else if (sailor.getX() >= x && sailor.getY() > y) {
            relativePosition = UPPER_LEFT;
        } else if (sailor.getX() < x && sailor.getY() >= y) {
            relativePosition = UPPER_RIGHT;
        }
    }

    protected void shoot(Sailor sailor, double projectileSpeed, Image projectile, double damage) {
        ShadowPirate.projectiles.add(new Projectile(getMidPoint().x, getMidPoint().y, projectileSpeed, projectile,
                damage, getRadian(sailor), relativePosition, getName()));
    }

    protected abstract String getName();

    protected boolean getInvincibleStatus() {
        return invincible;
    }

    protected double getHealthPoints() {
        return healthPoints;
    }

    protected abstract double getMaxHealthPoints();

}
