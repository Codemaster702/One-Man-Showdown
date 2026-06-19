package managers;

import core.ScoreManager;
import java.awt.*;
import java.awt.image.ImageObserver;

/**
 * Manages all UI rendering - menus, HUD, results, and winner screens
 */
public class UIManager {
    private ScoreManager scoreManager;
    private Font titleFont;
    private Font largeFont;
    private Font normalFont;
    private Font smallFont;
    private Color backgroundColor;
    private Color textColor;
    private Color titleColor;
    private Color accentColor;
    private Image menuBackground;
    private Image titleIcon;
    private Image startButtonIcon;
    private Image player1Icon;
    private Image player2Icon;
    private Image resultsBoard;
    private Image winnerBg;
    private Rectangle startButtonBounds;
    private boolean startButtonHovered;

    /**
     * Constructor - initialize fonts and colors
     * @param scoreManager reference to the score manager for displaying scores
     */
    public UIManager(ScoreManager scoreManager) {
        this.scoreManager = scoreManager;
        this.titleFont = new Font("Arial", Font.BOLD, 48);
        this.largeFont = new Font("Arial", Font.BOLD, 36);
        this.normalFont = new Font("Arial", Font.PLAIN, 24);
        this.smallFont = new Font("Arial", Font.PLAIN, 18);
        this.backgroundColor = Color.BLACK;
        this.titleColor = new Color(190, 180, 125);
        this.textColor = Color.WHITE;
        this.accentColor = new Color(70, 28, 0);
        this.startButtonBounds = new Rectangle((1200 - 400) / 2, 500 - 40, 400, 80);
        this.startButtonHovered = false;
        loadMenuIcons();
    }

    private void loadMenuIcons() {
        menuBackground  = AssetManager.load("assets/environment/background.gif");
        titleIcon       = AssetManager.load("assets/environment/title_icon.png");
        startButtonIcon = AssetManager.load("assets/environment/start_button.gif");
        player1Icon     = AssetManager.load("assets/Players/player1_icon.png");
        player2Icon     = AssetManager.load("assets/Players/player2_icon.png");
        resultsBoard    = AssetManager.load("assets/ui/result_scroll.png");
        winnerBg        = AssetManager.load("assets/environment/win_background.gif");
    }

    /**
     * Draw the main menu screen
     * @param g Graphics object to draw with
     */
    public void drawMainMenu(Graphics g, ImageObserver observer) {
        if (menuBackground != null) {
            g.drawImage(menuBackground, 0, 0, 1200, 800, observer);
        } else {
            g.setColor(backgroundColor);
            g.fillRect(0, 0, 1200, 800);
        }

        g.drawImage(titleIcon, 200, 30, 800, 140, null);


        g.setColor(titleColor);
        g.setFont(normalFont);
        drawCenteredString(g, "A 2-Player Minigame Battle", 600, 180);



        g.drawImage(startButtonIcon, startButtonBounds.x, startButtonBounds.y,
                    startButtonBounds.width, startButtonBounds.height, null);
        if (startButtonHovered && g instanceof Graphics2D) {
            Graphics2D g2d = (Graphics2D) g;
            Composite oldComposite = g2d.getComposite();
            g2d.setComposite(AlphaComposite.getInstance(AlphaComposite.SRC_OVER, 0.25f));
            g2d.setColor(Color.WHITE);
            g2d.fillRect(startButtonBounds.x, startButtonBounds.y,
                            startButtonBounds.width, startButtonBounds.height);
            g2d.setComposite(oldComposite);
        }


        
    }



    /**
     * Draw the HUD during gameplay (score display)
     * @param g Graphics object to draw with
     * @param minigameName name of current minigame
     */
    public void drawHUD(Graphics g, String minigameName) {
        g.setColor(textColor);
        g.setFont(normalFont);

        // Draw player 1 score
        g.drawString("P1: " + scoreManager.getPlayer1Wins(), 30, 40);

        // Draw player 2 score
        String p2Score = "P2: " + scoreManager.getPlayer2Wins();
        int p2Width = g.getFontMetrics().stringWidth(p2Score);
        g.drawString(p2Score, 1200 - p2Width - 30, 40);
    }

    // Player colours used to highlight winners (blue for P1, red for P2)
    private static final Color PLAYER1_COLOR = new Color(70, 120, 210);
    private static final Color PLAYER2_COLOR = new Color(200, 30, 50);
    private static final Color BOARD_INK = new Color(60, 38, 18);
    private static final Color BOARD_INK_LIGHT = new Color(110, 78, 48);

    /**
     * Draw the results screen after each minigame
     * @param g Graphics object to draw with
     */
    public void drawResults(Graphics g) {
        Object oldHint = null;
        Graphics2D g2d = null;
        if (g instanceof Graphics2D) {
            g2d = (Graphics2D) g;
            oldHint = g2d.getRenderingHint(RenderingHints.KEY_ANTIALIASING);
            g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
            g2d.setRenderingHint(RenderingHints.KEY_TEXT_ANTIALIASING,
                                 RenderingHints.VALUE_TEXT_ANTIALIAS_ON);
        }

        // Dim the gameplay behind the board
        g.setColor(new Color(0, 0, 0, 180));
        g.fillRect(0, 0, 1200, 800);

        // Wooden board backdrop
        g.drawImage(resultsBoard, 200, 0, 800, 800, null);

        String roundWinner = scoreManager.getRoundWinner();
        boolean p1Won = "PLAYER1".equals(roundWinner);
        boolean p2Won = "PLAYER2".equals(roundWinner);
        Color winnerColor = p1Won ? PLAYER1_COLOR : p2Won ? PLAYER2_COLOR : BOARD_INK;

        // Header
        g.setFont(normalFont);
        g.setColor(BOARD_INK);
        drawCenteredString(g, "ROUND COMPLETE", 600, 145);

        // Winner announcement with a soft shadow for readability
        g.setFont(largeFont);
        drawTextWithShadow(g, scoreManager.getRoundWinnerDisplay(), 600, 225, winnerColor);

        // Scoreboard: two player cards with badge icons + win counts
        drawScoreCard(g, 470, 250, player1Icon, scoreManager.getPlayer1Wins(),
                      PLAYER1_COLOR, p1Won);
        drawScoreCard(g, 730, 250, player2Icon, scoreManager.getPlayer2Wins(),
                      PLAYER2_COLOR, p2Won);

        // Centre divider between the two cards
        g.setFont(largeFont);
        g.setColor(BOARD_INK_LIGHT);
        drawCenteredString(g, "-", 600, 325);

        // Win-target reminder
        g.setFont(smallFont);
        g.setColor(BOARD_INK);
        drawCenteredString(g, "First to " + ScoreManager.getWinThreshold() + " wins the match",
                           600, 470);

        // Styled key prompts
        drawKeyPrompt(g, "R", "Replay Round", 600, 530);
        drawKeyPrompt(g, "N", "Next Minigame", 600, 580);

        if (g2d != null && oldHint != null) {
            g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING, oldHint);
        }
    }

    /**
     * Draw the pause menu — styled exactly like the results board, but offering
     * the choice to advance, restart, or quit instead of replaying a finished round.
     * @param g Graphics object to draw with
     */
    public void drawPauseMenu(Graphics g) {
        Object oldHint = null;
        Graphics2D g2d = null;
        if (g instanceof Graphics2D) {
            g2d = (Graphics2D) g;
            oldHint = g2d.getRenderingHint(RenderingHints.KEY_ANTIALIASING);
            g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
            g2d.setRenderingHint(RenderingHints.KEY_TEXT_ANTIALIASING,
                                 RenderingHints.VALUE_TEXT_ANTIALIAS_ON);
        }

        // Dim the frozen gameplay behind the board
        g.setColor(new Color(0, 0, 0, 180));
        g.fillRect(0, 0, 1200, 800);

        // Wooden board backdrop
        g.drawImage(resultsBoard, 200, 0, 800, 800, null);

        // Header
        g.setFont(normalFont);
        g.setColor(BOARD_INK);
        drawCenteredString(g, "PAUSED", 600, 145);

        // Title with a soft shadow for readability
        g.setFont(largeFont);
        drawTextWithShadow(g, "Game Paused", 600, 225, BOARD_INK);

        // Scoreboard: current standings (no winner to highlight)
        drawScoreCard(g, 470, 250, player1Icon, scoreManager.getPlayer1Wins(),
                      PLAYER1_COLOR, false);
        drawScoreCard(g, 730, 250, player2Icon, scoreManager.getPlayer2Wins(),
                      PLAYER2_COLOR, false);

        // Centre divider between the two cards
        g.setFont(largeFont);
        g.setColor(BOARD_INK_LIGHT);
        drawCenteredString(g, "-", 600, 325);

        // Win-target reminder
        g.setFont(smallFont);
        g.setColor(BOARD_INK);
        drawCenteredString(g, "First to " + ScoreManager.getWinThreshold() + " wins the match",
                           600, 450);

        // Styled key prompts — the three pause options
        drawKeyPrompt(g, "N", "Next Minigame", 600, 510);
        drawKeyPrompt(g, "R", "Restart Minigame", 600, 555);
        drawKeyPrompt(g, "Q", "Quit to Menu", 600, 600);

        if (g2d != null && oldHint != null) {
            g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING, oldHint);
        }
    }

    /**
     * Draw a single player's score card: badge icon above the win count,
     * with a highlight panel when that player won the round.
     */
    private void drawScoreCard(Graphics g, int centerX, int topY, Image icon,
                               int wins, Color color, boolean highlight) {
        int cardW = 150;
        int cardH = 160;
        int cardX = centerX - cardW / 2;

        if (g instanceof Graphics2D) {
            Graphics2D g2d = (Graphics2D) g;
            if (highlight) {
                g2d.setColor(new Color(color.getRed(), color.getGreen(), color.getBlue(), 60));
                g2d.fillRoundRect(cardX, topY, cardW, cardH, 24, 24);
            }
            g2d.setStroke(new BasicStroke(highlight ? 4f : 2f));
            g2d.setColor(highlight ? color : BOARD_INK_LIGHT);
            g2d.drawRoundRect(cardX, topY, cardW, cardH, 24, 24);
            g2d.setStroke(new BasicStroke(1f));
        }

        // Badge icon
        int iconSize = 64;
        if (icon != null) {
            g.drawImage(icon, centerX - iconSize / 2, topY + 18, iconSize, iconSize, null);
        }

        // Win count
        g.setFont(titleFont);
        drawTextWithShadow(g, String.valueOf(wins), centerX, topY + cardH - 18, color);
    }

    /**
     * Draw a keyboard-style key cap followed by its action label, centred as a unit.
     */
    private void drawKeyPrompt(Graphics g, String key, String label, int centerX, int y) {
        g.setFont(smallFont);
        FontMetrics fm = g.getFontMetrics();
        int keyBox = 34;
        int gap = 12;
        int labelWidth = fm.stringWidth(label);
        int totalWidth = keyBox + gap + labelWidth;
        int startX = centerX - totalWidth / 2;
        int boxY = y - keyBox + 6;

        if (g instanceof Graphics2D) {
            Graphics2D g2d = (Graphics2D) g;
            g2d.setColor(BOARD_INK);
            g2d.fillRoundRect(startX, boxY, keyBox, keyBox, 8, 8);
            g2d.setColor(BOARD_INK_LIGHT);
            g2d.setStroke(new BasicStroke(2f));
            g2d.drawRoundRect(startX, boxY, keyBox, keyBox, 8, 8);
            g2d.setStroke(new BasicStroke(1f));
        }

        // Key letter (centred in the cap)
        g.setColor(new Color(235, 220, 190));
        int keyCharWidth = fm.stringWidth(key);
        g.drawString(key, startX + (keyBox - keyCharWidth) / 2, y);

        // Action label
        g.setColor(BOARD_INK);
        g.drawString(label, startX + keyBox + gap, y);
    }

    /**
     * Draw centred text with a subtle dark shadow for contrast on the board.
     */
    private void drawTextWithShadow(Graphics g, String text, int x, int y, Color color) {
        FontMetrics fm = g.getFontMetrics();
        int textX = x - fm.stringWidth(text) / 2;
        g.setColor(new Color(0, 0, 0, 90));
        g.drawString(text, textX + 2, y + 2);
        g.setColor(color);
        g.drawString(text, textX, y);
    }

    /**
     * Draw the winner screen (overall game winner)
     * @param g Graphics object to draw with
     */
    public void drawWinnerScreen(Graphics g) {
        if (winnerBg != null) {
            g.drawImage(winnerBg, 0, 0, 1200, 800, null);
        } else {
            g.setColor(backgroundColor);
            g.fillRect(0, 0, 1200, 800);
        }
        Image celebrate = AssetManager.load("assets/environment/celebration.gif");
        if (celebrate != null) {
            g.drawImage(celebrate, 400, 350, 400, 200, null);
        }

        // Draw winner announcement
        g.setColor(accentColor);
        g.setFont(titleFont);
        drawCenteredString(g, scoreManager.getGameWinnerDisplay(), 600, 150);

        // Draw final score
        g.setColor(textColor);
        g.setFont(largeFont);
        drawCenteredString(g, "Final Score: " + scoreManager.getScoreDisplay(), 600, 280);

        // Draw celebration
        g.setColor(accentColor);
        g.setFont(normalFont);
        drawCenteredString(g, "🎉 VICTORY 🎉", 600, 360);

        // Draw restart instruction
        g.setColor(textColor);
        g.setFont(smallFont);
        drawCenteredString(g, "Press ENTER to return to menu", 600, 480);
    }

    /**
     * Draw a game over / tie screen
     * @param g Graphics object to draw with
     */
    public void drawGameOver(Graphics g) {
        g.setColor(backgroundColor);
        g.fillRect(0, 0, 1200, 800);

        g.setColor(textColor);
        g.setFont(titleFont);
        drawCenteredString(g, "Game Over", 600, 200);

        g.setFont(largeFont);
        drawCenteredString(g, "Final Score: " + scoreManager.getScoreDisplay(), 600, 300);

        g.setFont(smallFont);
        drawCenteredString(g, "Press ENTER to continue", 600, 450);
    }

    /**
     * Helper method to draw centered text
     * @param g Graphics object
     * @param text the text to draw
     * @param x center x coordinate
     * @param y y coordinate (baseline)
     */
    private void drawCenteredString(Graphics g, String text, int x, int y) {
        FontMetrics fm = g.getFontMetrics();
        int textWidth = fm.stringWidth(text);
        g.drawString(text, x - textWidth / 2, y);
    }

    /**
     * Change the color scheme
     * @param bgColor background color
     * @param textCol text color
     * @param accentCol accent color
     */
    public void setColorScheme(Color bgColor, Color textCol, Color accentCol) {
        this.backgroundColor = bgColor;
        this.textColor = textCol;
        this.accentColor = accentCol;
    }

    public void updateStartButtonHover(int mouseX, int mouseY) {
        startButtonHovered = startButtonBounds.contains(mouseX, mouseY);
    }

    private void drawIconWithAlpha(Graphics g, Image icon, int x, int y, int width, int height, float alpha) {
        if (icon == null || !(g instanceof Graphics2D)) {
            if (icon != null) {
                g.drawImage(icon, x, y, width, height, null);
            }
            return;
        }

        Graphics2D g2d = (Graphics2D) g;
        Composite oldComposite = g2d.getComposite();
        g2d.setComposite(AlphaComposite.getInstance(AlphaComposite.SRC_OVER, alpha));
        g2d.drawImage(icon, x, y, width, height, null);
        g2d.setComposite(oldComposite);
    }

    public boolean isStartButtonClicked(int mouseX, int mouseY) {
        return startButtonBounds.contains(mouseX, mouseY);
    }

    /**
     * Get the title font
     * @return title font
     */
    public Font getTitleFont() {
        return titleFont;
    }

    /**
     * Get the normal font
     * @return normal font
     */
    public Font getNormalFont() {
        return normalFont;
    }

    /**
     * Get the player 1 icon for minigame rendering.
     * @return player 1 icon image
     */
    public Image getPlayer1Icon() {
        return player1Icon;
    }

    /**
     * Get the player 2 icon for minigame rendering.
     * @return player 2 icon image
     */
    public Image getPlayer2Icon() {
        return player2Icon;
    }
}
