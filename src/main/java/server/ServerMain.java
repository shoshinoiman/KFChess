package server;

public class ServerMain {
    public static void main(String[] args) {
        int port = 8080 ; 
        GameServer server = new GameServer(port);
        server.start();
        System.out.println("Server started on port " + port);
    }
}
