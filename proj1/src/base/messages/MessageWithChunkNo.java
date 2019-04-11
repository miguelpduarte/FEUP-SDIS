package base.messages;

import base.ProtocolDefinitions;

public class MessageWithChunkNo extends CommonMessage {
    private final int chunk_no;

    public MessageWithChunkNo(ProtocolDefinitions.MessageType message_type, String version, String sender_id, String file_id, int chunk_no, int crlf_index, byte[] message, int msg_length) {
        super(message_type, version, sender_id, file_id, crlf_index, message, msg_length);
        this.chunk_no = chunk_no;
    }

    public int getChunkNo() {
        return chunk_no;
    }

}
