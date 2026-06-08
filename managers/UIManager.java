package managers;

import core.ScoreManager;
import javax.imageio.ImageIO;
import java.awt.*;
import java.awt.geom.AffineTransform;
import java.io.File;
import java.io.IOException;

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
    private Color accentColor;
    private Image menuBackground;
    private Image titleIcon;
    private Image startButtonIcon;
    private Image player1Icon;
    private Image player2Icon;
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
        this.textColor = Color.WHITE;
        this.accentColor = Color.CYAN;
        this.startButtonBounds = new Rectangle((1200 - 400) / 2, 500 - 40, 400, 80);
        this.startButtonHovered = false;
        loadMenuIcons();
    }

    private void loadMenuIcons() {
        try {
            menuBackground = ImageIO.read(new File("assets/environment/menu_background.jpg"));
            titleIcon = ImageIO.read(new File("assets/environment/title_icon.png"));
            startButtonIcon = ImageIO.read(new File("assets/environment/play_button.png"));
            player1Icon = ImageIO.read(new File("assets/Players/player1_icon.png"));
            player2Icon = ImageIO.read(new File("assets/Players/player2_icon.png"));
        } catch (IOException e) {
            System.err.println("Failed to load menu icons: " + e.getMessage());
        }
    }

    /**
     * Draw the main menu screen
     * @param g Graphics object to draw with
     */
    public void drawMainMenu(Graphics g) {
        if (menuBackground != null) {
            g.drawImage(menuBackground, 0, 0, 1200, 800, null);
        } else {
            g.setColor(backgroundColor);
            g.fillRect(0, 0, 1200, 800);
        }

        if (titleIcon != null) {
            g.drawImage(titleIcon, 200, 30, 800, 140, null);
        } else {
            g.setColor(accentColor);
            g.setFont(titleFont);
            drawCenteredString(g, "One Fun Showdown", 600, 100);
        }

        g.setColor(textColor);
        g.setFont(normalFont);
        drawCenteredString(g, "A 2-Player Minigame Battle", 600, 180);

        int iconY = 250;
        g.setFont(smallFont);

        if (player1Icon != null) {
            drawIconWithAlpha(g, player1Icon, 300, iconY, 100, 100, 0.75f);
            drawCenteredString(g, "Player 1: Z Key", 350, iconY + 140);
        } else {
            drawCenteredString(g, "Player 1: Z Key", 300, iconY + 60);
        }

        if (player2Icon != null) {
            drawIconWithAlpha(g, player2Icon, 800, iconY, 100, 100, 0.75f);
            drawCenteredString(g, "Player 2: / Key", 850, iconY + 140);
        } else {
            drawCenteredString(g, "Player 2: / Key", 800, iconY + 60);
        }

        g.setFont(normalFont);
        drawCenteredString(g, "Compete in random minigames", 600, 380);
        drawCenteredString(g, "First to 5 wins takes the crown!", 600, 430);

        if (startButtonIcon != null) {
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
        } else {
            g.setColor(startButtonHovered ? accentColor : Color.WHITE);
            g.fillRect(startButtonBounds.x, startButtonBounds.y,
                       startButtonBounds.width, startButtonBounds.height);
            g.setColor(Color.BLACK);
            g.drawRect(startButtonBounds.x, startButtonBounds.y,
                       startButtonBounds.width, startButtonBounds.height);
            g.setColor(textColor);
            g.setFont(largeFont);
            drawCenteredString(g, "Press ENTER to Start", 600, 525);
        }

        
    }

    /**
     * Draw the instructions screen
     * @param g Graphics object to draw with
     */
    public void drawInstructions(Graphics g) {
        g.setColor(backgroundColor);
        g.fillRect(0, 0, 1200, 800);

        g.setColor(accentColor);
        g.setFont(titleFont);
        drawCenteredString(g, "How to Play", 600, 80);

        g.setColor(textColor);
        g.setFont(normalFont);
        int y = 180;
        int spacing = 50;

        drawCenteredString(g, "Each minigame has different rules", 600, y);
        y += spacing;
        drawCenteredString(g, "Use your assigned key to compete", 600, y);
        y += spacing;
        drawCenteredString(g, "Win minigames to earn points", 600, y);
        y += spacing;
        drawCenteredString(g, "First player to 5 wins wins the game!", 600, y);

        g.setFont(smallFont);
        y += 80;
        drawCenteredString(g, "Press ENTER to return to menu", 600, y);
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

        // Draw minigame name in center
        g.setFont(smallFont);
        g.setColor(accentColor);
        String gameName = "Minigame: " + minigameName;
        int nameWidth = g.getFontMetrics().stringWidth(gameName);
        g.drawString(gameName, (1200 - nameWidth) / 2, 40);
    }

    /**
     * Draw the results screen after each minigame
     * @param g Graphics object to draw with
     */
    public void drawResults(Graphics g) {
        // Draw semi-transparent overlay
        g.setColor(new Color(0, 0, 0, 200));
        g.fillRect(0, 0, 1200, 800);

        // Draw results box
        g.setColor(backgroundColor);
        g.fillRect(350, 150, 500, 300);
        g.setColor(accentColor);
        if (g instanceof Graphics2D) {
            ((Graphics2D) g).setStroke(new BasicStroke(3));
        }
        g.drawRect(350, 150, 500, 300);

        // Draw round winner
        g.setColor(accentColor);
        g.setFont(largeFont);
        drawCenteredString(g, scoreManager.getRoundWinnerDisplay(), 600, 200);

        // Draw current score
        g.setColor(textColor);
        g.setFont(normalFont);
        drawCenteredString(g, "Score: " + scoreManager.getScoreDisplay(), 600, 260);

        // Draw options
        g.setFont(smallFont);
        g.setColor(accentColor);
        drawCenteredString(g, "R = Replay Round", 600, 340);
        drawCenteredString(g, "N = Next Minigame", 600, 370);
    }

    /**
     * Draw the winner screen (overall game winner)
     * @param g Graphics object to draw with
     */
    public void drawWinnerScreen(Graphics g) {
        // Draw background
        g.setColor(backgroundColor);
        g.fillRect(0, 0, 1200, 800);

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
