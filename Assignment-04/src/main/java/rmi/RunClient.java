package rmi;

import rmi.client.GameClientController;

import javax.swing.SwingUtilities;

public class RunClient {
    public static void main(String[] args) {
        final String host = (args.length < 1) ? "localhost" : args[0];
        
        SwingUtilities.invokeLater(() -> {
            GameClientController controller = new GameClientController();
            controller.startClient(host);
        });
    }
}