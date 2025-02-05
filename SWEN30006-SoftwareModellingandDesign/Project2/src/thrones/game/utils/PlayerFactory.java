package thrones.game.utils;

import thrones.game.players.*;

public final class PlayerFactory {
    
    private static PlayerFactory instance = null;

    private AbstractPlayer player;

    private PlayerFactory() {}

    public static PlayerFactory getInstance() {
        if (instance == null) {
            instance = new PlayerFactory();
        }
        return instance;
    }

    public AbstractPlayer createPlayer(int playerId, String type) {
        return switch (type) {
            case "human" -> new HumanPlayer(playerId, type);
            case "random" -> new RandomPlayer(playerId, type);
            case "smart" -> new SmartPlayer(playerId, type);
            default -> new SimplePlayer(playerId, type);
        };
    }

}
