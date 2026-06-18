package minigames;

import java.awt.Color;
import java.awt.Font;
import java.awt.Graphics;
import java.util.Random;

import managers.AssetManager;
import managers.SoundManager;
import core.InputHandler;

public class SheriffMinigame extends BaseMinigame {

    private long startTime;
    private long drawTime;
    private long drawSignalTime;   // when DRAW actually appeared

    private boolean drawSignalShown;
    private boolean falseStart;
    private String  falseStartPlayer;

    private long p1ReactionMs;
    private long p2ReactionMs;

    private final Random random = new Random();

    // Tension animation
    private int tensionFrame;

    // Round decided internally, but kept "not over" until animations finish
    private boolean roundOver;
    // Timestamp of when the round ended — drives win/death animations
    private long resultTime;

    private java.awt.Image sheriffBg;

    // Cowboy animation sheets (each frame is 64x64, laid out horizontally)
    private java.awt.Image idleSheet;
    private java.awt.Image attackSheet;
    private java.awt.Image deathSheet;
    private static final int FRAME_W   = 64;
    private static final int FRAME_H   = 64;
    private static final int IDLE_FRAMES   = 2;
    private static final int ATTACK_FRAMES = 5;
    private static final int DEATH_FRAMES  = 7;
    private static final long ANIM_FRAME_MS = 90; // ms per animation frame
    // Extra hold after the death animation before the results screen appears
    private static final long RESULT_HOLD_MS = 1500;

    public SheriffMinigame() {
        reset();
        sheriffBg   = AssetManager.load("assets/environment/sheriff_bg.png");
        idleSheet   = AssetManager.load("assets/Players/Cowboy Assets/Cowboy_Idle/Cowboy_Idle.png");
        attackSheet = AssetManager.load("assets/Players/Cowboy Assets/Cowboy_Attack/Cowboy Attack Animation.png");
        deathSheet  = AssetManager.load("assets/Players/Cowboy Assets/Cowboy_Death/Cowboy Death Animation.png");
    }

    @Override
    public String getAmbientMusic() {
        return "assets/sounds/sheriff_ambient.wav";
    }

    @Override
    public void update(InputHandler input) {
        if (gameOver) return;

        long now = System.currentTimeMillis();

        // Round decided — let the win/death animations play, then finalize so
        // the panel advances to the results screen.
        if (roundOver) {
            if (now - resultTime >= DEATH_FRAMES * ANIM_FRAME_MS + RESULT_HOLD_MS) {
                endGame(winner);
            }
            return;
        }

        tensionFrame++;

        if (!drawSignalShown && now >= drawTime) {
            drawSignalShown = true;
            drawSignalTime  = now;
            // "DRAW!" just flashed — sound the signal to fire.
            SoundManager.play("assets/sounds/draw_signal.wav");
        }

        if (!drawSignalShown) {
            // Shooting before the signal — false start
            boolean p1Shot = input.isPlayer1PressedThisFrame();
            boolean p2Shot = input.isPlayer2PressedThisFrame();
            if (p1Shot || p2Shot) {
                SoundManager.play("assets/sounds/gunshot.wav");
                falseStart = true;
                falseStartPlayer = p1Shot ? "PLAYER1" : "PLAYER2";
                // False starter loses
                decideRound(p1Shot ? "PLAYER2" : "PLAYER1", now);
            }
            return;
        }

        // DRAW signal visible — fastest finger wins
        boolean p1Shot = input.isPlayer1PressedThisFrame();
        boolean p2Shot = input.isPlayer2PressedThisFrame();

        if (p1Shot || p2Shot) {
            SoundManager.play("assets/sounds/gunshot.wav");
        }

        if (p1Shot && p2Shot) {
            // Same frame — random tiebreak
            p1ReactionMs = now - drawSignalTime;
            p2ReactionMs = now - drawSignalTime;
            decideRound(random.nextBoolean() ? "PLAYER1" : "PLAYER2", now);
        } else if (p1Shot) {
            p1ReactionMs = now - drawSignalTime;
            decideRound("PLAYER1", now);
        } else if (p2Shot) {
            p2ReactionMs = now - drawSignalTime;
            decideRound("PLAYER2", now);
        }
    }

    /**
     * Locks in the winner and starts the animation timer, but keeps the game
     * "not over" so the death/attack animations can play before the panel
     * transitions to the results screen.
     */
    private void decideRound(String winnerPlayer, long now) {
        winner     = winnerPlayer;
        resultTime = now;
        roundOver  = true;
    }

    @Override
    public void draw(Graphics g) {
        // Western background
        g.drawImage(sheriffBg, 0, 0, 1200, 800, null);


        // Title
        g.setColor(new Color(80, 40, 0));
        g.setFont(new Font("Arial", Font.BOLD, 32));
        drawCentered(g, "Sheriff Showdown", 600, 55);

        // Key hints
        g.setFont(new Font("Arial", Font.PLAIN, 17));
        g.drawString("P1: Z Key", 150, 95);
        g.drawString("P2: / Key", 990, 95);

        // Cowboys (left and right). P1 is on the left and faces right toward the
        // opponent; P2 is on the right and faces left. drawCowboy mirrors each
        // sheet as needed so this holds in idle, attack, and death states.
        int cowSize = 230;
        int p1X = 170, p2X = 800, cowY = 450;

        drawCowboy(g, "PLAYER1", p1X, cowY, cowSize, true);
        drawCowboy(g, "PLAYER2", p2X, cowY, cowSize, false);

        if (player1Icon != null) drawPlayerIcon(g, player1Icon, p1X + cowSize / 2 - 40, cowY - 50, 80, 80);
        if (player2Icon != null) drawPlayerIcon(g, player2Icon, p2X + cowSize / 2 - 40, cowY - 50, 80, 80);

        // Center signal — hidden once a shot is fired
        if (!roundOver) {
            if (!drawSignalShown) {
                // Pulsing WAIT dots
                int dots = (tensionFrame / 20) % 4;
                String waiting = "WAIT" + "...".substring(0, dots);
                g.setColor(new Color(50, 50, 50));
                g.setFont(new Font("Arial", Font.BOLD, 44));
                drawCentered(g, waiting, 600, 470);

                // Tension meter
                drawTensionMeter(g);

            } else {
                g.setColor(Color.RED);
                g.setFont(new Font("Arial", Font.BOLD, 80));
                drawCentered(g, "DRAW!", 600, 480);
            }
        }

        // Note: the round result (winner, scores) is drawn by the shared results
        // screen (UIManager.drawResults), so this minigame intentionally draws no
        // overlay of its own — that avoids a stray box showing behind the board.
    }

    /**
     * Draws a cowboy for the given player, picking the right animation:
     * idle while waiting, attack if this player won, death if this player lost.
     *
     * The idle sheet faces left while the attack/death sheets face right, so we
     * mirror per-sheet to guarantee the player always faces the opponent.
     *
     * @param faceRight true if this cowboy should face right (toward the opponent)
     */
    private void drawCowboy(Graphics g, String player, int x, int y, int size, boolean faceRight) {
        java.awt.Image sheet;
        int frame;
        boolean sheetFacesRight;

        if (!roundOver || (falseStart && !player.equals(falseStartPlayer))) {
            // Idle: during the standoff, and for the innocent player on a false
            // start (the other cowboy never drew, so he just stands there).
            sheet = idleSheet;
            frame = (tensionFrame / 24) % IDLE_FRAMES;
            sheetFacesRight = true;
        } else {
            long elapsed = System.currentTimeMillis() - resultTime;
            int step = (int) (elapsed / ANIM_FRAME_MS);
            if (player.equals(winner)) {
                // This player shot first — play the attack, hold the last frame
                sheet = attackSheet;
                frame = Math.min(step, ATTACK_FRAMES - 1);
            } else {
                // This player lost (or drew too early) — play the death animation
                sheet = deathSheet;
                frame = Math.min(step, DEATH_FRAMES - 1);
            }
            sheetFacesRight = false; // attack and death sprites face right
        }

        if (sheet == null) {
            // Fallback marker if a sheet failed to load
            g.setColor("PLAYER1".equals(player) ? Color.BLUE : Color.RED);
            g.fillRect(x, y, size, size);
            return;
        }

        int sx1 = frame * FRAME_W;
        int sx2 = sx1 + FRAME_W;
        // Mirror only when the sheet's native facing differs from the target
        if (faceRight == sheetFacesRight) {
            g.drawImage(sheet, x, y, x + size, y + size, sx1, 0, sx2, FRAME_H, null);
        } else {
            // Mirror horizontally by swapping the destination x edges
            g.drawImage(sheet, x + size, y, x, y + size, sx1, 0, sx2, FRAME_H, null);
        }
    }

    private void drawTensionMeter(Graphics g) {
        long now    = System.currentTimeMillis();
        long total  = drawTime - startTime;
        long elapsed = now - startTime;
        float pct   = Math.min(1f, (float) elapsed / total);

        int bx = 400, by = 500, bw = 400, bh = 20;
        g.setColor(new Color(60, 60, 60));
        g.fillRect(bx, by, bw, bh);
        // Color shifts red as it fills
        int r = (int)(255 * pct);
        int gb = (int)(200 * (1 - pct));
        g.setColor(new Color(r, gb, gb));
        g.fillRect(bx, by, (int)(bw * pct), bh);
        g.setColor(Color.WHITE);
        g.drawRect(bx, by, bw, bh);

        g.setColor(Color.WHITE);
        g.setFont(new Font("Arial", Font.PLAIN, 15));
        drawCentered(g, "tension...", 600, by + 40);
    }



    private void drawCentered(Graphics g, String text, int cx, int y) {
        int tw = g.getFontMetrics().stringWidth(text);
        g.drawString(text, cx - tw / 2, y);
    }

    @Override
    public void reset() {
        gameOver       = false;
        roundOver      = false;
        winner         = null;
        drawSignalShown = false;
        falseStart     = false;
        falseStartPlayer = null;
        p1ReactionMs   = 0;
        p2ReactionMs   = 0;
        tensionFrame   = 0;

        startTime = System.currentTimeMillis();
        // Random delay 2–5 seconds
        drawTime  = startTime + 2000 + random.nextInt(3000);
    }
}
