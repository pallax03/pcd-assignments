package pcd.poool.v2;

import pcd.poool.common.model.*;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class ExecutorCollisions implements CollisionResolver {

    private final int nCores = Runtime.getRuntime().availableProcessors() + 1;
    private final ExecutorService executor = Executors.newFixedThreadPool(nCores);

    @Override
    public void resolveSmallBalls(Board board) {
        var balls = board.getBalls();
        int size = balls.size();
        
        List<Callable<Void>> tasks = new ArrayList<>();
        int chunkSize = size / nCores;

        for (int i = 0; i < nCores; i++) {
            final int workerId = i;

            tasks.add(() -> {
                int startIndex = workerId * chunkSize;
                int endIndex = (workerId == nCores - 1) ? (size - 1) : startIndex + chunkSize;

                for (int idx = startIndex; idx < endIndex; idx++) {
                    Ball first = balls.get(idx);
                    
                    for (int j = idx + 1; j < size; j++) {
                        Ball second = balls.get(j);

                        if (Ball.checkAndResolveCollisionSafe(first, second)) {
                            first.setLastHitBy(Who.ANY);
                            second.setLastHitBy(Who.ANY);
                        }
                    }
                }
                return null;
            });
        }

        try {
            executor.invokeAll(tasks);
        } catch (InterruptedException ignored) {
            Thread.currentThread().interrupt();
        }
    }
}