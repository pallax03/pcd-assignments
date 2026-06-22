package pcd.poool.v2;

import pcd.poool.common.control.Controller;
import pcd.poool.common.control.GameCommand;
import pcd.poool.common.model.Logics;

import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

public class ExecutorController implements Controller {

    public static int TARGET_HZ = 120;

    private final Logics gameLogics;
    private final ScheduledExecutorService loop;
    private final ConcurrentLinkedQueue<GameCommand> cmdQueue;

    private final long t0;
    private long lastUpdatedTime;
    private final long tickMs;

    private volatile boolean started;

    public ExecutorController(final Logics logics) {
        this.gameLogics = logics;
        // Invece di un thread dedicato col while(true), si usa un ScheduledExecutor
        // che invoca periodicamente il meccanismo di aggiornamento della logica
        this.loop = Executors.newSingleThreadScheduledExecutor(r -> {
            Thread t = new Thread(r, "executor-controller-loop");
            t.setDaemon(true);
            return t;
        });
        this.cmdQueue = new ConcurrentLinkedQueue<>();
        this.t0 = System.currentTimeMillis();
        this.lastUpdatedTime = this.t0;
        this.tickMs = 1000/TARGET_HZ;
        this.started = false;
    }

    public synchronized void start() {
        if (this.started) {
            return;
        }
        this.started = true;
        this.loop.scheduleAtFixedRate(this::gameLoop, 0, this.tickMs, TimeUnit.MILLISECONDS);
    }

    @Override
    public void submit(final GameCommand cmd) {
        this.cmdQueue.offer(cmd);
    }


    private void gameLoop() {
        if (this.gameLogics.isGameOver()) {
            this.started = false;
            this.loop.shutdownNow();
            return;
        }

        GameCommand cmd;
        while ((cmd = this.cmdQueue.poll()) != null) {
            cmd.execute(this.gameLogics);
        }

        long currentTimeMs = System.currentTimeMillis();
        this.gameLogics.updateGame(currentTimeMs - this.lastUpdatedTime, currentTimeMs - this.t0);
        this.lastUpdatedTime = currentTimeMs;
        this.gameLogics.kickCOM();
    }
}
