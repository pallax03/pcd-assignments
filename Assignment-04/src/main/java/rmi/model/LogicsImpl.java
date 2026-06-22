package rmi.model;

import rmi.util.Position;

import java.util.Arrays;
import java.util.Objects;
import java.util.Optional;
import java.util.stream.IntStream;

public class LogicsImpl implements Logics {

    private final Player[][] grid = new Player[BOARD_SIZE][BOARD_SIZE];
    private Optional<Player> winner = Optional.empty();
    private Player turnOf = Player.X;

    private Boolean insideBounds(Position pos) {
        return pos.x() >= 0 && pos.x() < BOARD_SIZE &&
                pos.y() >= 0 && pos.y() < BOARD_SIZE;
    }

    private Boolean isEmptyCell(Position pos) {
        return grid[pos.x()][pos.y()] == null;
    }

    @Override
    public boolean hit(Player p, Position pos) {
        if (
                this.turnOf == p &&
                this.insideBounds(pos) &&
                isEmptyCell(pos)) {
            this.grid[pos.x()][pos.y()] = p;
            this.turnOf = p.getOpponent();
            return true;
        }
        return false;
    }

    @Override
    public boolean isGameOver() {
        this.winner = Arrays.stream(Player.values())
                .filter(this::hasWon)
                .findFirst();

        return this.winner.isPresent() || isGridFull();
    }

    @Override
    public Optional<Player> getWinner() {
        return this.winner;
    }

    private boolean hasWon(Player p) {
        boolean rowWin = IntStream.range(0, BOARD_SIZE)
                .anyMatch(r -> IntStream.range(0, BOARD_SIZE)
                        .allMatch(c -> grid[r][c] == p)
                );

        boolean colWin = IntStream.range(0, BOARD_SIZE)
                .anyMatch(c -> IntStream.range(0, BOARD_SIZE)
                        .allMatch(r -> grid[r][c] == p)
                );

        boolean mainDiagWin = IntStream.range(0, BOARD_SIZE)
                .allMatch(i -> grid[i][i] == p);

        boolean antiDiagWin = IntStream.range(0, BOARD_SIZE)
                .allMatch(i -> grid[i][BOARD_SIZE - 1 - i] == p
                );

        return rowWin || colWin || mainDiagWin || antiDiagWin;
    }

    private boolean isGridFull() {
        return Arrays.stream(grid)
                .flatMap(Arrays::stream)
                .noneMatch(Objects::isNull);
    }
}
