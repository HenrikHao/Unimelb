package thrones.game.decorators;

import ch.aplu.jcardgame.Card;
import ch.aplu.jcardgame.Hand;
import thrones.game.utils.CardUtil;
import thrones.game.utils.CardUtil.Rank;


public class DefenceDecorator extends CharacterDecorator {
    public DefenceDecorator(AbstractCharacter character, Card card, Card lastCard) {
        super(character, card, lastCard);
    }

    public int getAttack() {
        return super.getAttack();
    }
    @Override
    public int getDefence() {

        Rank rank = getRank(lastPlayedCard);

        int addedDefence = rank.getRankValue();
        Rank lastRank = getRank(super.getCurrCard());
        if (lastRank.equals(rank)) {
            addedDefence *= 2;
        }

        return super.getDefence() + addedDefence;
    }

}
