package pcd.poool.common.util;

public class Pair<A, B> {
    private final A x;
    private final B y;

    public Pair(final A x, final B y) {
        this.x = x;
        this.y = y;
    }

    public A x() {
        return this.x;
    }
    public B y() {
        return this.y;
    }
}
