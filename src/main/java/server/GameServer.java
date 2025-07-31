package server;

import server.interfaces.IPiece;
import server.listener.SoundListener;

import org.java_websocket.server.WebSocketServer;
import org.java_websocket.WebSocket;
import org.java_websocket.handshake.ClientHandshake;
import java.net.InetSocketAddress;
import java.util.ArrayList;
import java.util.List;
import server.board.Board;
import server.board.BoardConfig;
import server.game.Game;
import server.player.Player;
import server.command.MoveCommand;
import server.events.EventPublisher;
import server.events.GameEvent;
import server.command.JumpCommand;
import server.pieces.Position;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;

public class GameServer extends WebSocketServer {
    private final List<WebSocket> clients = new ArrayList<>();
    private Game game;
    private ObjectMapper mapper = new ObjectMapper();

    public GameServer(int port) {
        super(new InetSocketAddress(port));
    }

    @Override
    public void onStart() {
        System.out.println("WebSocket server started!");
        // אפשר להוסיף כאן לוגיקה נוספת אם צריך
    }
    @Override
    public void onOpen(WebSocket conn, ClientHandshake handshake) {
        // System.out.println("hrtjyuk,i,ujyhtgrf");
        try {
            System.out.println("DEBUG: onOpen called in GameServer");
            clients.add(conn);
            System.out.println("Client connected: " + conn.getRemoteSocketAddress() + " (Total clients: " + clients.size() + ")");
            
            // שליחת הודעת אישור התחברות
            conn.send("{\"status\":\"connected\",\"message\":\"Connected to server\"}");
            System.out.println("DEBUG: Sent connection confirmation to client");
            
        } catch (Exception e) {
            System.err.println("Exception in onOpen: " + e.getMessage());
            e.printStackTrace();
        }
        
        if (clients.size() == 2) {
            System.out.println("DEBUG: Starting game with 2 players");
            try {
                System.out.println("DEBUG: Creating players...");
                // אתחול שחקנים ולוח
                Player p1 = new Player("Player1", new server.player.PlayerCursor(new Position(0, 0), java.awt.Color.RED), new server.board.BoardConfig(new server.board.Dimension(8), new server.board.Dimension(64 * 8)));
                System.out.println("DEBUG: Player 1 created successfully");
                
                Player p2 = new Player("Player2", new server.player.PlayerCursor(new Position(7, 7), java.awt.Color.BLUE), new server.board.BoardConfig(new server.board.Dimension(8), new server.board.Dimension(64 * 8)));
                System.out.println("DEBUG: Player 2 created successfully");
                
                BoardConfig board = new server.board.BoardConfig(new server.board.Dimension(8), new server.board.Dimension(64 * 8));
                System.out.println("DEBUG: Board config created");
                
                game = new Game(board,p1, p2);
                System.out.println("DEBUG: Game created successfully");
                
                // התחלת לולאת המשחק
                game.run();
                System.out.println("DEBUG: Game loop started");
                
                // SoundListener soundListener = new SoundListener();
                // EventPublisher publisher = EventPublisher.getInstance();
                // publisher.subscribe(GameEvent.GAME_STARTED, soundListener);
                // publisher.subscribe(GameEvent.PIECE_JUMPED, soundListener);
                // publisher.subscribe(GameEvent.PIECE_MOVED, soundListener);
                // publisher.subscribe(GameEvent.PIECE_CAPTURED, soundListener);
                // publisher.subscribe(GameEvent.GAME_ENDED, soundListener);

                System.out.println("DEBUG: About to broadcast board...");
                broadcastBoard();
                System.out.println("DEBUG: Board broadcasted successfully");
            } catch (Exception e) {
                System.err.println("ERROR: Error during game initialization: " + e.getMessage());
                e.printStackTrace();
            }
        } else {
            System.out.println("DEBUG: Waiting for second player... (Current: " + clients.size() + "/2)");
            conn.send("{\"status\":\"waiting\",\"message\":\"Waiting for second player...\"}");
        }
    }

    @Override
    public void onMessage(WebSocket conn, String message) {
        if (game == null) {
            conn.send("{\"status\":\"error\",\"message\":\"Game not started yet\"}");
            return;
        }
        try {
            JsonNode msg = mapper.readTree(message);
            String action = msg.get("action").asText();
            if ("move".equals(action)) {
                int fromRow = msg.get("from").get(0).asInt();
                int fromCol = msg.get("from").get(1).asInt();
                int toRow = msg.get("to").get(0).asInt();
                int toCol = msg.get("to").get(1).asInt();
                // int playerId = msg.get("playerId").asInt();
                Position from = new Position(fromRow, fromCol);
                Position to = new Position(toRow, toCol);
                MoveCommand cmd = new MoveCommand( from, to, game.getBoard(),game.getBoard().getPiece(from));
                cmd.execute();
            } else if ("jump".equals(action)) {
                int row = msg.get("pos").get(0).asInt();
                int col = msg.get("pos").get(1).asInt();
                Position pos = new Position(row, col);
                JumpCommand cmd = new JumpCommand(game.getBoard().getPiece(pos), game.getBoard());
                cmd.execute();
            }
            broadcastBoard();
        } catch (Exception e) {
            conn.send("{\"status\":\"error\",\"message\":\"Invalid command\"}");
        }
    }

    private void broadcastBoard() {
        try {
            ObjectNode boardMsg = mapper.createObjectNode();
            boardMsg.put("status", "update");
            
            // יצירת נתוני לוח פשוטים
            ObjectNode boardData = mapper.createObjectNode();
            boardData.put("rows", game.getBoard().getROWS());
            boardData.put("cols", game.getBoard().getCOLS());
            
            // יצירת מערך הכלים
            com.fasterxml.jackson.databind.node.ArrayNode piecesArray = mapper.createArrayNode();
            
            for (int row = 0; row < game.getBoard().getROWS(); row++) {
                for (int col = 0; col < game.getBoard().getCOLS(); col++) {
                    server.interfaces.IPiece piece = game.getBoard().getPiece(row, col);
                    if (piece != null && !piece.isCaptured()) {
                        ObjectNode pieceNode = mapper.createObjectNode();
                        pieceNode.put("row", piece.getRow());
                        pieceNode.put("col", piece.getCol());
                        pieceNode.put("type", piece.getType().getVal());
                        pieceNode.put("player", piece.getPlayer());
                        piecesArray.add(pieceNode);
                    }
                }
            }
            
            boardData.set("pieces", piecesArray);
            boardMsg.set("board", boardData);
            
            String boardJson = boardMsg.toString();
            System.out.println("Broadcasting board with " + piecesArray.size() + " pieces"); // debug
            
            for (WebSocket client : clients) {
                client.send(boardJson);
            }
        } catch (Exception e) {
            System.err.println("Error broadcasting board: " + e.getMessage());
            e.printStackTrace();
        }
    }

    @Override
    public void onClose(WebSocket conn, int code, String reason, boolean remote) {
        System.out.println("DEBUG: onClose called - Code: " + code + ", Reason: " + reason + ", Remote: " + remote);
        clients.remove(conn);
        System.out.println("DEBUG: Client disconnected: " + conn.getRemoteSocketAddress() + " (Total clients: " + clients.size() + ")");
        
        // בדיקה אם יש שחקן נוסף מחובר
        if (clients.size() == 1) {
            System.out.println("DEBUG: Notifying remaining player about disconnection...");
            WebSocket remainingClient = clients.get(0);
            remainingClient.send("{\"status\":\"info\",\"message\":\"The other player has disconnected. Waiting for a new opponent...\"}");
        } else if (clients.size() == 0) {
            System.out.println("DEBUG: All clients disconnected, resetting game");
        }
        
        // איפוס המשחק כשכל הלקוחות מתנתקים
        if (clients.size() == 0) {
            game = null;
        }
    }

    @Override
    public void onError(WebSocket conn, Exception ex) {
        System.err.println("DEBUG: WebSocket error occurred:");
        ex.printStackTrace();
    }
}
