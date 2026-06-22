package pcd.poool.common.view;

import pcd.poool.common.control.CommandDispatcher;
import pcd.poool.common.model.BoardConf;
import pcd.poool.common.model.Logics;
import pcd.poool.common.model.LogicsObserver;

import javax.swing.*;

public class View implements LogicsObserver {

	private final ViewModel viewModel;
	private final ViewFrame frame;
	private int nFrames;

	public View(final String game_version, final BoardConf conf, final ViewModel viewModel, final CommandDispatcher controller) {
		this.viewModel = viewModel;
		this.frame = new ViewFrame(game_version, conf, viewModel, controller);
		nFrames = 0;
	}

	public void display() {
		SwingUtilities.invokeLater(() -> frame.setVisible(true));
	}

	@Override
	public synchronized void modelUpdated(final Logics logics) {
		nFrames++;
		var frames = 0;
		if (logics.getDt() > 0) {
			frames = (int)(nFrames*1000/logics.getDt());
		}
		viewModel.update(logics, frames);
		frame.refresh();
	}
}