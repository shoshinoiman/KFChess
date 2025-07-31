package utils;

import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;

/**
 * Utility class for logging debug messages to a file and console.
 */
public class LogUtils {
    public static void logDebug(String message) {
        // Print to console for immediate feedback
        System.out.println("DEBUG: " + message);
        
        // Also write to file
        try (PrintWriter out = new PrintWriter(new FileWriter("debug.log", true))) {
            out.println("DEBUG: " + message);
        } catch (IOException e) {
            System.err.println("Failed to write to debug.log: " + e.getMessage());
        }
    }
}
