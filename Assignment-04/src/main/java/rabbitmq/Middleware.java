package rabbitmq;

import java.io.IOException;

public interface Middleware {
    String EXCHANGE_NAME = "EXCHANGE_NAME";
    String ROUTING_BROADCAST = "agrawala.broadcast";
    String ROUTING_PRIVATE = "agrawala.node.";
    long WAITING_FOR_NODES_TIMEOUT = 30*1000;

    void enterCS() throws InterruptedException, IOException;
    void exitCS() throws InterruptedException;
}
