package rmi.model;

import rmi.util.Position;

import java.util.Optional;

public interface Logics {
    int BOARD_SIZE = 3;
    boolean hit(final Player p, final Position pos);
    boolean isGameOver();
    Optional<Player> getWinner();
}
