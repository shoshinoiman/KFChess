package client;

import org.java_websocket.client.WebSocketClient;
import org.java_websocket.handshake.ServerHandshake;
import java.net.URI;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.ObjectNode;

import client.view.GamePanel;
// import client.model.Board; // ייבוא מחלקת ה-Board
// import server.board.Board;
import server.board.Board;

public class GameClient extends WebSocketClient {
    private GamePanel gamePanel;
    private ObjectMapper mapper = new ObjectMapper();
    private static GameClient instance;

    public static GameClient getInstance() {
        return instance;
    }

    public static void init(URI serverUri) {
        if (instance == null) {
            instance = new GameClient(serverUri);
        }
    }

    public GameClient(URI serverUri) {
        super(serverUri);
        gamePanel = new GamePanel();
        instance = this;
    }

    public void setGamePanel(GamePanel panel) {
        this.gamePanel = panel;
    }

    @Override
    public void onOpen(ServerHandshake handshakedata) {
        System.out.println("Connected to server");
    }

    @Override
    public void onMessage(String message) {
        System.out.println("onMessage called in GameClient");
        try {
            System.out.println("Received message: " + message); // הוספת debug
            JsonNode msg = mapper.readTree(message);
            
            // טיפול בהודעות סטטוס
            if (msg.has("status")) {
                String status = msg.get("status").asText();
                System.out.println("Server status: " + status);
                
                if ("waiting".equals(status)) {
                    System.out.println("Waiting for another player...");
                    return;
                } else if ("connected".equals(status)) {
                    System.out.println("Successfully connected to server");
                    return;
                } else if ("error".equals(status)) {
                    System.err.println("Server error: " + msg.get("message").asText());
                    return;
                }
            }
            
            if (msg.has("board") && gamePanel != null) {
                // טיפול בפורמט הפשוט החדש של הלוח
                JsonNode boardData = msg.get("board");
                int rows = boardData.get("rows").asInt();
                int cols = boardData.get("cols").asInt();
                
                // יצירת לוח פשוט לתצוגה
                SimpleBoardData simpleBoardData = new SimpleBoardData(rows, cols);
                JsonNode pieces = boardData.get("pieces");
                
                for (JsonNode piece : pieces) {
                    int row = piece.get("row").asInt();
                    int col = piece.get("col").asInt();
                    String type = piece.get("type").asText();
                    int player = piece.get("player").asInt();
                    
                    SimplePieceData pieceData = new SimplePieceData(row, col, type, player);
                    simpleBoardData.addPiece(pieceData);
                }
                
                System.out.println("Board updated, pieces count: " + pieces.size()); // debug
                gamePanel.updateBoard(simpleBoardData); // עדכון התצוגה
            }
            // אפשר לטפל גם בהודעות סטטוס (waiting/start/error)
        } catch (Exception e) {
            System.err.println("Error parsing message: " + e.getMessage());
            e.printStackTrace(); // הוספת stack trace מלא
        }
    }

    @Override
    public void onClose(int code, String reason, boolean remote) {
        System.out.println("Disconnected from server. Code: " + code + ", Reason: " + reason + ", Remote: " + remote);
    }

    @Override
    public void onError(Exception ex) {
        System.err.println("WebSocket error occurred:");
        ex.printStackTrace();
    }

    // שליחת פקודת move לשרת
    public void sendMove(int fromRow, int fromCol, int toRow, int toCol, int playerId) {
        System.out.println("send moveeeeeeeee");
        try {
            // ObjectMapper mapper = new ObjectMapper();
            // JsonNode moveMsg = mapper.createObjectNode()
            // .put("action", "move")
            // .putArray("from").add(fromRow).add(fromCol)
            // .putArray("to").add(toRow).add(toCol)
            // .put("playerId", playerId);
            // send(moveMsg.toString());
            ObjectMapper mapper = new ObjectMapper();
            ObjectNode moveMsg = mapper.createObjectNode();
            ArrayNode fromArr = mapper.createArrayNode();
            fromArr.add(fromRow);
            fromArr.add(fromCol);
            moveMsg.set("from", fromArr);

            ArrayNode toArr = mapper.createArrayNode();
            toArr.add(toRow);
            toArr.add(toCol);
            moveMsg.set("to", toArr);

            moveMsg.put("action", "move");
            moveMsg.put("playerId", playerId);

            send(moveMsg.toString());
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    // שליחת פקודת jump לשרת
    public void sendJump(int row, int col) {
        try {
            ObjectMapper mapper = new ObjectMapper();
            JsonNode jumpMsg = mapper.createObjectNode()
                    .put("action", "jump")
                    .putArray("pos").add(row).add(col);
            send(jumpMsg.toString());
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
