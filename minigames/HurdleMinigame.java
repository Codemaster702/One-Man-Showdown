package minigames;

import java.awt.Color;
import java.awt.Font;
import java.awt.Graphics;
import java.util.Random;

import core.InputHandler;

public class HurdleMinigame extends BaseMinigame {

    // Player positions
    private int player1Y;
    private int player2Y;

    // Vertical velocity
    private double player1Velocity;
    private double player2Velocity;

    // Ground position
    private final int GROUND_Y = 350;

    // Physics
    private final double GRAVITY = 0.8;
    private final double JUMP_FORCE = -12;

    // Hurdle
    private int hurdleX;
    private final int HURDLE_WIDTH = 30;
    private final int HURDLE_HEIGHT = 60;
    private final int HURDLE_SPEED = 8;
    private final Random random = new Random();

    public HurdleMinigame() {
        reset();
    }

    @Override
    public void update(InputHandler inputHandler) {

        if (gameOver) {
            return;
        }

        // Jump input
        if (inputHandler.isPlayer1PressedThisFrame()
                && player1Y >= GROUND_Y) {

            player1Velocity = JUMP_FORCE;
        }

        if (inputHandler.isPlayer2PressedThisFrame()
                && player2Y >= GROUND_Y) {

            player2Velocity = JUMP_FORCE;
        }

        // Gravity
        player1Velocity += GRAVITY;
        player2Velocity += GRAVITY;

        player1Y += player1Velocity;
        player2Y += player2Velocity;

        // Stay on ground
        if (player1Y > GROUND_Y) {
            player1Y = GROUND_Y;
            player1Velocity = 0;
        }

        if (player2Y > GROUND_Y) {
            player2Y = GROUND_Y;
            player2Velocity = 0;
        }

        // Move hurdle
        hurdleX -= HURDLE_SPEED;

        // Respawn hurdle
        if (hurdleX < -HURDLE_WIDTH) {
            hurdleX = 1250;
        }

        // Check collisions
        if (checkCollision(150, player1Y)) {
            endGame("PLAYER2");
        } else if (checkCollision(550, player2Y)) {
            endGame("PLAYER1");
        }
    }

    private boolean checkCollision(int playerX, int playerY) {

        int playerWidth = 40;
        int playerHeight = 40;

        int hurdleY = GROUND_Y - HURDLE_HEIGHT + 40;

        return playerX < hurdleX + HURDLE_WIDTH
                && playerX + playerWidth > hurdleX
                && playerY < hurdleY + HURDLE_HEIGHT
                && playerY + playerHeight > hurdleY;
    }

    @Override
    public void draw(Graphics g) {

        // Sky
        g.setColor(new Color(135, 206, 235));
        g.fillRect(0, 0, 1200, 800);

        // Ground
        g.setColor(Color.DARK_GRAY);
        g.fillRect(0, 390, 1200, 210);

        // Title
        g.setColor(Color.BLACK);
        g.setFont(new Font("Arial", Font.BOLD, 28));
        g.drawString("Hurdle Run", 300, 50);

        g.setFont(new Font("Arial", Font.PLAIN, 18));
        g.drawString("Jump over the hurdle!", 280, 90);

        // Player 1
        g.setColor(Color.BLUE);
        g.fillRect(150, player1Y, 40, 40);
        if (player1Icon != null) {
            drawPlayerIcon(g, player1Icon, 150, player1Y - 50, 40, 40);
        }

        // Player 2
        g.setColor(Color.RED);
        g.fillRect(550, player2Y, 40, 40);
        if (player2Icon != null) {
            drawPlayerIcon(g, player2Icon, 550, player2Y - 50, 40, 40);
        }

        // Hurdle
        g.setColor(new Color(139, 69, 19));
        g.fillRect(
                hurdleX,
                GROUND_Y - HURDLE_HEIGHT + 40,
                HURDLE_WIDTH,
                HURDLE_HEIGHT
        );

        // Winner message
        if (gameOver) {

            g.setColor(Color.GREEN);
            g.setFont(new Font("Arial", Font.BOLD, 36));

            if ("PLAYER1".equals(winner)) {
                g.drawString("PLAYER 1 WINS!", 220, 520);
            } else {
                g.drawString("PLAYER 2 WINS!", 220, 520);
            }
        }
    }

    @Override
    public void reset() {

        gameOver = false;
        winner = null;

        player1Y = GROUND_Y;
        player2Y = GROUND_Y;

        player1Velocity = 0;
        player2Velocity = 0;

        hurdleX = 850;
    }
}