package pcd.poool.common.util;

import java.util.LinkedList;

/**
 * 
 * Simple implementation of a bounded buffer
 * as a monitor, using Producer-Consumer.
 * With a non-blocking poll, and a blocking get.
 * 
 * @param <Item>
 */
public class BoundedBufferImpl<Item> implements BoundedBuffer<Item> {

	private final LinkedList<Item> buffer;
	private final int maxSize;

	public BoundedBufferImpl(int size) {
		buffer = new LinkedList<Item>();
		maxSize = size;
	}

	public synchronized void put(Item item) throws InterruptedException {
		while (isFull()) {
			wait();
		}
		buffer.addLast(item);
		notifyAll();
	}

	public synchronized Item get() throws InterruptedException {
		while (isEmpty()) {
			wait();
		}
		Item item = buffer.removeFirst();
		notifyAll();
		return item;
	}

	@Override
	public synchronized Item poll(long timeoutMs) throws InterruptedException {
		// if data is already available, do not wait.
		if (!isEmpty()) {
			Item item = buffer.removeFirst();
			notifyAll();
			return item;
		}
		// "do not block", if timer is already expired.
		if (timeoutMs <= 0) return null;

		long deadline = System.currentTimeMillis() + timeoutMs;
		long remaining = timeoutMs;
		// Wait until data arrives or timeout expires.
		while (isEmpty() && remaining > 0) {
			wait(remaining);
			remaining = deadline - System.currentTimeMillis();
		}

		// Timed out without receiving any item.
		if (isEmpty()) return null;
		Item item = buffer.removeFirst();
		notifyAll();
		return item;
	}

	private boolean isFull() {
		return buffer.size() == maxSize;
	}

	private boolean isEmpty() {
		return buffer.size() == 0;
	}
}
