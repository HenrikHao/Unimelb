package thrones.game.decorators;

import ch.aplu.jcardgame.Card;
import ch.aplu.jcardgame.Hand;

public abstract class CharacterDecorator implements AbstractCharacter {

    protected AbstractCharacter character; // wrappee

    protected Card lastPlayedCard;
    protected Card lastCard;

    public CharacterDecorator(AbstractCharacter character, Card playedCard, Card lastCard) {
        this.character = character;
        this.lastPlayedCard = playedCard;
        this.lastCard = lastCard;
    }

    @Override
    public int getAttack() {
        return character.getAttack();
    }
    @Override
    public int getDefence() {
        return character.getDefence();
    }
    @Override
    public Card getCurrCard() {
        return character.getCurrCard();
    }

}
