package server.listener;

import java.io.FileWriter;
import java.io.IOException;

import client.view.PlayerInfoPanel;
import server.command.JumpCommand;
import server.command.MoveCommand;
import server.events.GameEvent;
import server.events.IEventListener;
import server.interfaces.IPiece;
import server.pieces.Piece;

public class MoveListener implements IEventListener{
    private final PlayerInfoPanel player1Panel;
    private final PlayerInfoPanel player2Panel;

    public MoveListener(PlayerInfoPanel p1, PlayerInfoPanel p2) {
        this.player1Panel = p1;
        this.player2Panel = p2;
    }

    @Override
    public void onEvent(GameEvent event) {
        if (event.data instanceof MoveCommand) {
            MoveCommand moveCommand = (MoveCommand) event.data;
            String moveDetails = String.format("Move from %s to %s", 
                moveCommand.getFrom(), moveCommand.getTo());
            int playerId = moveCommand.getPiece().getPlayer();
            if (playerId == player1Panel.getPlayerId()) {
                player1Panel.addMove(moveDetails);
            } else if (playerId == player2Panel.getPlayerId()) {
                player2Panel.addMove(moveDetails);
            }
            AsyncLogger.log(moveDetails);
        }
        if(event.data instanceof IPiece) {
            IPiece piece = (IPiece) event.data;
            String jumpDetails ="jump with piece " + piece.getId() + " at position " + piece.getCurrentPixelPosition();
            AsyncLogger.log(jumpDetails);
        }
    }
}




// public class MoveListener implements IEventListener{
//     @Override
//     public void onEvent(GameEvent event) {
//         if (event.data instanceof MoveCommand) {
//             MoveCommand moveCommand = (MoveCommand) event.data;
//             String moveDetails = String.format("Move from %s to %s", 
//                 moveCommand.getFrom(), moveCommand.getTo());
//             AsyncLogger.log(moveDetails);
//         }
//         if(event.data instanceof IPiece) {
//             IPiece piece = (IPiece) event.data;
//             String jumpDetails ="jump with piece " + piece.getId() + " at position " + piece.getCurrentPixelPosition();
//             AsyncLogger.log(jumpDetails);
//         }
//     }
// }