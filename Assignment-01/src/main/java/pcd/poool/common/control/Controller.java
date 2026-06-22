package pcd.poool.common.control;

public interface Controller extends CommandDispatcher {
    void start();
    default void log(String msg) {
        System.out.println("[ " + System.currentTimeMillis() + "][ Controller ] " + msg);
    }
}