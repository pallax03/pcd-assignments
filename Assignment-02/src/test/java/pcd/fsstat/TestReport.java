package pcd.fsstat;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;

class TestReport {

    private final Long MaxFS = 1024L;
    private final int NB = 4;
    private Report r;

    List<Long> sampleFiles = List.of(
            0L, 50L, 100L,  // 3
            256L, 450L,     // 2
            512L,           // 1
                            // 0
            1024L, 1200L    // 2
    );
    List<Integer> sampleFilesSizes = List.of(3, 2, 1, 0, 2);

    @BeforeEach
    void init() {
        this.r = new Report(MaxFS, NB);
        this.sampleFiles.forEach(this.r::addFile);
    }

    @Test
    void testNBRanges() {
        List<Long> ranges = List.of(
                0L, 256L, 512L, 768L, 1024L
        );
        assertEquals(ranges, this.r.getRanges());
    }

    @Test
    void testCountedFiles() {
        System.out.println(this.r.getRanges());
        System.out.println(this.sampleFiles);
        assertEquals(this.sampleFilesSizes, this.r.getCountedFiles());
    }

    @Test void testRecursiveReports() {
        Report r2 = new Report(MaxFS, NB);
        this.r.merge(r2);
        testCountedFiles();
        this.sampleFiles.forEach(r2::addFile);

        var counted = this.sampleFilesSizes.stream().map(v -> v*2).toList();
        this.r.merge(r2);
        assertEquals(counted, this.r.getCountedFiles());
    }

    @Test void testReportJson() {
        System.out.println(this.r.getReport());
    }
}
