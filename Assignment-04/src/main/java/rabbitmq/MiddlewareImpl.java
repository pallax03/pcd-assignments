package rabbitmq;

import com.rabbitmq.client.*;
import rabbitmq.utils.Logger;
import rabbitmq.utils.Message;
import rabbitmq.utils.MessageSerializer;
import rabbitmq.utils.MessageType;

import java.io.IOException;
import java.util.*;
import java.util.concurrent.CountDownLatch;

public class MiddlewareImpl implements Middleware {

    private final Channel channel;
    private CountDownLatch latch;

    private final String id;

    private int N = 0;
    private int logicalClock = 0;
    private int requestTimestamp = 0;
    private boolean requestingCS = false;
    private final Set<String> pendingNodes = new HashSet<>();
    private final Set<String> activeNodes = new HashSet<>();

    public MiddlewareImpl(final String id) throws Exception {
        this.id = id;

        ConnectionFactory factory = new ConnectionFactory();
        factory.setHost("localhost");
        Connection connection = factory.newConnection();
        this.channel = connection.createChannel();

        channel.exchangeDeclare(EXCHANGE_NAME, BuiltinExchangeType.TOPIC);

        String queue = "QUEUE_" + this.id;
        channel.queueDeclare(queue, false, true, true, null);
        channel.queueBind(queue, EXCHANGE_NAME, ROUTING_BROADCAST);
        channel.queueBind(queue, EXCHANGE_NAME, ROUTING_PRIVATE+this.id);

        DeliverCallback deliverCallback = (consumerTag, delivery) -> {
            Message msg = MessageSerializer.fromBytes(delivery.getBody());
            if (msg.clientId().equals(this.id)) {
                Logger.log(this.id, "SAME ID: IGNORING ~ Received: ["+msg.type()+"] from client-"+msg.clientId()+", t="+ msg.timestamp());
                return;
            } else {
                Logger.log(this.id, "Received: ["+msg.type()+"] from client-"+msg.clientId()+", t="+ msg.timestamp());
            }

            synchronized (this) {
                this.logicalClock = Math.max(this.logicalClock, msg.timestamp());

                switch (msg.type()) {
                    case HELLO -> {
                        var isNewNode = this.activeNodes.add(msg.clientId());
                        this.N = this.activeNodes.size();
                        if (isNewNode) {
                            try {
                                runDiscovery();
                            } catch (InterruptedException e) {
                                e.printStackTrace();
                            }
                        }
                    }
                    case OK -> this.latch.countDown();
                    case REQUEST -> {
                        boolean isMyReqOlder = this.requestTimestamp < msg.timestamp() ||
                                (this.requestTimestamp == msg.timestamp() && this.id.compareTo(msg.clientId()) < 0);
                        if (requestingCS && isMyReqOlder) {
                            pendingNodes.add(msg.clientId());
                            Logger.log(this.id, "Request from client-"+ msg.clientId() + " DEFERRED.");
                        } else {
                            try {
                                send(ROUTING_PRIVATE+msg.clientId(), MessageType.OK);
                            } catch (IOException e) {}
                        }
                    }
                }
            }
        };

        channel.basicConsume(queue, true, deliverCallback, tag -> {});
        runDiscovery();
    }

    private void runDiscovery() throws IOException, InterruptedException {
        synchronized (this) {
            activeNodes.add(this.id);
            this.N = activeNodes.size();
            Logger.log(this.id, "Starting ricart-agrawala for N="+this.N);
        }
        send(ROUTING_BROADCAST, MessageType.HELLO);
    }

    private void send(final String routingKey, final MessageType type) throws IOException {
        channel.basicPublish(
                EXCHANGE_NAME,
                routingKey,
                null,
                MessageSerializer.toBytes(
                        new Message(type, this.id, this.logicalClock)
                )
        );
    }

    @Override
    public void enterCS() throws InterruptedException, IOException {
        if (this.N < 1) return;
        boolean entered = false;
        while (!entered) {
            synchronized (this) {
                this.requestingCS = true;
                this.logicalClock++;
                this.requestTimestamp = this.logicalClock;

                this.latch = new CountDownLatch(this.N - 1);

                try {
                    send(ROUTING_BROADCAST, MessageType.REQUEST);
                    Logger.log(this.id, "Waiting for "+(this.N - 1)+" OK");
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }

            this.latch.await();
            Logger.log(this.id, "now in critical section");
            entered = true;
        }
    }

    @Override
    public synchronized void exitCS() throws InterruptedException {
        Logger.log(this.id, "exiting cs");
        this.requestingCS = false;
        for (String targetNode : this.pendingNodes) {
            try {
                send(ROUTING_PRIVATE+targetNode, MessageType.OK);
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        this.pendingNodes.clear();
    }
}