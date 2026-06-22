package pcd.poool.common.model;

import pcd.poool.common.util.P2d;

public class Hole {
    private final P2d center;
    private final double radius;

    public Hole(final P2d center, final double radius) {
        this.center = center;
        this.radius = radius;
    }

    public P2d center() {
        return this.center;
    }

    public double radius() {
        return this.radius;
    }

    public boolean inHole(final P2d pos) {
        return Math.pow((pos.x() - this.center.x()), 2) + Math.pow((pos.y() - this.center.y()), 2) <= Math.pow(this.radius, 2);
    }
}
