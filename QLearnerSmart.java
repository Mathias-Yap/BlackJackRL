import java.util.*;

public class QLearnerSmart {

    private double[][][][] QTable; // Q-Table with extended dimensions
    private int episodeCounter;
    private static final int HIT = 0;
    private static final int STAND = 1;
    private static final double LEARNING_RATE = 0.1;
    private static final double DISCOUNT_FACTOR = 0.9;
    private static final double EPSILON = 0.2;
    private BlackJackEnv game;

    public QLearnerSmart() {
        this.game = new BlackJackEnv(BlackJackEnv.NONE);
        this.initializeQTable();
        this.episodeCounter = 0;
    }

    private void initializeQTable() {
        QTable = new double[22][2][12][2];
        for (int playerHandValue = 0; playerHandValue < QTable.length; playerHandValue++) {
            for (int hasActiveAce = 0; hasActiveAce < QTable[playerHandValue].length; hasActiveAce++) {
                for (int dealerVisibleCard = 1; dealerVisibleCard < QTable[playerHandValue][hasActiveAce].length; dealerVisibleCard++) {
                    Arrays.fill(QTable[playerHandValue][hasActiveAce][dealerVisibleCard], 0.0);
                }
            }
        }
    }

    private int chooseAction(int playerHandValue, boolean hasActiveAce, int dealerVisibleCard) {
        if (Math.random() < EPSILON) {
            return new Random().nextInt(2);
        } else {
            return QTable[playerHandValue][hasActiveAce ? 1 : 0][dealerVisibleCard][HIT] >
                    QTable[playerHandValue][hasActiveAce ? 1 : 0][dealerVisibleCard][STAND] ? HIT : STAND;
        }
    }

    public void playGames(int numberOfEpisodes) {
        for (int episode = 0; episode < numberOfEpisodes; episode++) {
            ArrayList<String> currentState = game.reset();
            boolean gameEnded = Boolean.parseBoolean(currentState.get(0));
            double totalReward = 0;

            while (!gameEnded) {
                int dealerVisibleCard = BlackJackEnv.valueOf(game.getDealerCards(currentState).get(0));
                List<String> playerCards = BlackJackEnv.getPlayerCards(currentState);
                int playerHandValue = BlackJackEnv.totalValue(playerCards);
                boolean hasActiveAce = BlackJackEnv.holdActiveAce(playerCards);

                int action = chooseAction(playerHandValue, hasActiveAce, dealerVisibleCard);
                ArrayList<String> nextState = game.step(action);
                double reward = Double.parseDouble(nextState.get(1));
                gameEnded = Boolean.parseBoolean(nextState.get(0));

                int nextDealerVisibleCard = dealerVisibleCard; // Dealer's card doesn't change within a game episode
                List<String> nextPlayerCards = BlackJackEnv.getPlayerCards(nextState);
                int nextPlayerHandValue = BlackJackEnv.totalValue(nextPlayerCards);
                boolean hasNextActiveAce = BlackJackEnv.holdActiveAce(nextPlayerCards);

                double bestNextActionQ = Math.max(
                        QTable[nextPlayerHandValue][hasNextActiveAce ? 1 : 0][nextDealerVisibleCard][HIT],
                        QTable[nextPlayerHandValue][hasNextActiveAce ? 1 : 0][nextDealerVisibleCard][STAND]
                );

                QTable[playerHandValue][hasActiveAce ? 1 : 0][dealerVisibleCard][action] +=
                        LEARNING_RATE * (reward + DISCOUNT_FACTOR * bestNextActionQ -
                                QTable[playerHandValue][hasActiveAce ? 1 : 0][dealerVisibleCard][action]);

                totalReward += reward;
                currentState = nextState;
            }

            if ((episode + 1) % 10000 == 0) {
                System.out.println("Episode: " + (episode + 1) + ", Total Reward: " + totalReward);
            }
        }
    }

    public static void main(String[] args) {
        QLearnerSmart learner = new QLearnerSmart();
        learner.playGames(1000000); // Adjust the number of games as needed
    }
}
