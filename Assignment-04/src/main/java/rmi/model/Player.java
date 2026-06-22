package rmi.model;

public enum Player {
    X,
    O;

    public Player getOpponent() {
        return this == X ? O : X;
    }
}