import java.util.*;

public class QLearner {

    public static void main(String[] args) {
        BlackJackEnv game = new BlackJackEnv(BlackJackEnv.NONE);
        double[][] QTable = initializeQTable(); // Initialize Q-table
        double totalReward = 0.0;
        int numberOfGames = 0;
        while (notDone()) {
            totalReward += playOneGame(game, QTable); // Play one game and update total reward
            numberOfGames++;
            if ((numberOfGames % 10000) == 0) {
                System.out.println("Avg reward after " + numberOfGames + " games = " + (totalReward / numberOfGames));
            }
        }
        outputQTable(QTable); // Output the learned QTable
    }

    private static double[][] initializeQTable() {
        double[][] QTable = new double[22][2]; // 22 states (hand values 0 to 21) and 2 actions (hit or stand)
        // Initialize Q-values to zero
        for (int i = 0; i < QTable.length; i++) {
            for (int j = 0; j < QTable[i].length; j++) {
                QTable[i][j] = 0.0;
            }
        }
        return QTable;
    }


    private static double playOneGame(BlackJackEnv game, double[][] QTable) {
        double totalReward = 0.0;
        ArrayList<String> currentState = game.reset(); // Initial state
        List<String> dealerState = BlackJackEnv.getDealerCards(currentState);
        int playerState = BlackJackEnv.totalValue(BlackJackEnv.getPlayerCards(currentState));
        while (!Boolean.parseBoolean(currentState.get(0))) { // While game is not over
            int state = Math.min(21, Math.max(0, playerState)); // Current state: player's hand value
            int action = chooseAction(state, QTable); // Choose action based on Q-values
            ArrayList<String> nextState = game.step(action); // Take action and observe next state
            double reward = Double.parseDouble(nextState.get(1)); // Obtain reward
            int nextStateValue = Math.min(21, Math.max(0, BlackJackEnv.totalValue(BlackJackEnv.getPlayerCards(nextState)))); // Next state: player's hand value
            // Update Q-value based on Q-learning update rule
            QTable[state][action] += 0.1 * (reward + 0.9 * getMaxQValue(nextStateValue, QTable) - QTable[state][action]);
            totalReward += reward;
            currentState = nextState; // Update current state
            playerState = nextStateValue; // Update player's state

            // Dealer's turn
            while (BlackJackEnv.totalValue(dealerState) < 17) {
                String card = game.drawCardForDealer(); // Draw card for dealer
                if (card != null) {
                    dealerState.add(card); // Dealer hits
                } else {
                    // Handle case when drawdeck is empty
                    break;
                }
            }

            // Check if dealer busts
            if (BlackJackEnv.totalValue(dealerState) > 21) {
                // Player wins
                reward = 1;
                totalReward += reward;
                break; // Game ends
            }

            // Compare player's and dealer's hand values
            if (BlackJackEnv.totalValue(dealerState) >= playerState) {
                // Dealer wins or tie
                if (BlackJackEnv.totalValue(dealerState) == playerState) {
                    // Tie
                    reward = 0;
                } else {
                    // Dealer wins
                    reward = -1;
                }
                totalReward += reward;
                break; // Game ends
            }
        }
        return totalReward;
    }

    private static int chooseAction(int state, double[][] QTable) {
        if (Math.random() < 0.2) { // Epsilon-greedy policy with epsilon = 0.2
            return (int) (Math.random() * 2); // Random action
        } else {
            // Select action with maximum Q-value
            return QTable[state][0] > QTable[state][1] ? 0 : 1;
        }
    }

    private static double getMaxQValue(int state, double[][] QTable) {
        // Get maximum Q-value for a given state
        return Math.max(QTable[state][0], QTable[state][1]);
    }

    private static boolean notDone() {
        // Stopping condition (fixed number of games)
        return episodeCounter++ <= 1000000;
    }

    private static int episodeCounter = 0;

    private static void outputQTable(double[][] QTable) {
        // Output the learned Q-table
        System.out.println("Q-Table:");
        for (int i = 0; i < QTable.length; i++) {
            System.out.println("State " + i + ": HIT=" + QTable[i][0] + ", STAND=" + QTable[i][1]);
        }
    }
}