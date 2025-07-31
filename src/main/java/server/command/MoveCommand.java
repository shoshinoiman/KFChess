package server.command;
import server.events.EventPublisher;
import server.events.GameEvent;
import server.interfaces.*;
import server.pieces.Position;
import utils.LogUtils;

/**
 * Command for moving a piece from one position to another on the board.
 */
public class MoveCommand implements ICommand {
    /** The xbfcvzgzvstarting position of the move. */
    private final Position from;
    /** The target position of the move. */
    private final Position to;
    /** The board on which the move is performed. */
    private final IBoard board;
    private final IPiece piece;

    /**
     * Constructs a MoveCommand for moving a piece from one position to another.
     *
     * @param from The starting position
     * @param to The target position
     * @param board The board instance
     */
    public MoveCommand(Position from, Position to, IBoard board, IPiece piece) {
        this.piece = piece;
        this.from = from;
        this.to = to;
        this.board = board;
    }

    /**
     * Executes the move command, moving the piece if the move is legal.
     * Logs the action and handles illegal moves.
     */
    @Override
    public void execute() {
        if (!board.isMoveLegal(from, to)) {
            System.err.println("Illegal move from " + from + " to " + to);
            LogUtils.logDebug("Illegal move from " + from + " to " + to);
            return;
        }
        System.out.println("Moving from " + from + " to " + to);
        LogUtils.logDebug("Moving from " + from + " to " + to);
        board.move(from, to);
        // EventPublisher.getInstance().publish(GameEvent.PIECE_MOVED, 
        //     new GameEvent(GameEvent.PIECE_MOVED, this));
    }
    public Position getFrom() {
        return from;
    }
    public Position getTo() {
        return to;
    }
    public IBoard getBoard() {
        return board;
    }
    public IPiece getPiece() {
        return piece;
    }
}
