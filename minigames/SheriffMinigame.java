package minigames;

import java.awt.Color;
import java.awt.Font;
import java.awt.Graphics;
import java.util.Random;

import core.InputHandler;

public class SheriffMinigame extends BaseMinigame {

    private long startTime;
    private long drawTime;

    private boolean drawSignalShown;

    private Random random;

    public SheriffMinigame() {
        random = new Random();
        reset();
    }

    @Override
    public void update(InputHandler inputHandler) {

        if (gameOver) {
            return;
        }

        long currentTime = System.currentTimeMillis();

        // Show DRAW signal after a random delay
        if (!drawSignalShown && currentTime >= drawTime) {
            drawSignalShown = true;
        }

        // Someone shoots early
        if (!drawSignalShown) {

            if (inputHandler.isPlayer1PressedThisFrame()) {
                endGame("PLAYER2");
                return;
            }

            if (inputHandler.isPlayer2PressedThisFrame()) {
                endGame("PLAYER1");
                return;
            }
        }

        // DRAW signal has appeared
        if (drawSignalShown) {

            if (inputHandler.isPlayer1PressedThisFrame()) {
                endGame("PLAYER1");
                return;
            }

            if (inputHandler.isPlayer2PressedThisFrame()) {
                endGame("PLAYER2");
            }
        }
    }

    @Override
    public void draw(Graphics g) {

        // Background
        g.setColor(new Color(210, 180, 140));
        g.fillRect(0, 0, 1200, 800);

        // Title
        g.setColor(Color.BLACK);
        g.setFont(new Font("Arial", Font.BOLD, 30));
        g.drawString("Sheriff Showdown", 420, 60);

        // Instructions
        g.setFont(new Font("Arial", Font.PLAIN, 18));
        g.drawString("Wait for DRAW!", 500, 100);
        g.drawString("Player 1: Z Key", 200, 130);
        g.drawString("Player 2: / Key", 850, 130);

        // Sheriff 1 (centered in left half)
        g.setColor(Color.BLUE);
        g.fillRect(225, 300, 150, 200);
        if (player1Icon != null) {
            drawPlayerIcon(g, player1Icon, 225 + 35, 300 - 90, 80, 80);
        }

        // Sheriff 2 (centered in right half)
        g.setColor(Color.RED);
        g.fillRect(825, 300, 150, 200);
        if (player2Icon != null) {
            drawPlayerIcon(g, player2Icon, 825 + 35, 300 - 90, 80, 80);
        }

        // Center message
        if (!drawSignalShown && !gameOver) {

            g.setColor(Color.BLACK);
            g.setFont(new Font("Arial", Font.BOLD, 40));
            g.drawString("WAIT...", 500, 450);

        } else if (drawSignalShown && !gameOver) {

            g.setColor(Color.RED);
            g.setFont(new Font("Arial", Font.BOLD, 60));
            g.drawString("DRAW!", 480, 450);
        }

        // Winner message
        if (gameOver) {

            g.setColor(Color.GREEN);
            g.setFont(new Font("Arial", Font.BOLD, 36));

            if ("PLAYER1".equals(winner)) {
                g.drawString("PLAYER 1 WINS!", 390, 520);
            } else {
                g.drawString("PLAYER 2 WINS!", 390, 520);
            }
        }
    }

    @Override
    public void reset() {

        gameOver = false;
        winner = null;

        drawSignalShown = false;

        startTime = System.currentTimeMillis();

        // Random delay between 2 and 5 seconds
        drawTime = startTime + (2000 + random.nextInt(3000));
    }
}