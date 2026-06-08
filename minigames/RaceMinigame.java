package minigames;

import java.awt.Color;
import java.awt.Font;
import java.awt.Graphics;

import core.InputHandler;

public class RaceMinigame extends BaseMinigame {

    private int player1X;
    private int player2X;

    private final int START_X = 100;
    private final int FINISH_X = 1100;
    private final int MOVE_DISTANCE = 8;

    public RaceMinigame() {
        reset();
    }

    @Override
    public void update(InputHandler inputHandler) {

        if (gameOver) {
            return;
        }

        // Move Player 1
        if (inputHandler.isPlayer1PressedThisFrame()) {
            player1X += MOVE_DISTANCE;
        }

        // Move Player 2
        if (inputHandler.isPlayer2PressedThisFrame()) {
            player2X += MOVE_DISTANCE;
        }

        // Check winner
        if (player1X >= FINISH_X) {
            endGame("PLAYER1");
        } else if (player2X >= FINISH_X) {
            endGame("PLAYER2");
        }
    }

    @Override
    public void draw(Graphics g) {

        // Background
        g.setColor(Color.DARK_GRAY);
        g.fillRect(0, 0, 1200, 800);

        // Title
        g.setColor(Color.WHITE);
        g.setFont(new Font("Arial", Font.BOLD, 28));
        g.drawString("Race Minigame", 420, 50);

        // Instructions
        g.setFont(new Font("Arial", Font.PLAIN, 18));
        g.drawString("Player 1: Z Key", 200, 90);
        g.drawString("Player 2: / Key", 850, 90);

        // Track lines
        g.setColor(Color.WHITE);
        g.drawLine(100, 300, 1100, 300);
        g.drawLine(100, 450, 1100, 450);

        // Finish line
        g.setColor(Color.YELLOW);
        g.fillRect(FINISH_X, 250, 10, 250);

        int playerSize = 40;
        int player1Y = 280;
        int player2Y = 430;

        // Draw player bodies
        g.setColor(Color.BLUE);
        g.fillOval(player1X, player1Y, 30, 30);
        g.setColor(Color.RED);
        g.fillOval(player2X, player2Y, 30, 30);

        // Draw player icons above the bodies
        if (player1Icon != null) {
            drawPlayerIcon(g, player1Icon, player1X - 5, player1Y - 50, 40, 40);
        }
        if (player2Icon != null) {
            drawPlayerIcon(g, player2Icon, player2X - 5, player2Y - 50, 40, 40);
        }

        // Winner message
        if (gameOver) {
            g.setColor(Color.GREEN);
            g.setFont(new Font("Arial", Font.BOLD, 36));

            if ("PLAYER1".equals(winner)) {
                g.drawString("PLAYER 1 WINS!", 390, 500);
            } else {
                g.drawString("PLAYER 2 WINS!", 390, 500);
            }
        }
    }

    @Override
    public void reset() {
        gameOver = false;
        winner = null;

        player1X = START_X;
        player2X = START_X;
    }
}