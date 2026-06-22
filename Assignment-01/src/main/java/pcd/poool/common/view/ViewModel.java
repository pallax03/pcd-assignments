package pcd.poool.common.view;

import pcd.poool.common.model.Logics;
import pcd.poool.common.model.Who;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class ViewModel {

	private List<BallView> balls;
	private PlayerView player;
	private PlayerView com;
	private int framePerSec;
	private boolean gameOver;
	private Who gameWonBy;

	public ViewModel() {
		this.balls = new ArrayList<BallView>();
		this.framePerSec = 0;
	}
	
	public synchronized void update(final Logics logics, final int framePerSec) {
		this.framePerSec = framePerSec;
		var board = logics.getBoard();
		List<BallView> newBallsView = new ArrayList<>();
		for (var b: board.getBalls()) {
			newBallsView.add(new BallView(b.getPos(), b.getRadius(), b.getLastHitBy()));
		}
		this.balls = newBallsView;
		this.player = new PlayerView(
				new BallView(board.getPlayerBall().getPos(), board.getPlayerBall().getRadius(), board.getPlayerBall().getLastHitBy()),
				logics.getPlayerScore()
		);
		this.com = new PlayerView(
				new BallView(board.getCOMBall().getPos(), board.getCOMBall().getRadius(), board.getCOMBall().getLastHitBy()),
				logics.getCOMScore()
		);
		this.gameOver = logics.isGameOver();
		this.gameWonBy = logics.whoWon();
	}

	public synchronized int getFramePerSec() {
		return this.framePerSec;
	}
	public synchronized List<BallView> getBalls() {
		return Collections.unmodifiableList(this.balls);
	}
	public synchronized PlayerView getPlayer() {
		return this.player;
	}
	public synchronized PlayerView getCOM() {
		return this.com;
	}
	public synchronized boolean isGameOver() { return this.gameOver; }
	public synchronized Who getWhoWon() {
		return this.gameWonBy;
	}
}
