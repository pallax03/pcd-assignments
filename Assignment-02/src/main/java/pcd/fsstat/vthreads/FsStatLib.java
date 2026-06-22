package pcd.fsstat.vthreads;

import pcd.fsstat.Report;

import java.io.File;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.stream.Stream;

public class FsStatLib implements AutoCloseable {

    private final ExecutorService executor;

    public FsStatLib() {
        this.executor = Executors.newVirtualThreadPerTaskExecutor();
    }

    public CompletableFuture<Report> getFSReport(final String D, final long MaxFS, final int NB) {
        return CompletableFuture.supplyAsync(() -> scan(Path.of(D), MaxFS, NB), this.executor);
    }

    private Report scan(final Path path, final long MaxFS, final int NB) {
        if (Thread.currentThread().isInterrupted()) {
            return new Report(MaxFS, NB);
        }

        try {
            var file = new File(path.toUri());

            if (file.isFile()) {
                log("added file: " + path);
                return new Report(MaxFS, NB).addFile(file.length());
            }

            if (file.isDirectory()) {
                log("dir: " + path + " exploring it");
                return scanDir(path, MaxFS, NB);
            }
        } catch (Exception ex) {
            log("unable to read file: " + path);
        }
        return new Report(MaxFS, NB);
    }

    private Report scanDir(final Path dir, final long MaxFS, final int NB) {
        final List<Path> children;

        try (Stream<Path> stream = Files.list(dir)) {
            children = stream.toList();
        } catch (Exception ex) {
            log("unable to read dir " + dir);
            return new Report(MaxFS, NB);
        }

        List<CompletableFuture<Report>> tasks = new ArrayList<>();
        children.forEach(c -> {
            tasks.add(getFSReport(c.toString(), MaxFS, NB));
        });

        Report report = new Report(MaxFS, NB);
        for (CompletableFuture<Report> task : tasks) {
            report.merge(task.join());
        }
        log("completed dir: " + dir);
        return report;
    }

    @Override
    public void close() {
        this.executor.close();
    }

    private void log(String msg) {
        System.out.println("[ " + System.currentTimeMillis() + " ][ " + Thread.currentThread() + " ] " + msg);
    }
}
