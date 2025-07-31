package server.interfaces;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;

import server.board.BoardConfig;
import server.graphics.GraphicsLoader;
import server.pieces.EPieceType;
import server.pieces.Piece;
import server.pieces.Position;
import server.state.GraphicsData;
import server.state.PhysicsData;
import server.state.State;
import utils.LogUtils;

import java.awt.image.BufferedImage;
import java.io.File;
import java.io.InputStream;
import java.net.URL;
import java.util.*;

/**
 * Factory for creating pieces by code and position.
 */
public class PiecesFactory {

    private static double TILE_SIZE;
    private static final ObjectMapper mapper = new ObjectMapper();

    /**
     * Creates a piece by its code, position, and board configuration.
     * Loads all states and graphics for the piece from resources.
     * @param code Piece code
     * @param pos Position on the board
     * @param config Board configuration
     * @return Piece instance or null if failed
     */
    public static Piece createPieceByCode(EPieceType code, Position pos, BoardConfig config) {
        TILE_SIZE = config.tileSize;

        // ...continue as previously built, using tileSize
        Map<EState, IState> states = new HashMap<>();
        String basePath = "/pieces/" + code.getVal() + "/states/";

        try {
            // Step 1 – Find all existing states in the states directory
            URL dirURL = PiecesFactory.class.getResource(basePath);
            if (dirURL == null || !dirURL.getProtocol().equals("file")) {
                System.err.println("Cannot load states from: " + basePath);
                LogUtils.logDebug("Cannot load states from: " + basePath);
                return null;
            }

            File statesDir = new File(dirURL.toURI());
            File[] subdirs = statesDir.listFiles(File::isDirectory);
            if (subdirs == null) return null;

            // Step 2 – Load each state
            for (File stateFolder : subdirs) {
                EState stateName = EState.getValueOf(stateFolder.getName());
                String configPath = basePath + stateName + "/config.json";
                InputStream is = PiecesFactory.class.getResourceAsStream(configPath);
                if (is == null) {
                    System.err.println("Missing config for state: " + stateName);
                    LogUtils.logDebug("Missing config for state: " + stateName);
                    continue;
                }

                JsonNode root = mapper.readTree(is);
                JsonNode physicsNode = root.path("physics");
                double speed = physicsNode.path("speed_m_per_sec").asDouble(0.0);
                EState nextState = EState.getValueOf(physicsNode.path("next_state_when_finished").asText(stateName.toString()));

                IPhysicsData physics = new PhysicsData(speed, nextState);

                JsonNode graphicsNode = root.path("graphics");
                int fps = graphicsNode.path("frames_per_sec").asInt(1);
                boolean isLoop = graphicsNode.path("is_loop").asBoolean(true);

                BufferedImage[] sprites = GraphicsLoader.loadAllSprites(code, BoardConfig.getPlayerOf(pos.getRow()), stateName);
                if (sprites.length == 0) {
                    System.err.println("No sprites for state: " + stateName);
                    LogUtils.logDebug("No sprites for state: " + stateName);
                    continue;
                }

                IGraphicsData graphics = new GraphicsData(sprites, fps, isLoop);
                IState state = new State(stateName, pos, pos, TILE_SIZE, physics, graphics);
                states.put(stateName, state);
            }

            if (states.isEmpty()) {
                LogUtils.logDebug("No states loaded for piece: " + code.getVal());
                return null;
            }

            // Step 3 – Create the Piece with the first state as default
            EState initialState = EState.IDLE;
            return new Piece(code, states, initialState, pos, BoardConfig.getPlayerOf(pos.getRow()));

        } catch (Exception e) {
            e.printStackTrace();
            LogUtils.logDebug("Exception in createPieceByCode: " + e.getMessage());
            return null;
        }
    }

    /**
     * Creates a piece by its code, position, board configuration, and player.
     * Loads all states and graphics for the piece from resources.
     * @param code Piece code
     * @param pos Position on the board
     * @param config Board configuration
     * @param player Player index
     * @return Piece instance or null if failed
     */
    public static Piece createPieceByCode(EPieceType code, Position pos, BoardConfig config, int player) {
        TILE_SIZE = config.tileSize;
        Map<EState, IState> states = new HashMap<>();
        String basePath = "/pieces/" + code.getVal() + "/states/";

        // רשימה קבועה של states במקום לחפש תיקיות
        EState[] allStates = {EState.IDLE, EState.MOVE, EState.JUMP, EState.SHORT_REST, EState.LONG_REST};

        try {
            System.out.println("Creating piece " + code.getVal() + " for player " + player + " at (" + pos.getRow() + "," + pos.getCol() + ")");

            for (EState stateName : allStates) {
                String configPath = basePath + stateName + "/config.json";
                InputStream is = PiecesFactory.class.getResourceAsStream(configPath);
                if (is == null) {
                    System.out.println("Missing config for state: " + stateName + " (path: " + configPath + ")");
                    continue;
                }

                try {
                    JsonNode root = mapper.readTree(is);
                    JsonNode physicsNode = root.path("physics");
                    double speed = physicsNode.path("speed_m_per_sec").asDouble(0.0);
                    EState nextState = EState.getValueOf(physicsNode.path("next_state_when_finished").asText(stateName.toString()));
                    IPhysicsData physics = new PhysicsData(speed, nextState);

                    JsonNode graphicsNode = root.path("graphics");
                    int fps = graphicsNode.path("frames_per_sec").asInt(1);
                    boolean isLoop = graphicsNode.path("is_loop").asBoolean(true);

                    BufferedImage[] sprites = GraphicsLoader.loadAllSprites(code, player, stateName);
                    if (sprites.length == 0) {
                        System.out.println("No sprites for state: " + stateName + ", using empty sprites");
                        sprites = new BufferedImage[1]; // מערך ריק זמני
                    }

                    IGraphicsData graphics = new GraphicsData(sprites, fps, isLoop);
                    IState state = new State(stateName, pos, pos, TILE_SIZE, physics, graphics);
                    states.put(stateName, state);
                    System.out.println("Loaded state: " + stateName + " for piece " + code.getVal());
                } catch (Exception e) {
                    System.err.println("Error loading state " + stateName + ": " + e.getMessage());
                } finally {
                    is.close();
                }
            }

            if (states.isEmpty()) {
                System.err.println("No states loaded for piece: " + code.getVal());
                return null;
            }

            EState initialState = EState.IDLE;
            if (!states.containsKey(initialState)) {
                initialState = states.keySet().iterator().next(); // קח state ראשון זמין
            }

            System.out.println("Successfully created piece " + code.getVal() + " with " + states.size() + " states");
            return new Piece(code, states, initialState, pos, player);
        } catch (Exception e) {
            System.err.println("Exception creating piece " + code.getVal() + ": " + e.getMessage());
            e.printStackTrace();
            return null;
        }
    }
}
