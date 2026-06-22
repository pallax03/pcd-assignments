package rmi.model;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import rmi.util.Position;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;

class LogicsTest {

    private Logics logics;

    @BeforeEach
    void setUp() {
        logics = new LogicsImpl();
    }

    @Test
    void invalidHit() {
        assertFalse(logics.hit(Player.X, new Position(Logics.BOARD_SIZE, 0)));
        assertTrue(logics.hit(Player.X, new Position(0, 0)));
        assertFalse(logics.hit(Player.O, new Position(0, -1)));
        assertFalse(logics.hit(Player.O, new Position(0, 0)));
    }

    @Test
    void testRowWin() {
        logics.hit(Player.X, new Position(0, 0));
        logics.hit(Player.O, new Position(1, 0));
        logics.hit(Player.X, new Position(0, 1));
        logics.hit(Player.O, new Position(1, 1));
        logics.hit(Player.X, new Position(0, 2));

        assertTrue(logics.isGameOver());
        assertEquals(Optional.of(Player.X), logics.getWinner());
    }

    @Test
    void testColumnWin() {
        logics.hit(Player.X, new Position(0, 1));
        logics.hit(Player.O, new Position(0, 0));
        logics.hit(Player.X, new Position(1, 1));
        logics.hit(Player.O, new Position(1, 0));
        logics.hit(Player.X, new Position(2, 1));

        assertTrue(logics.isGameOver());
        assertEquals(Optional.of(Player.X), logics.getWinner());
    }

    @Test
    void testMainDiagonalWin() {
        logics.hit(Player.X, new Position(0, 0));
        logics.hit(Player.O, new Position(0, 1));
        logics.hit(Player.X, new Position(1, 1));
        logics.hit(Player.O, new Position(0, 2));
        logics.hit(Player.X, new Position(2, 2));

        assertTrue(logics.isGameOver());
        assertEquals(Optional.of(Player.X), logics.getWinner());
    }

    @Test
    void testAntiDiagonalWin() {
        logics.hit(Player.X, new Position(0, 2));
        logics.hit(Player.O, new Position(0, 0));
        logics.hit(Player.X, new Position(1, 1));
        logics.hit(Player.O, new Position(0, 1));
        logics.hit(Player.X, new Position(2, 0));

        assertTrue(logics.isGameOver());
        assertEquals(Optional.of(Player.X), logics.getWinner());
    }

    @Test
    void testDraw() {
        logics.hit(Player.X, new Position(0, 0));
        logics.hit(Player.O, new Position(0, 1));
        logics.hit(Player.X, new Position(0, 2));

        logics.hit(Player.O, new Position(1, 1));
        logics.hit(Player.X, new Position(1, 0));
        logics.hit(Player.O, new Position(1, 2));

        logics.hit(Player.X, new Position(2, 1));
        logics.hit(Player.O, new Position(2, 0));
        logics.hit(Player.X, new Position(2, 2));

        assertTrue(logics.isGameOver());
        assertTrue(logics.getWinner().isEmpty());
    }
}