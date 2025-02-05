import bagel.DrawOptions;
import bagel.Font;
import bagel.util.Colour;

public class Healthbar {

    // different colors used on drawing healthpoints percentage and position
    private final static double HEALTHBAR_X = 10;
    private final static double HEALTHBAR_Y = 25;
    private final static double GREEN_R = 0;
    private final static double GREEN_G = 0.8;
    private final static double GREEN_B = 0.2;
    private final static double ORANGE_R = 0.9;
    private final static double ORANGE_G = 0.6;
    private final static double ORANGE_B = 0;
    private final static double RED_R = 1;
    private final static double RED_G = 0;
    private final static double RED_B = 0;
    private final Colour green = new Colour(GREEN_R, GREEN_G, GREEN_B);
    private final Colour orange = new Colour(ORANGE_R, ORANGE_G, ORANGE_B);
    private final Colour red = new Colour(RED_R, RED_G, RED_B);
    private final static int PIRATE_FONT_SIZE = 15;
    private final static int SAILOR_FONT_SIZE = 30;

    // Font used on drawing healthBar
    private final Font PIRATE_FONT = new Font("res/wheaton.otf", PIRATE_FONT_SIZE);
    private final Font SAILOR_FONT = new Font("res/wheaton.otf", SAILOR_FONT_SIZE);

    // Constructor of HealthBar
    public Healthbar() {}

    /**
     * set color
     * @param percentage character's healthPoints percentage
     * @return drawoption
     */
    private DrawOptions getColor(int percentage) {
        DrawOptions drawoption;
        if (percentage > 65) {
            drawoption = new DrawOptions().setBlendColour(green);
        } else if (35 < percentage) {
            drawoption = new DrawOptions().setBlendColour(orange);
        } else {
            drawoption = new DrawOptions().setBlendColour(red);
        }
        return drawoption;
    }


    /**
     * update each enemy's healthbar
     * @param percentage enemy's percentage
     * @param x enemy's x position
     * @param y enemy's y position
     */
    public void update(int percentage, double x, double y) {
        DrawOptions drawoption = getColor(percentage);
        PIRATE_FONT.drawString(percentage + "%", x, y - 6, drawoption);
    }

    /**
     * render sailor's healthbar
     * @param percentage sailor's healthpercentage
     */
    public void render(int percentage) {
        DrawOptions drawoption = getColor(percentage);
        SAILOR_FONT.drawString(percentage + "%", HEALTHBAR_X, HEALTHBAR_Y, drawoption);
    }
}
