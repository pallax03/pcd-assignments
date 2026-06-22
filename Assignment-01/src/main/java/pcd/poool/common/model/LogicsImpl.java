package pcd.poool.common.model;

import pcd.poool.common.util.V2d;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

public class LogicsImpl implements Logics {

    private final Board board;
    private int playerScore;
    private int comScore;
    private final List<LogicsObserver> observers;
    private long dt;
    private Who gameWinningBy;

    private static final Double KICK_VEL_GAP = 0.05;
    private final Random randomKickCOMDirection = new Random(2);
    private long lastKickTimeCOM;

    public LogicsImpl(Board board) {
        this.board = board;
        this.observers = new ArrayList<>();
        this.dt = 0;
        this.gameWinningBy = Who.ANY;
    }

    @Override
    public void notifyObservers() {
        for (var o: this.observers) {
            o.modelUpdated(this);
        }
    }

    @Override
    public synchronized void addObserver(final LogicsObserver observer) {
        this.observers.add(observer);
    }

    @Override
    public void updateGame(long elapsed, long dt) {
        synchronized (this) {
            this.dt = dt;

            this.board.updateMovement(elapsed);
            this.board.updateCollisions();
            this.board.updateScores();
            this.playerScore += this.board.getPlayerScoredThisTick();
            this.comScore += this.board.getComScoredThisTick();

            if (this.board.ballInHole(this.board.getPlayerBall())) {
                this.gameWinningBy = Who.COM;
            } else if (this.board.ballInHole(this.board.getCOMBall())) {
                this.gameWinningBy = Who.PLAYER;
            }
        }
        notifyObservers();
    }

    @Override
    public synchronized long getDt() {
        return this.dt;
    }

    @Override
    public synchronized Who whoWon() {
        return this.gameWinningBy;
    }

    @Override
    public synchronized boolean isGameOver() {
        if (this.getBoard().getBalls().isEmpty()) {
            this.gameWinningBy = (this.playerScore > this.comScore) ? Who.PLAYER : (this.playerScore < this.comScore) ? Who.COM : Who.ANY;
            return true;
        }
        return this.gameWinningBy.notAny();
    }

    @Override
    public synchronized int getPlayerScore() {
        return this.playerScore;
    }

    @Override
    public synchronized int getCOMScore() {
        return this.comScore;
    }

    private boolean canKickBall(final Ball ball) {
        return ball.getVel().abs() < KICK_VEL_GAP;
    }

    @Override
    public synchronized void kickCOM() {
        var currentDt = System.currentTimeMillis();
        if (canKickBall(this.board.getCOMBall()) && currentDt - this.lastKickTimeCOM > 2000) {
            var angle = this.randomKickCOMDirection.nextDouble()*Math.PI*0.25;
            var v = new V2d(Math.cos(angle),Math.sin(angle)).mul(1.5);
            this.board.getCOMBall().kick(v);
            this.lastKickTimeCOM = currentDt;
        }
    }

    @Override
    public synchronized void kickPlayer(final V2d direction) {
        if (!canKickBall(this.board.getPlayerBall())) return;
        this.board.getPlayerBall().kick(direction);
    }

    @Override
    public synchronized Board getBoard() {
        return this.board;
    }

}
