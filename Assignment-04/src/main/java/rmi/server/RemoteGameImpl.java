package rmi.server;

import rmi.client.RemoteClientListener;
import rmi.model.Logics;
import rmi.model.Player;
import rmi.util.GameFullException;
import rmi.util.Position;

import java.rmi.RemoteException;
import java.util.*;

public class RemoteGameImpl implements RemoteGame {

    private final Logics gameLogics;
    private final Map<Player, RemoteClientListener> clients;

    private final String lobbyCode;
    private final RemoteLobbyImpl parentLobby;

    private final Timer pingTimer;

    public RemoteGameImpl(Logics logics, String lobbyCode, RemoteLobbyImpl parentLobby) {
        this.gameLogics = logics;
        this.clients = new HashMap<>();
        this.lobbyCode = lobbyCode;
        this.parentLobby = parentLobby;
        this.pingTimer = new Timer();
    }

    private void ping() {
        Map<Player, RemoteClientListener> clientsSnapshot;
        synchronized (this) {
            clientsSnapshot = new HashMap<>(this.clients);
        }
        clientsSnapshot.forEach((player, client) -> {
            try {
                client.ping();
            } catch (RemoteException e) {
                try {
                    clients.get(player.getOpponent()).onOpponentDisconnected();
                } catch (Exception ex) {} finally {
                    closeGame();
                }
            }
        });
    }

    private void autonomousPing() {
        this.pingTimer.scheduleAtFixedRate(new TimerTask() {
            @Override
            public void run() {
                ping();
            }
        }, 0, PING_DELAY_MS);
    }

    @Override
    public boolean makeMove(Player p, Position pos) throws RemoteException {
        RemoteClientListener opponent = null;
        boolean isMoveValid;
        boolean isGameOver = false;
        Player winner = null;

        synchronized(this) {
            isMoveValid = this.gameLogics.hit(p, pos);

            if (isMoveValid) {
                opponent = clients.get(p.getOpponent());

                isGameOver = this.gameLogics.isGameOver();
                if (isGameOver) {
                    winner = this.gameLogics.getWinner().orElse(null);
                }
            }
        }

        if (isMoveValid && opponent != null) {
            try {
                opponent.onOpponentHit(pos);

            } catch (RemoteException e) {
                System.err.println("Opponent disconnected during move: " + e.getMessage());
                clients.get(p).onOpponentDisconnected();
                closeGame();
                return true;
            }
        }

        if (isGameOver) {
            for (var c: this.clients.values()) {
                try {
                    c.onGameOver(winner);
                } catch (RemoteException e) {
                    System.err.println("Opponent disconnected during game over: " + e.getMessage());
                }
            }
            closeGame();
        }

        return isMoveValid;
    }

    @Override
    public void joinPlayer(Player p, RemoteClientListener clientListener) throws RemoteException, GameFullException {
        synchronized(this) {
            if (this.clients.size() >= 2 || this.clients.containsKey(p)) {
                throw new GameFullException();
            }
            this.clients.put(p, clientListener);
        }
        
        if (this.clients.size() == 1) {
            autonomousPing();
        }
        
        if (this.clients.size() == 2) {
            for (var entry : this.clients.entrySet()) {
                try {
                    entry.getValue().onGameStarted(entry.getKey());
                } catch (RemoteException e) {
                    System.err.println("client disconnected.");
                    closeGame();
                    return;
                }
            }
        }
    }

    private void closeGame() {
        this.pingTimer.cancel();
        this.parentLobby.removeLobby(this.lobbyCode, this);
    }
}