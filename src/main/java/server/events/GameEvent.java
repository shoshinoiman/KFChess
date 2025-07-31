package server.events;

public class GameEvent {
    public final String type;
    public final Object data;

    public GameEvent(String type, Object data) {
        this.type = type;
        this.data = data;
    }
    public String getType() {
        return type;
    }

    public static final String PIECE_MOVED = "pieceMoved";
    public static final String PIECE_CAPTURED = "pieceCaptured";
    public static final String PIECE_JUMPED = "gameJumped";
    public static final String GAME_STARTED = "gameStarted";
    public static final String GAME_ENDED = "gameEnded";

}

