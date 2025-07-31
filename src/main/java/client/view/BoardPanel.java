package client.view;


import client.SimpleBoardData;
import client.SimplePieceData;
import server.board.BoardRenderer;
import server.interfaces.*;

import javax.imageio.ImageIO;
import javax.swing.*;

import java.awt.*;
import java.awt.event.KeyAdapter;
import java.awt.event.KeyEvent;
import java.awt.image.BufferedImage;
import java.io.IOException;
import java.net.URL;
import java.util.function.Consumer;
import utils.LogUtils;

/**
 * Panel for displaying the game board and handling player input.
 */
public class BoardPanel extends JPanel {
    private BufferedImage boardImage;
    private IBoard board;
    private SimpleBoardData simpleBoardData;

    private final IPlayerCursor cursor1;
    private final IPlayerCursor cursor2;

    private Consumer<Void> onPlayer1Action;
    private Consumer<Void> onPlayer2Action;

    /**
     * Constructs the board panel, sets up key listeners and loads board image.
     */
    // public BoardPanel(IBoard board, IPlayerCursor pc1, IPlayerCursor pc2) {
     public BoardPanel(IBoard board, IPlayerCursor pc1, IPlayerCursor pc2) {
       
    this.board = board;
        this.cursor1 = pc1;
        this.cursor2 = pc2;

        setPreferredSize(new Dimension(800, 800));
        setFocusable(true);
        

        loadBoardImage();

        addKeyListener(new KeyAdapter() {
            @Override
            public void keyPressed(KeyEvent e) {
                handleKey(e);
            }
        });
    }
    public void setBord(IBoard board) {
        this.board = board;
        updateAll(); // עדכון מצב הכלים והלוח
        repaint();
    }
    /**
     * Loads the board image from resources for rendering.
     */
    private void loadBoardImage() {
        try {
            URL imageUrl = getClass().getClassLoader().getResource("board/board.png");
            if (imageUrl != null) {
                boardImage = ImageIO.read(imageUrl);
            } else {
                System.err.println("Image not found in resources!");
                LogUtils.logDebug("Image not found in resources!");
            }
        } catch (IOException e) {
            e.printStackTrace();
            LogUtils.logDebug("Exception loading board image: " + e.getMessage());
        }
    }

    /**
     * Handles keyboard input for moving player cursors and triggering actions.
     *
     * @param e The KeyEvent to handle.
     */
    private void handleKey(KeyEvent e) {
        int key = e.getKeyCode();

        switch (key) {
            case KeyEvent.VK_UP:
                cursor1.moveUp();
                break;
            case KeyEvent.VK_DOWN:
                cursor1.moveDown();
                break;
            case KeyEvent.VK_LEFT:
                cursor1.moveLeft();
                break;
            case KeyEvent.VK_RIGHT:
                cursor1.moveRight();
                break;
            case KeyEvent.VK_ENTER:
                if (onPlayer1Action != null) onPlayer1Action.accept(null);
                break;
        }

        switch (key) {
            case KeyEvent.VK_W:
                cursor2.moveUp();
                break;
            case KeyEvent.VK_S:
                cursor2.moveDown();
                break;
            case KeyEvent.VK_A:
                cursor2.moveLeft();
                break;
            case KeyEvent.VK_D:
                cursor2.moveRight();
                break;
            case KeyEvent.VK_SPACE:
                if (onPlayer2Action != null)
                    onPlayer2Action.accept(null);
                break;
        }

        repaint();
    }

    /**
     * Sets the action handler for player 1.
     */
    public void setOnPlayer1Action(Consumer<Void> handler) {
        this.onPlayer1Action = handler;
    }

    /**
     * Sets the action handler for player 2.
     */
    public void setOnPlayer2Action(Consumer<Void> handler) {
        this.onPlayer2Action = handler;
    }

    /**
     * Updates all pieces on the board by calling board.updateAll().
     */
    public void updateAll() {
        if (board != null)
            board.updateAll();
    }

    /**
     * Updates the board panel with simple data from the server.
     */
    public void updateFromSimpleData(SimpleBoardData boardData) {
        System.out.println("DEBUG: BoardPanel.updateFromSimpleData called with " + 
            (boardData != null ? boardData.getPieces().size() + " pieces" : "null data"));
        this.simpleBoardData = boardData;
        repaint();
        System.out.println("DEBUG: BoardPanel repaint() called");
    }

    /**
     * Paints the board, pieces, and player cursors.
     *
     * @param g The Graphics context.
     */
    @Override
    protected void paintComponent(Graphics g) {
        super.paintComponent(g);

        if (boardImage != null) {
            g.drawImage(boardImage, 0, 0, getWidth(), getHeight(), this);
        } else {
            g.setColor(Color.DARK_GRAY);
            g.fillRect(0, 0, getWidth(), getHeight());
        }

        // צייר את הכלים מהנתונים הפשוטים אם יש
        if (simpleBoardData != null) {
            drawSimplePieces(g);
        } else if (board != null) {
            BoardRenderer.draw(g, board, getWidth(), getHeight());
        }

        if (cursor1 != null) cursor1.draw(g, getWidth(), getHeight());
        if (cursor2 != null) cursor2.draw(g, getWidth(), getHeight());
    }

    private void drawSimplePieces(Graphics g) {
        int squareWidth = getWidth() / simpleBoardData.getCols();
        int squareHeight = getHeight() / simpleBoardData.getRows();
        
        for (SimplePieceData piece : simpleBoardData.getPieces()) {
            int x = piece.getCol() * squareWidth;
            int y = piece.getRow() * squareHeight;
            
            // צייר ריבוע צבעוני לכלי (זמנית עד שנטעון תמונות)
            Color pieceColor = piece.getPlayer() == 0 ? Color.WHITE : Color.BLACK;
            g.setColor(pieceColor);
            g.fillOval(x + 10, y + 10, squareWidth - 20, squareHeight - 20);
            
            // הוסף טקסט לסוג הכלי
            g.setColor(piece.getPlayer() == 0 ? Color.BLACK : Color.WHITE);
            g.setFont(new Font("Arial", Font.BOLD, 16));
            FontMetrics fm = g.getFontMetrics();
            String text = piece.getType();
            int textX = x + (squareWidth - fm.stringWidth(text)) / 2;
            int textY = y + (squareHeight + fm.getAscent()) / 2;
            g.drawString(text, textX, textY);
        }
    }
}