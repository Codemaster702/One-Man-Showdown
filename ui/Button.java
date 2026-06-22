//Mohammed Shekhibrahim
//June 15 2026
//Button base class

package ui;

import java.awt.*;

public class Button {
    private int x;
    private int y;
    private int width;
    private int height;
    private String label;
    private boolean hovered;
    private Color normalColor;
    private Color hoverColor;
    private Color textColor;
    private Font font;

    /**
     * Constructor for a clickable button
     * @param x x-coordinate of top-left corner
     * @param y y-coordinate of top-left corner
     * @param width width of button
     * @param height height of button
     * @param label text displayed on button
     */
    public Button(int x, int y, int width, int height, String label) {
        this.x = x;
        this.y = y;
        this.width = width;
        this.height = height;
        this.label = label;
        this.hovered = false;
        this.normalColor = Color.WHITE;
        this.hoverColor = Color.YELLOW;
        this.textColor = Color.BLACK;
        this.font = new Font("Arial", Font.BOLD, 18);
    }

    /**
     * Draw the button on the screen
     * @param g Graphics object to draw with
     */
    public void draw(Graphics g) {
        // Draw button background
        if (hovered) {
            g.setColor(hoverColor);
        } else {
            g.setColor(normalColor);
        }
        g.fillRect(x, y, width, height);

        // Draw button border
        g.setColor(Color.BLACK);
        if (g instanceof Graphics2D) {
            ((Graphics2D) g).setStroke(new BasicStroke(2));
        }
        g.drawRect(x, y, width, height);

        // Draw button text
        g.setColor(textColor);
        g.setFont(font);
        FontMetrics fm = g.getFontMetrics();
        int textX = x + (width - fm.stringWidth(label)) / 2;
        int textY = y + ((height - fm.getHeight()) / 2) + fm.getAscent();
        g.drawString(label, textX, textY);
    }

    /**
     * Check if the button was clicked
     * @param mouseX x-coordinate of mouse click
     * @param mouseY y-coordinate of mouse click
     * @return true if click is within button bounds
     */
    public boolean isClicked(int mouseX, int mouseY) {
        return mouseX >= x && mouseX <= x + width &&
               mouseY >= y && mouseY <= y + height;
    }

    /**
     * Check if mouse is hovering over button
     * @param mouseX x-coordinate of mouse
     * @param mouseY y-coordinate of mouse
     */
    public void updateHover(int mouseX, int mouseY) {
        hovered = mouseX >= x && mouseX <= x + width &&
                  mouseY >= y && mouseY <= y + height;
    }

    // Getters and Setters
    public String getLabel() {
        return label;
    }

    public void setLabel(String label) {
        this.label = label;
    }

    public void setHovered(boolean hovered) {
        this.hovered = hovered;
    }

    public boolean isHovered() {
        return hovered;
    }

    public void setNormalColor(Color color) {
        this.normalColor = color;
    }

    public void setHoverColor(Color color) {
        this.hoverColor = color;
    }

    public void setTextColor(Color color) {
        this.textColor = color;
    }

    public void setFont(Font font) {
        this.font = font;
    }

    public int getX() {
        return x;
    }

    public int getY() {
        return y;
    }

    public int getWidth() {
        return width;
    }

    public int getHeight() {
        return height;
    }
}
