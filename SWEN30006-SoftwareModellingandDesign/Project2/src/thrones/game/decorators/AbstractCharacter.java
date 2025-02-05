package thrones.game.decorators;

import ch.aplu.jcardgame.Card;
import thrones.game.utils.CardUtil.Rank;
import thrones.game.utils.CardUtil.Suit;

public interface AbstractCharacter {

    public int getAttack();

    public int getDefence();

    public Card getCurrCard();


    public default Rank getRank(Card card) {
        return (Rank) card.getRank();
    }

    public default Suit getSuit(Card card) {
        return (Suit) card.getSuit();
    }
    
}
