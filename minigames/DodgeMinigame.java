//Mohammed Shekhibrahim
//June 15 2026
//Dodge minigame

package minigames;

import entities.Obstacle;
import java.awt.Color;
import java.awt.Font;
import java.awt.Graphics;
import java.awt.Rectangle;
import java.awt.image.BufferedImage;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;

import managers.AssetManager;
import managers.SoundManager;
import core.GamePanel;
import core.InputHandler;

public class DodgeMinigame extends BaseMinigame {

    private int player1X, player2X;
    private int player1Y, player2Y;
    private static final int PLAYER_SIZE = 38;

    private static final int HALF       = GamePanel.WIDTH / 2;
    private static final int BLOCK_W    = 55;
    private static final int BLOCK_H    = 38;
    private static final int MOVE_SPEED = 6;

    // Floor line: players' feet rest here and falling blocks stack up from it
    private static final int GROUND_Y = GamePanel.HEIGHT - BLOCK_H - 20;
    // Difficulty scaling
    private static final int BASE_BLOCK_SPEED  = 3;
    private static final int BASE_SPAWN_RATE   = 55; // frames between spawns
    private static final int DIFF_INTERVAL     = 480; // frames between difficulty increases (~8s)

    private int blockSpeed;
    private int spawnRate;
    private int frameCount;
    private int spawnCounter;

    private List<Obstacle> fallingBlocks;
    private List<Obstacle> landedBlocks;
    private final Random random = new Random();

    // Survival timers (frames)
    private int p1AliveFrames;
    private int p2AliveFrames;

    private java.awt.Image dodgeBg;
    private java.awt.Image blockImg;

    // Shared mushroom sprite sheets (single row, 80x64 per frame)
    private BufferedImage runSheet;
    private BufferedImage dieSheet;
    private static final int FRAME_W = 80;
    private static final int FRAME_H = 64;
    private static final int RUN_FRAMES = 8;
    private static final int DIE_FRAMES = 15;
    private static final int ANIM_SPEED       = 5; // game frames per run frame
    private static final int DEATH_ANIM_SPEED = 4; // game frames per death frame

    // Run-cycle animation, per player
    private int p1AnimTick, p1AnimFrame;
    private int p2AnimTick, p2AnimFrame;
    private boolean p1FacingRight, p2FacingRight;

    // Death animation state, per player
    private boolean p1Dying, p2Dying;
    private int p1DeathTick, p1DeathFrame;
    private int p2DeathTick, p2DeathFrame;

    public DodgeMinigame() {
        reset();
        dodgeBg  = AssetManager.load("assets/environment/dodge_background.gif");
        blockImg = AssetManager.load("assets/environment/block.png");
        runSheet = AssetManager.loadBuffered("assets/Players/Mushroom with VFX/Mushroom-Run.png");
        dieSheet = AssetManager.loadBuffered("assets/Players/Mushroom with VFX/Mushroom-Die.png");
    }

    @Override
    public String getAmbientMusic() {
        return "assets/sounds/dodge_ambient.wav";
    }

    @Override
    public void update(InputHandler input) {
        if (gameOver) return;

        // Play out a death animation, then award the round to the survivor.
        if (p1Dying) {
            p1DeathTick++;
            if (p1DeathTick >= DEATH_ANIM_SPEED) {
                p1DeathTick = 0;
                if (p1DeathFrame < DIE_FRAMES - 1) p1DeathFrame++;
                else endGame("PLAYER2");
            }
            return;
        }
        if (p2Dying) {
            p2DeathTick++;
            if (p2DeathTick >= DEATH_ANIM_SPEED) {
                p2DeathTick = 0;
                if (p2DeathFrame < DIE_FRAMES - 1) p2DeathFrame++;
                else endGame("PLAYER1");
            }
            return;
        }

        frameCount++;
        if (!gameOver) p1AliveFrames++;
        if (!gameOver) p2AliveFrames++;

        // Difficulty ramp every DIFF_INTERVAL frames
        if (frameCount % DIFF_INTERVAL == 0) {
            if (blockSpeed < 9)    blockSpeed++;
            if (spawnRate  > 18)   spawnRate -= 5;
        }

        // P1 movement (hold = right, release = left) — left half
        if (input.isPlayer1Pressed()) { player1X += MOVE_SPEED; p1FacingRight = true; }
        else                          { player1X -= MOVE_SPEED; p1FacingRight = false; }
        player1X = Math.max(20, Math.min(player1X, HALF - PLAYER_SIZE - 20));

        // Advance player 1's running animation (always moving in this game)
        p1AnimTick++;
        if (p1AnimTick >= ANIM_SPEED) {
            p1AnimTick = 0;
            p1AnimFrame = (p1AnimFrame + 1) % RUN_FRAMES;
        }

        // P2 movement — right half
        if (input.isPlayer2Pressed()) { player2X += MOVE_SPEED; p2FacingRight = true; }
        else                          { player2X -= MOVE_SPEED; p2FacingRight = false; }
        player2X = Math.max(HALF + 20, Math.min(player2X, GamePanel.WIDTH - PLAYER_SIZE - 20));

        // Advance player 2's running animation
        p2AnimTick++;
        if (p2AnimTick >= ANIM_SPEED) {
            p2AnimTick = 0;
            p2AnimFrame = (p2AnimFrame + 1) % RUN_FRAMES;
        }

        // Spawn blocks — one in each half per cycle for symmetry
        spawnCounter++;
        if (spawnCounter >= spawnRate) {
            spawnBlockInHalf(0);          // P1 half
            spawnBlockInHalf(HALF);       // P2 half
            spawnCounter = 0;
        }

        // Update falling blocks
        for (int i = fallingBlocks.size() - 1; i >= 0; i--) {
            Obstacle b = fallingBlocks.get(i);
            b.setY(b.getY() + blockSpeed);

            if (checkHit(b, player1X, player1Y)) { startP1Death(); return; }
            if (checkHit(b, player2X, player2Y)) { startP2Death(); return; }

            if (hasLanded(b)) {
                landedBlocks.add(b);
                fallingBlocks.remove(i);
            }
        }

        // Update player Y from stacked blocks
        player1Y = standingY(player1X);
        player2Y = standingY(player2X);

        // Check if any stack is too high
        for (Obstacle b : landedBlocks) {
            if (b.getY() < 60) {
                // The player on that side loses
                if (b.getX() + b.getWidth() <= HALF) endGame("PLAYER2");
                else                                   endGame("PLAYER1");
                return;
            }
        }
    }

    private void spawnBlockInHalf(int halfStart) {
        int maxX = (halfStart == 0) ? HALF - BLOCK_W - 10 : GamePanel.WIDTH - BLOCK_W - 10;
        int x = halfStart + 10 + random.nextInt(maxX - halfStart);
        fallingBlocks.add(new Obstacle(x, -BLOCK_H, BLOCK_W, BLOCK_H));
    }

    private void startP1Death() {
        p1Dying     = true;
        p1DeathTick = 0;
        p1DeathFrame = 0;
        // A block clipped player 1.
        SoundManager.play("assets/sounds/hit.wav");
    }

    private void startP2Death() {
        p2Dying     = true;
        p2DeathTick = 0;
        p2DeathFrame = 0;
        // A block clipped player 2.
        SoundManager.play("assets/sounds/hit.wav");
    }

    private boolean checkHit(Obstacle b, int px, int py) {
        return new Rectangle(px, py, PLAYER_SIZE, PLAYER_SIZE)
                .intersects(new Rectangle(b.getX(), b.getY(), b.getWidth(), b.getHeight()));
    }

    private boolean hasLanded(Obstacle b) {
        if (b.getY() + b.getHeight() >= GROUND_Y) {
            b.setY(GROUND_Y - b.getHeight());
            return true;
        }
        for (Obstacle landed : landedBlocks) {
            if (b.getX() + b.getWidth() > landed.getX()
                    && b.getX() < landed.getX() + landed.getWidth()
                    && b.getY() + b.getHeight() >= landed.getY()
                    && b.getY() + b.getHeight() <= landed.getY() + blockSpeed * 2 + 2) {
                b.setY(landed.getY() - b.getHeight());
                return true;
            }
        }
        return false;
    }

    private int standingY(int px) {
        int floor = GROUND_Y - PLAYER_SIZE;
        for (Obstacle b : landedBlocks) {
            if (px + PLAYER_SIZE > b.getX() && px < b.getX() + b.getWidth()) {
                int top = b.getY() - PLAYER_SIZE;
                if (top < floor) floor = top;
            }
        }
        return floor;
    }

    @Override
    public void draw(Graphics g) {
        // Background
        if (dodgeBg != null) {
            g.drawImage(dodgeBg, 0, 0, GamePanel.WIDTH, GamePanel.HEIGHT, null);
        } else {
            g.setColor(new Color(15, 15, 25));
            g.fillRect(0, 0, GamePanel.WIDTH, GamePanel.HEIGHT);
            g.setColor(new Color(20, 20, 50));
            g.fillRect(0, 0, HALF, GamePanel.HEIGHT);
            g.setColor(new Color(50, 15, 15));
            g.fillRect(HALF, 0, HALF, GamePanel.HEIGHT);
        }

        // Divider
        g.setColor(new Color(100, 100, 100));
        g.drawLine(HALF, 0, HALF, GamePanel.HEIGHT);

        // Lane labels
        g.setFont(new Font("Arial", Font.BOLD, 20));
        g.setColor(new Color(100, 140, 255));
        g.drawString("PLAYER 1", 20, 35);
        g.setColor(new Color(255, 120, 120));
        g.drawString("PLAYER 2", HALF + 20, 35);

        // Controls
        g.setFont(new Font("Arial", Font.PLAIN, 14));
        g.setColor(new Color(180, 180, 180));
        g.drawString("Hold Z = Right  |  Release = Left", 20, 58);
        g.drawString("Hold / = Right  |  Release = Left", HALF + 20, 58);

        // Survival timers
        g.setFont(new Font("Arial", Font.BOLD, 18));
        g.setColor(Color.WHITE);
        g.drawString("Time: " + framesToSec(p1AliveFrames), 20, 82);
        g.drawString("Time: " + framesToSec(p2AliveFrames), HALF + 20, 82);



        // Landed blocks
        for (Obstacle b : landedBlocks) {
            if (blockImg != null) {
                g.drawImage(blockImg, b.getX(), b.getY(), b.getWidth(), b.getHeight(), null);
            } else {
                boolean inP1Half = b.getX() + b.getWidth() / 2 < HALF;
                g.setColor(inP1Half ? new Color(60, 80, 200) : new Color(200, 60, 60));
                g.fillRect(b.getX(), b.getY(), b.getWidth(), b.getHeight());
                g.setColor(inP1Half ? new Color(40, 60, 160) : new Color(160, 40, 40));
                g.drawRect(b.getX(), b.getY(), b.getWidth(), b.getHeight());
            }
        }

        // Falling blocks — tinted reddish while in the air, normal once landed
        for (Obstacle b : fallingBlocks) {
            if (blockImg != null) {
                g.drawImage(blockImg, b.getX(), b.getY(), b.getWidth(), b.getHeight(), null);
                // Translucent red overlay to signal "still falling"
                g.setColor(new Color(255, 40, 40, 120));
                g.fillRect(b.getX(), b.getY(), b.getWidth(), b.getHeight());
            } else {
                g.setColor(new Color(230, 70, 70));
                g.fillRect(b.getX(), b.getY(), b.getWidth(), b.getHeight());
                g.setColor(new Color(160, 30, 30));
                g.drawRect(b.getX(), b.getY(), b.getWidth(), b.getHeight());
            }
        }

        // Players — animated mushroom sprites (run / death)
        drawPlayer1(g);
        drawPlayer2(g);

        // Winner
        if (gameOver) {
            g.setColor(new Color(0, 0, 0, 170));
            g.fillRect(200, 310, 800, 100);
            g.setFont(new Font("Arial", Font.BOLD, 40));
            g.setColor(Color.GREEN);
            if ("PLAYER1".equals(winner))      drawCentered(g, "PLAYER 1 WINS!", 600, 373);
            else if ("PLAYER2".equals(winner)) drawCentered(g, "PLAYER 2 WINS!", 600, 373);

            // Show survival times
            g.setFont(new Font("Arial", Font.PLAIN, 22));
            g.setColor(Color.WHITE);
            g.drawString("P1: " + framesToSec(p1AliveFrames), 260, 400);
            g.drawString("P2: " + framesToSec(p2AliveFrames), 760, 400);
        }
    }

    private String framesToSec(int frames) {
        int s = frames / 60;
        int ms = (frames % 60) * 100 / 60;
        return s + "." + (ms < 10 ? "0" : "") + ms + "s";
    }

    private Color levelColor(int level) {
        if (level <= 2) return Color.GREEN;
        if (level <= 4) return Color.YELLOW;
        return Color.RED;
    }

    private void drawPlayer1(Graphics g) {
        BufferedImage sheet = p1Dying ? dieSheet : runSheet;
        int frame           = p1Dying ? p1DeathFrame : p1AnimFrame;

        if (sheet == null) {
            // Fallback to the original blue box if the sprite sheet is missing.
            g.setColor(Color.BLUE);
            g.fillRect(player1X, player1Y, PLAYER_SIZE, PLAYER_SIZE);
            if (player1Icon != null) drawPlayerIcon(g, player1Icon, player1X, player1Y - 46, PLAYER_SIZE, PLAYER_SIZE);
            return;
        }

        // Scale the sprite up a little and align its feet to the hitbox bottom.
        int drawW = 90, drawH = 72;
        int dx = player1X + PLAYER_SIZE / 2 - drawW / 2;
        int dy = player1Y + PLAYER_SIZE - drawH;
        drawSprite(g, sheet, frame, dx, dy, drawW, drawH, p1FacingRight);
    }

    private void drawPlayer2(Graphics g) {
        BufferedImage sheet = p2Dying ? dieSheet : runSheet;
        int frame           = p2Dying ? p2DeathFrame : p2AnimFrame;

        if (sheet == null) {
            // Fallback to the original red box if the sprite sheet is missing.
            g.setColor(Color.RED);
            g.fillRect(player2X, player2Y, PLAYER_SIZE, PLAYER_SIZE);
            if (player2Icon != null) drawPlayerIcon(g, player2Icon, player2X, player2Y - 46, PLAYER_SIZE, PLAYER_SIZE);
            return;
        }

        // Scale the sprite up a little and align its feet to the hitbox bottom.
        int drawW = 90, drawH = 72;
        int dx = player2X + PLAYER_SIZE / 2 - drawW / 2;
        int dy = player2Y + PLAYER_SIZE - drawH;
        drawSprite(g, sheet, frame, dx, dy, drawW, drawH, p2FacingRight);
    }

    private void drawSprite(Graphics g, BufferedImage sheet, int frame, int x, int y, int w, int h, boolean facingRight) {
        int sx = frame * FRAME_W;
        if (sx + FRAME_W > sheet.getWidth()) return;
        // The source art faces left, so draw mirrored when facing right.
        if (facingRight) {
            // Mirror horizontally by swapping the source x edges.
            g.drawImage(sheet, x + w, y, x, y + h, sx, 0, sx + FRAME_W, FRAME_H, null);
        } else {
            g.drawImage(sheet, x, y, x + w, y + h, sx, 0, sx + FRAME_W, FRAME_H, null);
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

        player1X = 150;
        player2X = HALF + 150;
        player1Y = GROUND_Y - PLAYER_SIZE;
        player2Y = GROUND_Y - PLAYER_SIZE;

        blockSpeed   = BASE_BLOCK_SPEED;
        spawnRate    = BASE_SPAWN_RATE;
        frameCount   = 0;
        spawnCounter = 0;

        p1AliveFrames = 0;
        p2AliveFrames = 0;

        p1AnimTick    = 0;
        p1AnimFrame   = 0;
        p1FacingRight = true;
        p1Dying       = false;
        p1DeathTick   = 0;
        p1DeathFrame  = 0;

        p2AnimTick    = 0;
        p2AnimFrame   = 0;
        p2FacingRight = true;
        p2Dying       = false;
        p2DeathTick   = 0;
        p2DeathFrame  = 0;

        fallingBlocks = new ArrayList<>();
        landedBlocks  = new ArrayList<>();
    }
}
