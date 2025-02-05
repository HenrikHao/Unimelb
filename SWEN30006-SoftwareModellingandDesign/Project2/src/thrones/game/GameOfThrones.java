package thrones.game;

import ch.aplu.jcardgame.*;
import ch.aplu.jgamegrid.*;
import thrones.game.decorators.*;
import thrones.game.observers.GameObserver;
import thrones.game.players.AbstractPlayer;
import thrones.game.utils.*;
import static thrones.game.utils.CardUtil.*; // import static methods from cardutil class

import java.awt.Color;
import java.awt.Font;
import java.util.*;

@SuppressWarnings("serial")
public class GameOfThrones extends CardGame {

    // NOTE: PRIVATE attributes ensure encapsulation

    // constants
    public static final String DEFAULT_PROPERTIES_PATH = "properties/original.properties";
    public static final String DEFAULT_SEED = "30006";
    public static final String DEFAULT_WATCHING_TIME = "5000";
    public static final int NON_SELECTION_VALUE = -1;
    public static final int ATTACK_RANK_INDEX = 0;
    public static final int DEFENCE_RANK_INDEX = 1;

    private int seed;
    private int watchingTime;

    private final String version = "1.0";
    private final int nbPlayers = 4;
    private final int nbStartCards = 9;
    private final int nbPlays = 6;
    private final int nbRounds = 3;

    private AbstractPlayer[] players;
    private AbstractCharacter[] characters;
    
    private Hand[] piles;
    private Optional<Card> selected;
    private int selectedPileIndex;

    private int magicCardNum;
    private int nextStartingPlayer;
    private RandomUtil random;

    private List<GameObserver> observers = new ArrayList<>();

    private final int handWidth = 400;
    private final int pileWidth = 40;
    private final Deck deck = new Deck(Suit.values(), Rank.values(), "cover");
    private final Location[] handLocations = {
            new Location(350, 625),
            new Location(75, 350),
            new Location(350, 75),
            new Location(625, 350)
    };
    private final Location[] scoreLocations = {
            new Location(575, 675),
            new Location(25, 575),
            new Location(25, 25),
            new Location(575, 125)
    };
    private final Location[] pileLocations = {
            new Location(350, 280),
            new Location(350, 430)
    };
    private final Location[] pileStatusLocations = {
            new Location(250, 200),
            new Location(250, 520)
    };
    private final Font bigFont = new Font("Arial", Font.BOLD, 36);
    private final Font smallFont = new Font("Arial", Font.PLAIN, 10);
    private final String[] playerTeams = { "[Players 0 & 2]", "[Players 1 & 3]" };
    private Actor[] pileTextActors = { null, null };
    private Actor[] scoreActors = { null, null, null, null };


    private void initWithProperties(Properties properties) {
        this.seed = Integer.parseInt(properties.getProperty("seed", DEFAULT_SEED));
        RandomUtil.setSeed(seed);
        random = RandomUtil.getInstance();
        this.watchingTime = Integer.parseInt(properties.getProperty("watchingTime", DEFAULT_WATCHING_TIME));
        this.nextStartingPlayer = random.nextInt(nbPlayers);
    }

    private void dealingOut(int nbPlayers, int nbCardsPerPlayer) {
        Hand pack = deck.toHand(false);
        assert pack.getNumberOfCards() == 52 : " Starting pack is not 52 cards.";
        // Remove 4 Aces
        List<Card> aceCards = pack.getCardsWithRank(Rank.ACE);
        for (Card card : aceCards) {
            card.removeFromHand(false);
        }
        assert pack.getNumberOfCards() == 48 : " Pack without aces is not 48 cards.";

        for (int i = 0; i < nbPlayers; i++) {
            for (int j = 0; j < 3; j++) {
                List<Card> heartCards = pack.getCardsWithSuit(Suit.HEARTS);
                int x = random.nextInt(heartCards.size());
                Card randomCard = heartCards.get(x);
                randomCard.removeFromHand(false);
                players[i].getHand().insert(randomCard, false);
            }
        }
        assert pack.getNumberOfCards() == 36 : " Pack without aces and hearts is not 36 cards.";

        for (int i = 0; i < nbCardsPerPlayer; i++) {
            for (int j = 0; j < nbPlayers; j++) {
                assert !pack.isEmpty() : " Pack has prematurely run out of cards.";
                Card dealt = random.randomCard(pack);
                dealt.removeFromHand(false);
                players[j].getHand().insert(dealt, false);
            }
        }
        for (int j = 0; j < nbPlayers; j++) {
            assert players[j].getHand().getNumberOfCards() == 12 : " Hand does not have twelve cards.";
        }
    }

    private void initScores() {
        for (int i = 0; i < nbPlayers; i++) {
            String text = "P" + i + "-0";
            scoreActors[i] = new TextActor(text, Color.WHITE, bgColor, bigFont);
            addActor(scoreActors[i], scoreLocations[i]);
        }

        String text = "Attack: 0 - Defence: 0";
        for (int i = 0; i < pileTextActors.length; i++) {
            pileTextActors[i] = new TextActor(text, Color.WHITE, bgColor, smallFont);
            addActor(pileTextActors[i], pileStatusLocations[i]);
        }
    }

    private void updateScores() {
        for (int i = 0; i < nbPlayers; i++) {
            int player = i;
            removeActor(scoreActors[player]);
            String text = "P" + player + "-" + players[player].getScore();
            scoreActors[player] = new TextActor(text, Color.WHITE, bgColor, bigFont);
            addActor(scoreActors[player], scoreLocations[player]);
        }
        System.out.println(playerTeams[0] + " score = " + players[0].getScore() + "; " + playerTeams[1] + " score = " + players[1].getScore());
    }

    private void setupGame(Properties properties) {

        selectedPileIndex = NON_SELECTION_VALUE;

        // SETUP GAME
        initWithProperties(properties);
        players = new AbstractPlayer[nbPlayers];
        characters = new AbstractCharacter[2];
        PlayerFactory playerFactory = PlayerFactory.getInstance();
        for (int i=0; i<nbPlayers; i++) {
            players[i] = playerFactory.createPlayer(i, 
                properties.getProperty(String.format("players.%d", i), ""));
            if ((properties.getProperty(String.format("players.%d", i)).equals("smart"))) {
                addObserver((GameObserver) players[i]);
                System.out.println("here");
            }
        }

        for (int i = 0; i < nbPlayers; i++) {
            players[i].setHand(new Hand(deck));
        }
        dealingOut(nbPlayers, nbStartCards);

        for (int i = 0; i < nbPlayers; i++) {
            players[i].getHand().sort(Hand.SortType.SUITPRIORITY, true);
            System.out.println("hands[" + i + "]: " + canonical(players[i].getHand()));
        }

        RowLayout[] layouts = new RowLayout[nbPlayers];
        for (int i = 0; i < nbPlayers; i++) {
            layouts[i] = new RowLayout(handLocations[i], handWidth);
            layouts[i].setRotationAngle(90 * i);
            players[i].getHand().setView(this, layouts[i]);
            players[i].getHand().draw();
        }

    }

    private void resetPile() {
        if (piles != null) {
            for (Hand pile : piles) {
                pile.removeAll(true);
            }
        }
        piles = new Hand[2];
        for (int i = 0; i < 2; i++) {
            piles[i] = new Hand(deck);
            piles[i].setView(this, new RowLayout(pileLocations[i], 8 * pileWidth));
            piles[i].draw();
        }
        updatePileRanks();
    }

    private int[] calculatePileRanks(int pileIndex) {
        Hand currentPile = piles[pileIndex];
        int i = currentPile.isEmpty() ? 0 : ((Rank) currentPile.get(0).getRank()).getRankValue();
        return new int[] { i, i };
    }

    private void updatePileRankState(int pileIndex, int attackRank, int defenceRank) {
        TextActor currentPile = (TextActor) pileTextActors[pileIndex];
        removeActor(currentPile);
        String text = playerTeams[pileIndex] + " Attack: " + attackRank + " - Defence: " + defenceRank;
        pileTextActors[pileIndex] = new TextActor(text, Color.WHITE, bgColor, smallFont);
        addActor(pileTextActors[pileIndex], pileStatusLocations[pileIndex]);
    }

    private void updatePileRanks() {
        for (int j = 0; j < piles.length; j++) {
            int[] ranks = calculatePileRanks(j);
            updatePileRankState(j, ranks[ATTACK_RANK_INDEX], ranks[DEFENCE_RANK_INDEX]);
        }
    }

    private int getPlayerIndex(int index) {
        return index % nbPlayers;
    }

    private void executeAPlay() {
        resetPile();

        nextStartingPlayer = getPlayerIndex(nextStartingPlayer);
        if (players[nextStartingPlayer].getHand().getNumberOfCardsWithSuit(Suit.HEARTS) == 0)
            nextStartingPlayer = getPlayerIndex(nextStartingPlayer + 1);
        assert players[nextStartingPlayer].getHand().getNumberOfCardsWithSuit(Suit.HEARTS) != 0 : " Starting player has no hearts.";

        for (int i = 0; i < 2; i++) {
            int playerIndex = getPlayerIndex(nextStartingPlayer + i);
            setStatusText("Player " + playerIndex + " select a Heart card to play");

            selected = players[playerIndex].selectACard(piles);
            System.out.format("Player %d select %s to play\n", playerIndex, selected);

            int pileIndex = playerIndex % 2;
            assert selected.isPresent() : " Pass returned on selection of character.";
            System.out
                    .println("Player " + playerIndex + " plays " + canonical(selected.get()) + " on pile " + pileIndex);
            selected.get().setVerso(false);
            selected.get().transfer(piles[pileIndex], true);
            updatePileRanks();

            characters[pileIndex] = new BaseCharacter(selected.get());
        }
        Rank rank0 = (Rank) piles[0].getCardList().get(0).getRank();
        Rank rank1 = (Rank) piles[1].getCardList().get(0).getRank();

        notifyObservers(rank0, rank1, characters, 0, piles);

        int remainingTurns = nbPlayers * nbRounds - 2;
        int nextPlayer = nextStartingPlayer + 2;

        while (remainingTurns > 0) {
            notifyObservers(rank0, rank1, characters, magicCardNum, piles);
            selectedPileIndex = NON_SELECTION_VALUE;

            nextPlayer = getPlayerIndex(nextPlayer);
            setStatusText("Player" + nextPlayer + " select a non-Heart card to play.");
            
            selected = players[nextPlayer].selectACard(piles);
            if (selected.isPresent()) {
                if (canonical(selected.get()).charAt(1) == 'D') {
                    magicCardNum += 1;
                }
                setStatusText("Selected: " + canonical(selected.get()) + ". Player" + nextPlayer
                        + " select a pile to play the card.");
                
                selectedPileIndex = players[nextPlayer].selectAPile(selected.get(), piles);

                if (selectedPileIndex == NON_SELECTION_VALUE) {
                    setStatusText("Pass.");
                } else {
                    System.out.println("Player " + nextPlayer + " plays " + canonical(selected.get()) + " on pile "
                        + selectedPileIndex);
                    selected.get().setVerso(false);
                    selected.get().transfer(piles[selectedPileIndex], true); // transfer to pile (includes graphic effect)
                    //updatePileRanks();
                    Card lastCard = piles[selectedPileIndex].get(piles[selectedPileIndex].getNumberOfCards() - 2);
                    for (int i=piles[selectedPileIndex].getNumberOfCards()-3; i>0; i--) {
                        if (lastCard.getSuit() != CardUtil.Suit.DIAMONDS) {
                            break;
                        }
                        lastCard = piles[selectedPileIndex].get(i);
                    }
                    // update decorator
                    if (((Suit) (selected.get().getSuit())).isAttack()) {
                        characters[selectedPileIndex] = 
                            new AttackDecorator(characters[selectedPileIndex], selected.get(), lastCard);
                    }
                    if (((Suit) (selected.get().getSuit())).isDefence()) {
                        characters[selectedPileIndex] =
                                new DefenceDecorator(characters[selectedPileIndex], selected.get(), lastCard);
                    }
                    if (((Suit) (selected.get().getSuit())).isMagic()) {
                        characters[selectedPileIndex] =
                                new MagicDecorator(characters[selectedPileIndex], selected.get(), lastCard);
                    }
                    updatePileRankState(selectedPileIndex, characters[selectedPileIndex].getAttack(), characters[selectedPileIndex].getDefence());
                }
            } else {
                setStatusText("Pass.");
            }

            nextPlayer++;
            remainingTurns--;
        }

        updatePileRankState(0, characters[0].getAttack(), characters[0].getDefence());
        updatePileRankState(1, characters[1].getAttack(), characters[1].getDefence());
        System.out.println("piles[0]: " + canonical(piles[0]));
        System.out.println("piles[0] is " + "Attack: " + characters[0].getAttack() + " - Defence: "
                + characters[0].getDefence());
        System.out.println("piles[1]: " + canonical(piles[1]));
        System.out.println("piles[1] is " + "Attack: " + characters[1].getAttack() + " - Defence: "
                + characters[1].getDefence());
        Rank pile0CharacterRank = (Rank) piles[0].getCardList().get(0).getRank();
        Rank pile1CharacterRank = (Rank) piles[1].getCardList().get(0).getRank();
        String character0Result;
        String character1Result;

        if (characters[0].getAttack() > characters[1].getDefence()) {
            int win = pile1CharacterRank.getRankValue();
            players[0].setScore(players[0].getScore() + win); 
            players[2].setScore(players[2].getScore() + win); 
            character0Result = "Character 0 attack on character 1 succeeded.";
        } else {
            int win = pile1CharacterRank.getRankValue();
            players[1].setScore(players[1].getScore() + win); 
            players[3].setScore(players[3].getScore() + win); 
            character0Result = "Character 0 attack on character 1 failed.";
        }

        if (characters[1].getAttack() > characters[0].getDefence()) {
            int win = pile0CharacterRank.getRankValue();
            players[0].setScore(players[1].getScore() + win);
            players[2].setScore(players[3].getScore() + win);
            character1Result = "Character 1 attack on character 0 succeeded.";
        } else {
            int win = pile0CharacterRank.getRankValue();
            players[1].setScore(players[0].getScore() + win);
            players[3].setScore(players[2].getScore() + win);
            character1Result = "Character 1 attack character 0 failed.";
        }
        updateScores();
        System.out.println(character0Result);
        System.out.println(character1Result);
        setStatusText(character0Result + " " + character1Result);

        nextStartingPlayer += 1;
        delay(watchingTime);
    }


    private void addObserver(GameObserver observer) {
         observers.add(observer);
    }

    private void notifyObservers(Rank rank0, Rank rank1, AbstractCharacter[] characters, int magicCardNum, Hand[] piles) {
        for (GameObserver o : observers) {
            o.update(rank0, rank1, characters, magicCardNum, piles);
        }
    }

    public GameOfThrones(Properties properties) {
        super(700, 700, 30);

        setTitle("Game of Thrones (V" + version + ") Constructed for UofM SWEN30006 with JGameGrid (www.aplu.ch)");
        setStatusText("Initializing...");
        initScores();

        setupGame(properties);
        for (int i = 0; i < nbPlays; i++) {
            executeAPlay();
            updateScores();
        }

        String text;
        int scoreOne = players[0].getScore();
        int scoreTwo = players[1].getScore();
        if (scoreOne > scoreTwo) {
            text = "Players 0 and 2 won.";
        } else if (scoreOne == scoreTwo) {
            text = "All players drew.";
        } else {
            text = "Players 1 and 3 won.";
        }
        System.out.println("Result: " + text);
        setStatusText(text);

        refresh();
    }

    public static void main(String[] args) {
        System.out.println("Working Directory = " + System.getProperty("user.dir"));
        String propertiesPath = DEFAULT_PROPERTIES_PATH;
        if (args.length > 0) {
            propertiesPath = args[0];
        }
        final Properties properties = PropertiesLoader.loadPropertiesFile(propertiesPath);
        new GameOfThrones(properties);
    }

}
