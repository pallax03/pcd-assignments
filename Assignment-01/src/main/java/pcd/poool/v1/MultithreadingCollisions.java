package pcd.poool.v1;

import java.util.List;
import pcd.poool.common.model.Ball;
import pcd.poool.common.model.Board;
import pcd.poool.common.model.CollisionResolver;
import pcd.poool.common.model.Who;
import pcd.poool.common.util.*;

public class MultithreadingCollisions implements CollisionResolver {

	private static final int N_CORES = Runtime.getRuntime().availableProcessors() - 1;

	private final Barrier startBarrier = new CyclicBarrier(N_CORES);
	private final Barrier endBarrier = new CyclicBarrier(N_CORES);

	private List<Ball> currentBalls;

	public MultithreadingCollisions() {
		for (int t = 0; t < N_CORES - 1; t++) {
			final int workerId = t;
			new Thread(() -> {
				while (true) {
					try {
						startBarrier.hitAndWaitAll();

						int totalBalls = currentBalls.size();
						int chunkSize = totalBalls / N_CORES;
						int start = workerId * chunkSize;
						int end = (workerId + 1) * chunkSize;

						resolve(currentBalls, start, end, totalBalls);

						endBarrier.hitAndWaitAll();

					} catch (InterruptedException e) {
						Thread.currentThread().interrupt();
						break;
					}
				}
			}, "Collision-Worker-" + workerId).start();
		}
	}

	@Override
	public void resolveSmallBalls(final Board board) {
		this.currentBalls = board.getBalls();
		int totalBalls = currentBalls.size();
		int chunkSize = totalBalls / N_CORES;

		try {
			startBarrier.hitAndWaitAll();
			resolve(currentBalls, (N_CORES - 1) * chunkSize, totalBalls, totalBalls);
			endBarrier.hitAndWaitAll();

		} catch (InterruptedException e) {
			Thread.currentThread().interrupt();
		}
	}

	private void resolve(List<Ball> balls, int start, int end, int totalBalls) {
		for (int i = start; i < end; i++) {
			Ball first = balls.get(i);
			for (int j = i + 1; j < totalBalls; j++) {
				Ball second = balls.get(j);
				if (Ball.checkAndResolveCollisionSafe(first, second)) {
					first.setLastHitBy(Who.ANY);
					second.setLastHitBy(Who.ANY);
				}
			}
		}
	}
}