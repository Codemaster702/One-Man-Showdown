package core;

import javax.swing.*;
import managers.*;
import minigames.*;
import java.awt.*;
import java.awt.event.KeyEvent;

/**
 * The main rendering panel - controls what gets drawn and in what order
 * This is the game's render layer
 */
public class GamePanel extends JPanel {
    private managers.UIManager uiManager;
    private MinigameManager minigameManager;
    private GameStateManager stateManager;
    private ScoreManager scoreManager;
    private InputHandler inputHandler;
    private boolean scoreUpdatedThisGame;

    public static final int WIDTH = 1200;
    public static final int HEIGHT = 800;
    public static final int FPS = 60;

    /**
     * Constructor - initialize the panel with all necessary managers
     * @param uiManager handles menu and UI rendering
     * @param minigameManager handles minigame logic
     * @param stateManager tracks game state
     * @param scoreManager tracks scores
     */
    public GamePanel(managers.UIManager uiManager, MinigameManager minigameManager,
                     GameStateManager stateManager, ScoreManager scoreManager) {
        this.uiManager = uiManager;
        this.minigameManager = minigameManager;
        this.stateManager = stateManager;
        this.scoreManager = scoreManager;
        this.scoreUpdatedThisGame = false;

        setPreferredSize(new Dimension(WIDTH, HEIGHT));
        setBackground(Color.BLACK);
        setFocusable(true);
    }

    /**
     * Set the input handler for this panel
     * @param inputHandler the input handler to use
     */
    public void setInputHandler(InputHandler inputHandler) {
        this.inputHandler = inputHandler;
    }

    @Override
    protected void paintComponent(Graphics g) {
        super.paintComponent(g);

        // Enable anti-aliasing for smoother graphics
        if (g instanceof Graphics2D) {
            Graphics2D g2d = (Graphics2D) g;
            g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING, 
                                RenderingHints.VALUE_ANTIALIAS_ON);
        }

        // Draw based on current game state
        String currentState = stateManager.getState();

        if (currentState.equals(GameStateManager.MENU)) {
            drawMenuScreen(g);

        } else if (currentState.equals(GameStateManager.PLAYING)) {
            drawGameplayScreen(g);

        } else if (currentState.equals(GameStateManager.RESULTS)) {
            drawResultsScreen(g);

        } else if (currentState.equals(GameStateManager.WINNER)) {
            drawWinnerScreen(g);
        }
    }

    /**
     * Draw the main menu screen
     * Drawing order: background -> menu
     * @param g Graphics object
     */
    private void drawMenuScreen(Graphics g) {
        uiManager.drawMainMenu(g);
    }

    /**
     * Draw the active minigame with HUD
     * Drawing order: minigame world -> HUD overlay
     * @param g Graphics object
     */
    private void drawGameplayScreen(Graphics g) {
        // Draw the current minigame (handles its own background/visuals)
        BaseMinigame currentMinigame = minigameManager.getCurrentMinigame();
        if (currentMinigame != null) {
            currentMinigame.setPlayerIcons(uiManager.getPlayer1Icon(), uiManager.getPlayer2Icon());
            currentMinigame.draw(g);
        }

        // Draw HUD on top (scores, minigame name)
        uiManager.drawHUD(g, minigameManager.getCurrentMinigameName());
    }

    /**
     * Draw the results screen after a minigame
     * Drawing order: minigame fades -> results overlay
     * @param g Graphics object
     */
    private void drawResultsScreen(Graphics g) {
        // Draw minigame in background (faded)
        BaseMinigame currentMinigame = minigameManager.getCurrentMinigame();
        if (currentMinigame != null) {
            currentMinigame.setPlayerIcons(uiManager.getPlayer1Icon(), uiManager.getPlayer2Icon());
            currentMinigame.draw(g);
        }

        // Draw semi-transparent overlay with results
        uiManager.drawResults(g);
    }

    /**
     * Draw the final winner screen
     * Drawing order: background -> winner announcement
     * @param g Graphics object
     */
    private void drawWinnerScreen(Graphics g) {
        uiManager.drawWinnerScreen(g);
    }

    /**
     * Get the preferred size of this panel
     * @return dimension of the game window
     */
    @Override
    public Dimension getPreferredSize() {
        return new Dimension(WIDTH, HEIGHT);
    }

    /**
     * Update game logic - called from the game loop in MainGame
     * Processes input and updates the current minigame
     */
    public void updateGame() {
        if (inputHandler == null) {
            return;
        }

        String currentState = stateManager.getState();

        if (currentState.equals(GameStateManager.MENU)) {
            updateMenuState();

        } else if (currentState.equals(GameStateManager.PLAYING)) {
            updateGameplayState();

        } else if (currentState.equals(GameStateManager.RESULTS)) {
            updateResultsState();

        } else if (currentState.equals(GameStateManager.WINNER)) {
            updateWinnerState();
        }
    }

    /**
     * Handle menu input
     */
    private void updateMenuState() {
        if (inputHandler.isKeyPressedThisFrame(KeyEvent.VK_ENTER)) {
            scoreUpdatedThisGame = false;
            stateManager.setState(GameStateManager.PLAYING);
            minigameManager.loadRandomMinigame();
            scoreManager.resetRoundWinner();
        }
    }

    /**
     * Update the currently active minigame
     */
    private void updateGameplayState() {
        BaseMinigame currentMinigame = minigameManager.getCurrentMinigame();

        if (currentMinigame != null) {
            // Update minigame logic
            currentMinigame.update(inputHandler);

            // Check if minigame is finished
            if (currentMinigame.isGameOver() && !scoreUpdatedThisGame) {
                // Update score manager with the winner immediately
                String winner = currentMinigame.getWinner();
                scoreManager.updateScore(winner);
                scoreUpdatedThisGame = true;
                stateManager.setState(GameStateManager.RESULTS);
            }
        }
    }

    /**
     * Handle results screen input (replay or next)
     */
    private void updateResultsState() {
        if (inputHandler.isKeyPressedThisFrame(KeyEvent.VK_R)) {
            // Replay the current minigame
            minigameManager.restartMinigame();
            scoreManager.resetRoundWinner();
            scoreUpdatedThisGame = false;
            stateManager.setState(GameStateManager.PLAYING);

        } else if (inputHandler.isKeyPressedThisFrame(KeyEvent.VK_N)) {
            // Check if someone won the overall game
            if (scoreManager.hasWinner()) {
                stateManager.setState(GameStateManager.WINNER);
            } else {
                // Load next random minigame
                minigameManager.loadRandomMinigame();
                scoreUpdatedThisGame = false;
                scoreManager.resetRoundWinner();
                stateManager.setState(GameStateManager.PLAYING);
            }
        }
    }

    /**
     * Handle winner screen input
     */
    private void updateWinnerState() {
        if (inputHandler.isEnterPressed()) {
            // Reset everything and return to menu
            scoreUpdatedThisGame = false;
            scoreManager.reset();
            minigameManager.resetAllMinigames();
            stateManager.setState(GameStateManager.MENU);
        }
    }

    /**
     * Import KeyEvent for key code constants
     */
    //private static final long serialVersionUID = 1L;
}