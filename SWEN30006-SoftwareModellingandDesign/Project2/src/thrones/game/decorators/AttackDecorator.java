package thrones.game.decorators;

import ch.aplu.jcardgame.Card;
import ch.aplu.jcardgame.Hand;
import thrones.game.utils.CardUtil.Rank;

public class AttackDecorator extends CharacterDecorator {

    public AttackDecorator(AbstractCharacter character, Card card, Card lastCard) {
        super(character, card, lastCard);
    }

    @Override
    public int getAttack() {

        Rank rank = getRank(lastPlayedCard);

        int addedAttack = rank.getRankValue();
        Rank lastRank = getRank(super.getCurrCard());
        if (lastRank.equals(rank)) {
            addedAttack *= 2;
        }

        return super.getAttack() + addedAttack;
    }

    public int getDefence() {
        return super.getDefence();
    }
}
