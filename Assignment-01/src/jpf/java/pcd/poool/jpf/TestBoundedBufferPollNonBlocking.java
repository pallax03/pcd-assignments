package pcd.poool.jpf;

import pcd.poool.common.util.BoundedBuffer;
import pcd.poool.common.util.BoundedBufferImpl;

public class TestBoundedBufferPollNonBlocking {

    public static void main(String[] args) throws Exception {
        BoundedBuffer<Integer> buffer = new BoundedBufferImpl<>(2);
        final int expected = 7;

        // Empty buffer + timeout 0 => immediate null.
        Integer v0 = buffer.poll(0);
        assert v0 == null : "poll(0) on empty buffer must return null";

        // If item exists, poll(0) must return it without waiting.
        buffer.put(expected);
        long t0 = System.currentTimeMillis();
        Integer v1 = buffer.poll(0);
        assert System.currentTimeMillis() - t0 < 100 : "poll(0) must return immediately when item is available";
        assert v1 != null && v1 == expected : "poll(0) must return the available item";

        // After draining the queue, behavior returns to immediate null.
        Integer v2 = buffer.poll(0);
        assert v2 == null : "After consuming all elements, poll(0) must return null";
    }
}