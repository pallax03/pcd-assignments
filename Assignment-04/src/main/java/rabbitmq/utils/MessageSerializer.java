package rabbitmq.utils;

import com.fasterxml.jackson.databind.ObjectMapper;

public class MessageSerializer {
    private static final ObjectMapper mapper = new ObjectMapper();

    public static byte[] toBytes(Message message) {
        try {
            return mapper.writeValueAsBytes(message);
        } catch (Exception e) {
            throw new RuntimeException("error serializing JSON", e);
        }
    }

    public static Message fromBytes(byte[] body) {
        try {
            return mapper.readValue(body, Message.class);
        } catch (Exception e) {
            throw new RuntimeException("error deserializing JSON", e);
        }
    }
}