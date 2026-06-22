package rabbitmq.utils;

public record Message(MessageType type, String clientId, int timestamp) {
}
