package pcd.fsstat.rx;

public class TestRx {

    static void main() {
        var source = new FsStatLib().getReport(".", 1024L, 4);
        source.blockingSubscribe(r -> log(r.getReport().toString()));
    }
    static private void log(String msg) {
        System.out.println("[ " + System.currentTimeMillis() + " ][ " + Thread.currentThread() + " ] " + msg);
    }
}