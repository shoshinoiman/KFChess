package board;

import interfaces.*;
import pieces.EPieceType;
import pieces.Piece;
import pieces.Position;
import utils.LogUtils;

import java.util.ArrayList;
import java.util.List;

import events.EventPublisher;
import events.GameEvent;

/**
 * Represents the game board and manages piece placement and movement.
 */
public class Board implements IBoard {
    /** 2D array representing the board grid with pieces. */
    private final IPiece[][] boardGrid;
    /** Array of players in the game. */
    public final IPlayer[] players;
    /** Board configuration object. */
    public final BoardConfig boardConfig;

    /**
     * Constructs the board with the given configuration and players.
     * Initializes the board grid with the pieces from each player.
     *
     * @param bc      Board configuration
     * @param players Array of players
     */
    public Board(BoardConfig bc, IPlayer[] players) {
        boardConfig = bc;
        this.boardGrid = new IPiece[bc.numRowsCols.getX()][bc.numRowsCols.getY()];
        this.players = players;

        for (IPlayer p : players)
            for (IPiece piece : p.getPieces()) {
                String[] pos = piece.getId().split(",");
                boardGrid[Integer.parseInt(pos[0])][Integer.parseInt(pos[1])] = piece;
            }
    }

    /**
     * Places a piece on the board at its logical position.
     * 
     * @param piece The piece to place
     */
    @Override
    public void placePiece(IPiece piece) {
        int row = piece.getRow();
        int col = piece.getCol();
        if (isInBounds(row, col)) {
            boardGrid[row][col] = piece;
        } else {
            throw new IllegalArgumentException("Invalid position row=" + row + ", col=" + col);
        }
    }

    /**
     * Checks if there is a piece at the specified row and column.
     */
    @Override
    public boolean hasPiece(int row, int col) {
        return isInBounds(row, col) && boardGrid[row][col] != null;
    }

    /**
     * Gets the piece at the specified row and column.
     */
    @Override
    public IPiece getPiece(int row, int col) {
        if (!isInBounds(row, col))
            return null;
        return boardGrid[row][col];
    }

    /**
     * Gets the piece at the specified position.
     */
    @Override
    public IPiece getPiece(Position pos) {
        return getPiece(pos.getRow(), pos.getCol());
    }

    /**
     * Returns the player index for a given row.
     */
    @Override
    public int getPlayerOf(int row) {
        return BoardConfig.getPlayerOf(row);
    }

    /**
     * Returns the player index for a given position.
     */
    @Override
    public int getPlayerOf(Position pos) {
        return getPlayerOf(pos.getRow());
    }

    /**
     * Returns the player index for a given piece.
     */
    @Override
    public int getPlayerOf(IPiece piece) {
        return getPlayerOf(Integer.parseInt(piece.getId().split(",")[0]));
    }

    /**
     * Moves a piece from one position to another.
     */
    @Override
    public void move(Position from, Position to) {
        if (!isInBounds(from) || !isInBounds(to))
            return;

        IPiece piece = boardGrid[from.getRow()][from.getCol()];
        if (piece != null) {
            piece.move(to);
        }
    }

    /**
     * Updates all pieces and handles captures and board state.
     * This method resets previous positions, updates piece states,
     * and handles captures before and after movement.
     */
    public void updateAll() {

        // Step 1 - Reset previous positions
        for (int row = 0; row < boardConfig.numRowsCols.getX(); row++) {
            for (int col = 0; col < boardConfig.numRowsCols.getY(); col++) {
                IPiece piece = boardGrid[row][col];
                if (piece != null) {
                    int newRow = piece.getRow();
                    int newCol = piece.getCol();
                    if (newRow != row || newCol != col) {
                        boardGrid[row][col] = null;
                    }

                }
            }
        }

        // Step 2 - Update state and handle captures before movement
        for (IPlayer p : players) {
            for (IPiece piece : p.getPieces()) {
                if (piece.isCaptured())
                    continue;

                if (piece.getCurrentState().isActionFinished()) {
                    int targetRow = piece.getCurrentState().getTargetRow();
                    int targetCol = piece.getCurrentState().getTargetCol();

                    IPiece target = boardGrid[targetRow][targetCol];
                    if (target != null && target != piece && !target.isCaptured() && target.canMoveOver()) {
                        if (target.getCurrentStateName() == EState.JUMP) {
                            // EventPublisher.getInstance().publish(GameEvent.PIECE_CAPTURED,
                            // new GameEvent(GameEvent.PIECE_CAPTURED, piece));
                            players[piece.getPlayer()].markPieceCaptured(piece, players[target.getPlayer()]);
                            System.out.println("Captured before move: " + piece.getId());
                            LogUtils.logDebug("Captured before move: " + piece.getId());
                            piece.update();
                            // פרסום אירוע אכילה

                        } else {
                            // EventPublisher.getInstance().publish(GameEvent.PIECE_CAPTURED,
                            // new GameEvent(GameEvent.PIECE_CAPTURED, target));
                            players[target.getPlayer()].markPieceCaptured(target, players[piece.getPlayer()]);
                            System.out.println("Captured before move: " + target.getId());
                            LogUtils.logDebug("Captured before move: " + target.getId());

                            // פרסום אירוע אכילה

                        }
                    }
                }

                piece.update();
            }
        }

        // Step 3 - Handle captures after landing and update board positions
        for (IPlayer p : players) {
            List<IPiece>  pieces = new ArrayList<>();
            for (IPiece piece : p.getPieces()) {
                pieces.add(piece);
            }
            for (IPiece piece : p.getPieces()) {

                if (piece.isCaptured())
                    continue;

                int row = piece.getRow();
                int col = piece.getCol();

                IPiece existing = boardGrid[row][col];
                if (existing != null && existing != piece && !existing.isCaptured()) {
                    System.out.println(existing.getCurrentStateName());
                    LogUtils.logDebug("State: " + existing.getCurrentStateName());
                    if (existing.getCurrentStateName() != EState.JUMP) {
                        players[existing.getPlayer()].markPieceCaptured(existing, players[piece.getPlayer()]);
                        System.out.println("Captured on landing: " + existing.getId());
                        LogUtils.logDebug("Captured on landing: " + existing.getId());
                    } else {
                        players[piece.getPlayer()].markPieceCaptured(piece, players[existing.getPlayer()]);
                        System.out.println("No capture: piece not jumping on landing");
                        LogUtils.logDebug("No capture: piece not jumping on landing");
                    }

                }
                // להפוך את החייל למלכה
                if (piece.getType() == EPieceType.P) {
                    if (row == 0 || row == boardConfig.numRowsCols.getX() - 1) {
                        IPiece queenPiece = PiecesFactory.createPieceByCode(
                            EPieceType.Q,
                            // piece.getPosition(),
                            new Position(row == 7 ? 0 : 7, col),
                            boardConfig,
                            piece.getPlayer() // מעביר את השחקן של החייל
                        );
                        queenPiece.setPosition(piece.getPosition());
                        queenPiece.setState(piece.getCurrentStateName());
                        pieces.remove(piece);
                        pieces.add(queenPiece);
                        piece = queenPiece;
                    }
                }
                
                boardGrid[row][col] = piece;
            }
            p.setPieces(pieces);
        }
    }

    /**
     * Checks if the specified row and column are within board bounds.
     */
    @Override
    public boolean isInBounds(int r, int c) {
        return boardConfig.isInBounds(r, c);
    }

    /**
     * Checks if the specified position is within board bounds.
     */
    public boolean isInBounds(Position p) {
        return isInBounds(p.getRow(), p.getCol());
    }

    /**
     * Checks if a move from one position to another is legal.
     */
    @Override
    public boolean isMoveLegal(Position from, Position to) {
        IPiece fromPiece = getPiece(from);
        if (fromPiece == null)
            return false;

        // Check resting states first
        if (!fromPiece.getCurrentStateName().isCanAction())
            return false;

        // Check if the move is in the legal move list
        List<Moves.Move> moves = fromPiece.getMoves().getMoves();

        int player = BoardConfig.getPlayerOf(Integer.parseInt(fromPiece.getId().split(",")[0]));
        int dx = (to.getRow() - from.getRow()) * (player == 0 ? 1 : -1);
        int dy = to.getCol() - from.getCol();

        boolean isLegal = moves.stream().anyMatch(m -> m.getDx() == dx && m.getDy() == dy);

        if (!isLegal)
            return false;

        // Check path clearance (except knights)
        if (!fromPiece.getType().isCanSkip() && !isPathClear(from, to)) {
            isPathClear(from, to);
            return false;
        }

        // Check if capturing own piece
        IPiece toPiece = getPiece(to);
        return toPiece == null || fromPiece.getPlayer() != toPiece.getPlayer();
    }

    /**
     * Checks if the path between two positions is clear for movement.
     */
    @Override
    public boolean isPathClear(Position from, Position to) {
        int dRow = Integer.signum(to.dx(from));
        int dCol = Integer.signum(to.dy(from));

        Position current = from.add(dRow, dCol);

        while (!current.equals(to)) {
            if (getPiece(current) != null && !getPiece(current).canMoveOver())
                return false;
            current = current.add(dRow, dCol);
        }

        return true;
    }

    /**
     * Checks if a jump action is legal for the given piece.
     */
    @Override
    public boolean isJumpLegal(IPiece p) {
        return p.getCurrentStateName().isCanAction();
    }

    /**
     * Performs a jump action for the given piece.
     */
    @Override
    public void jump(IPiece p) {
        if (p == null)
            return;

        p.jump();
    }

    /**
     * Returns the array of players.
     */
    @Override
    public IPlayer[] getPlayers() {
        return players;
    }

    /**
     * Returns the number of columns on the board.
     */
    @Override
    public int getCOLS() {
        return boardConfig.numRowsCols.getY();
    }

    /**
     * Returns the number of rows on the board.
     */
    @Override
    public int getROWS() {
        return boardConfig.numRowsCols.getX();
    }

    /**
     * Returns the board configuration.
     */
    @Override
    public BoardConfig getBoardConfig() {
        return boardConfig;
    }
}
