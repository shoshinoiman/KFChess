package client.view;

import server.board.Board;
import client.SimpleBoardData;
import server.events.EventPublisher;
import server.events.GameEvent;
import server.events.IEventListener;
import server.interfaces.IGame;
import server.interfaces.IPlayerCursor;
import utils.LogUtils;

import javax.imageio.ImageIO;
import javax.swing.*;
import javax.swing.border.EmptyBorder;

import java.awt.*;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.io.IOException;

/**
 * Full game panel with board in center, players on sides, and background.
 * This is a VIEW-ONLY component that displays game state.
 */
public class GamePanel extends JPanel implements IEventListener {
    private BoardPanel boardPanel;
    private PlayerInfoPanel player1Panel;
    private PlayerInfoPanel player2Panel;
    private Image backgroundImage;
    private final IGame model;

    public GamePanel(IGame model, int player1Id, int player2Id) {
        this.model = model;
        initializeComponents(player1Id, player2Id);
        setupLayout();
        setupEventListeners();
        LogUtils.logDebug("GamePanel created with model");
    }

    public GamePanel() {
        this.model = null;
        // initializeComponents(0, 1);
        // setupLayout();
        LogUtils.logDebug("Default GamePanel created without model");
    }

    private void initializeComponents(int player1Id, int player2Id) {
        // Load background image
        try {
            backgroundImage = ImageIO.read(getClass().getClassLoader().getResource("background/background.jpg"));
        } catch (IOException | IllegalArgumentException e) {
            System.err.println("Could not load background image: " + e.getMessage());
        }

        // Players info panels
        player1Panel = new PlayerInfoPanel(player1Id);
        player2Panel = new PlayerInfoPanel(player2Id);
        
        if (model != null) {
            player1Panel.setPlayerName(model.getPlayer1().getName());
            player2Panel.setPlayerName(model.getPlayer2().getName());
        }

        // צבע שקוף קליל או לבן עם אלפא
        Color semiTransparent = new Color(255, 255, 255, 180);
        player1Panel.setBackground(semiTransparent);
        player2Panel.setBackground(semiTransparent);

        // BoardPanel setup
        if (model != null) {
            IPlayerCursor c1 = model.getPlayer1().getCursor();
            IPlayerCursor c2 = model.getPlayer2().getCursor();
            boardPanel = new BoardPanel(model.getBoard(), c1, c2);
            boardPanel.setOnPlayer1Action((v) -> model.handleSelection(model.getPlayer1()));
            boardPanel.setOnPlayer2Action((v) -> model.handleSelection(model.getPlayer2()));
        } else {
            boardPanel = new BoardPanel(null, null, null);
        }
        
        boardPanel.setPreferredSize(new Dimension(700, 700));
        boardPanel.setOpaque(false);
    }

    private void setupLayout() {
        setLayout(new BorderLayout(20, 20));
        setBorder(new EmptyBorder(20, 20, 20, 20));
        
        add(player1Panel, BorderLayout.WEST);
        add(player2Panel, BorderLayout.EAST);
        add(boardPanel, BorderLayout.CENTER);
    }

    private void setupEventListeners() {
        // Listen to game events for view updates
        // EventPublisher.getInstance().subscribe("BOARD_UPDATED", this);
        // EventPublisher.getInstance().subscribe(GameEvent.GAME_ENDED, this);
        // EventPublisher.getInstance().subscribe(GameEvent.GAME_STARTED, this);

        // Mouse listener for focus
        boardPanel.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent e) {
                boardPanel.requestFocusInWindow();
            }
        });
        SwingUtilities.invokeLater(() -> boardPanel.requestFocusInWindow());
    }

    @Override
    public void onEvent(GameEvent event) {
        SwingUtilities.invokeLater(() -> {
            switch (event.getType()) {
                case "BOARD_UPDATED":
                    if (event.data instanceof Board) {
                        updateBoard((Board) event.data);
                    }
                    break;
                case GameEvent.GAME_ENDED:
                    if (event.data instanceof server.interfaces.IPlayer) {
                        server.interfaces.IPlayer winner = (server.interfaces.IPlayer) event.data;
                        JOptionPane.showMessageDialog(this, "Game Over. Winner: Player " + winner.getName());
                    }
                    break;
                case GameEvent.GAME_STARTED:
                    LogUtils.logDebug("Game started - view updated");
                    break;
            }
        });
    }

    @Override
    protected void paintComponent(Graphics g) {
        super.paintComponent(g);
        if (backgroundImage != null) {
            g.drawImage(backgroundImage, 0, 0, getWidth(), getHeight(), this);
        }
    }

    public void updateBoard(SimpleBoardData boardData) {
        System.out.println("DEBUG: GamePanel.updateBoard called with " + 
            (boardData != null ? boardData.getPieces().size() + " pieces" : "null data"));
        this.boardPanel.updateFromSimpleData(boardData);
        this.boardPanel.repaint();
        repaint();
        System.out.println("DEBUG: GamePanel repaint() called");
    }

    public void updateBoard(Board board) {
        this.boardPanel.setBord(board);
        this.boardPanel.repaint();
        repaint();
    }

    public PlayerInfoPanel getPlayer1Panel() {
        return player1Panel;
    }

    public PlayerInfoPanel getPlayer2Panel() {
        return player2Panel;
    }
    
    public BoardPanel getBoardPanel() {
        return boardPanel;
    }
    
    public void setBackgroundImage(Image backgroundImage) {
        this.backgroundImage = backgroundImage;
        repaint();
    }
}
