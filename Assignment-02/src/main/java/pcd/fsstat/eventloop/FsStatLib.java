package pcd.fsstat.eventloop;

import io.vertx.core.*;
import io.vertx.core.file.FileSystem;
import pcd.fsstat.Report;

import java.util.ArrayList;
import java.util.List;


public class FsStatLib {

    private final Vertx vertx;

    public FsStatLib(final Vertx vertx) {
        this.vertx = vertx;
    }

    protected Future<Report> getFSReport(final String D, final long MaxFS , final int NB) {
        FileSystem fs = this.vertx.fileSystem();
        log("generating report for "+D);
        return fs.props(D)
                .compose(fileProps -> {
                    if (fileProps.isDirectory()) {
                        return fs.readDir(D).compose(files -> {
                            log("dir: "+D+" exploring it");
                            List<Future<Report>> tasks = new ArrayList<>();
                            files.stream().map(f -> getFSReport(f, MaxFS, NB)).forEach(tasks::add);
                            return Future.all(tasks).map(_ -> {
                                Report r = new Report(MaxFS, NB);
                                log("completed dir:" + D);
                                tasks.stream().map(Future::result).forEach(r::merge);
                                return r;
                            });
                        });
                    }
                    if (fileProps.isRegularFile()) {
                        log("added file: " + D);
                        return Future.succeededFuture(new Report(MaxFS, NB).addFile(fileProps.size()));
                    }
                    return Future.succeededFuture(new Report(MaxFS, NB));
                }).recover(_ -> {
                    log("unable to read props of " + D);
                    return Future.succeededFuture(new Report(MaxFS, NB));
                });
    }

    private void log(String msg) {
        System.out.println("[ " + System.currentTimeMillis() + " ][ " + Thread.currentThread() + " ] " + msg);
    }
}