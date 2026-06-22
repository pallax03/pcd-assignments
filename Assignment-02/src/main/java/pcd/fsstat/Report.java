package pcd.fsstat;

import io.vertx.core.json.JsonArray;
import io.vertx.core.json.JsonObject;

import java.util.ArrayList;
import java.util.List;

public class Report {
    private final List<Long> ranges = new ArrayList<>();
    private final List<Integer> buckets = new ArrayList<>();

    public Report(final Long MaxFS, final int NB) {
        for (int i = 0; i < NB + 1; i++) {
            ranges.add((i*MaxFS)/NB);
            buckets.add(0);
        }
    }

    public List<Long> getRanges() {
        return this.ranges;
    }

    public Report addFile(final long size) {
        boolean added = true;
        for (int i = 1; added; i++) {
            if (i == this.ranges.size() || size < this.ranges.get(i)) {
                this.buckets.set(i-1, this.buckets.get(i-1)+1);
                added = false;
            }
        }
        return this;
    }

    public List<Integer> getCountedFiles() {
        return this.buckets;
    }

    public JsonArray getReport() {
        JsonArray res = new JsonArray();
        for (int i = 0; i < this.ranges.size(); i++) {
            res.add(JsonObject.of(
                    "ranges", "["+this.ranges.get(i)+", " + (i+1 == this.ranges.size() ? "+∞" : this.ranges.get(i+1)) + ")",
                    "counted", this.buckets.get(i))
            );
        }
        return res;
    }

    /**
     * @param r -> another report to add.
     * if this and r.ranges.size differs, it fails silently (do not merge)
     */
    public void merge(final Report r) {
        var files = r.getCountedFiles();
        if (this.buckets.size() != files.size()) {
            return ;
        }

        for (int i = 0; i < this.buckets.size(); i++) {
            this.buckets.set(i, this.buckets.get(i) + files.get(i));
        }
    }
}
