package pcd.fsstat.rx;

import io.reactivex.rxjava3.core.Flowable;
import io.reactivex.rxjava3.schedulers.Schedulers;
import pcd.fsstat.Report;

import java.io.File;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.stream.BaseStream;

public class FsStatLib {

    private Flowable<File> generateFilesTree(final Path path) {
        if (!path.toFile().isDirectory()) {
            return Flowable.empty();
        }

        return Flowable.using(
                () -> Files.list(path),
                stream -> Flowable.fromStream(stream)
                        .map(Path::toFile)
                        .flatMap(f -> {
                            if (f.isFile()) {
                                return Flowable.just(f);
                            } else if (f.isDirectory()) {
                                return generateFilesTree(f.toPath());
                            } else {
                                return Flowable.empty();
                            }
                        }),
                BaseStream::close
        ).onErrorResumeNext(error -> {
            log("error with: " + path);
            return Flowable.empty();
        });
    }

    public Flowable<Report> getReport(final String D, final Long MaxFS, final int NB) {
        return generateFilesTree(Path.of(D)).scan(new Report(MaxFS, NB), (current, file) -> {
            Report newReport = new Report(MaxFS, NB);
            newReport.merge(current);
            return newReport.addFile(file.length());
        })
                .onBackpressureLatest()
                .subscribeOn(Schedulers.io());
    }

    static private void log(String msg) {
        System.out.println("[ " + System.currentTimeMillis() + " ][ " + Thread.currentThread() + " ] " + msg);
    }
}
