package rmi.server;

import rmi.client.RemoteClientListener;
import rmi.model.Player;
import rmi.util.GameFullException;
import rmi.util.Position;

import java.rmi.Remote;
import java.rmi.RemoteException;

public interface RemoteGame extends Remote {
    
    long PING_DELAY_MS = 5000;

    boolean makeMove(Player p, Position pos) throws RemoteException;

    void joinPlayer(Player p, RemoteClientListener clientListener) throws RemoteException, GameFullException;
}