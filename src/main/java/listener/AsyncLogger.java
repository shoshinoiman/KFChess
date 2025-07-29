package listener;
import java.io.BufferedWriter;
import java.io.FileWriter;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingQueue;

public class AsyncLogger {
    private static final String FILE_PATH = "move_log.txt";
    private static final BlockingQueue<String> logQueue = new LinkedBlockingQueue<>();
    static {
        Thread loggerThread = new Thread(() -> {
            while (true) {
                try {
                    String msg = logQueue.take(); // waits if empty
                    try (BufferedWriter writer = new BufferedWriter(new FileWriter(FILE_PATH, true))) {
                        writer.write(msg);
                        writer.newLine();
                    }
                } catch (Exception e) {
                    System.err.println("Logger error: " + e.getMessage());
                }
            }
        });
        loggerThread.setDaemon(true); // מסתיים אוטומטית עם סיום התוכנה
        loggerThread.start();
    }
    public static void log(String message) {
        String timestamp = LocalDateTime.now()
            .format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss"));
        logQueue.offer("[" + timestamp + "] " + message);
    }
}
