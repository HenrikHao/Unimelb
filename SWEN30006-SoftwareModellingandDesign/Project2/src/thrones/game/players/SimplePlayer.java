package thrones.game.players;

import java.util.Optional;

import ch.aplu.jcardgame.Card;
import ch.aplu.jcardgame.Hand;
import thrones.game.GameOfThrones;
import thrones.game.utils.RandomUtil;
import thrones.game.utils.CardUtil.Suit;

public class SimplePlayer extends AbstractPlayer {

    private RandomUtil random = RandomUtil.getInstance();

    public SimplePlayer(int playerId, String playerType) {
        super(playerId, playerType);
    }

    @Override
    public Optional<Card> selectACard(Hand[] piles) {
        if (getHand().getNumberOfCards() == 0) {
            return Optional.empty();
        }
        if (isCharacter(piles)) {
            return Optional.of(getHand()
                .getCardsWithSuit(Suit.HEARTS)
                .get(random.nextInt(getHand().getNumberOfCardsWithSuit(Suit.HEARTS))));
        }
        return Optional.of(random.randomCard(getHand()));
    }

    @Override
    public int selectAPile(Card card, Hand[] piles) {
        int selectedPile = random.nextInt(2);
        if (this.isValid(card, piles, selectedPile)) {
            return selectedPile;
        }
        // invalid move return -1;
        return GameOfThrones.NON_SELECTION_VALUE;
    }

    // Override super method to check the additional constraint
    @Override
    public boolean isValid(Card card, Hand[] piles, int selectedPile) {
        // if not satisfy the base rule, not proceed
        if (!super.isValid(card, piles, selectedPile)) {
            return false;
        }
        // check if the current move will help the opponent
        // ensure attack/defence card is never to be placed in the opponent's pile
        // and a magic card not in self's pile
        Suit suit = (Suit) card.getSuit();
        if ((suit.isAttack() || suit.isDefence()) && selectedPile != getPlayerId() % 2) {
            return false;
        }
        if (suit.isMagic() && selectedPile == getPlayerId() % 2) {
            return false;
        }
        return true;
    }
    
}
