package pcd.poool.common.util;

public class CyclicBarrier implements Barrier {
	
	private final int nParticipants;
	private int count;
	private int cycle;
	
	public CyclicBarrier(int nParticipants) {
		this.nParticipants = nParticipants;
		this.count = 0;
		this.cycle = 0;
	}
	
	@Override
	public synchronized void hitAndWaitAll() throws InterruptedException {
		count++;
		int currentCycle = cycle;
		if (count == nParticipants) {
			count = 0;
			cycle++;
			notifyAll();
		} else {
			while (cycle == currentCycle) {
				wait();
			}
		}
	}
}
