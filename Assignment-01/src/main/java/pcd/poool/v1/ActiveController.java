package pcd.poool.v1;

import pcd.poool.common.control.Controller;
import pcd.poool.common.control.GameCommand;
import pcd.poool.common.model.Logics;
import pcd.poool.common.util.BoundedBuffer;
import pcd.poool.common.util.BoundedBufferImpl;

public class ActiveController extends Thread implements Controller {

    public static int TARGET_HZ = 120;

    private final BoundedBuffer<GameCommand> cmdBuffer;
    private final Logics gameLogics;

    private final long t0;
    private long lastUpdatedTime;
    private final long tickMs;
    private long nextTickAt;

    public ActiveController(final Logics logics) {
        this.cmdBuffer = new BoundedBufferImpl<>(100);
        this.gameLogics = logics;
        this.t0 = System.currentTimeMillis();
        this.lastUpdatedTime = this.t0;
        this.tickMs = 1000/TARGET_HZ;
        this.nextTickAt = this.t0 + tickMs;
    }

    public void run() {
        do {
            var currentTimeMS = System.currentTimeMillis();
            long waitMs = Math.max(0, nextTickAt - currentTimeMS);

            GameCommand cmd = null;
            try {
                cmd = cmdBuffer.poll(waitMs);
            } catch (InterruptedException ignored) {}

            if (cmd != null) {
                cmd.execute(this.gameLogics);
                continue;
            }

            gameLoop();
        } while (!gameLogics.isGameOver());
    }

    private void gameLoop() {
        long currentTimeMS = System.currentTimeMillis();
        if (currentTimeMS >= nextTickAt) {
            this.gameLogics.updateGame(currentTimeMS - this.lastUpdatedTime, currentTimeMS - this.t0);
            this.lastUpdatedTime = currentTimeMS;
            this.gameLogics.kickCOM();
            this.nextTickAt += tickMs;
        }
    }

    @Override
    public void submit(final GameCommand cmd) {
        try {
            cmdBuffer.put(cmd);
        } catch (Exception ex) {
            log(ex.getMessage());
        }
    }
}