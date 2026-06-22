package pcd.poool.jpf;

import pcd.poool.common.util.Barrier;
import pcd.poool.common.util.CyclicBarrier;

public class TestCyclicBarrier {

    private static final int N_PARTICIPANTS = 3;
    private static final int N_CYCLES = 2;

    static class BarrierState {
        private final int[] phase = new int[N_PARTICIPANTS];

        synchronized void updateAndCheckPhase(int threadId, int currentPhase) {
            phase[threadId] = currentPhase;
            int minPhase = phase[0];
            int maxPhase = phase[0];
            for (int p : phase) {
                if (p < minPhase) minPhase = p;
                if (p > maxPhase) maxPhase = p;
            }
            assert (maxPhase - minPhase) <= 1 : "Barrier violated: phases out of sync";
        }
    }

    static class Worker extends Thread {
        private final int id;
        private final Barrier barrier;
        private final BarrierState state;

        Worker(int id, Barrier barrier, BarrierState state) {
            this.id = id;
            this.barrier = barrier;
            this.state = state;
        }

        @Override
        public void run() {
            for (int cycle = 1; cycle <= N_CYCLES; cycle++) {
                state.updateAndCheckPhase(id, cycle);
                try {
                    barrier.hitAndWaitAll();
                } catch (InterruptedException e) {
                    Thread.currentThread().interrupt();
                    return;
                }
            }
        }
    }

    public static void main(String[] args) {
        Barrier barrier = new CyclicBarrier(N_PARTICIPANTS);
        BarrierState state = new BarrierState();
        Thread[] threads = new Thread[N_PARTICIPANTS];

        for (int i = 0; i < N_PARTICIPANTS; i++) {
            threads[i] = new Worker(i, barrier, state);
        }

        for (Thread t : threads) {
            t.start();
        }

        try {
            for (Thread t : threads) {
                t.join();
            }
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }
    }
}
