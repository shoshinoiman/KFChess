package server.graphics;

import server.interfaces.EState;
import server.pieces.EPieceType;

import javax.imageio.ImageIO;
import java.awt.Image;
import java.awt.image.BufferedImage;
import java.io.IOException;
import java.util.*;

import utils.LogUtils;

public class GraphicsLoader {

    private static final Map<String, Image> cache = new HashMap<>();

    /**
     * Loads a single sprite image by piece type, state, and frame index (1-based).
     */
    public static BufferedImage loadSprite(EPieceType pieceType, int player, EState stateName, int frameIndex) {
        String path = String.format("/pieces/%s/states/%s/sprites/sprites%d/%d.png", pieceType.getVal(), stateName, player, frameIndex);

        if (cache.containsKey(path)) {
            return (BufferedImage) cache.get(path);
        }

        try {
            BufferedImage image = ImageIO.read(GraphicsLoader.class.getResourceAsStream(path));
            cache.put(path, image);
            return image;
        } catch (IOException | IllegalArgumentException e) {
            // אם לא מצא תמונה, נסה תמונה דיפולטית
            String defaultPath = String.format("/pieces/%s/states/%s/sprites/sprites%d/1.png", pieceType.getVal(), stateName, player);
            try {
                BufferedImage defaultImage = ImageIO.read(GraphicsLoader.class.getResourceAsStream(defaultPath));
                cache.put(path, defaultImage);
                return defaultImage;
            } catch (IOException | IllegalArgumentException e2) {
                // אם גם זה לא עובד, ננסה בלי שחקן ספציפי
                String genericPath = String.format("/pieces/%s/states/%s/1.png", pieceType.getVal(), stateName);
                try {
                    BufferedImage genericImage = ImageIO.read(GraphicsLoader.class.getResourceAsStream(genericPath));
                    cache.put(path, genericImage);
                    return genericImage;
                } catch (IOException | IllegalArgumentException e3) {
                    // במקרה הגרוע, ניצור תמונה ריקה
                    BufferedImage emptyImage = new BufferedImage(64, 64, BufferedImage.TYPE_INT_ARGB);
                    cache.put(path, emptyImage);
                    return emptyImage;
                }
            }
        }
    }

    /**
     * Check if a sprite file exists at the given path
     */
    private static boolean spriteExists(EPieceType pieceType, int player, EState stateName, int frameIndex) {
        String path = String.format("/pieces/%s/states/%s/sprites/sprites%d/%d.png", pieceType.getVal(), stateName, player, frameIndex);
        return GraphicsLoader.class.getResourceAsStream(path) != null;
    }

    /**
     * Loads all sprite frames in sequence (1,2,3,...) until the next file does not exist.
     */
    public static BufferedImage[] loadAllSprites(EPieceType pieceType, int player, EState stateName) {
        List<BufferedImage> sprites = new ArrayList<>();
        int index = 1;
        final int MAX_SPRITES = 100; // הגבלה למניעת לולאה אינסופית

        while (index <= MAX_SPRITES && spriteExists(pieceType, player, stateName, index)) {
            BufferedImage sprite = loadSprite(pieceType, player, stateName, index);
            if (sprite != null) {
                sprites.add(sprite);
            }
            index++;
        }

        // אם לא נמצאו sprites, צור לפחות תמונה אחת ריקה
        if (sprites.isEmpty()) {
            System.out.println("No sprites found for " + pieceType.getVal() + " player " + player + " state " + stateName + ", creating empty sprite");
            BufferedImage emptyImage = new BufferedImage(64, 64, BufferedImage.TYPE_INT_ARGB);
            sprites.add(emptyImage);
        }

        System.out.println("Loaded " + sprites.size() + " sprites for " + pieceType.getVal() + " player " + player + " state " + stateName);
        return sprites.toArray(new BufferedImage[0]);
    }
}