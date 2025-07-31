package server.listener;

import client.view.PlayerInfoPanel;
import server.events.GameEvent;
import server.events.IEventListener;
import server.interfaces.IPiece;

public class ScoreListener implements IEventListener {
   
    private final PlayerInfoPanel playerInfoPanel;
    private int score = 0;

    public ScoreListener(PlayerInfoPanel panel) {
        this.playerInfoPanel = panel;
    }

    @Override
    public void onEvent(GameEvent event) {
        System.out.println("pojhgfd");
        if (event != null && event.data != null && GameEvent.PIECE_CAPTURED.equals(event.getType())) {
            System.out.println("rrrrrrrrrrrrrrrrrrr" );
            System.out.println(event.data.getClass().getSimpleName());
            if (event.data instanceof IPiece) {
                
                // Assuming the data contains the type of piece captured
                IPiece piece = (IPiece) event.data;
                if(piece.getPlayer() != playerInfoPanel.getPlayerId()) {
                    score += piece.getType().getScore();
                }
            }
            playerInfoPanel.setScore(score);
        }
    }

}
