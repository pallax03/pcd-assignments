package rmi.server;

import rmi.model.LogicsImpl;
import rmi.util.GameAlreadyExistsException;
import rmi.util.GameNotFoundException;

import java.rmi.NoSuchObjectException;
import java.rmi.RemoteException;
import java.rmi.server.UnicastRemoteObject;
import java.util.HashMap;
import java.util.Map;

public class RemoteLobbyImpl implements RemoteLobby {

    private final Map<String, RemoteGame> lobbies;

    public RemoteLobbyImpl() {
        this.lobbies = new HashMap<>();
    }

    @Override
    public synchronized RemoteGame createGame(String lobbyCode) throws RemoteException, GameAlreadyExistsException {
        if (this.lobbies.containsKey(lobbyCode)) {
            throw new GameAlreadyExistsException();
        }
        RemoteGame gameStub = (RemoteGame) UnicastRemoteObject.exportObject(
                new RemoteGameImpl(new LogicsImpl(), lobbyCode, this),
                0);
        this.lobbies.put(lobbyCode, gameStub);
        return gameStub;
    }

    @Override
    public synchronized RemoteGame joinGame(String lobbyCode) throws RemoteException, GameNotFoundException {
        if (!this.lobbies.containsKey(lobbyCode)) {
            throw new GameNotFoundException();
        }
        return this.lobbies.get(lobbyCode);
    }

    public synchronized void removeLobby(String lobbyCode, RemoteGameImpl game) {
        this.lobbies.remove(lobbyCode);

        try {
            UnicastRemoteObject.unexportObject(game, true);
            System.out.println("Lobby: " + lobbyCode + " removed.");
        } catch (NoSuchObjectException e) {
            System.err.println("Lobby: " + lobbyCode + " not exported.");
        }
    }
}