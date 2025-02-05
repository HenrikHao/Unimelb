package thrones.game.players;

import java.util.Collection;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import ch.aplu.jcardgame.Card;
import ch.aplu.jcardgame.Hand;
import thrones.game.utils.CardUtil.Suit;


public abstract class AbstractPlayer {

    private int playerId;
    private Hand hand;
    private String playerType;
    private int score;

    public AbstractPlayer(int playerId, String playerType) {
        this.playerId = playerId;
        this.playerType = playerType;
        this.score = 0;
        this.hand = null;
    }


    public abstract Optional<Card> selectACard(Hand[] piles);

    public abstract int selectAPile(Card card, Hand[] piles);

    public boolean isValid(Card card, Hand[] piles, int selectedPile) {

        int numOfCardsPile = piles[selectedPile].getNumberOfCards();

        if (numOfCardsPile == 0 && card.getSuit() != Suit.HEARTS) {
            return false;
        }

        if (numOfCardsPile > 0 && card.getSuit() == Suit.HEARTS) {
            return false;
        }

        return numOfCardsPile != 1 || card.getSuit() != Suit.DIAMONDS;
    }


    public boolean isCharacter(Hand[] piles) {  
        // if either pile is missing a character, return true
        return piles[0].getNumberOfCardsWithSuit(Suit.HEARTS) == 0 ||
                piles[1].getNumberOfCardsWithSuit(Suit.HEARTS) == 0;
    }


    @Override
    public String toString() {
        return String.format(
            "Player %d is a %s player with current hand={%s}, current score=%d\n", 
            playerId, playerType, hand, score);
    }

    public int getPlayerId() {
        return playerId;
    }

    public Hand getHand() {
        return hand;
    }

    public void setHand(Hand hand) {
        this.hand = hand;
    }

    public int getScore() {
        return score;
    }
    public void setScore(int score) {
        this.score = score;
    }
    
}
