package client;

//import client.view.GamePanel;

import javax.swing.*;
import java.awt.*;
import java.net.URI;

public class ClientMain {
    public static void main(String[] args) {
        // System.out.println("client main");
        SwingUtilities.invokeLater(() -> {
            JFrame frame = new JFrame("KFChess - Client");
            frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);

//            try {
//                System.out.println("Connecting to server...");
//                // התחברות לשרת
//                URI serverUri = new URI("ws://localhost:8080");
//                GameClient.init(serverUri);
////                GameClient client = GameClient.getInstance();
//
//                // יצירת GamePanel ריק (ייתמלא מהשרת)
//                GamePanel gameView = new GamePanel();
//                // client.setGamePanel(gameView);
//
//                // התחברות לשרת
//                client.connect();
//
//                // frame.setContentPane(gameView);
//                frame.pack();
//                frame.setLocationRelativeTo(null);
//                frame.setVisible(true);
//
//                System.out.println("Client started, connecting to server...");
//
//            } catch (Exception e) {
//                System.out.println("Failed to connect to server: " + e.getMessage());
//                e.printStackTrace();
//                JOptionPane.showMessageDialog(null, "Failed to connect to server: " + e.getMessage());
//            }
        });
    }
}
