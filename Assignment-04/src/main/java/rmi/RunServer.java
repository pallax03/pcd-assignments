package rmi;

import rmi.server.RemoteLobby;
import rmi.server.RemoteLobbyImpl;
import java.rmi.registry.LocateRegistry;
import java.rmi.registry.Registry;
import java.rmi.server.UnicastRemoteObject;

public class RunServer {
    public static final String BIND_NAME = "TicTacToe";
    public static final int PORT = 1099;

    public static void main(String[] args) {
        try {
            Registry registry;
            try {
                registry = LocateRegistry.createRegistry(PORT);
            } catch (java.rmi.server.ExportException e) {
                registry = LocateRegistry.getRegistry(PORT);
            }

            RemoteLobbyImpl lobby = new RemoteLobbyImpl();
            RemoteLobby lobbyStub = (RemoteLobby) UnicastRemoteObject.exportObject(lobby, 0);

            registry.rebind(BIND_NAME, lobbyStub);
            System.out.println(BIND_NAME+" registered, on port "+PORT);

        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}