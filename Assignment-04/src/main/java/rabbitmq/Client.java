package rabbitmq;

import rabbitmq.utils.Logger;

import java.util.Scanner;

public class Client {

    public static void main(String[] args) {
        String nodeId = args[0];
        Logger.log(nodeId, "started");

        try {
            MiddlewareImpl middleware = new MiddlewareImpl(nodeId);
            while (true) {
               Logger.log(nodeId, "press enter to request cs");
               new Scanner(System.in).nextLine();

                middleware.enterCS();

                Thread.sleep(Middleware.WAITING_FOR_NODES_TIMEOUT/2);

                middleware.exitCS();
            }

        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}