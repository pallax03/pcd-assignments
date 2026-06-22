package pcd.poool.common.util;

public class V2d extends Pair<Double, Double> {

    public V2d(final double x, final double y) {
        super(x, y);
    }

    public double abs() {
        return Math.sqrt(x() * x() + y() * y());
    }

    public V2d getNormalized() {
        double module = Math.sqrt(x() * x() + y() * y());
        return new V2d(x() / module, y() / module);
    }

    public V2d mul(double fact) {
        return new V2d(x() * fact, y() * fact);
    }

    public V2d getSwappedX() {
        return new V2d(-x(), y());
    }

    public V2d getSwappedY() {
        return new V2d(x(), -y());
    }

    public String toString() {
        return "V2d(" + x() + "," + y() + ")";
    }
}
