package rmi.server;

import rmi.util.GameAlreadyExistsException;
import rmi.util.GameNotFoundException;

import java.rmi.Remote;
import java.rmi.RemoteException;

public interface RemoteLobby extends Remote {
    
    RemoteGame createGame(String lobbyCode) throws RemoteException, GameAlreadyExistsException;
    
    RemoteGame joinGame(String lobbyCode) throws RemoteException, GameNotFoundException;
}