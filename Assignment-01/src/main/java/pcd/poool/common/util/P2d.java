package pcd.poool.common.util;

public class P2d extends Pair<Double, Double>{

    public P2d(final double x, final double y) {
        super(x, y);
    }

    public P2d sum(final V2d v) {
        return new P2d(super.x() + v.x(), super.y() + v.y());
    }

    public V2d sub(final P2d v) {
        return new V2d(super.x() - v.x(), super.y() - v.y());
    }

    @Override
    public String toString() {
        return "P2d(" + super.x() + "," + super.y() + ")";
    }
}