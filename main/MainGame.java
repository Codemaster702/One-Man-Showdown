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
    public static final int FPS = 30;

    // The game's movement/speeds were tuned for 60 logic updates per second.
    // Logic is decoupled from the render rate so the game runs at the same pace
    // no matter what FPS is — at 30 FPS we simply run two logic steps per frame.
    private static final int LOGIC_UPS = 60;

    public MainGame() {
        // Set up the window
        setTitle("One Fun Showdown");
        setSize(WIDTH, HEIGHT);
        // DISPOSE rather than EXIT: System.exit is unsupported in CheerpJ's browser sandbox.
        setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
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
                    startNextMinigame();
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

        // Looping menu theme — plays while the title screen is up.
        SoundManager.playMusic("assets/sounds/menu_music.wav");

        // Start the game loop
        startGameLoop();
    }

    private void startGameLoop() {
        // A Swing Timer drives the loop on the Event Dispatch Thread. CheerpJ
        // schedules Java threads cooperatively on the browser's single main thread,
        // so a busy-wait while(true) loop would never yield and freeze the tab.
        // The Timer fires on the EDT, which keeps both the browser and Swing happy.
        int delayMillis = 1000 / FPS;
        final long[] lastTime = { System.nanoTime() };
        final double[] accumulator = { 0.0 };
        double nanosPerLogicStep = 1_000_000_000.0 / LOGIC_UPS;

        javax.swing.Timer gameTimer = new javax.swing.Timer(delayMillis, e -> {
            long now = System.nanoTime();
            accumulator[0] += now - lastTime[0];
            lastTime[0] = now;

            // Run as many fixed 1/60s logic steps as real time has elapsed.
            // Cap the catch-up so a hitch can't trigger a "spiral of death".
            int steps = 0;
            while (accumulator[0] >= nanosPerLogicStep && steps < 5) {
                update();
                accumulator[0] -= nanosPerLogicStep;
                steps++;
            }
            if (steps == 5) {
                accumulator[0] = 0; // dropped behind; resync instead of piling up
            }

            gamePanel.repaint();
        });
        gameTimer.start();
    }

    private void update() {
        String state = stateManager.getState();

        if (state.equals("MENU")) {
            // Handle menu input
            if (inputHandler.isKeyPressedThisFrame(KeyEvent.VK_ENTER)) {
                startNextMinigame();
            }

        } else if (state.equals("PLAYING")) {
            // ESC pauses the round and freezes the minigame.
            if (inputHandler.isKeyPressedThisFrame(KeyEvent.VK_ESCAPE)) {
                SoundManager.play("assets/sounds/click.wav");
                SoundManager.pauseMusic();
                stateManager.setState(GameStateManager.PAUSED);
            } else {
                // Update current minigame
                minigameManager.getCurrentMinigame().update(inputHandler);

                // Check if minigame is over
                if (minigameManager.getCurrentMinigame().isGameOver()) {
                    String winner = minigameManager.getCurrentMinigame().getWinner();
                    scoreManager.updateScore(winner);
                    stateManager.setState("RESULTS");
                }
            }

        } else if (state.equals(GameStateManager.PAUSED)) {
            // Pause menu: next minigame, restart, quit to menu (ESC resumes).
            if (inputHandler.isKeyPressedThisFrame(KeyEvent.VK_ESCAPE)) {
                SoundManager.play("assets/sounds/click.wav");
                SoundManager.resumeMusic();
                stateManager.setState(GameStateManager.PLAYING);

            } else if (inputHandler.isKeyPressedThisFrame(KeyEvent.VK_N)) {
                // Skip to a fresh random minigame (no score awarded).
                startNextMinigame(); // plays its own click + ambient

            } else if (inputHandler.isKeyPressedThisFrame(KeyEvent.VK_R)) {
                SoundManager.play("assets/sounds/click.wav");
                minigameManager.restartMinigame();
                // Restart the same minigame — restart its ambient track.
                SoundManager.playMusic(minigameManager.getCurrentMinigame().getAmbientMusic());
                stateManager.setState(GameStateManager.PLAYING);

            } else if (inputHandler.isKeyPressedThisFrame(KeyEvent.VK_Q)) {
                // Quit to the title screen — reset scores and restore the menu theme.
                SoundManager.play("assets/sounds/click.wav");
                scoreManager.reset();
                SoundManager.playMusic("assets/sounds/menu_music.wav");
                stateManager.setState(GameStateManager.MENU);
            }

        } else if (state.equals("RESULTS")) {
            // Handle replay/next options
            if (inputHandler.isKeyPressedThisFrame(KeyEvent.VK_R)) {
                SoundManager.play("assets/sounds/click.wav");
                minigameManager.restartMinigame();
                scoreManager.undoLastRoundScore();
                // Replay the same minigame — restart its ambient track.
                SoundManager.playMusic(minigameManager.getCurrentMinigame().getAmbientMusic());
                stateManager.setState("PLAYING");

            } else if (inputHandler.isKeyPressedThisFrame(KeyEvent.VK_N)) {
                if (scoreManager.hasWinner()) {
                    // Overall match decided — swap to the victory theme.
                    SoundManager.play("assets/sounds/click.wav");
                    SoundManager.playMusic("assets/sounds/victory_music.wav");
                    stateManager.setState("WINNER");
                } else {
                    startNextMinigame(); // plays its own click + ambient
                }
            }

        } else if (state.equals("WINNER")) {
            // Handle game restart
            if (inputHandler.isKeyPressedThisFrame(KeyEvent.VK_ENTER)) {
                SoundManager.play("assets/sounds/click.wav");
                scoreManager.reset();
                // Back to the title screen — restore the menu theme.
                SoundManager.playMusic("assets/sounds/menu_music.wav");
                stateManager.setState("MENU");
            }
        }

        inputHandler.clearPressedKeys();
    }

    /**
     * Begins the next round: a UI click, a freshly chosen random minigame,
     * and that minigame's looping ambient track. Shared by the menu (Enter /
     * Start button) and the "Next" option on the results screen.
     */
    private void startNextMinigame() {
        SoundManager.play("assets/sounds/click.wav");
        minigameManager.loadRandomMinigame();
        scoreManager.resetRoundWinner();
        // Swap the looping background music to the new minigame's theme.
        SoundManager.playMusic(minigameManager.getCurrentMinigame().getAmbientMusic());
        stateManager.setState(GameStateManager.PLAYING);
    }

    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> new MainGame());
    }
}