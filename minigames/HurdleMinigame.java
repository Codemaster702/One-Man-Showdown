//Mohammed Shekhibrahim
//June 15 2026
//Hurdle minigame

package minigames;

import java.awt.Color;
import java.awt.Font;
import java.awt.Graphics;
import java.awt.image.BufferedImage;
import java.util.Random;

import core.InputHandler;
import managers.AssetManager;
import managers.SoundManager;

public class HurdleMinigame extends BaseMinigame {



    private static class Hurdle {
    int x;
    int height;

    Hurdle(int x, int height) {
        this.x = x;
        this.height = height;
    }
}

    // Player Y positions (X is fixed per lane)
    private int p1Y, p2Y;
    private double p1Vel, p2Vel;

    private static final int GROUND_Y   = 400;
    private static final double GRAVITY = 0.75;
    private static final double JUMP    = -13.0;

    // Both players share one full-width track; hurdles travel SPAWN->0 across it
    private static final int P1_X       = 150;
    private static final int P2_X       = 320;
    private static final int HURDLE_SPAWN = 1280;

    private java.util.ArrayList<Hurdle> hurdles = new java.util.ArrayList<>();
    
    private int hurdleSpeed;
    private int difficultyLevel;
    private static final int HURDLE_W = 28;

    private int hurdlesCleared;

    private int countdownFrames;
    private boolean raceStarted;

    private final Random random = new Random();

    private java.awt.Image hurdleImg;

    private java.awt.Image bgBack, bgMiddle, bgFront;
    private double scrollBack, scrollMiddle, scrollFront;
    private static final double SPEED_BACK   = 1.0;
    private static final double SPEED_MIDDLE = 2.5;
    private static final double SPEED_FRONT  = 5.0;

    // Sprite sheet animation
    private BufferedImage spriteSheet;
    private static final int FRAME_W      = 64;
    private static final int FRAME_H      = 64;
    private static final int RUN_FRAMES   = 6;
    private static final int JUMP_FRAMES  = 4;
    private static final int RUN_ROW      = 0;
    private static final int JUMP_ROW     = 1;
    private static final int DEATH_ROW    = 3; // 4th row of the sprite sheet
    private static final int DEATH_FRAMES = 6;
    private static final int ANIM_SPEED   = 5; // game frames per sprite frame
    private static final int DEATH_ANIM_SPEED = 6;
    private int animTick;
    private int animFrame;

    // Death animation state
    private boolean dying;
    private int deadPlayer; // 0 = none, 1 = P1, 2 = P2
    private int deathTick;
    private int deathFrame;




    private void spawnHurdlePack() {
    hurdles.clear();

    int count = 1 + random.nextInt(3); // 1-3 hurdles

    int x = HURDLE_SPAWN;

    for (int i = 0; i < count; i++) {
        int height = 40 + random.nextInt(35);

        hurdles.add(new Hurdle(x, height));

        // almost touching
        x += HURDLE_W + random.nextInt(6);
    }
}

    private void updateDifficulty() {
    difficultyLevel = hurdlesCleared / 5;

    hurdleSpeed = Math.min(18, 7 + difficultyLevel);
}

    public HurdleMinigame() {
        reset();
        hurdleImg = AssetManager.load("assets/environment/Grave02.png");
        bgBack    = AssetManager.load("assets/environment/v2/layers/back.png");
        bgMiddle  = AssetManager.load("assets/environment/v2/layers/middle.png");
        bgFront   = AssetManager.load("assets/environment/v2/layers/front.png");
        spriteSheet = AssetManager.loadBuffered("assets/Players/Massacre Sprite Sheet.png");
    }

    @Override
    public String getAmbientMusic() {
        return "assets/sounds/hurdle_ambient.wav";
    }

    @Override
    public void update(InputHandler input) {
        if (gameOver) return;

        // Play out the death animation, then declare the winner
        if (dying) {
            deathTick++;
            if (deathTick >= DEATH_ANIM_SPEED) {
                deathTick = 0;
                if (deathFrame < DEATH_FRAMES - 1) {
                    deathFrame++;
                } else {
                    endGame(deadPlayer == 1 ? "PLAYER2" : "PLAYER1");
                }
            }
            return;
        }

        if (!raceStarted) {
            countdownFrames--;
            if (countdownFrames <= 0) raceStarted = true;
            return;
        }

        scrollBack   += SPEED_BACK;
        scrollMiddle += SPEED_MIDDLE;
        scrollFront  += SPEED_FRONT;

        // Advance sprite animation
        animTick++;
        if (animTick >= ANIM_SPEED) {
            animTick = 0;
            animFrame = (animFrame + 1) % RUN_FRAMES;
        }

        // Jump input
        if (input.isPlayer1PressedThisFrame() && p1Y >= GROUND_Y) {
            p1Vel = JUMP;
            SoundManager.play("assets/sounds/jump.wav");
            
        }
        if (input.isPlayer2PressedThisFrame() && p2Y >= GROUND_Y) {
            p2Vel = JUMP;
            SoundManager.play("assets/sounds/jump.wav");
            
        }

        // Physics
        p1Vel += GRAVITY; p1Y += p1Vel;
        p2Vel += GRAVITY; p2Y += p2Vel;
        if (p1Y > GROUND_Y) { p1Y = GROUND_Y; p1Vel = 0; }
        if (p2Y > GROUND_Y) { p2Y = GROUND_Y; p2Vel = 0; }

    for (Hurdle h : hurdles) {
        h.x -= hurdleSpeed;
    }

    if (!hurdles.isEmpty() &&
        hurdles.get(hurdles.size() - 1).x < -HURDLE_W) {

        hurdlesCleared++;
        updateDifficulty();
        spawnHurdlePack();
    }

    for (Hurdle h : hurdles) {

        if (hurdleHit(P1_X, p1Y, h.x, h.height)) {
            startDeath(1);
            return;
        }

        if (hurdleHit(P2_X, p2Y, h.x, h.height)) {
            startDeath(2);
            return;
        }
    }
}

    private void startDeath(int player) {
        dying      = true;
        deadPlayer = player;
        deathTick  = 0;
        deathFrame = 0;
        // A runner clipped a hurdle.
        SoundManager.play("assets/sounds/hit.wav");
    }

    private boolean hurdleHit(int px, int py, int hx, int hh) {
        int pw = 36, ph = 36;
        int hurdleTop = GROUND_Y - hh + 30;
        return px + pw > hx && px < hx + HURDLE_W
            && py + ph > hurdleTop && py < hurdleTop + hh;
    }

    @Override
    public void draw(Graphics g) {
        // Background — parallax layers
        drawLoopingLayer(g, bgBack,   scrollBack,   GROUND_Y + 36);
        drawLoopingLayer(g, bgMiddle, scrollMiddle, GROUND_Y + 36);

        // Track surface — single shared lane
        g.setColor(new Color(200, 170, 100, 80));
        g.fillRect(0, GROUND_Y + 10, 1200, 26);



        // Controls reminder
        g.setFont(new Font("Arial", Font.PLAIN, 15));
        g.setColor(Color.BLACK);
        g.drawString("Z = Jump", 30, 65);
        g.drawString("/ = Jump", 630, 65);

        // Shared hurdle counter
        g.setFont(new Font("Arial", Font.BOLD, 20));
        g.setColor(Color.WHITE);
        g.drawString("Hurdles: " + hurdlesCleared, 30, 100);

        for (Hurdle h : hurdles) {

        if (hurdleImg != null) {
            int top = GROUND_Y - h.height + 30;
            g.drawImage(
                hurdleImg,
                h.x - 4,
                top,
                HURDLE_W + 8,
                h.height,
                null
            );
        } else {
            drawHurdle(g, h.x, h.height, new Color(160, 80, 20));
        }
    }

        // Players — sprite sheet if loaded, otherwise coloured rectangles
        int p1DrawY = p1Y, p2DrawY = p2Y;
        if (spriteSheet != null) {
            int p1Row, p1Frame, p2Row, p2Frame;
            if (deadPlayer == 1) {
                p1Row = DEATH_ROW; p1Frame = deathFrame;
            } else {
                p1Row = (p1Y < GROUND_Y) ? JUMP_ROW : RUN_ROW;
                p1Frame = animFrame % ((p1Row == JUMP_ROW) ? JUMP_FRAMES : RUN_FRAMES);
            }
            if (deadPlayer == 2) {
                p2Row = DEATH_ROW; p2Frame = deathFrame;
            } else {
                p2Row = (p2Y < GROUND_Y) ? JUMP_ROW : RUN_ROW;
                p2Frame = animFrame % ((p2Row == JUMP_ROW) ? JUMP_FRAMES : RUN_FRAMES);
            }
            // Draw 80x80, feet aligned to bottom of hitbox
            drawSprite(g, p1Row, p1Frame, P1_X - 22, p1DrawY - 44, 80, 80);
            drawSprite(g, p2Row, p2Frame, P2_X - 22, p2DrawY - 44, 80, 80);
        } else {
            g.setColor(Color.BLUE);
            g.fillRect(P1_X, p1DrawY, 36, 36);
            g.setColor(Color.RED);
            g.fillRect(P2_X, p2DrawY, 36, 36);
            if (player1Icon != null) drawPlayerIcon(g, player1Icon, P1_X - 2, p1DrawY - 45, 40, 40);
            if (player2Icon != null) drawPlayerIcon(g, player2Icon, P2_X - 2, p2DrawY - 45, 40, 40);
        }

        // Foreground parallax layer (drawn over players, shifted down)
        drawLoopingLayer(g, bgFront, scrollFront, GROUND_Y + 40, 40);

        // Speed indicator
        drawSpeedDots(g, 30, 120, hurdleSpeed - 6, Color.ORANGE);

        // Countdown
        if (!raceStarted) {
            g.setColor(new Color(0, 0, 0, 150));
            g.fillRect(0, 0, 1200, 800);
            int sec = (countdownFrames / 60) + 1;
            g.setFont(new Font("Arial", Font.BOLD, 110));
            g.setColor(Color.WHITE);
            drawCentered(g, String.valueOf(sec), 600, 430);
        }

        // Winner
        if (gameOver) {
            g.setColor(new Color(0, 0, 0, 160));
            g.fillRect(250, 300, 700, 90);
            g.setColor(Color.GREEN);
            g.setFont(new Font("Arial", Font.BOLD, 44));
            String msg = "PLAYER1".equals(winner) ? "PLAYER 1 WINS!" : "PLAYER 2 WINS!";
            drawCentered(g, msg, 600, 357);
        }
    }

    private void drawSprite(Graphics g, int row, int frame, int x, int y, int w, int h) {
        if (spriteSheet == null) return;
        int sx = frame * FRAME_W;
        int sy = row * FRAME_H;
        if (sx + FRAME_W > spriteSheet.getWidth() || sy + FRAME_H > spriteSheet.getHeight()) return;
        // swap sx1/sx2 to mirror the sprite horizontally
        g.drawImage(spriteSheet, x, y, x + w, y + h, sx + FRAME_W, sy, sx, sy + FRAME_H, null);
    }

    private void drawLoopingLayer(Graphics g, java.awt.Image img, double scroll, int height) {
        drawLoopingLayer(g, img, scroll, height, 0);
    }

    private void drawLoopingLayer(Graphics g, java.awt.Image img, double scroll, int height, int yOffset) {
        if (img == null) return;
        int w = img.getWidth(null);
        if (w <= 0) return;
        for (int x = -(int)(scroll % w); x < 1200; x += w) {
            g.drawImage(img, x, yOffset, w, height, null);
        }
    }

    private void drawHurdle(Graphics g, int hx, int hh, Color color) {
        int top = GROUND_Y - hh + 30;
        g.setColor(color);
        g.fillRect(hx, top, HURDLE_W, hh);
        g.setColor(color.brighter());
        g.fillRect(hx - 4, top, HURDLE_W + 8, 8);
        g.setColor(Color.WHITE);
        for (int y = top + 12; y < top + hh; y += 14) {
            g.fillRect(hx + 4, y, HURDLE_W - 8, 4);
        }
    }

    private void drawSpeedDots(Graphics g, int x, int y, int level, Color color) {
        for (int i = 0; i < 5; i++) {
            g.setColor(i < level ? color : new Color(80, 80, 80));
            g.fillOval(x + i * 14, y, 10, 10);
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

        p1Y = GROUND_Y; p2Y = GROUND_Y;
        p1Vel = 0;       p2Vel = 0;

        difficultyLevel = 0;
    hurdleSpeed = 7;

    spawnHurdlePack();

        hurdlesCleared = 0;

        animTick  = 0;
        animFrame = 0;

        dying      = false;
        deadPlayer = 0;
        deathTick  = 0;
        deathFrame = 0;

        countdownFrames = 120;
        raceStarted     = false;
    }
}
