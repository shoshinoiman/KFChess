package server.pieces;

import server.board.BoardConfig;
import server.events.EventPublisher;
import server.events.GameEvent;
import server.interfaces.*;
import utils.LogUtils;

import java.awt.geom.Point2D;
import java.io.IOException;
import java.util.Map;

public class Piece implements IPiece {
    private final String id;
    private final EPieceType type;
    private Map<EState, IState> states;
    private EState currentStateName;
    private IState currentState;

    private final Moves moves;

    private Position pos;

    private boolean wasCaptured = false;
    private int player; // שדה חדש לשחקן

    public Piece(EPieceType type, Map<EState, IState> states, EState initialState, Position pos, int player) throws IOException {
        id = pos.getRow() + "," + pos.getCol();
        this.states = states;
        this.currentStateName = initialState;
        this.currentState = states.get(initialState);
        this.pos = pos;
        this.type = type;
        this.player = player;

        moves = new Moves(type);
    }

    @Override
    public int getPlayer() {
        return player;
    }
    @Override
    public void setPlayer(int id){
        this.player = id;
    }


    @Override
    public String getId() {
        return id;
    }

    @Override
    public EPieceType getType() {
        return type;
    }

    @Override
    public void setState(EState newStateName) {
        if (states.containsKey(newStateName) && !newStateName.equals(currentStateName)) {
            currentStateName = newStateName;
            currentState = states.get(newStateName);

            // Update state.startPos before reset
            currentState.reset(newStateName, pos, pos);
        } else if (!states.containsKey(newStateName)) {
            System.err.println("State '" + newStateName + "' not found!");
            LogUtils.logDebug("State '" + newStateName + "' not found!");
        }
    }

    @Override
    public IState getCurrentState() {
        return currentState;
    }

    @Override
    public void update() {
        currentState.update();

        if (currentState.isActionFinished()) {
            // עדכון המיקום הלוגי רק אחרי שהפעולה הסתיימה
            pos = new Position(currentState.getTargetRow(), currentState.getTargetCol());

            EState nextState = currentState.getPhysics().getNextStateWhenFinished();

            setState(nextState);
            return; // עצור כדי לא לעבור שוב סטייט בפעם אחת
        }

        // מעבר אוטומטי אם האנימציה הסתיימה
        if (currentState.getGraphics() != null && currentState.getGraphics().isAnimationFinished()) {
            EState nextState = currentState.getPhysics().getNextStateWhenFinished();
            setState(nextState);
        }
    }

    public Position getPosition() {
        return pos;
    }

    @Override
    public void move(Position to) {
        if (states.containsKey(EState.MOVE)) {
            currentStateName = EState.MOVE;
            currentState = states.get(EState.MOVE);
            currentState.reset(EState.MOVE, pos, to);
        } else {
            System.err.println("Missing 'move' state!");
            LogUtils.logDebug("Missing 'move' state!");
        }
    }

    @Override
    public void jump() {
        if (states.containsKey(EState.JUMP)) {
            currentStateName = EState.JUMP;
            currentState = states.get(EState.JUMP);
            currentState.reset(EState.JUMP, pos, pos);
        } else {
            System.err.println("Missing 'jump' state!");
            LogUtils.logDebug("Missing 'jump' state!");
        }
    }

    @Override
    public boolean isCaptured() {
        return wasCaptured;
    }

    public void markCaptured() {
        EventPublisher.getInstance().publish(GameEvent.PIECE_CAPTURED,
                new GameEvent(GameEvent.PIECE_CAPTURED, this));
        this.wasCaptured = true;
    }

    @Override
    public int getRow() {
        return pos.getRow();
    }

    @Override
    public int getCol() {
        return pos.getCol();
    }

    @Override
    public EState getCurrentStateName() {
        return currentStateName;
    }

    @Override
    public Point2D.Double getCurrentPixelPosition() {
        return currentState.getCurrentPosition();
    }

    @Override
    public Moves getMoves() {
        return moves;
    }

    @Override
    public Map<EState, IState> getStates() {
        return states;
    }

    @Override
    public boolean canMoveOver() {
        return currentStateName.isCanMoveOver();
    }

    @Override
    public void setPosition(Position position) {
        this.pos = position.copy();
    }
}
