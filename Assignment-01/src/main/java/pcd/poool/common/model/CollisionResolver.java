package pcd.poool.common.model;

import java.util.List;

@FunctionalInterface
public interface CollisionResolver {

	void resolveSmallBalls(Board board);

	default void resolve(final Board board) {
		resolveSmallBalls(board);
		Ball.resolveCollision(board.getPlayerBall(), board.getCOMBall());

		var balls = board.getBalls();
		resolveActorAgainstSmallBalls(board.getPlayerBall(), Who.PLAYER, balls);
		resolveActorAgainstSmallBalls(board.getCOMBall(), Who.COM, balls);
	}

	private void resolveActorAgainstSmallBalls(final Ball actor, final Who actorWho, final List<Ball> balls) {
		for (Ball b : balls) {
			if (Ball.checkAndResolveCollisionSafe(actor, b)) {
				b.setLastHitBy(actorWho);
			}
		}
	}
}