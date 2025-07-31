package server.interfaces;

import java.util.List;

import server.pieces.Position;

/**
 * Interface for player operations.
 */
public interface IPlayer {
    /**
     * Gets the list of pieces owned by the player.
     * 
     * @return List of pieces
     */
    public List<IPiece> getPieces();

    public int getScores();

    public void setScorses(int scores);

    /**
     * Gets the player's ID.
     * 
     * @return The player ID
     */
    public int getId();

    String getName();

    /**
     * Gets the player's cursor.
     * 
     * @return The player cursor
     */
    public IPlayerCursor getCursor();

    /**
     * Gets the pending position for selection.
     * 
     * @return The pending position
     */
    public Position getPendingFrom();

    /**
     * Sets the pending position for selection.
     * 
     * @param pending The pending position
     */
    public void setPendingFrom(Position pending);

    public void setPieces(List<IPiece> pieces);
    /**
     * Returns true if the player has failed (e.g., lost their king).
     * 
     * @return true if failed, false otherwise
     */
    public boolean isFailed();

    /**
     * Marks a piece as captured and updates player status if king is captured.
     * 
     * @param p The piece to mark as captured
     */
    public void markPieceCaptured(IPiece p, IPlayer targetPlayer);

    /**
     * Handles the selection logic for the player, returning a command if an action
     * is performed.
     * 
     * @param board The game board
     * @return ICommand representing the action, or null if no action
     */
    public ICommand handleSelection(IBoard board);

    int getScore();
}
