package minigames;

import java.awt.Color;
import java.awt.Font;
import java.awt.Graphics;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;

import core.InputHandler;
import managers.AssetManager;
import managers.SoundManager;

public class RaceMinigame extends BaseMinigame {

    private double player1X;
    private double player2X;
    private double p1Velocity;
    private double p2Velocity;

    private static final int    START_X   = 100;
    private static final int    FINISH_X  = 1100;

    // The race_bg.png has a single asphalt road; both cars share it in two lanes
    private static final int    ROAD_TOP    = 491;
    private static final int    ROAD_BOTTOM = 658;
    private static final int    P1_LANE_Y   = 538; // upper car lane centre (above centre line)
    private static final int    P2_LANE_Y   = 622; // lower car lane centre (below centre line)

    // Velocity physics
    private static final double FRICTION  = 0.92;  // multiplied each frame
    private static final double MAX_PUSH  = 6.0;   // perfect-timing boost
    private static final double MIN_PUSH  = 0.5;   // worst-timing boost
    private static final double MAX_VEL   = 8.0;   // cap

    // Timing bar — pointer bounces 0→1→0 each cycle
    private double p1TimerPos;
    private double p2TimerPos;
    private int    p1TimerDir;
    private int    p2TimerDir;
    private static final double TIMER_SPEED   = 0.025; // step per frame
    private static final double PERFECT_HALF  = 0.10;  // ± from center (0.5)
    private static final double GOOD_HALF     = 0.22;

    // Click cooldown — players must wait 1.5s between clicks
    private int p1Cooldown;
    private int p2Cooldown;
    private static final int COOLDOWN_FRAMES = 90; // 1.5s at 60 fps

    // Hit feedback
    private int    p1FeedbackFrames;
    private int    p2FeedbackFrames;
    private String p1FeedbackText;
    private String p2FeedbackText;
    private Color  p1FeedbackColor;
    private Color  p2FeedbackColor;
    private static final int FEEDBACK_FRAMES = 35;

    // Countdown
    private int     countdownFrames;
    private boolean raceStarted;

    // Mud puddles
    private List<Integer> p1Puddles;
    private List<Integer> p2Puddles;
    private int p1FreezeFrames;
    private int p2FreezeFrames;
    private static final int PUDDLE_W     = 50;
    private static final int FREEZE_FRAMES = 45;

    private final Random random = new Random();

    private java.awt.Image raceBg;
    private java.awt.Image mudPuddleImg;
    private java.awt.Image carP1; // Ford GT (faces left in the art)
    private java.awt.Image carP2; // Pagani (faces right in the art)

    public RaceMinigame() {
        reset();
        raceBg       = AssetManager.load("assets/environment/race_bg.png");
        mudPuddleImg = AssetManager.load("assets/environment/mud_puddle.gif");
        carP1        = AssetManager.load("assets/Players/fordgt_p1.png");
        carP2        = AssetManager.load("assets/Players/pagani_p2.png");
    }

    @Override
    public String getAmbientMusic() {
        return "assets/sounds/race_ambient.wav";
    }

    @Override
    public void update(InputHandler input) {
        if (gameOver) return;

        if (!raceStarted) {
            countdownFrames--;
            if (countdownFrames <= 0) raceStarted = true;
            return;
        }

        // Advance timing bar pointers
        p1TimerPos += TIMER_SPEED * p1TimerDir;
        if (p1TimerPos >= 1.0) { p1TimerPos = 1.0; p1TimerDir = -1; }
        if (p1TimerPos <= 0.0) { p1TimerPos = 0.0; p1TimerDir =  1; }

        p2TimerPos += TIMER_SPEED * p2TimerDir;
        if (p2TimerPos >= 1.0) { p2TimerPos = 1.0; p2TimerDir = -1; }
        if (p2TimerPos <= 0.0) { p2TimerPos = 0.0; p2TimerDir =  1; }

        if (p1FreezeFrames > 0) p1FreezeFrames--;
        if (p2FreezeFrames > 0) p2FreezeFrames--;
        if (p1Cooldown > 0) p1Cooldown--;
        if (p2Cooldown > 0) p2Cooldown--;
        if (p1FeedbackFrames > 0) p1FeedbackFrames--;
        if (p2FeedbackFrames > 0) p2FeedbackFrames--;

        if (input.isPlayer1PressedThisFrame() && p1FreezeFrames == 0 && p1Cooldown == 0) {
            double push = calcPush(p1TimerPos);
            setFeedback(1, p1TimerPos, push);
            p1Velocity = Math.min(p1Velocity + push, MAX_VEL);
            p1Cooldown = COOLDOWN_FRAMES;
            SoundManager.play("assets/sounds/engine_rev.wav");
        }
        if (input.isPlayer2PressedThisFrame() && p2FreezeFrames == 0 && p2Cooldown == 0) {
            double push = calcPush(p2TimerPos);
            setFeedback(2, p2TimerPos, push);
            p2Velocity = Math.min(p2Velocity + push, MAX_VEL);
            p2Cooldown = COOLDOWN_FRAMES;
            SoundManager.play("assets/sounds/engine_rev.wav");
        }

        // Apply velocity, decelerate, then check puddles each frame
        if (p1FreezeFrames == 0) {
            player1X  += p1Velocity;
            p1Velocity *= FRICTION;
            if (p1Velocity < 0.05) p1Velocity = 0;
            checkPuddles(1);
        }
        if (p2FreezeFrames == 0) {
            player2X  += p2Velocity;
            p2Velocity *= FRICTION;
            if (p2Velocity < 0.05) p2Velocity = 0;
            checkPuddles(2);
        }

        if (player1X >= FINISH_X) endGame("PLAYER1");
        else if (player2X >= FINISH_X) endGame("PLAYER2");
    }

    // Returns push strength based on distance from center of timing bar
    private double calcPush(double timerPos) {
        double dist  = Math.abs(timerPos - 0.5); // 0 = perfect, 0.5 = worst
        double ratio = Math.max(0, 1.0 - (dist / 0.5)); // 1 = perfect, 0 = worst
        return MIN_PUSH + ratio * ratio * (MAX_PUSH - MIN_PUSH); // quadratic curve
    }

    private void setFeedback(int player, double timerPos, double push) {
        double dist = Math.abs(timerPos - 0.5);
        String text;
        Color  color;
        if (dist <= PERFECT_HALF)      { text = "PERFECT!"; color = Color.GREEN; }
        else if (dist <= GOOD_HALF)    { text = "GOOD!";    color = Color.YELLOW; }
        else                           { text = "OK";       color = Color.ORANGE; }
        if (player == 1) {
            p1FeedbackText = text; p1FeedbackColor = color; p1FeedbackFrames = FEEDBACK_FRAMES;
        } else {
            p2FeedbackText = text; p2FeedbackColor = color; p2FeedbackFrames = FEEDBACK_FRAMES;
        }
    }

    private void checkPuddles(int player) {
        int px = (int)((player == 1) ? player1X : player2X);
        List<Integer> puddles = (player == 1) ? p1Puddles : p2Puddles;
        for (int i = 0; i < puddles.size(); i++) {
            int pz = puddles.get(i);
            if (px + 30 >= pz && px <= pz + PUDDLE_W) {
                puddles.remove(i); // consumed so it can't re-freeze on unthaw
                if (player == 1) { p1FreezeFrames = FREEZE_FRAMES; p1Velocity = 0; }
                else             { p2FreezeFrames = FREEZE_FRAMES; p2Velocity = 0; }
                SoundManager.play("assets/sounds/splash.wav");
                return;
            }
        }
    }

    @Override
    public void draw(Graphics g) {
        if (raceBg != null) {
            g.drawImage(raceBg, 0, 0, 1200, 800, null);
        } else {
            g.setColor(new Color(40, 40, 40));
            g.fillRect(0, 0, 1200, 800);
        }

        // Start and finish lines span the road band (the road itself is part of
        // race_bg.png).
        g.setColor(Color.WHITE);
        g.fillRect(START_X, ROAD_TOP + 7, 3, ROAD_BOTTOM - ROAD_TOP);
        g.fillRect(FINISH_X, ROAD_TOP + 7, 3, ROAD_BOTTOM - ROAD_TOP);

        // Mud puddles sit on the road, in each car's lane
        if (mudPuddleImg != null) {
            for (int pz : p1Puddles) g.drawImage(mudPuddleImg, pz - 5, P1_LANE_Y - 15, PUDDLE_W + 10, 30, null);
            for (int pz : p2Puddles) g.drawImage(mudPuddleImg, pz - 5, P2_LANE_Y - 15, PUDDLE_W + 10, 30, null);
        } else {
            g.setColor(new Color(100, 70, 30));
            for (int pz : p1Puddles) g.fillOval(pz, P1_LANE_Y - 9, PUDDLE_W, 18);
            for (int pz : p2Puddles) g.fillOval(pz, P2_LANE_Y - 9, PUDDLE_W, 18);
        }

        boolean p1Flash = (p1FreezeFrames > 0 && (p1FreezeFrames / 5) % 2 == 0);
        boolean p2Flash = (p2FreezeFrames > 0 && (p2FreezeFrames / 5) % 2 == 0);

        // Cars race left->right. The Ford GT art faces left so it is flipped;
        // the Pagani already faces right.
        int carW = 96, carH = 96;
        drawCar(g, carP1, (int) player1X - 18, P1_LANE_Y - carH / 2, carW, carH, true,  p1Flash, Color.BLUE);
        drawCar(g, carP2, (int) player2X - 18, P2_LANE_Y - carH / 2, carW, carH, false, p2Flash, Color.RED);

        if (player1Icon != null) drawPlayerIcon(g, player1Icon, (int) player1X - 5, P1_LANE_Y - 60, 40, 40);
        if (player2Icon != null) drawPlayerIcon(g, player2Icon, (int) player2X - 5, P2_LANE_Y - 60, 40, 40);

        // Title + key hints
        g.setColor(Color.WHITE);
        g.setFont(new Font("Arial", Font.BOLD, 26));
        drawCentered(g, "Timing Race", 600, 45);
        g.setFont(new Font("Arial", Font.PLAIN, 17));
        g.drawString("P1 (Blue): Z Key", 60, 88);
        g.drawString("P2 (Red):  / Key", 880, 88);

        // Timing bars
        drawRaceTimingBar(g, 60,  108, p1TimerPos, Color.BLUE, p1Cooldown);
        drawRaceTimingBar(g, 880, 108, p2TimerPos, Color.RED, p2Cooldown);

        // Speed readouts
        g.setFont(new Font("Arial", Font.PLAIN, 14));
        g.setColor(new Color(100, 180, 255));
        g.drawString(String.format("Speed: %.1f", p1Velocity), 60, 152);
        g.setColor(new Color(255, 120, 120));
        g.drawString(String.format("Speed: %.1f", p2Velocity), 880, 152);

        // Timing feedback above players
        g.setFont(new Font("Arial", Font.BOLD, 20));
        int p1TextY = P1_LANE_Y - 40, p2TextY = P2_LANE_Y - 40;
        if (p1FreezeFrames > 0) {
            g.setColor(Color.YELLOW);
            g.drawString("STUCK!", (int) player1X - 10, p1TextY);
        } else if (p1FeedbackFrames > 0) {
            g.setColor(p1FeedbackColor);
            g.drawString(p1FeedbackText, (int) player1X - 10, p1TextY);
        }
        if (p2FreezeFrames > 0) {
            g.setColor(Color.YELLOW);
            g.drawString("STUCK!", (int) player2X - 10, p2TextY);
        } else if (p2FeedbackFrames > 0) {
            g.setColor(p2FeedbackColor);
            g.drawString(p2FeedbackText, (int) player2X - 10, p2TextY);
        }

        // Countdown overlay
        g.setFont(new Font("Arial", Font.BOLD, 120));
        if (!raceStarted) {
            int sec = (countdownFrames / 60) + 1;
            g.setColor(new Color(0, 0, 0, 160));
            g.fillRect(0, 0, 1200, 800);
            g.setColor(sec == 1 ? Color.GREEN : Color.WHITE);
            drawCentered(g, String.valueOf(sec), 600, 430);
        }
        if (raceStarted && countdownFrames > -10) {
            g.setColor(Color.GREEN);
            drawCentered(g, "GO!", 600, 430);
        }

        // Winner banner
        if (gameOver) {
            g.setColor(new Color(0, 0, 0, 140));
            g.fillRect(300, 320, 600, 80);
            g.setColor(Color.GREEN);
            g.setFont(new Font("Arial", Font.BOLD, 42));
            String msg = "PLAYER1".equals(winner) ? "PLAYER 1 WINS!" : "PLAYER 2 WINS!";
            drawCentered(g, msg, 600, 373);
        }
    }

    private void drawRaceTimingBar(Graphics g, int x, int y, double timerPos, Color playerColor, int cooldown) {
        int barW = 200, barH = 22;

        // Zone fills (whole bar = OK zone)
        g.setColor(Color.ORANGE);
        g.fillRect(x, y, barW, barH);

        // Good zone
        int goodX = (int) (x + barW * (0.5 - GOOD_HALF));
        int goodW = (int) (barW * GOOD_HALF * 2);
        g.setColor(Color.YELLOW);
        g.fillRect(goodX, y, goodW, barH);

        // Perfect zone
        int perfX = (int) (x + barW * (0.5 - PERFECT_HALF));
        int perfW = (int) (barW * PERFECT_HALF * 2);
        g.setColor(Color.GREEN);
        g.fillRect(perfX, y, perfW, barH);

        // Moving pointer
        int ptrX = x + (int) (timerPos * barW);
        g.setColor(Color.WHITE);
        g.fillRect(ptrX - 2, y - 4, 4, barH + 8);

        // Border
        g.setColor(playerColor);
        g.drawRect(x, y, barW, barH);

        // Zone labels (small)
        g.setFont(new Font("Arial", Font.PLAIN, 10));
        g.setColor(Color.WHITE);
        g.drawString("PERFECT", perfX + 2, y + barH - 5);

        // Cooldown overlay — dim the bar and show a refilling progress strip
        if (cooldown > 0) {
            g.setColor(new Color(0, 0, 0, 160));
            g.fillRect(x, y, barW, barH);
            int readyW = (int) (barW * (1.0 - cooldown / (double) COOLDOWN_FRAMES));
            g.setColor(playerColor);
            g.fillRect(x, y + barH - 5, readyW, 5);
            g.setColor(Color.WHITE);
            g.setFont(new Font("Arial", Font.BOLD, 12));
            String cd = "COOLDOWN";
            int tw = g.getFontMetrics().stringWidth(cd);
            g.drawString(cd, x + (barW - tw) / 2, y + barH - 7);
        }
    }

    /**
     * Draws a player's car sprite, optionally mirrored to face right, with a
     * yellow flash overlay while the car is stuck in mud.
     */
    private void drawCar(Graphics g, java.awt.Image car, int x, int y, int w, int h,
                         boolean flip, boolean flash, Color fallback) {
        if (car == null) {
            // Fallback to a coloured circle if the sprite failed to load
            g.setColor(flash ? Color.YELLOW : fallback);
            g.fillOval(x + w / 3, y + h / 2 - 15, 30, 30);
            return;
        }
        int sw = car.getWidth(null);
        int sh = car.getHeight(null);
        if (!flip) {
            g.drawImage(car, x, y, x + w, y + h, 0, 0, sw, sh, null);
        } else {
            // Mirror horizontally by swapping the destination x edges
            g.drawImage(car, x + w, y, x, y + h, 0, 0, sw, sh, null);
        }
        if (flash) {
            // Translucent yellow band over the car body to show it is stuck
            g.setColor(new Color(255, 255, 0, 120));
            g.fillRect(x, y + h * 4 / 10, w, h / 5);
        }
    }

    private void drawCentered(Graphics g, String text, int cx, int y) {
        int tw = g.getFontMetrics().stringWidth(text);
        g.drawString(text, cx - tw / 2, y);
    }

    @Override
    public void reset() {
        gameOver = false;
        winner   = null;

        player1X   = START_X;
        player2X   = START_X;
        p1Velocity = 0;
        p2Velocity = 0;

        p1TimerPos = 0.0;
        p2TimerPos = 0.5; // offset so bars aren't synchronized
        p1TimerDir = 1;
        p2TimerDir = 1;

        p1FeedbackFrames = 0;
        p2FeedbackFrames = 0;
        p1FeedbackText   = "";
        p2FeedbackText   = "";
        p1FeedbackColor  = Color.WHITE;
        p2FeedbackColor  = Color.WHITE;

        countdownFrames = 180;
        raceStarted     = false;

        p1FreezeFrames = 0;
        p2FreezeFrames = 0;
        p1Cooldown = 0;
        p2Cooldown = 0;

        p1Puddles = new ArrayList<>();
        p2Puddles = new ArrayList<>();
        for (int i = 0; i < 2; i++) {
            p1Puddles.add(250 + random.nextInt(630));
            p2Puddles.add(250 + random.nextInt(630));
        }
    }
}
