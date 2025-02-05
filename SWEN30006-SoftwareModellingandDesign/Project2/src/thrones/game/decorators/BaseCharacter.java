package thrones.game.decorators;

import ch.aplu.jcardgame.Card;
import thrones.game.utils.CardUtil.Rank;

public class BaseCharacter implements AbstractCharacter {

    protected int attack;
    protected int defence;
    protected Card card;

    public BaseCharacter(Card baseCharacter) {
        Rank rank = (Rank) baseCharacter.getRank();
        this.attack = rank.getRankValue();
        this.defence = rank.getRankValue();
        this.card = baseCharacter;
    }

    public int getAttack() {
        return this.attack;
    }

    public int getDefence() {
        return this.defence;
    }

    @Override
    public Card getCurrCard() {
        return this.card;
    }

}
