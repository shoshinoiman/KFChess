package client;

import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingQueue;

/**
 * Command queue for client-server communication
 */
public class CommandQueue {
    // תור שמוביל לשרת (פקודות מהמקלדת)
    private final BlockingQueue<String> toServerQueue = new LinkedBlockingQueue<>();
    
    // תור שמגיע מהשרת (פקודות מאושרות)
    private final BlockingQueue<String> fromServerQueue = new LinkedBlockingQueue<>();
    
    public void addToServer(String command) {
        toServerQueue.offer(command);
    }
    
    public String getFromServer() throws InterruptedException {
        return fromServerQueue.take();
    }
    
    public void addFromServer(String command) {
        fromServerQueue.offer(command);
    }
    
    public String getToServer() throws InterruptedException {
        return toServerQueue.take();
    }
    
    public boolean hasCommandsFromServer() {
        return !fromServerQueue.isEmpty();
    }
}