package managers;

import minigames.*;
import java.util.Random;
import java.util.ArrayList;

/**
 * Manages all minigames - loads, switches, and tracks the current minigame
 */
public class MinigameManager {
    private ArrayList<BaseMinigame> minigames;
    private BaseMinigame currentMinigame;
    private Random random;

    /**
     * Constructor - initializes all 5 minigames
     */
    public MinigameManager() {
        this.minigames = new ArrayList<>();
        this.random = new Random();

        // Add all 5 minigames to the list
        minigames.add(new RaceMinigame());
        minigames.add(new TugOfWarMinigame());
        minigames.add(new SheriffMinigame());
        minigames.add(new HurdleMinigame());
        minigames.add(new DodgeMinigame());

        // Load the first random minigame
        loadRandomMinigame();
    }

    /**
     * Load a random minigame from the list
     */
    public void loadRandomMinigame() {
        int randomIndex = random.nextInt(minigames.size());
        currentMinigame = minigames.get(randomIndex);
        currentMinigame.reset();
    }

    /**
     * Get the currently active minigame
     * @return the current minigame
     */
    public BaseMinigame getCurrentMinigame() {
        return currentMinigame;
    }

    /**
     * Restart the current minigame without changing which minigame it is
     * Used for the "Replay" option
     */
    public void restartMinigame() {
        if (currentMinigame != null) {
            currentMinigame.reset();
        }
    }

    /**
     * Get the name/type of the current minigame
     * @return minigame name as String
     */
    public String getCurrentMinigameName() {
        if (currentMinigame == null) {
            return "Unknown";
        }
        return currentMinigame.getClass().getSimpleName();
    }

    /**
     * Get the total number of minigames available
     * @return number of minigames
     */
    public int getTotalMinigames() {
        return minigames.size();
    }

    /**
     * Get a specific minigame by index
     * @param index the index of the minigame
     * @return the minigame at that index, or null if invalid
     */
    public BaseMinigame getMinigameAt(int index) {
        if (index >= 0 && index < minigames.size()) {
            return minigames.get(index);
        }
        return null;
    }

    /**
     * Add a new minigame to the pool (for extending the game later)
     * @param minigame the minigame to add
     */
    public void addMinigame(BaseMinigame minigame) {
        minigames.add(minigame);
    }

    /**
     * Remove a minigame from the pool
     * @param index the index of the minigame to remove
     */
    public void removeMinigame(int index) {
        if (index >= 0 && index < minigames.size()) {
            minigames.remove(index);
        }
    }

    /**
     * Reset all minigames (clears state for fresh game)
     */
    public void resetAllMinigames() {
        for (BaseMinigame minigame : minigames) {
            minigame.reset();
        }
    }
}