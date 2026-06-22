package pcd.fsstat.vthreads;

public class TestVThreads {

    public static void main(String[] args) {
        String D = ".";
        long MaxFS = 1024L;
        int NB = 4;

        try (var lib = new FsStatLib()) {
            lib.getFSReport(D, MaxFS, NB)
                    .thenAccept(r -> {
                        log("Report Generated for " + D + ":");
                        System.out.println(r.getReport());
                    })
                    .join();
        }
    }

    private static void log(String msg) {
        System.out.println("[ " + System.currentTimeMillis() + " ][ " + Thread.currentThread() + " ] " + msg);
    }
}
