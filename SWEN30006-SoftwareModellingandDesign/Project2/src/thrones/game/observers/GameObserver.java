package thrones.game.observers;

import java.util.Optional;

import ch.aplu.jcardgame.Card;
import ch.aplu.jcardgame.Hand;
import thrones.game.decorators.AbstractCharacter;
import thrones.game.utils.CardUtil;

public interface GameObserver {

    void update(CardUtil.Rank rank0, CardUtil.Rank rank1, AbstractCharacter[] characters, int magicCardNum, Hand[] piles);
    
}
