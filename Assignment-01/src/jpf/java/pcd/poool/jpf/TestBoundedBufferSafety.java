package pcd.poool.jpf;

import pcd.poool.common.util.BoundedBuffer;
import pcd.poool.common.util.BoundedBufferImpl;

public class TestBoundedBufferSafety {

    private static final int BUFFER_SIZE = 2;
    private static final int PRODUCERS = 2;
    private static final int CONSUMERS = 2;
    private static final int ITEMS_PER_PRODUCER = 2;
    private static final int TOTAL_ITEMS = PRODUCERS * ITEMS_PER_PRODUCER;

    static class Counters {
        private int produced;
        private int consumed;

        synchronized void onProduced() {
            produced++;
        }

        synchronized void onConsumed() {
            consumed++;
            // A consume can never overtake production.
            assert consumed <= produced : "Consumed more elements than produced";
        }

        synchronized int getProduced() {
            return produced;
        }

        synchronized int getConsumed() {
            return consumed;
        }
    }

    static class Producer extends Thread {
        private final int id;
        private final BoundedBuffer<Integer> buffer;
        private final Counters counters;

        Producer(final int id, final BoundedBuffer<Integer> buffer, final Counters counters) {
            this.id = id;
            this.buffer = buffer;
            this.counters = counters;
        }

        @Override
        public void run() {
            for (int i = 0; i < ITEMS_PER_PRODUCER; i++) {
                try {
                    buffer.put(id * 100 + i);
                    counters.onProduced();
                } catch (InterruptedException e) {
                    Thread.currentThread().interrupt();
                    return;
                }
            }
        }
    }

    static class Consumer extends Thread {
        private final BoundedBuffer<Integer> buffer;
        private final Counters counters;
        private final int itemsToConsume;

        Consumer(final BoundedBuffer<Integer> buffer, final Counters counters, final int itemsToConsume) {
            this.buffer = buffer;
            this.counters = counters;
            this.itemsToConsume = itemsToConsume;
        }

        @Override
        public void run() {
            for (int i = 0; i < itemsToConsume; i++) {
                try {
                    Integer v = buffer.get();
                    // get must return a non-null item, as producers are guaranteed to produce enough items.
                    assert v != null : "Unexpected null item";
                    counters.onConsumed();
                } catch (InterruptedException e) {
                    Thread.currentThread().interrupt();
                    return;
                }
            }
        }
    }

    private static void startAll(Thread[] threads) {
        for (Thread t : threads) {
            t.start();
        }
    }

    private static void joinAll(Thread[] threads) throws InterruptedException {
        for (Thread t : threads) {
            t.join();
        }
    }

    public static void main(String[] args) throws Exception {
        BoundedBuffer<Integer> buffer = new BoundedBufferImpl<>(BUFFER_SIZE);
        Counters counters = new Counters();

        int base = TOTAL_ITEMS / CONSUMERS;
        int remainder = TOTAL_ITEMS % CONSUMERS;

        Thread[] producerThreads = new Thread[PRODUCERS];
        Thread[] consumerThreads = new Thread[CONSUMERS];

        for (int i = 0; i < PRODUCERS; i++) {
            producerThreads[i] = new Producer(i, buffer, counters);
        }
        for (int i = 0; i < CONSUMERS; i++) {
            int items = base + (i < remainder ? 1 : 0);
            consumerThreads[i] = new Consumer(buffer, counters, items);
        }

        startAll(producerThreads);
        startAll(consumerThreads);

        joinAll(producerThreads);
        joinAll(consumerThreads);

        // All items produced must be consumed, and no more.
        assert counters.getProduced() == TOTAL_ITEMS : "Produced count mismatch";
        assert counters.getConsumed() == TOTAL_ITEMS : "Consumed count mismatch";
    }
}