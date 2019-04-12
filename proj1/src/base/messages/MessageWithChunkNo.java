package base.messages;

import base.ProtocolDefinitions;

public class MessageWithChunkNo extends CommonMessage {
    private final int chunk_no;

    public MessageWithChunkNo(ProtocolDefinitions.MessageType message_type, String version, String sender_id, String file_id, int chunk_no, byte[] message, int msg_length, int crlf_index) {
        super(message_type, version, sender_id, file_id, message, msg_length, crlf_index);
        this.chunk_no = chunk_no;
    }

    public int getChunkNo() {
        return chunk_no;
    }

    @Override
    public String toKey() {
        return super.toKey() + this.chunk_no;
    }
}
