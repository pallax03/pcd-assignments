package rmi.client;

import rmi.RunServer;
import rmi.model.Player;
import rmi.server.RemoteGame;
import rmi.server.RemoteLobby;
import rmi.util.GameAlreadyExistsException;
import rmi.util.GameFullException;
import rmi.util.GameNotFoundException;
import rmi.util.Position;

import java.rmi.registry.LocateRegistry;
import java.rmi.registry.Registry;
import java.rmi.server.UnicastRemoteObject;

public class GameClientController {
    private ViewFrame view;
    private RemoteLobby remoteLobby;
    private RemoteGame remoteGame;
    private RemoteClientListener listenerStub;

    private Player myRole;
    private boolean myTurn;

    public void startClient(String host) {
        try {
            Registry registry = LocateRegistry.getRegistry(host, RunServer.PORT);
            this.remoteLobby = (RemoteLobby) registry.lookup(RunServer.BIND_NAME);

            this.view = new ViewFrame(this);
            this.view.setVisible(true);

            this.listenerStub = (RemoteClientListener) UnicastRemoteObject.exportObject(
                    new RemoteClientListenerImpl(this),
                    0
            );

        } catch (Exception e) {
            System.out.println("server not found");
            e.printStackTrace();
        }
    }

    public void handleCreateLobby(String code) {
        if (code.isEmpty()) { view.showMessage("code not valid"); return; }
        try {
            this.remoteGame = remoteLobby.createGame(code);
            this.myRole = Player.X;
            this.remoteGame.joinPlayer(this.myRole, listenerStub);

            view.showGameScreen();
            view.showMessage("Lobby created. Waiting for opponent...");
            view.refreshTitle(code, myRole.name());
            view.setGridEnabled(false);
        } catch (GameAlreadyExistsException e) {
            view.showMessage("Lobby already exists");
            resetToMenu();
        } catch (Exception e) {
            view.showMessage("server error");
            resetToMenu();
        }
    }

    public void handleJoinLobby(String code) {
        if (code.isEmpty()) { view.showMessage("not valid code!"); return; }
        try {
            this.remoteGame = remoteLobby.joinGame(code);
            this.myRole = Player.O;
            this.remoteGame.joinPlayer(this.myRole, listenerStub);
            view.showGameScreen();
            view.refreshTitle(code, myRole.name());
            view.updateTurnTitle(this.myTurn);
            view.setGridEnabled(false);
        } catch (GameNotFoundException e) {
            view.showMessage("Lobby not found");
            resetToMenu();
        } catch (GameFullException e) {
            view.showMessage("Lobby is full");
            resetToMenu();
        } catch (Exception e) {
            view.showMessage("server error");
            resetToMenu();
        }
    }

    public void handleCellClicked(Position pos) {
        try {
            boolean success = remoteGame.makeMove(myRole, pos);
            if (success) {
                view.setCellText(pos, myRole.name());
                this.myTurn = false;
                view.updateTurnTitle(this.myTurn);
            }
        } catch (Exception e) {
            view.showMessage("server error");
            resetToMenu();
        }
    }

    public void startGame(Player assignedRole) {
        this.myRole = assignedRole;
        this.myTurn = (this.myRole == Player.X);
        view.updateTurnTitle(this.myTurn);
        view.setGridEnabled(true);
        view.showMessage("Game started! player: " + myRole + "\n" +
                (myTurn ? "Your Turn!" : "Waiting opponent turn."));
    }

    public void registerOpponentMove(Position pos) {
        view.setCellText(pos, myRole.getOpponent().name());
        this.myTurn = true;
        view.updateTurnTitle(this.myTurn);
    }

    public void showGameOver(Player winner) {
        if (winner == null) {
            view.showMessage("Game Over! \nDRAW.");
        } else if (winner == myRole) {
            view.showMessage("Game Over! \nYOU WIN");
        } else {
            view.showMessage("Game Over! \nYOU LOSE");
        }
        resetToMenu();
    }

    public void handleOpponentDisconnect() {
        view.showMessage("opponent disconnected");
        resetToMenu();
    }

    private void resetToMenu() {
        this.remoteGame = null;
        this.myRole = null;
        this.myTurn = false;
        view.showLobbyScreen();
        view.refreshTitle("", "");
    }
}