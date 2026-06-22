package pcd.fsstat.eventloop;

import io.vertx.core.Future;
import io.vertx.core.VerticleBase;


public class FsStatVerticle extends VerticleBase {

    public Future<?> start() throws Exception {
        log("started");

        String D = config().getString("D", ".");
        Long MaxFS = config().getLong("MaxFS", 1024L * 1024L);
        int NB = config().getInteger("NB", 4);

        log("scanning\n\troot: "+D+"\n\tMaxFS: "+MaxFS+"(Byte)\n\tNB: "+NB+"(+1)");

        return new FsStatLib(vertx)
                .getFSReport(D, MaxFS, NB)
                .onSuccess( report -> {
                    log("Report Generated for "+D+":");
                    System.out.println("\t"+report.getReport());
                })
                .onFailure(err -> {
                    log("Error: "+err.getMessage());
                });
    }

    private void log(String msg) {
        System.out.println("[ " + System.currentTimeMillis() + " ][ " + Thread.currentThread() + " ] " + msg);
    }
}