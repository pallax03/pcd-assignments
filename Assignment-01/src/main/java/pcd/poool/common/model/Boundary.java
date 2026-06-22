package pcd.poool.common.model;

import pcd.poool.common.util.Pair;

public class Boundary {
    private final Pair<Double, Double> from;
    private final Pair<Double, Double> to;

    public Boundary(double x0, double y0, double x1, double y1) {
        this.from = new Pair<>(x0, y0);
        this.to = new Pair<>(x1, y1);
    }

    public double x0() { return this.from.x(); }
    public double y0() { return this.from.y(); }
    public double x1() { return this.to.x(); }
    public double y1() { return this.to.y(); }
}
