package rmi.client;

import rmi.model.Player;
import rmi.util.Position;

import java.rmi.Remote;
import java.rmi.RemoteException;

public interface RemoteClientListener extends Remote {
    void onGameStarted(Player p) throws RemoteException;
    void onOpponentHit(Position pos) throws RemoteException;
    void onGameOver(Player winner) throws RemoteException;
    void onOpponentDisconnected() throws RemoteException;
    void ping() throws RemoteException;
}
