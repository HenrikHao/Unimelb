package thrones.game.players;

import java.util.Optional;

import ch.aplu.jcardgame.Card;
import ch.aplu.jcardgame.CardAdapter;
import ch.aplu.jcardgame.Hand;
import thrones.game.GameOfThrones;
import thrones.game.utils.CardUtil.Suit;

public class HumanPlayer extends AbstractPlayer {

    private Optional<Card> selected;
    private int selectedPile;

    public HumanPlayer(int playerId, String playerType) {
        super(playerId, playerType);
    }

    private void setupInteraction() {
        Hand currentHand = getHand();
        currentHand.addCardListener(new CardAdapter() {
            public void leftDoubleClicked(Card card) {
                selected = Optional.of(card);
                currentHand.setTouchEnabled(false);
            }

            public void rightClicked(Card card) {
                selected = Optional.empty(); // Don't care which card we right-clicked for player to pass
                currentHand.setTouchEnabled(false);
            }
        });
    }

    private void setupPileInteraction(Hand[] piles) {
        for (int i=0; i<2; i++) {
            final Hand currentPile = piles[i];
            final int pileIndex = i;
            piles[i].addCardListener(new CardAdapter() {
                public void leftClicked(Card card) {
                    selectedPile = pileIndex;
                    currentPile.setTouchEnabled(false);
                }
            });
        }
    }

    @Override
    public Optional<Card> selectACard(Hand[] piles) {
        setupInteraction();
        if (getHand().isEmpty()) {
            selected = Optional.empty();
        } else {
            selected = null;
            getHand().setTouchEnabled(true);
            do {
                if (selected == null) {
                    GameOfThrones.delay(100);
                    continue;
                }
                Suit suit = selected.isPresent() ? (Suit) selected.get().getSuit() : null;
                if (isCharacter(piles) && suit != null && suit.isCharacter() || // If we want character, can't pass and suit
                                                                         // must be right
                        !isCharacter(piles) && (suit == null || !suit.isCharacter())) { // If we don't want character, can pass
                                                                                 // or suit must not be character
                    // if (suit != null && suit.isCharacter() == isCharacter) {
                    break;
                } else {
                    selected = null;
                    getHand().setTouchEnabled(true);
                }
                GameOfThrones.delay(100);
            } while (true);
        }
        return selected;
    }


    @Override
    public int selectAPile(Card card, Hand[] piles) {
        selectedPile = GameOfThrones.NON_SELECTION_VALUE;
        setupPileInteraction(piles);
        for (Hand pile : piles) {
            pile.setTouchEnabled(true);
        }
        while (selectedPile == GameOfThrones.NON_SELECTION_VALUE) {
            GameOfThrones.delay(100);
        }
        for (Hand pile : piles) {
            pile.setTouchEnabled(false);
        }

        // check if the rule is broken
        // only check the base rule since a human player can play any card
        if (super.isValid(card, piles, selectedPile)) {
            return selectedPile;
        }
        System.out.println("Play is illegal, pass.");
        return GameOfThrones.NON_SELECTION_VALUE;
    }
    
}
