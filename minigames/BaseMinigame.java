package minigames;

import java.awt.AlphaComposite;
import java.awt.Composite;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.Image;
import core.InputHandler;
import managers.SoundManager;

/**
 * Abstract parent class for all minigames.
 * Every minigame must implement these methods.
 */
public abstract class BaseMinigame {

    // Tracks whether the minigame has ended
    protected boolean gameOver;

    // Optional player icons for minigame rendering
    protected Image player1Icon;
    protected Image player2Icon;

    // Stores the winner of the minigame
    // Values: "PLAYER1", "PLAYER2", or null
    protected String winner;

    /**
     * Constructor
     */
    public BaseMinigame() {
        gameOver = false;
        winner = null;
    }

    /**
     * Updates game logic every frame.
     *
     * @param inputHandler keyboard input manager
     */
    public abstract void update(InputHandler inputHandler);

    /**
     * Draws the minigame.
     *
     * @param g Graphics object used for rendering
     */
    public abstract void draw(Graphics g);

    /**
     * Resets the minigame back to its starting state.
     */
    public abstract void reset();

    /**
     * Sets optional player icons for rendering.
     *
     * @param player1Icon image for player 1
     * @param player2Icon image for player 2
     */
    public void setPlayerIcons(Image player1Icon, Image player2Icon) {
        this.player1Icon = player1Icon;
        this.player2Icon = player2Icon;
    }

    /**
     * Returns whether the minigame has finished.
     *
     * @return true if game is over
     */
    public boolean isGameOver() {
        return gameOver;
    }

    /**
     * Returns the winner of the minigame.
     *
     * @return "PLAYER1", "PLAYER2", or null
     */
    public String getWinner() {
        return winner;
    }

    /**
     * The looping ambient/background track for this minigame. MainGame plays
     * this when the minigame starts. Subclasses override it with their own
     * theme; the default is silence.
     *
     * @return path to a sound file, or null for no ambient track
     */
    public String getAmbientMusic() {
        return null;
    }

    /**
     * Draw a player icon with transparency.
     *
     * @param g Graphics object
     * @param icon the player icon image
     * @param x x coordinate
     * @param y y coordinate
     * @param width width to draw
     * @param height height to draw
     */
    protected void drawPlayerIcon(Graphics g, Image icon, int x, int y, int width, int height) {
        if (icon == null) {
            return;
        }

        if (g instanceof Graphics2D) {
            Graphics2D g2d = (Graphics2D) g;
            Composite oldComposite = g2d.getComposite();
            g2d.setComposite(AlphaComposite.getInstance(AlphaComposite.SRC_OVER, 0.75f));
            g2d.drawImage(icon, x, y, width, height, null);
            g2d.setComposite(oldComposite);
        } else {
            g.drawImage(icon, x, y, width, height, null);
        }
    }

    /**
     * Ends the minigame and sets the winner.
     *
     * @param winner "PLAYER1" or "PLAYER2"
     */
    protected void endGame(String winner) {
        this.gameOver = true;
        this.winner = winner;
        // Round-win jingle, shared by every minigame.
        SoundManager.play("assets/sounds/win.wav");
    }
}