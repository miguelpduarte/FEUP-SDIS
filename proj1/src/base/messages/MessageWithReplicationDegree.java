package base.messages;

import base.ProtocolDefinitions;

public class MessageWithReplicationDegree extends MessageWithChunkNo {
    private final int replication_degree;

    public MessageWithReplicationDegree(ProtocolDefinitions.MessageType message_type, String version, String sender_id, String file_id, int crlf_index, int chunk_no, int replication_degree, byte[] message, int msg_length) {
        super(message_type, version, sender_id, file_id, crlf_index, chunk_no, message, msg_length);
        this.replication_degree = replication_degree;
    }

    public int getReplicationDegree() {
        return replication_degree;
    }
}
