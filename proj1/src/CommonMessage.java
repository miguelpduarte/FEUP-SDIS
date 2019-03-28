public class CommonMessage implements Keyable {
    private final ProtocolDefinitions.MessageType message_type;
    private final String version;
    private final String sender_id;
    private final String file_id;
    private final int chunk_no;
    private final int crlf_index;
    private final byte[] message;

    public CommonMessage(ProtocolDefinitions.MessageType message_type, String version, String sender_id, String file_id, int chunk_no, int crlf_index, byte[] message) {
        this.message_type = message_type;
        this.version = version;
        this.sender_id = sender_id;
        this.file_id = file_id;
        this.chunk_no = chunk_no;
        this.crlf_index = crlf_index;
        this.message = message;
    }

    public ProtocolDefinitions.MessageType getMessageType() {
        return message_type;
    }

    public String getVersion() {
        return version;
    }

    public String getSenderId() {
        return sender_id;
    }

    public String getFileId() {
        return file_id;
    }

    public int getChunkNo() {
        return chunk_no;
    }

    public int getCrlfIndex() {
        return crlf_index;
    }

    public byte[] getMessage() {
        return message;
    }

    public String toKey() {
        return message_type.name() + file_id + chunk_no;
    }
}
