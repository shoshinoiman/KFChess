package server.pieces;

import java.awt.*;
import java.awt.geom.Point2D;
import java.awt.image.BufferedImage;

import server.interfaces.IPiece;

/**
 * Utility class for rendering pieces on the board.
 */
public class PieceRenderer {
    /**
     * Draws a piece on the board using its current state and animation frame.
     * @param g Graphics context
     * @param p The piece to draw
     * @param squareWidth Width of a board square
     * @param squareHeight Height of a board square
     */
    public static void draw(Graphics g, IPiece p, int squareWidth, int squareHeight) {
        BufferedImage frame = p.getCurrentState().getGraphics().getCurrentFrame();

        int pixelX, pixelY;
        
        // אם הכלי במהלך תנועה, השתמש במיקום הפיקסלי לאנימציה חלקה
        if (p.getCurrentStateName().toString().equals("move")) {
            Point2D.Double pos = p.getCurrentPixelPosition();
            pixelX = (int) (pos.x * squareWidth / 64.0);
            pixelY = (int) (pos.y * squareHeight / 64.0);
        } else {
            // אחרת, השתמש במיקום הלוגי הרגיל
            pixelX = p.getCol() * squareWidth;
            pixelY = p.getRow() * squareHeight;
        }

        g.drawImage(frame, pixelX, pixelY, squareWidth, squareHeight, null);
    }

}
