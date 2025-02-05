package thrones.game.decorators;

import ch.aplu.jcardgame.Card;
import ch.aplu.jcardgame.Hand;
import thrones.game.utils.CardUtil;

public class MagicDecorator extends CharacterDecorator{

    public MagicDecorator(AbstractCharacter character, Card card, Card lastCard) {
        super(character, card, lastCard);
    }

    public int getAttack() {

        CardUtil.Rank rank = getRank(lastPlayedCard);

        CardUtil.Rank lastRank = getRank(lastCard);
        CardUtil.Suit lastSuit = getSuit(lastCard);

        int addedAttack = rank.getRankValue();
        if (lastSuit.equals(CardUtil.Suit.CLUBS)) {
            if (lastRank.equals(rank)) {
                return Math.max(0, super.getAttack() - 2 * addedAttack);
            }
            return Math.max(0, super.getAttack() - addedAttack);
        }
        return super.getAttack();
    }

    public int getDefence() {
        CardUtil.Rank rank = getRank(lastPlayedCard);
        CardUtil.Suit suit = getSuit(lastPlayedCard);

        CardUtil.Rank lastRank = getRank(lastCard);
        CardUtil.Suit lastSuit = getSuit(lastCard);

        int addedDefence = rank.getRankValue();
        if (lastSuit.equals(CardUtil.Suit.SPADES)) {

            if (lastRank.equals(rank)) {
                return Math.max(0, super.getDefence() - 2 * addedDefence);
            }
            return Math.max(0, super.getDefence() - addedDefence);
        }
        return super.getDefence();
    }

}
