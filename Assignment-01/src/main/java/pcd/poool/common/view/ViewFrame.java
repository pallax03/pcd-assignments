package pcd.poool.common.view;


import pcd.poool.common.control.CommandDispatcher;
import pcd.poool.common.model.BoardConf;
import pcd.poool.common.model.Hole;
import pcd.poool.common.model.Who;
import pcd.poool.common.util.V2d;
import pcd.poool.common.util.P2d;

import javax.swing.*;
import java.awt.*;
import java.awt.event.*;
import java.util.List;

public class ViewFrame extends JFrame implements KeyListener {
    
    private final VisualiserPanel panel;
    private final ViewModel viewModel;
	private final CommandDispatcher controller;
	private final List<Hole> holes;
	private volatile boolean gameOverHandled;
    
	public ViewFrame(final String game_version, final BoardConf staticElements, final ViewModel viewModel, final CommandDispatcher controller){
    	var w = 800; var h = 600;
		setTitle("Poool: "+game_version);
		setResizable(false);
		setSize(w,h);
        panel = new VisualiserPanel(w,h);
        getContentPane().add(panel);

        this.viewModel = viewModel;
		this.holes = staticElements.getHoles();
		this.gameOverHandled = false;

		MouseAdapter mouse = new MouseAdapter() {

			@Override
			public void mousePressed(MouseEvent e) {
				controller.submit(l -> {
                    var mousePosition = panel.fromPixelPositionToPanel(e.getX(), e.getY());
                    var d = mousePosition.sub(l.getBoard().getPlayerBall().getPos());
                    var norm = d.getNormalized();
                    l.kickPlayer(norm.mul(d.abs()*2));
                });
			}
		};

		this.addKeyListener(this);
		panel.addMouseListener(mouse);
		panel.addMouseMotionListener(mouse);

		setFocusable(true);
		setFocusTraversalKeysEnabled(false);
		requestFocusInWindow();
		this.controller = controller;

        addWindowListener(new WindowAdapter(){
			public void windowClosing(WindowEvent ev){
				System.exit(-1);
			}
			public void windowClosed(WindowEvent ev){
				System.exit(-1);
			}
		});
    }
     
    public void refresh(){
		SwingUtilities.invokeLater(() -> {
			panel.repaint();
			scheduleGameOverIfNeeded();
		});
    }

	private void scheduleGameOverIfNeeded() {
		if (this.gameOverHandled || !viewModel.isGameOver()) {
			return;
		}
		this.gameOverHandled = true;
		var timer = new javax.swing.Timer(2000, e -> {
			dispose();
			System.exit(0);
		});
		timer.setRepeats(false);
		timer.start();
	}
        
    public class VisualiserPanel extends JPanel {
		private final int ox;
		private final int oy;
		private final int delta;

		public VisualiserPanel(int w, int h){
            setSize(w,h);
			this.ox = w/2;
			this.oy = h/2;
			this.delta = Math.min(ox, oy);
        }

		public P2d fromPixelPositionToPanel(final int x, final int y) {
			return new P2d((double) (x - ox) / delta, (double) (oy - y) / delta);
		}

		private Color getColorForWho(Who who) {
            return switch (who) {
                case PLAYER -> new Color(0, 207, 239);
                case COM -> new Color(189, 106, 255);
                default -> Color.BLACK;
            };
		}

		private void paintBall(final BallView ball, final Graphics2D g2, final Who actor) {
			if (ball == null) return;
			int x0 = (int)(ox + ball.pos().x()*delta);
			int y0 = (int)(oy - ball.pos().y()*delta);
			int r = (int)(ball.radius()*delta);
			if (actor.notAny()) {
				g2.setColor(getColorForWho(actor));
			} else {
				g2.setColor(getColorForWho(ball.hitBy()));
			}


			g2.drawOval(x0 - r,y0 - r,r*2,r*2);

			if (actor.notAny()) {
				g2.setFont(new Font("SansSerif", Font.BOLD, 14));
				FontMetrics fm = g2.getFontMetrics();
				int tx = x0 - fm.stringWidth(actor.name())/2;
				int ty = y0 + (fm.getAscent() - fm.getDescent())/2;
				g2.drawString(actor.name(), tx, ty);
			}
		}

		@Override
		protected void paintComponent(Graphics g){
			super.paintComponent(g);
    		Graphics2D g2 = (Graphics2D) g;

			g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING,
					RenderingHints.VALUE_ANTIALIAS_ON);
			g2.setRenderingHint(RenderingHints.KEY_RENDERING,
					RenderingHints.VALUE_RENDER_QUALITY);

			g2.setColor(Color.LIGHT_GRAY);
			g2.setStroke(new BasicStroke(1));
			g2.drawLine(ox,0,ox,oy*2);
			g2.drawLine(0,oy,ox*2,oy);
			g2.setColor(Color.BLACK);

			for (var h : holes) {
				int x0 = (int)(ox + h.center().x()*delta);
				int y0 = (int)(oy - h.center().y()*delta);
				int r = (int)(h.radius()*delta);
				g2.setColor(new Color(0, 0, 0));
				g2.fillOval(x0 - r,y0 - r,r*2,r*2);
			}

			g2.setStroke(new BasicStroke(1));
			for (var b: viewModel.getBalls()) {
				paintBall(b, g2, Who.ANY);
			}

			g2.setStroke(new BasicStroke(3));

			paintBall(viewModel.getPlayer().ball(), g2, Who.PLAYER);
			g2.setFont(new Font("SansSerif", Font.BOLD, 34));
			g2.drawString("Player: " + viewModel.getPlayer().score(), 20, getHeight() - 20);

			g2.setStroke(new BasicStroke(3));
			paintBall(viewModel.getCOM().ball(), g2, Who.COM);
			g2.setFont(new Font("SansSerif", Font.BOLD, 34));
			g2.drawString("COM: " + viewModel.getCOM().score(), getWidth()-150, getHeight() - 20);

			g2.setStroke(new BasicStroke(1));
			g2.setFont(new Font("SansSerif", Font.PLAIN, 12));
			g2.setColor(new Color(0, 0,0));
			g2.drawString(viewModel.getFramePerSec() + "fps", (getWidth()/2)-20, getHeight()-20);
			g2.setFont(new Font("SansSerif", Font.PLAIN, 10));
			g2.drawString("num of balls: "+ viewModel.getBalls().size() , (getWidth()/2)-40, getHeight()-5);

			if (viewModel.isGameOver()) {
				g2.setFont(new Font("SansSerif", Font.BOLD, 52));
				var actor = viewModel.getWhoWon().name();
				g2.drawString( actor + " won!", (getWidth()/2)-g2.getFontMetrics().stringWidth(actor)-10, (getHeight()/2)+100);
			}
        }
    }

	@Override
	public void keyTyped(KeyEvent e) {

	}

	@Override
	public void keyPressed(KeyEvent e) {
		if (e.getExtendedKeyCode() == KeyEvent.VK_UP){
			controller.submit(l -> l.kickPlayer(new V2d(0, 1)));
		} else if (e.getExtendedKeyCode() == KeyEvent.VK_DOWN){
			controller.submit(l -> l.kickPlayer(new V2d(0, -1)));
		} else if (e.getExtendedKeyCode() == KeyEvent.VK_RIGHT){
			controller.submit(l -> l.kickPlayer(new V2d( 1, 0)));
		} else if (e.getExtendedKeyCode() == KeyEvent.VK_LEFT) {
			controller.submit(l -> l.kickPlayer(new V2d(-1, 0)));
		}
	}

	@Override
	public void keyReleased(KeyEvent e) {

	}
}
