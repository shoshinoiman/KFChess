package client;

/**
 * Simple data structure for piece information from server
 */
public class SimplePieceData {
    private final int row;
    private final int col;
    private final String type;
    private final int player;
    
    public SimplePieceData(int row, int col, String type, int player) {
        this.row = row;
        this.col = col;
        this.type = type;
        this.player = player;
    }
    
    public int getRow() { return row; }
    public int getCol() { return col; }
    public String getType() { return type; }
    public int getPlayer() { return player; }
}