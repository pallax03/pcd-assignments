package rabbitmq.utils;

public class Logger {
    public static void log(final String nodeId, final String message) {
        System.out.println("[client-" + nodeId + "] "+message);
    }
}
