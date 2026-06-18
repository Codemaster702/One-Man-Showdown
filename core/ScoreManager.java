package core;

/**
 * Manages the score system for the game
 * Tracks wins for both players and determines overall winner
 */
public class ScoreManager {
    private int player1Wins;
    private int player2Wins;
    private String roundWinner;
    private String gameWinner;
    private boolean roundScoreRecorded;
    private static final int WIN_THRESHOLD = 5;

    /**
     * Constructor - initialize scores to 0
     */
    public ScoreManager() {
        this.player1Wins = 0;
        this.player2Wins = 0;
        this.roundWinner = null;
        this.gameWinner = null;
        this.roundScoreRecorded = false;
    }

    /**
     * Get player 1's current wins
     * @return player 1's win count
     */
    public int getPlayer1Wins() {
        return player1Wins;
    }

    /**
     * Get player 2's current wins
     * @return player 2's win count
     */
    public int getPlayer2Wins() {
        return player2Wins;
    }

    /**
     * Update score when a minigame ends
     * @param winner "PLAYER1" or "PLAYER2"
     */
    public void updateScore(String winner) {
        if (roundScoreRecorded) {
            return;
        }

        if (winner == null) {
            this.roundWinner = null;
        } else if (winner.equals("PLAYER1")) {
            player1Wins++;
            this.roundWinner = "PLAYER1";
        } else if (winner.equals("PLAYER2")) {
            player2Wins++;
            this.roundWinner = "PLAYER2";
        }

        roundScoreRecorded = true;
    }

    /**
     * Check if a player has won the overall game (reached 5 wins)
     * @return true if someone has won
     */
    public boolean hasWinner() {
        if (player1Wins >= WIN_THRESHOLD) {
            this.gameWinner = "PLAYER1";
            return true;
        } else if (player2Wins >= WIN_THRESHOLD) {
            this.gameWinner = "PLAYER2";
            return true;
        }
        return false;
    }

    /**
     * Get the winner of the current round (minigame)
     * @return "PLAYER1" or "PLAYER2"
     */
    public String getRoundWinner() {
        return roundWinner;
    }

    /**
     * Get the winner of the overall game
     * @return "PLAYER1" or "PLAYER2"
     */
    public String getGameWinner() {
        return gameWinner;
    }

    /**
     * Get a display string for the round winner
     * @return formatted string like "Player 1 wins this round!"
     */
    public String getRoundWinnerDisplay() {
        if (roundWinner == null) {
            return "It's a draw!";
        }
        String playerNum;
        if (roundWinner.equals("PLAYER1")) {
            playerNum = "1";
        } else if (roundWinner.equals("PLAYER2")) {
            playerNum = "2";
        } else {
            return "Draw!";
        }
        return "Player " + playerNum + " wins this round!";
    }

    /**
     * Get a display string for the game winner
     * @return formatted string like "Player 1 wins the game!"
     */
    public String getGameWinnerDisplay() {
        if (gameWinner == null) {
            return "Game Over!";
        }
        String playerNum = gameWinner.equals("PLAYER1") ? "1" : "2";
        return "Player " + playerNum + " wins the game!";
    }

    /**
     * Get the score difference
     * @return player1Wins - player2Wins (can be negative)
     */
    public int getScoreDifference() {
        return player1Wins - player2Wins;
    }

    /**
     * Check if game is tied
     * @return true if both players have same wins
     */
    public boolean isTied() {
        return player1Wins == player2Wins;
    }

    /**
     * Reset all scores for a new game
     */
    public void reset() {
        this.player1Wins = 0;
        this.player2Wins = 0;
        this.roundWinner = null;
        this.gameWinner = null;
        this.roundScoreRecorded = false;
    }

    /**
     * Reset only the round winner (between minigames)
     */
    public void resetRoundWinner() {
        this.roundWinner = null;
        this.roundScoreRecorded = false;
    }

    /**
     * Undo the last recorded round score, used when retrying a minigame.
     * This removes the previously awarded point before replaying.
     */
    public void undoLastRoundScore() {
        if (!roundScoreRecorded) {
            return;
        }

        if ("PLAYER1".equals(roundWinner) && player1Wins > 0) {
            player1Wins--;
        } else if ("PLAYER2".equals(roundWinner) && player2Wins > 0) {
            player2Wins--;
        }

        this.roundWinner = null;
        this.roundScoreRecorded = false;
    }

    /**
     * Get the win threshold
     * @return number of wins needed to win the game
     */
    public static int getWinThreshold() {
        return WIN_THRESHOLD;
    }

    /**
     * Get a score display string
     * @return formatted string like "5 - 3"
     */
    public String getScoreDisplay() {
        return player1Wins + " - " + player2Wins;
    }
}