package rmi.client;

import rmi.model.Player;
import rmi.util.Position;

import javax.swing.SwingUtilities;
import java.rmi.RemoteException;

public class RemoteClientListenerImpl implements RemoteClientListener {

    private final GameClientController localController;

    public RemoteClientListenerImpl(GameClientController controller) {
        this.localController = controller;
    }

    @Override
    public void onGameStarted(Player p) throws RemoteException {
        updateUI(() -> localController.startGame(p));
    }

    @Override
    public void onOpponentHit(Position pos) throws RemoteException {
        updateUI(() -> localController.registerOpponentMove(pos));
    }

    @Override
    public void onGameOver(Player winner) throws RemoteException {
        updateUI(() -> localController.showGameOver(winner));
    }

    @Override
    public void onOpponentDisconnected() throws RemoteException {
        updateUI(localController::handleOpponentDisconnect);
    }

    @Override
    public void ping() throws RemoteException {
        return;
    }

    private void updateUI(Runnable action) {
        SwingUtilities.invokeLater(action);
    }
}