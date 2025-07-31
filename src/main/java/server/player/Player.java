package server.player;

import server.board.BoardConfig;
import server.game.LoadPieces;
import server.interfaces.*;
import server.pieces.EPieceType;
import server.pieces.Position;
import utils.LogUtils;

import java.util.ArrayList;
import java.util.List;

/**
 * Represents a player in the game, holding pieces and managing actions.
 */
public class Player implements IPlayer{
    private final int id;
    private String name;
    private final IPlayerCursor cursor;
    private Position pending;
    private static int mone = 0;
    private int scores = 0;

    private List<IPiece> pieces;
    private boolean isFailed;

    /**
     * Constructs a Player, initializes pieces and status.
     */
    public Player(String name ,IPlayerCursor pc, BoardConfig bc){
        id = mone++;
        this.cursor = pc;
        pending=null;
        isFailed = false;
        this.name = name;

        pieces = new ArrayList<>();

        for(int i:BoardConfig.rowsOfPlayer.get(id)) {
            for(int j=0; j<8; j++) {
                String pieceCode = LoadPieces.board[i][j];
                if (pieceCode != null && !pieceCode.trim().isEmpty()) {
                    // לוקח את התו הראשון של הקוד (R, N, B, K, Q, P)
                    String pieceType = pieceCode.substring(0, 1);
                    try {
                        IPiece piece = PiecesFactory.createPieceByCode(EPieceType.valueOf(pieceType), new Position(i, j), bc, id);
                        if (piece != null) {
                            this.pieces.add(piece);
                            System.out.println("Created piece: " + pieceType + " at (" + i + "," + j + ") for player " + id);
                        } else {
                            System.err.println("Failed to create piece: " + pieceType + " at (" + i + "," + j + ")");
                        }
                    } catch (Exception e) {
                        System.err.println("Error creating piece " + pieceCode + " at (" + i + "," + j + "): " + e.getMessage());
                    }
                }
            }
        }
        
        System.out.println("Player " + id + " (" + name + ") created with " + pieces.size() + " pieces");
    }

    /**
     * Returns the list of pieces owned by the player.
     */
    @Override
    public List<IPiece> getPieces() {
        return pieces;
    }

    /**
     * Returns the player's ID.
     */
    @Override
    public int getId() {
        return id;
    }

    @Override
    public String getName() {
        return name;
    }

    /**
     * Returns the player's cursor.
     */
    @Override
    public IPlayerCursor getCursor() {
        return cursor;
    }

    /**
     * Gets the pending position for selection.
     */
    @Override
    public Position getPendingFrom() {
        return pending;
    }

    /**
     * Sets the pending position for selection.
     */

    @Override
    public void setPendingFrom(Position pending) {
        this.pending = pending == null? null : pending.copy();
    }

    @Override
    public int getScores() {

        return scores;
    }
    @Override
    public void setScorses(int scores) {
        this.scores += scores;
        
    }   
    
    /**
     * Returns true if the player has failed (e.g., lost their king).
     */
    @Override
    public boolean isFailed(){
        return isFailed;
    }

    /**
     * Marks a piece as captured and updates player status if king is captured.
     * @param p The piece to mark as captured.
     */
    @Override
    public void markPieceCaptured(IPiece p, IPlayer targetPlayer) {
       targetPlayer.setScorses(p.getType().getScore());
        
       
        p.markCaptured();

        if(p.getType() == EPieceType.K)
            isFailed = true;
    }

    /**
     * Handles the selection logic for the player, returning a command if an action is performed.
     * @param board The game board.
     * @return ICommand representing the action, or null if no action.
     */
    @Override
    public ICommand handleSelection(IBoard board){
        Position previous = getPendingFrom();
        Position selected = getCursor().getPosition();

        if (previous == null) {
            if(board.getPlayerOf(board.getPiece(selected)) != id)
                return null;

            if (board.hasPiece(getCursor().getRow(), getCursor().getCol()) && board.getPiece(getCursor().getPosition()).getCurrentStateName().isCanAction())
                setPendingFrom(selected);
            else {
                System.err.println("can not choose piece");
                LogUtils.logDebug("can not choose piece");
            }
        } else {
            setPendingFrom(null);
            if(previous.equals(selected)) {
                // שליחת פקודת jump לשרת
                client.GameClient.getInstance().sendJump(selected.getRow(), selected.getCol());
                return null;
            }
            // שליחת פקודת move לשרת
            client.GameClient.getInstance().sendMove(previous.getRow(), previous.getCol(), selected.getRow(), selected.getCol(), id);
            return null;
        }

        return null;
    }

    @Override
    public int getScore(){
        return 0;
    }

    @Override
    public void setPieces(List<IPiece> pieces) {
        this.pieces = new ArrayList<>();
        for(IPiece p : pieces) {
            this.pieces.add(p);
        }

    }

}