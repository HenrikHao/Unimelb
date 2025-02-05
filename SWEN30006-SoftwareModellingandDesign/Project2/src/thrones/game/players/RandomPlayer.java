package thrones.game.players;

import java.util.Optional;

import ch.aplu.jcardgame.Card;
import ch.aplu.jcardgame.Hand;
import thrones.game.GameOfThrones;
import thrones.game.utils.RandomUtil;
import thrones.game.utils.CardUtil.Suit;


public class RandomPlayer extends AbstractPlayer {

    private RandomUtil random = RandomUtil.getInstance();

    public RandomPlayer(int playerId, String playerType) {
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

        for (int i=0; i<999; i++) {
            Card card = random.randomCard(getHand());
            if (card.getSuit() != Suit.HEARTS) {
                return Optional.of(card);
            }
        }
        return Optional.of(random.randomCard(getHand()));
    }

    @Override
    public int selectAPile(Card card, Hand[] piles) {
        int selectedPile = random.nextInt(2);
        if (isValid(card, piles, selectedPile)) {
            return selectedPile;
        }
        // invalid move return -1;
        return GameOfThrones.NON_SELECTION_VALUE;
    }
    
}
