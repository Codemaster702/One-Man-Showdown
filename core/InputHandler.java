package core;

import java.awt.event.KeyListener;
import java.awt.event.KeyEvent;
import java.util.HashSet;
import java.util.Set;

/**
 * Handles keyboard input for both players
 */
public class InputHandler implements KeyListener {
    private Set<Integer> pressedKeys;
    private Set<Integer> keysThisFrame;

    // Player key assignments
    private int player1Key;
    private int player2Key;

    /**
     * Constructor - sets up default key assignments
     * Player 1: Z key
     * Player 2: / (slash) key
     */
    public InputHandler() {
        this.pressedKeys = new HashSet<>();
        this.keysThisFrame = new HashSet<>();
        this.player1Key = KeyEvent.VK_Z;
        this.player2Key = KeyEvent.VK_SLASH;
    }

    /**
     * Constructor - custom key assignments
     * @param p1Key key code for player 1
     * @param p2Key key code for player 2
     */
    public InputHandler(int p1Key, int p2Key) {
        this.pressedKeys = new HashSet<>();
        this.keysThisFrame = new HashSet<>();
        this.player1Key = p1Key;
        this.player2Key = p2Key;
    }

    @Override
    public void keyPressed(KeyEvent e) {
        pressedKeys.add(e.getKeyCode());
        keysThisFrame.add(e.getKeyCode());
    }

    @Override
    public void keyReleased(KeyEvent e) {
        pressedKeys.remove(e.getKeyCode());
    }

    @Override
    public void keyTyped(KeyEvent e) {
        // Not needed for our game
    }

    /**
     * Check if player 1's key is currently pressed
     * @return true if player 1's key is being held down
     */
    public boolean isPlayer1Pressed() {
        return pressedKeys.contains(player1Key);
    }

    /**
     * Check if player 2's key is currently pressed
     * @return true if player 2's key is being held down
     */
    public boolean isPlayer2Pressed() {
        return pressedKeys.contains(player2Key);
    }

    /**
     * Check if player 1 pressed their key THIS FRAME (used for one-time actions)
     * @return true if player 1 pressed their key this frame
     */
    public boolean isPlayer1PressedThisFrame() {
        return keysThisFrame.contains(player1Key);
    }

    /**
     * Check if player 2 pressed their key THIS FRAME (used for one-time actions)
     * @return true if player 2 pressed their key this frame
     */
    public boolean isPlayer2PressedThisFrame() {
        return keysThisFrame.contains(player2Key);
    }

    /**
     * Check if any key is currently pressed
     * @param keyCode the key code to check
     * @return true if that key is being held down
     */
    public boolean isKeyPressed(int keyCode) {
        return pressedKeys.contains(keyCode);
    }

    /**
     * Check if a key was pressed this frame
     * @param keyCode the key code to check
     * @return true if that key was pressed this frame
     */
    public boolean isKeyPressedThisFrame(int keyCode) {
        return keysThisFrame.contains(keyCode);
    }

    /**
     * Clear the "pressed this frame" set (call this once per frame at the end of update)
     */
    public void clearPressedKeys() {
        keysThisFrame.clear();
    }

    /**
     * Check for menu navigation keys
     */
    public boolean isEnterPressed() {
        return isKeyPressedThisFrame(KeyEvent.VK_ENTER);
    }

    public boolean isEscapePressed() {
        return isKeyPressedThisFrame(KeyEvent.VK_ESCAPE);
    }

    /**
     * Change player key assignments
     * @param p1Key new key code for player 1
     * @param p2Key new key code for player 2
     */
    public void setPlayerKeys(int p1Key, int p2Key) {
        this.player1Key = p1Key;
        this.player2Key = p2Key;
    }

    /**
     * Get player 1's assigned key
     * @return key code
     */
    public int getPlayer1Key() {
        return player1Key;
    }

    /**
     * Get player 2's assigned key
     * @return key code
     */
    public int getPlayer2Key() {
        return player2Key;
    }
}