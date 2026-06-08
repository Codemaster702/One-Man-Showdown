package main;

import javax.swing.JFrame;
import javax.swing.SwingUtilities;
import java.awt.event.KeyEvent;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import core.*;
import managers.*;
import ui.*;

public class MainGame extends JFrame {
    private GamePanel gamePanel;
    private GameStateManager stateManager;
    private MinigameManager minigameManager;
    private ScoreManager scoreManager;
    private InputHandler inputHandler;
    private UIManager uiManager;

    public static final int WIDTH = 1200;
    public static final int HEIGHT = 800;
    public static final int FPS = 60;

    public MainGame() {
        // Set up the window
        setTitle("One Fun Showdown");
        setSize(WIDTH, HEIGHT);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setResizable(false);
        setLocationRelativeTo(null);

        // Initialize all managers
        scoreManager = new ScoreManager();
        inputHandler = new InputHandler();
        stateManager = new GameStateManager();
        minigameManager = new MinigameManager();
        uiManager = new UIManager(scoreManager);

        // Clear initial input state before game loop
        inputHandler.clearPressedKeys();

        // Create and add GamePanel
        gamePanel = new GamePanel(uiManager, minigameManager, stateManager, scoreManager);
        gamePanel.addKeyListener(inputHandler);
        gamePanel.addMouseListener(new MouseAdapter() {
            @Override
            public void mousePressed(MouseEvent e) {
                if (stateManager.getState().equals(GameStateManager.MENU)
                        && uiManager.isStartButtonClicked(e.getX(), e.getY())) {
                    stateManager.setState(GameStateManager.PLAYING);
                    minigameManager.loadRandomMinigame();
                    scoreManager.resetRoundWinner();
                }
            }
        });
        gamePanel.addMouseMotionListener(new MouseAdapter() {
            @Override
            public void mouseMoved(MouseEvent e) {
                if (stateManager.getState().equals(GameStateManager.MENU)) {
                    uiManager.updateStartButtonHover(e.getX(), e.getY());
                }
            }
        });
        gamePanel.setFocusable(true);
        add(gamePanel);

        setVisible(true);
        gamePanel.requestFocusInWindow();

        // Start the game loop
        startGameLoop();
    }

    private void startGameLoop() {
        Thread gameThread = new Thread(() -> {
            long lastTime = System.nanoTime();
            long targetNanoPerFrame = 1_000_000_000 / FPS;

            while (true) {
                long currentTime = System.nanoTime();
                long deltaTime = currentTime - lastTime;

                if (deltaTime >= targetNanoPerFrame) {
                    update();
                    gamePanel.repaint();
                    lastTime = currentTime;
                }
            }
        });
        gameThread.start();
    }

    private void update() {
        String state = stateManager.getState();

        if (state.equals("MENU")) {
            // Handle menu input
            if (inputHandler.isKeyPressed(KeyEvent.VK_ENTER)) {
                stateManager.setState("PLAYING");
                minigameManager.loadRandomMinigame();
                scoreManager.resetRoundWinner();
            }

        } else if (state.equals("PLAYING")) {
            // Update current minigame
            minigameManager.getCurrentMinigame().update(inputHandler);

            // Check if minigame is over
            if (minigameManager.getCurrentMinigame().isGameOver()) {
                String winner = minigameManager.getCurrentMinigame().getWinner();
                scoreManager.updateScore(winner);
                stateManager.setState("RESULTS");
            }

        } else if (state.equals("RESULTS")) {
            // Handle replay/next options
            if (inputHandler.isKeyPressed(KeyEvent.VK_R)) {
                minigameManager.restartMinigame();
                scoreManager.resetRoundWinner();
                stateManager.setState("PLAYING");

            } else if (inputHandler.isKeyPressed(KeyEvent.VK_N)) {
                if (scoreManager.hasWinner()) {
                    stateManager.setState("WINNER");
                } else {
                    minigameManager.loadRandomMinigame();
                    scoreManager.resetRoundWinner();
                    stateManager.setState("PLAYING");
                }
            }

        } else if (state.equals("WINNER")) {
            // Handle game restart
            if (inputHandler.isKeyPressed(KeyEvent.VK_ENTER)) {
                scoreManager.reset();
                stateManager.setState("MENU");
            }
        }

        inputHandler.clearPressedKeys();
    }

    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> new MainGame());
    }
}