package minigames;

import entities.Obstacle;
import java.awt.Color;
import java.awt.Font;
import java.awt.Graphics;
import java.awt.Rectangle;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;

import core.GamePanel;
import core.InputHandler;

public class DodgeMinigame extends BaseMinigame {

    private int player1X;
    private int player2X;

    private int playerY;
    private int playerSize;

    private static final int OBSTACLE_COUNT = 6;
    private static final int MIN_OBSTACLE_SIZE = 28;
    private static final int MAX_OBSTACLE_SIZE = 52;
    private static final int OBSTACLE_SPEED = 8;
    private static final String FONT_FAMILY = "Arial";

    private List<Obstacle> obstacles;
    private Random random;

    // Used to alternate movement direction
    private boolean player1MoveRight;
    private boolean player2MoveRight;

    public DodgeMinigame() {
        random = new Random();
        reset();
    }

    @Override
    public void update(InputHandler inputHandler) {

        if (gameOver) {
            return;
        }

        // Player 1 movement
        if (inputHandler.isPlayer1PressedThisFrame()) {

            if (player1MoveRight) {
                player1X += 100;
            } else {
                player1X -= 100;
            }

            player1MoveRight = !player1MoveRight;
        }

        // Player 2 movement
        if (inputHandler.isPlayer2PressedThisFrame()) {

            if (player2MoveRight) {
                player2X += 100;
            } else {
                player2X -= 100;
            }

            player2MoveRight = !player2MoveRight;
        }

        // Keep players on screen inside their half
        int halfWidth = GamePanel.WIDTH / 2;
        player1X = Math.clamp(player1X, 50, halfWidth - playerSize - 50);
        player2X = Math.clamp(player2X, halfWidth + 50, GamePanel.WIDTH - playerSize - 50);

        // Move obstacles and respawn if they leave the screen
        for (Obstacle obstacle : obstacles) {
            obstacle.setY(obstacle.getY() + OBSTACLE_SPEED);
            if (obstacle.getY() > GamePanel.HEIGHT) {
                respawnObstacle(obstacle);
            }
        }

        // Collision checks
        if (checkCollision(player1X, playerY)) {
            endGame("PLAYER2");
        } else if (checkCollision(player2X, playerY)) {
            endGame("PLAYER1");
        }
    }

    private boolean checkCollision(int playerX, int playerY) {
        Rectangle playerBounds = new Rectangle(playerX, playerY, playerSize, playerSize);
        for (Obstacle obstacle : obstacles) {
            Rectangle obstacleBounds = new Rectangle(obstacle.getX(), obstacle.getY(), obstacle.getWidth(), obstacle.getHeight());
            if (playerBounds.intersects(obstacleBounds)) {
                return true;
            }
        }
        return false;
    }

    private void respawnObstacle(Obstacle obstacle) {
        int width = GamePanel.WIDTH;
        int x = random.nextInt(width - obstacle.getWidth() - 100) + 50;
        int y = -random.nextInt(300) - obstacle.getHeight();
        obstacle.setX(x);
        obstacle.setY(y);
    }

    @Override
    public void draw(Graphics g) {

        int width = g.getClipBounds().width;
        int height = g.getClipBounds().height;

        // Background
        g.setColor(Color.BLACK);
        g.fillRect(0, 0, width, height);

        // Title
        g.setColor(Color.WHITE);
        g.setFont(new Font(FONT_FAMILY, Font.BOLD, 28));
        g.drawString("Dodge Minigame", 30, 40);

        g.setFont(new Font(FONT_FAMILY, Font.PLAIN, 18));
        g.drawString("Avoid the falling objects!", 30, 70);

        // Divider
        g.setColor(Color.GRAY);
        g.drawLine(width / 2, 0, width / 2, height);

        // Player 1
        g.setColor(Color.BLUE);
        g.fillRect(player1X, playerY, playerSize, playerSize);
        if (player1Icon != null) {
            drawPlayerIcon(g, player1Icon, player1X, playerY - 50, playerSize, playerSize);
        }

        // Player 2
        g.setColor(Color.RED);
        g.fillRect(player2X, playerY, playerSize, playerSize);
        if (player2Icon != null) {
            drawPlayerIcon(g, player2Icon, player2X, playerY - 50, playerSize, playerSize);
        }

        // Obstacles
        g.setColor(Color.YELLOW);
        for (Obstacle obstacle : obstacles) {
            g.fillOval(obstacle.getX(), obstacle.getY(), obstacle.getWidth(), obstacle.getHeight());
        }

        // Winner text
        if (gameOver) {

            g.setColor(Color.GREEN);
            g.setFont(new Font(FONT_FAMILY, Font.BOLD, 36));

            if ("PLAYER1".equals(winner)) {
                g.drawString("PLAYER 1 WINS!", 220, 300);
            } else {
                g.drawString("PLAYER 2 WINS!", 220, 300);
            }
        }
    }

    @Override
    public void reset() {

        gameOver = false;
        winner = null;

        playerSize = 40;
        playerY = GamePanel.HEIGHT - playerSize - 50;
        player1X = 200;
        player2X = GamePanel.WIDTH - 200 - playerSize;

        obstacles = new ArrayList<>();
        for (int i = 0; i < OBSTACLE_COUNT; i++) {
            int size = random.nextInt(MAX_OBSTACLE_SIZE - MIN_OBSTACLE_SIZE + 1) + MIN_OBSTACLE_SIZE;
            int x = random.nextInt(GamePanel.WIDTH - size - 100) + 50;
            int y = -random.nextInt(GamePanel.HEIGHT);
            obstacles.add(new Obstacle(x, y, size, size));
        }

        player1MoveRight = true;
        player2MoveRight = false;
    }
}