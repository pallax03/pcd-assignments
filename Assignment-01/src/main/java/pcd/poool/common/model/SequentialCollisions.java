package pcd.poool.common.model;

public class SequentialCollisions implements CollisionResolver {

	@Override
	public void resolveSmallBalls(final Board board) {
		var balls = board.getBalls();

		for (int i = 0; i < balls.size() - 1; i++) {
			Ball first = balls.get(i);
			for (int j = i + 1; j < balls.size(); j++) {
				Ball second = balls.get(j);
				if (Ball.checkAndResolveCollisionSafe(first, second)) {
					first.setLastHitBy(Who.ANY);
					second.setLastHitBy(Who.ANY);
				}
			}
		}
	}
}
