package org.example;

import board.BoardConfig;
import board.Dimension;
import events.EventPublisher;
import events.GameEvent;
import game.Game;
import interfaces.IGame;
import interfaces.IPlayer;
import listener.MoveListener;
import listener.ScoreListener;
import listener.SoundListener;
import pieces.Position;
import player.Player;
import player.PlayerCursor;
import view.GamePanel;

import javax.swing.*;

import java.awt.*;

public class Main {
    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> {
            JFrame frame = new JFrame("KFChess");
            frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);

            BoardConfig boardConfig = new BoardConfig(new Dimension(8), new Dimension(64 * 8));

            IPlayer p1 = new Player("aaa", new PlayerCursor(new Position(0, 0), Color.RED), boardConfig);
            IPlayer p2 = new Player("bbb", new PlayerCursor(new Position(7, 7), Color.BLUE), boardConfig);

            SoundListener soundListener = new SoundListener();
            EventPublisher publisher = EventPublisher.getInstance();
            publisher.subscribe(GameEvent.GAME_STARTED, soundListener);
            publisher.subscribe(GameEvent.PIECE_JUMPED, soundListener);
            publisher.subscribe(GameEvent.PIECE_MOVED, soundListener);
            publisher.subscribe(GameEvent.PIECE_CAPTURED, soundListener);
            publisher.subscribe(GameEvent.GAME_ENDED, soundListener);
            IGame game = new Game(boardConfig, p1, p2);
            GamePanel gameView = new GamePanel(game,p1.getId(),p2.getId());

            ScoreListener scoreListener1 = new ScoreListener(gameView.getPlayer1Panel());
            ScoreListener scoreListener2 = new ScoreListener(gameView.getPlayer2Panel()); // מעביר את הפאנל
             // מעביר את הפאנל
            EventPublisher.getInstance().subscribe(GameEvent.PIECE_CAPTURED, scoreListener1);
            EventPublisher.getInstance().subscribe(GameEvent.PIECE_CAPTURED, scoreListener2);

// 
            // MoveListener moveListener = new MoveListener();
            MoveListener moveListener = new MoveListener(gameView.getPlayer1Panel(), gameView.getPlayer2Panel());
            EventPublisher.getInstance().subscribe(GameEvent.PIECE_MOVED, moveListener);

           
            // יצירת SoundListener ורישום שלו למאזין

            // Add debug prints
            System.out.println("Debug: Initial game state setup");

            gameView.run();

            frame.setContentPane(gameView); // מכניס את המשחק
            frame.pack();
            frame.setLocationRelativeTo(null);
            frame.setVisible(true);
        });
    }
}
