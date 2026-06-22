//Mohammed Shekhibrahim
//June 15 2026
//Tug of war minigame

package minigames;

import java.awt.Color;
import java.awt.Font;
import java.awt.Graphics;
import java.util.Random;

import managers.AssetManager;
import managers.SoundManager;
import core.InputHandler;

public class TugOfWarMinigame extends BaseMinigame {

    private double ropePosition;
    private double ropeVelocity;

    private static final int   CENTER       = 600;
    private static final int   LEFT_WIN     = 330;
    private static final int   RIGHT_WIN    = 870;
    // Rope is drawn wider than the win zone so its ends reach the players' hands.
    private static final int   ROPE_LEFT    = 240;
    private static final int   ROPE_RIGHT   = 930;
    private static final double PULL_IMPULSE = 6.5;  // velocity added per key press
    private static final double FRICTION     = 0.88; // velocity multiplied each frame

    // Visual "heave" flash
    private int p1PullFlash;
    private int p2PullFlash;

    // Pull sprite-sheet animation (6 frames of 32x32)
    private static final int PULL_FRAMES  = 6;
    private static final int PULL_FRAME_W = 32;
    private static final int PULL_FRAME_H = 32;
    private int p1Frame, p2Frame;       // current animation frame
    private int p1AnimTick, p2AnimTick; // ticks since last frame advance

    private final Random random = new Random();

    private java.awt.Image tugBg;
    private java.awt.Image ropeImg;
    private java.awt.Image knotImg;
    private java.awt.Image p1PullImg;
    private java.awt.Image p2PullImg;

    public TugOfWarMinigame() {
        reset();
        tugBg     = AssetManager.load("assets/environment/tow_bg.png");
        //ropeImg   = AssetManager.load("assets/environment/rope.png");
        knotImg   = AssetManager.load("assets/environment/bell.png");
        p1PullImg = AssetManager.load("assets/Players/p1_pull.png");
        p2PullImg = AssetManager.load("assets/Players/p2_pull.png");
    }

    @Override
    public String getAmbientMusic() {
        return "assets/sounds/race_ambient.wav";
    }

    @Override
    public void update(InputHandler input) {
        if (gameOver) return;

        if (p1PullFlash > 0) p1PullFlash--;
        if (p2PullFlash > 0) p2PullFlash--;

        if (input.isPlayer1PressedThisFrame()) {
            ropeVelocity -= PULL_IMPULSE;
            p1PullFlash = 8;
            SoundManager.play("assets/sounds/rope_pull.wav");
        }
        if (input.isPlayer2PressedThisFrame()) {
            ropeVelocity += PULL_IMPULSE;
            p2PullFlash = 8;
            SoundManager.play("assets/sounds/rope_pull.wav");
        }

        // Advance the pull animation while actively heaving, reset to the
        // first frame when idle.
        if (p1PullFlash > 0) {
            if (++p1AnimTick >= 2) { p1AnimTick = 0; p1Frame = (p1Frame + 1) % PULL_FRAMES; }
        } else { p1Frame = 0; p1AnimTick = 0; }
        if (p2PullFlash > 0) {
            if (++p2AnimTick >= 2) { p2AnimTick = 0; p2Frame = (p2Frame + 1) % PULL_FRAMES; }
        } else { p2Frame = 0; p2AnimTick = 0; }

        ropeVelocity *= FRICTION;
        ropePosition += ropeVelocity;

        // Clamp position within drawable range
        ropePosition = Math.max(LEFT_WIN - 20, Math.min(ropePosition, RIGHT_WIN + 20));

        if (ropePosition <= LEFT_WIN)  endGame("PLAYER1");
        else if (ropePosition >= RIGHT_WIN) endGame("PLAYER2");
    }

    @Override
    public void draw(Graphics g) {
        // Grassy background
        if (tugBg != null) {
            g.drawImage(tugBg, 0, 0, 1200, 800, null);
        } else {
            g.setColor(new Color(34, 120, 34));
            g.fillRect(0, 0, 1200, 800);
            g.setColor(new Color(25, 90, 25));
            g.fillRect(0, 500, 1200, 300);
        }

        // Title
        g.setColor(Color.WHITE);
        g.setFont(new Font("Arial", Font.BOLD, 28));
        drawCentered(g, "Tug of War", 600, 50);

        g.setFont(new Font("Arial", Font.PLAIN, 17));
        g.drawString("P1 (Blue): Z Key", 160, 90);
        g.drawString("P2 (Red):  / Key", 870, 90);

        int ropeY   = 560;
        int ropeTop = ropeY - 20;

        // Win zone markers
        g.setColor(new Color(200, 50, 50, 180));
        g.fillRect(LEFT_WIN - 8, ropeTop - 40, 16, 120);
        g.setColor(new Color(50, 50, 200, 180));
        g.fillRect(RIGHT_WIN - 8, ropeTop - 40, 16, 120);

        // Rope body
        int rp = (int) ropePosition;

        // Players and the rope slide at the same rate as the marker (knot),
        // matching its displacement from the centre.
        int pullShift = (int) (ropePosition - CENTER);

        if (ropeImg != null) {
            g.drawImage(ropeImg, ROPE_LEFT + pullShift, ropeY - 8, ROPE_RIGHT - ROPE_LEFT, 22, null);
        } else {
            for (int x = ROPE_LEFT; x < ROPE_RIGHT; x += 12) {
                int wave = (x % 24 < 12) ? 2 : -2;
                g.setColor(new Color(139, 90, 43));
                g.fillRect(x + pullShift, ropeY - 6 + wave, 12, 10);
            }
        }

        // Rope marker (bell) – hangs below the rope and swings like a
        // pendulum in the direction it is being pulled.
        int bellW = 40, bellH = 48;
        int bellTop = ropeY + 6;            // lowered so it hangs under the rope
        int bellX = rp - bellW / 2;

        // Swing angle proportional to pull speed; the bottom lags behind the
        // top as it is dragged, so it rotates clockwise/counter-clockwise.
        double swing = ropeVelocity * 0.06;
        swing = Math.max(-0.5, Math.min(0.5, swing));

        java.awt.Graphics2D g2 = (java.awt.Graphics2D) g;
        java.awt.geom.AffineTransform oldTx = g2.getTransform();
        g2.rotate(swing, rp, bellTop);       // pivot at the hang point (top of bell)

        if (knotImg != null) {
            g2.drawImage(knotImg, bellX, bellTop, bellW, bellH, null);
        } else {
            g2.setColor(Color.WHITE);
            g2.fillOval(bellX + 6, bellTop, 28, 36);
            g2.setColor(new Color(200, 200, 200));
            g2.drawOval(bellX + 6, bellTop, 28, 36);
        }

        g2.setTransform(oldTx);

        // Center line
        g.setColor(Color.YELLOW);
        g.fillRect(CENTER - 2, ropeTop - 50, 4, 140);

        // Direction indicator – arrow shows which way knot is drifting
        if (Math.abs(ropeVelocity) > 0.5) {
            drawArrow(g, rp, ropeY, ropeVelocity < 0);
        }

        // Players – animated pull sprite while heaving, frame 0 (rest) when idle
        int p1BodyX = 210 + pullShift, p2BodyX = 910 + pullShift, bodyY = 505;
        int spriteW = 96, spriteH = 96;
        int p1SpriteX = p1BodyX + 22 - spriteW / 2;
        int p2SpriteX = p2BodyX + 22 - spriteW / 2;
        int spriteY = bodyY + 90 - spriteH;   // bottom-align with the old body

        if (p1PullImg != null) {
            drawPullFrame(g, p1PullImg, p1Frame, p1SpriteX, spriteY, spriteW, spriteH);
        } else {
            g.setColor(p1PullFlash > 0 ? new Color(100, 150, 255) : Color.BLUE);
            g.fillRect(p1BodyX, bodyY, 45, 90);
        }
        if (p2PullImg != null) {
            drawPullFrame(g, p2PullImg, p2Frame, p2SpriteX, spriteY, spriteW, spriteH);
        } else {
            g.setColor(p2PullFlash > 0 ? new Color(255, 120, 120) : Color.RED);
            g.fillRect(p2BodyX, bodyY, 45, 90);
        }

        if (player1Icon != null) drawPlayerIcon(g, player1Icon, p1BodyX - 5, bodyY - 75, 70, 70);
        if (player2Icon != null) drawPlayerIcon(g, player2Icon, p2BodyX - 5, bodyY - 75, 70, 70);

        // Heave labels
        if (p1PullFlash > 0) {
            g.setColor(Color.WHITE);
            g.setFont(new Font("Arial", Font.BOLD, 22));
            g.drawString("HEAVE!", p1BodyX - 10, bodyY - 80);
        }
        if (p2PullFlash > 0) {
            g.setColor(Color.WHITE);
            g.setFont(new Font("Arial", Font.BOLD, 22));
            g.drawString("HEAVE!", p2BodyX - 10, bodyY - 80);
        }

        // Rope-progress bar at top
        drawProgressBar(g, rp);

        // Win zones labels
        g.setColor(Color.WHITE);
        g.setFont(new Font("Arial", Font.PLAIN, 15));
        g.drawString("P1 wins here", LEFT_WIN - 48, ropeTop - 48);
        g.drawString("P2 wins here", RIGHT_WIN - 10, ropeTop - 48);

        if (gameOver) {
            g.setColor(new Color(0, 0, 0, 150));
            g.fillRect(300, 430, 600, 75);
            g.setColor(Color.GREEN);
            g.setFont(new Font("Arial", Font.BOLD, 40));
            String msg = "PLAYER1".equals(winner) ? "PLAYER 1 WINS!" : "PLAYER 2 WINS!";
            drawCentered(g, msg, 600, 480);
        }
    }

    private void drawProgressBar(Graphics g, int ropeX) {
        int barX = LEFT_WIN, barY = 380, barW = RIGHT_WIN - LEFT_WIN, barH = 18;
        g.setColor(new Color(60, 60, 60));
        g.fillRect(barX, barY, barW, barH);

        // Blue fill from left, red fill from right
        int knot = ropeX - LEFT_WIN;
        g.setColor(new Color(80, 80, 200));
        g.fillRect(barX, barY, knot, barH);
        g.setColor(new Color(200, 80, 80));
        g.fillRect(barX + knot, barY, barW - knot, barH);

        g.setColor(Color.WHITE);
        g.fillRect(barX + knot - 2, barY - 3, 4, barH + 6);
        g.setColor(Color.DARK_GRAY);
        g.drawRect(barX, barY, barW, barH);
    }

    /** Draws a single 32x32 frame from a horizontal pull sprite sheet, scaled to w x h. */
    private void drawPullFrame(Graphics g, java.awt.Image sheet, int frame, int x, int y, int w, int h) {
        int sx = frame * PULL_FRAME_W;
        g.drawImage(sheet, x, y, x + w, y + h,
                    sx, 0, sx + PULL_FRAME_W, PULL_FRAME_H, null);
    }

    private void drawArrow(Graphics g, int x, int y, boolean leftward) {
        g.setColor(Color.YELLOW);
        int dx = leftward ? -30 : 30;
        g.fillPolygon(
            new int[]{x, x + dx, x + dx},
            new int[]{y, y - 10, y + 10},
            3
        );
    }

    private void drawCentered(Graphics g, String text, int cx, int y) {
        int tw = g.getFontMetrics().stringWidth(text);
        g.drawString(text, cx - tw / 2, y);
    }

    @Override
    public void reset() {
        gameOver      = false;
        winner        = null;
        ropeVelocity  = 0;

        // Random starting tilt: ±60 px from center
        ropePosition  = CENTER + (random.nextInt(121) - 60);

        p1PullFlash = 0;
        p2PullFlash = 0;

        p1Frame = p2Frame = 0;
        p1AnimTick = p2AnimTick = 0;
    }
}
