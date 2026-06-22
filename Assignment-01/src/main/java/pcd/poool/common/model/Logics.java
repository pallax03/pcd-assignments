package pcd.poool.common.model;

import pcd.poool.common.util.V2d;

public interface Logics {

    void notifyObservers();
    void addObserver(final LogicsObserver observer);

    void updateGame(final long elapsed, final long dt);

    long getDt();

    Who whoWon();

    boolean isGameOver();

    int getPlayerScore();

    int getCOMScore();

    void kickCOM();
    void kickPlayer(final V2d direction);

    Board getBoard();
}
