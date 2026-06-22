//Mohammed Shekhibrahim
//June 15 2026
//Game State Manager


package core;

/**
 * Manages the current game state and controls which screen is active
 */
public class GameStateManager {
    private String currentState;

    // State constants
    public static final String MENU = "MENU";
    public static final String PLAYING = "PLAYING";
    public static final String PAUSED = "PAUSED";
    public static final String RESULTS = "RESULTS";
    public static final String WINNER = "WINNER";

    /**
     * Constructor - starts at the main menu
     */
    public GameStateManager() {
        this.currentState = MENU;
    }

    /**
     * Get the current game state
     * @return current state as a String
     */
    public String getState() {
        return currentState;
    }

    /**
     * Set the game state to a new state
     * @param newState the state to change to
     */
    public void setState(String newState) {
        if (isValidState(newState)) {
            this.currentState = newState;
        }
    }

    /**
     * Check if a state is valid
     * @param state the state to check
     * @return true if state is valid, false otherwise
     */
    private boolean isValidState(String state) {
        return state.equals(MENU) ||
               state.equals(PLAYING) ||
               state.equals(PAUSED) ||
               state.equals(RESULTS) ||
               state.equals(WINNER);
    }

    /**
     * Check if currently in menu
     * @return true if in menu state
     */
    public boolean isMenu() {
        return currentState.equals(MENU);
    }

    /**
     * Check if currently playing a minigame
     * @return true if in playing state
     */
    public boolean isPlaying() {
        return currentState.equals(PLAYING);
    }

    /**
     * Check if the game is paused
     * @return true if in paused state
     */
    public boolean isPaused() {
        return currentState.equals(PAUSED);
    }

    /**
     * Check if currently showing results
     * @return true if in results state
     */
    public boolean isResults() {
        return currentState.equals(RESULTS);
    }

    /**
     * Check if showing winner screen
     * @return true if in winner state
     */
    public boolean isWinner() {
        return currentState.equals(WINNER);
    }

    /**
     * Reset state back to menu
     */
    public void reset() {
        this.currentState = MENU;
    }
}
