package pcd.fsstat.eventloop;

import io.vertx.core.DeploymentOptions;
import io.vertx.core.Vertx;
import io.vertx.core.json.JsonObject;

public class TestEventLoop {
    static void main(String[] args) {
        Vertx vertx = Vertx.vertx();
        JsonObject config = new JsonObject()
//                .put("D", "/Volumes/ESD-USB")
                .put("MaxFS", 1024L)
                .put("NB", 4);
        vertx.deployVerticle(new FsStatVerticle(), new DeploymentOptions().setConfig(config))
                .eventually(vertx::close);
    }
}