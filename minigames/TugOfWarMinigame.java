package minigames;

import java.awt.Color;
import java.awt.Font;
import java.awt.Graphics;

import core.InputHandler;

public class TugOfWarMinigame extends BaseMinigame {

    private int ropePosition;

    private final int CENTER = 600;
    private final int LEFT_WIN = 350;
    private final int RIGHT_WIN = 850;
    private final int PULL_AMOUNT = 10;

    public TugOfWarMinigame() {
        reset();
    }

    @Override
    public void update(InputHandler inputHandler) {

        if (gameOver) {
            return;
        }

        // Player 1 pulls left
        if (inputHandler.isPlayer1PressedThisFrame()) {
            ropePosition -= PULL_AMOUNT;
        }

        // Player 2 pulls right
        if (inputHandler.isPlayer2PressedThisFrame()) {
            ropePosition += PULL_AMOUNT;
        }

        // Check win conditions
        if (ropePosition <= LEFT_WIN) {
            endGame("PLAYER1");
        } else if (ropePosition >= RIGHT_WIN) {
            endGame("PLAYER2");
        }
    }

    @Override
    public void draw(Graphics g) {

        // Background
        g.setColor(new Color(34, 139, 34));
        g.fillRect(0, 0, 1200, 800);

        // Title
        g.setColor(Color.WHITE);
        g.setFont(new Font("Arial", Font.BOLD, 28));
        g.drawString("Tug Of War", 450, 50);

        // Instructions
        g.setFont(new Font("Arial", Font.PLAIN, 18));
        g.drawString("Player 1: Z Key", 200, 90);
        g.drawString("Player 2: / Key", 850, 90);

        // Center line
        g.setColor(Color.YELLOW);
        g.drawLine(CENTER, 200, CENTER, 400);

        // Win lines
        g.setColor(Color.RED);
        g.drawLine(LEFT_WIN, 200, LEFT_WIN, 400);
        g.drawLine(RIGHT_WIN, 200, RIGHT_WIN, 400);

        // Rope
        g.setColor(new Color(139, 69, 19));
        g.fillRect(350, 290, 500, 20);

        // Rope marker
        g.setColor(Color.WHITE);
        g.fillRect(ropePosition - 10, 270, 20, 60);

        int iconSize = 70;
        int player1BodyX = 280;
        int player1BodyY = 280;
        int player2BodyX = 900;
        int player2BodyY = 280;

        // Player bodies
        g.setColor(Color.BLUE);
        g.fillRect(player1BodyX, player1BodyY, 40, 80);
        g.setColor(Color.RED);
        g.fillRect(player2BodyX, player2BodyY, 40, 80);

        // Draw icons above the bodies
        if (player1Icon != null) {
            drawPlayerIcon(g, player1Icon, player1BodyX - 5, player1BodyY - iconSize - 10, iconSize, iconSize);
        } else {
            g.setColor(Color.BLUE);
            g.drawString("P1", 300, 300);
        }

        if (player2Icon != null) {
            drawPlayerIcon(g, player2Icon, player2BodyX - 5, player2BodyY - iconSize - 10, iconSize, iconSize);
        } else {
            g.setColor(Color.RED);
            g.drawString("P2", 900, 300);
        }

        // Winner message
        if (gameOver) {

            g.setColor(Color.WHITE);
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
        ropePosition = CENTER;
    }
}