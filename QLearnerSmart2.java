import java.util.*;

public class QLearnerSmart2 {
    public static void main(String[] args) {
        BlackJackEnv game = new BlackJackEnv(BlackJackEnv.NONE);
        double[][][][] QTable = initializeQTable(); // Initialize Q-table
        double totalReward = 0.0;
        int numberOfGames = 0;
        while (notDone()) {
            double reward = playOneGame(game, QTable);
            totalReward += reward; // Play one game and update total reward
            numberOfGames++;
            if ((numberOfGames % 10000) == 0) {
                System.out.println("Avg reward after " + numberOfGames + " games = " + (totalReward / numberOfGames));
            }
        }
        //outputQTable(QTable); // Output the learned QTable
    }

    private static double[][][][] initializeQTable() {
        /*
        Returns array with:
        index 0 = player hand
        index 1 = dealer hand
        index 2 = active ace boolean
        index 3 = action (hit = 0 or stand = 1)
         */
        double[][][][] QTable = new double[22][22][2][2]; // 22 player states, 22 dealer states (hand values 0 to 21), boolean for active ace and 2 actions (hit or stand)
        // Initialize Q-values to zero
        for (int i = 0; i < 22; i++) {
            for (int j = 0; j < 22; j++) {
                for (int k = 0; k < 2; k++) {
                    for (int l = 0; l < 2; l++) {
                        QTable[i][j][k][l] = 0.0; // Initialize each element to zero
                    }
                }
            }
        }
        return QTable;
    }


    private static double playOneGame(BlackJackEnv game, double[][][][] QTable) {
        double totalReward = 0.0;
        ArrayList<String> currentState = game.reset(); // Initial state
        List<String> dealerCards= BlackJackEnv.getDealerCards(currentState);
        List<String> playerCards = BlackJackEnv.getPlayerCards(currentState);
        int activeAce = 0;
        if(BlackJackEnv.holdActiveAce(playerCards) == true) {
            activeAce = 1;
        }
        int playerSum = BlackJackEnv.totalValue(playerCards);
        int dealerSum = BlackJackEnv.totalValue(dealerCards);
        int action = 0;
        while (!Boolean.parseBoolean(currentState.get(0))&& action != 1) { // While game is not over
            int state = Math.max(0, playerSum); // Current state: player's hand value
            action = chooseAction(playerSum, dealerSum, activeAce, QTable); // Choose action based on Q-values
            ArrayList<String> nextState = game.step(action);

            int playerIndex = nextState.indexOf("Player");
            ArrayList<String> nextPlayerHand =  new ArrayList<>();
            nextPlayerHand.addAll(nextState.subList(playerIndex + 1, nextState.size()));

            int dealerIndex = nextState.indexOf("Dealer");
            ArrayList<String> nextDealerHand =  new ArrayList<>();
            nextDealerHand.addAll(nextState.subList(dealerIndex + 1, playerIndex-1));

            int nextPlayerSum = BlackJackEnv.totalValue(nextPlayerHand);
            int nextDealerSum = BlackJackEnv.totalValue(nextDealerHand);

            int nextactiveAce = 0;
            if(BlackJackEnv.holdActiveAce(nextPlayerHand) == true) {
            nextactiveAce = 1;
            }

            
            // Update Q-value based on Q-learning update rule
            QTable[playerSum][dealerSum][activeAce][action] += 0.1 * (nextPlayerSum + 0.9 * getMaxQValue(nextPlayerSum,nextDealerSum,nextactiveAce, QTable) - QTable[playerSum][dealerSum][activeAce][action]);
            currentState = nextState; // Update current state
            playerSum = nextPlayerSum;
            dealerSum = nextDealerSum;
            activeAce = nextactiveAce; // Update player's state
            playerCards = nextPlayerHand;
            dealerCards = nextDealerHand;
    
        }
            // Dealer's turn
            while (dealerSum < 17) {
                String card = game.drawCardForDealer(); // Draw card for dealer
                if (card != null) {
                    dealerCards.add(card); // Dealer hits
                } else {
                    // Handle case when drawdeck is empty
                    break;
                }
                dealerSum = BlackJackEnv.totalValue(dealerCards);
            }

            // Check if dealer busts
            int reward = 0;
            if (BlackJackEnv.totalValue(dealerCards) > 21) {
                // Player wins
                reward = 1;
                totalReward += reward;
            }

            // Compare player's and dealer's hand values
            else if (BlackJackEnv.totalValue(dealerCards) >= playerSum) {
                // Dealer wins or tie
                if (BlackJackEnv.totalValue(dealerCards) == playerSum) {
                    // Tie
                    reward = 0;
                } else {
                    // Dealer wins
                    reward = -1;
                }
                totalReward += reward;
            }
        return totalReward;
    }

    private static int chooseAction(int playerSum,int dealerSum, int activeAce, double[][][][] QTable) {
        if (Math.random() < 0.2) { // Epsilon-greedy policy with epsilon = 0.2
            return (int) (Math.random() * 2); // Random action
        } else {
            // Select action with maximum Q-value
            return QTable[playerSum][dealerSum][activeAce][0] > QTable[playerSum][dealerSum][activeAce][1] ? 0 : 1;
        }
    }

    private static double getMaxQValue(int playerSum,int dealerSum, int activeAce, double[][][][] QTable) {
        // Get maximum Q-value for a given state
        if(dealerSum>21 && playerSum >21){ 
            return 0;
        }
        if(playerSum >21){ 
            return -1;
        }
        if(dealerSum >21){ 
            return 1;
        }
        return Math.max(QTable[playerSum][dealerSum][activeAce][0],QTable[playerSum][dealerSum][activeAce][1]);
    }

    private static boolean notDone() {
        // Stopping condition (fixed number of games)
        return episodeCounter++ <= 1000000;
    }

    private static int episodeCounter = 0;

    private static void outputQTable(double[][][][] QTable) {
        // Output the learned Q-table
        System.out.println("Q-Table:");
        for (int i = 0; i < QTable.length; i++) {
            for (int j = 0; j < QTable[i].length; j++) {
                for (int k = 0; k < QTable[i][j].length; k++) {
                    for (int l = 0; l < QTable[i][j][k].length; l++) {
                        System.out.println("Playersum" + i + ", DealerSum " + j + ", ActiveAce " + k + ", Action " + l + ": Value=" + QTable[i][j][k][l]);
                    }
                }
            }
        }
    }
}
    

