package src;

import src.game.TetrisEasy;
import src.game.TetrisHard;
import src.game.TetrisMedian;

import java.util.Properties;

public class Tetris {
    AbstractTetris game;
    TetrisGameCallback gameCallback;
    Properties properties;

    public Tetris(TetrisGameCallback gameCallback, Properties properties) {
        this.gameCallback = gameCallback;
        this.properties = properties;
        String difficulty = properties.getProperty("difficulty", "madness");
        switch (difficulty) {
        case "easy" -> game = new TetrisEasy(gameCallback, properties);
        case "medium" -> game = new TetrisMedian(gameCallback, properties);
        default -> game = new TetrisHard(gameCallback, properties);
        }
    }

    public void setVisible(boolean b) {
        this.game.setVisible(true);
    }
}
