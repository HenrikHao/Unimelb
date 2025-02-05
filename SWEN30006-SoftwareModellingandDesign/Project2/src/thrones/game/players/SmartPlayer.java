package thrones.game.players;

import java.util.*;

import ch.aplu.jcardgame.Card;
import ch.aplu.jcardgame.Hand;
import thrones.game.GameOfThrones;
import thrones.game.decorators.AbstractCharacter;
import thrones.game.observers.GameObserver;
import thrones.game.utils.CardUtil.*;
import thrones.game.utils.RandomUtil;

public class SmartPlayer extends AbstractPlayer implements GameObserver {

    public SmartPlayer(int playerId, String playerType) {
        super(playerId, playerType);
    }

    private Rank teamRank;
    private Rank oppositeRank;
    private int teamPile;
    private int oppositePile;

    private Hand[] piles;

    private int targetPile;

    private RandomUtil random = RandomUtil.getInstance();
    private int teamA, oppositeA,  teamD, oppositeD;

    private int magicCardNum;

    @Override
    public void update(Rank rank0, Rank rank1, AbstractCharacter[] characters, int magicCardNum, Hand[] piles) {

        if (getPlayerId() == 0 || getPlayerId() == 2) {
            teamPile = 0;
            teamRank = rank0;
            oppositePile = 1;
            oppositeRank = rank1;
        } else {
            teamPile = 1;
            teamRank = rank1;
            oppositePile = 0;
            oppositeRank = rank0;
        }


        teamA = characters[teamPile].getAttack();
        teamD = characters[teamPile].getDefence();
        oppositeA = characters[oppositePile].getAttack();
        oppositeD = characters[oppositePile].getDefence();
        this.magicCardNum = magicCardNum;
        this.piles = piles;
    }

    private boolean battle(int teamA, int oppositeA, int teamD, int oppositeD) {

        int score1 = 0, score2 = 0;
        if (teamA > oppositeD) {
            score1 += oppositeRank.getRankValue();
        } else {
            score2 += teamRank.getRankValue();
        }
        if (oppositeA > teamD) {
            score2 += teamRank.getRankValue();
        } else {
            score1 += oppositeRank.getRankValue();
        }
        return score1 > score2;
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

        if (magicCardNum + getHand().extractCardsWithSuit(Suit.DIAMONDS).getNumberOfCards() == 12) {
            return Optional.of(random.randomCard(getHand().extractCardsWithSuit(Suit.DIAMONDS)));
        }
        if (battle(teamA, teamD, oppositeA, oppositeD)) {
            return Optional.empty();
        } else {
            for (Card card: getHand().getCardList()) {
                int tmpTeamD = 0, tmpTeamA = 0, tmpOppoD = 0, tmpOppoA = 0;
                Suit suit = (Suit) card.getSuit();
                Rank rank = (Rank) card.getRank();
                if (!suit.isMagic()) {
                    if (suit.isDefence()) {
                        tmpTeamD = teamD + rank.getRankValue();
                    }
                    if (suit.isAttack()) {
                        tmpTeamA = teamA + rank.getRankValue();
                    }
                    if (battle(tmpTeamA, tmpTeamD, oppositeA, oppositeD)) {

                        return Optional.of(card);
                    }
                }
                else {
                    if (piles[oppositePile].getNumberOfCards() > 2) {
                        Card lastCard = piles[oppositePile].getLast();
                        Suit lastSuit = (Suit) lastCard.getSuit();
                        Rank lastRank = (Rank) lastCard.getRank();

                        if (lastSuit.isAttack()) {
                            if (lastRank == rank) {
                                tmpOppoA = oppositeA - 2*lastRank.getRankValue();
                            } else {
                                tmpOppoA = oppositeA - lastRank.getRankValue();
                            }
                            if (tmpOppoA < 0) {
                                tmpOppoA = 0;
                            }
                        }
                        if (lastSuit.isDefence()) {
                            if (lastRank == rank) {
                                tmpOppoD = oppositeD - 2*lastRank.getRankValue();
                            } else {
                                tmpOppoD = oppositeD - lastRank.getRankValue();
                            }
                            if (tmpOppoD < 0) {
                                tmpOppoD = 0;
                            }
                        }
                        if (battle(teamA, teamD, tmpOppoA, tmpOppoD)) {
                            return Optional.of(card);
                        }
                    }
                }
            }
        }


        return Optional.of(random.randomCard(getHand()));
    }

    @Override
    public int selectAPile(Card card, Hand[] piles) {
        Suit suit = (Suit) card.getSuit();
        int playerIndex = getPlayerId();
        targetPile = GameOfThrones.NON_SELECTION_VALUE;

        if (playerIndex == 0 || playerIndex == 2) {
            if (suit.isAttack() || suit.isDefence() || suit.isCharacter()) {
                targetPile = 0;
            }
            if (suit.isMagic()) {
                targetPile = 1;
                if (piles[targetPile].getNumberOfCards() == 1) {
                    targetPile = GameOfThrones.NON_SELECTION_VALUE;
                }
            }
        } else {
            if (suit.isAttack() || suit.isDefence() || suit.isCharacter()) {
                targetPile = 1;
                if (suit.isCharacter() || piles[targetPile].getNumberOfCards() == 1) {
                    targetPile = GameOfThrones.NON_SELECTION_VALUE;
                }
            }
            if (suit.isMagic()) {
                targetPile = 0;
                if (piles[targetPile].getNumberOfCards() == 1) {
                    targetPile = GameOfThrones.NON_SELECTION_VALUE;
                }
            }
        }
        return targetPile;
    }


}



