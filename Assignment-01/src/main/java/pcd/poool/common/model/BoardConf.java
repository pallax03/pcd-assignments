package pcd.poool.common.model;

import pcd.poool.common.util.P2d;
import pcd.poool.common.util.V2d;

import java.util.List;

public class BoardConf {

	public Ball getPlayerBall() {
    	return new Ball(new P2d(-.75, -.8), 0.1, 1, new V2d(0,0.5));
	}
	public Ball getBotBall() {
		return new Ball(new P2d(0.75, -.8), 0.1, 1, new V2d(0,0.5));
	}

	public List<Hole> getHoles() {
		return List.of(
				new Hole(new P2d(-1.35, 1), .20),
				new Hole(new P2d(1.35, 1), .20)
		);
	}

	public List<Ball> getBalls(SmallBallsStrategy s) {
		return s.generateBalls();
	}

	public Boundary getBoardBoundary() {
        return new Boundary(-1.35,-.9,1.35,1);
	}
}

