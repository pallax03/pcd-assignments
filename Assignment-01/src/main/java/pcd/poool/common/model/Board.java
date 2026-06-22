package pcd.poool.common.model;

import java.util.*;

public class Board {

    private final Ball playerBall;
    private final Ball comBall;
    private final List<Ball> balls;
    private final List<Hole> holes;
    private final Boundary bounds;
    private final CollisionResolver collisionResolver;

    private int playerScoredThisTick;
    private int comScoredThisTick;

    public Board(final BoardConf conf, final SmallBallsStrategy s, final CollisionResolver collisionResolver) {
        this.playerBall = conf.getPlayerBall();
        this.comBall = conf.getBotBall();
        this.balls = conf.getBalls(s);
        this.holes = conf.getHoles();
        this.bounds = conf.getBoardBoundary();
        this.collisionResolver = collisionResolver;
    }

    public void updateMovement(final long elapsed) {
        this.playerBall.updateState(elapsed, this);
        this.comBall.updateState(elapsed, this);
        for (var b: this.balls) {
            b.updateState(elapsed, this);
        }
    }

    public void updateCollisions() {
        this.collisionResolver.resolve(this);
    }

    public void updateScores() {
        this.playerScoredThisTick = 0;
        this.comScoredThisTick = 0;
        this.balls.removeIf(b -> {
            var filter = ballInHole(b);
            if (filter) {
                if (b.getLastHitBy() == Who.PLAYER) {
                    this.playerScoredThisTick++;
                } else if (b.getLastHitBy() == Who.COM) {
                    this.comScoredThisTick++;
                }
            }
            return filter;
        });
    }

    public boolean ballInHole(final Ball ball) {
        return this.holes.stream().anyMatch(hole -> hole.inHole(ball.getPos()));
    }

    public Ball getPlayerBall() {
        return this.playerBall;
    }

    public Boundary getBounds() {
        return this.bounds;
    }

    public List<Ball> getBalls() {
        return this.balls;
    }

    public int getPlayerScoredThisTick() {
        return this.playerScoredThisTick;
    }

    public int getComScoredThisTick() {
        return this.comScoredThisTick;
    }

    public Ball getCOMBall() {
        return this.comBall;
    }
}
