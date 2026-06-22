package pcd.poool.common.model;

import pcd.poool.common.util.P2d;
import pcd.poool.common.util.V2d;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

@FunctionalInterface
public interface SmallBallsStrategy {
	List<Ball> generateBalls();

	class Named implements SmallBallsStrategy {
		private final String name;
		private final SmallBallsStrategy strategy;

		public Named(String name, SmallBallsStrategy strategy) {
			this.name = name;
			this.strategy = strategy;
		}

		public String name() {
			return name;
		}

		public SmallBallsStrategy strategy() {
			return strategy;
		}

		@Override
		public List<Ball> generateBalls() {
			return strategy.generateBalls();
		}

		@Override
		public String toString() {
			return name;
		}
	}

	static SmallBallsStrategy zero() {
		return new Named("Zero", Collections::emptyList);
	}

	static SmallBallsStrategy minimal() {
		return new Named("Minimal", () -> {
			var balls = new ArrayList<Ball>();
			var b1 = new Ball(new P2d(0, 0.5), 0.05, 0.75, new V2d(0,0));
			var b2 = new Ball(new P2d(0.05, 0.55), 0.025, 0.25, new V2d(0,0));
			balls.add(b1);
			balls.add(b2);
			return balls;
		});
	}
	static SmallBallsStrategy large() {
		return new Named("Large", () -> {
			var ballRadius = 0.01;
			var balls = new ArrayList<Ball>();

			for (int row = 0; row < 20; row++) {
				for (int col = 0; col < 20; col++) {
					var px = -0.25 + col*0.025;
					var py =  row*0.025;
					var b = new Ball(new P2d(px, py), ballRadius, 0.25, new V2d(0,0));
					balls.add(b);
				}
			}
			return balls;
		});
	}
	static SmallBallsStrategy massive() {
		return new Named("Massive", () -> {
			var ballRadius = 0.01;
			var balls = new ArrayList<Ball>();

			for (int row = 0; row < 30; row++) {
				for (int col = 0; col < 150; col++) {
					var px = -1.0 + col*0.015;
					var py =  row*0.015;
					var b = new Ball(new P2d(px, py), ballRadius, 0.25, new V2d(0,0));
					balls.add(b);
				}
			}
			return balls;
		});
	}
}
