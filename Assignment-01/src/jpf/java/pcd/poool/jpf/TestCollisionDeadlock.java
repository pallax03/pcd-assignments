package pcd.poool.jpf;

import pcd.poool.common.model.Ball;
import pcd.poool.common.util.P2d;
import pcd.poool.common.util.V2d;

public class TestCollisionDeadlock {

    public static void main(String[] args) {
        
        // Creiamo 3 palline nello stesso punto.
        P2d center = new P2d(0, 0);
        V2d vel = new V2d(1, 1);
        double radius = 10.0;
        double mass = 1.0;

        Ball b1 = new Ball(center, radius, mass, vel);
        Ball b2 = new Ball(center, radius, mass, vel);
        Ball b3 = new Ball(center, radius, mass, vel);

        // Simuliamo 3 worker che tentano di risolvere collisioni incrociate.
        // Un classico pattern di Deadlock circolare (A->B, B->C, C->A).
        Thread t1 = new Thread(() -> {
            Ball.checkAndResolveCollisionSafe(b1, b2);
        }, "Worker-1");

        Thread t2 = new Thread(() -> {
            Ball.checkAndResolveCollisionSafe(b2, b3);
        }, "Worker-2");

        Thread t3 = new Thread(() -> {
            Ball.checkAndResolveCollisionSafe(b3, b1);
        }, "Worker-3");

        t1.start();
        t2.start();
        t3.start();

        // Se il lock ordering fallisce, JPF si accorgerà di un deadlock qui.
        try {
            t1.join();
            t2.join();
            t3.join();
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }
}