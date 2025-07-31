package client;

import java.util.ArrayList;
import java.util.List;

/**
 * Simple data structure for board information from server
 */
public class SimpleBoardData {
    private final int rows;
    private final int cols;
    private final List<SimplePieceData> pieces;
    
    public SimpleBoardData(int rows, int cols) {
        this.rows = rows;
        this.cols = cols;
        this.pieces = new ArrayList<>();
    }
    
    public void addPiece(SimplePieceData piece) {
        pieces.add(piece);
    }
    
    public int getRows() { return rows; }
    public int getCols() { return cols; }
    public List<SimplePieceData> getPieces() { return pieces; }
}