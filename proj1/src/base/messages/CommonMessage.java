package base.messages;

import base.Keyable;
import base.ProtocolDefinitions;

import java.util.Arrays;

public class CommonMessage implements Keyable {
    private final ProtocolDefinitions.MessageType message_type;
    private final String version;
    private final String sender_id;
    private final String file_id;
    protected final int crlf_index;
    protected final byte[] message;
    protected final int msg_length;
    private final int last_crlf_index;

    public CommonMessage(ProtocolDefinitions.MessageType message_type, String version, String sender_id, String file_id, byte[] message, int msg_length, int crlf_index) {
        this.message_type = message_type;
        this.version = version;
        this.sender_id = sender_id;
        this.file_id = file_id;
        this.crlf_index = crlf_index;
        this.message = message;
        this.msg_length = msg_length;
        this.last_crlf_index = this.getLastCRLFIndex();
    }

    private int getLastCRLFIndex() {
        // Must find the first two consecutive CRLFs
        // (this ensures that CRLF's in the body don't break everything - if searching for the last one it could break)

        if (this.crlf_index + 2 > this.msg_length) {
            // Message only has one CRLF, invalid format
            return -1;
        }

        // Must start at the already found CRLF in case that there are no additional lines (protocol version 1.0)
        for (int i = this.crlf_index; i < this.msg_length - 3; ++i) {
            if (this.message[i] == ProtocolDefinitions.CR && this.message[i + 1] == ProtocolDefinitions.LF && this.message[i + 2] == ProtocolDefinitions.CR && this.message[i + 3] == ProtocolDefinitions.LF) {
                return i + 2;
            }
        }

        return -1;
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

    public int getCrlfIndex() {
        return crlf_index;
    }

    public byte[] getMessage() {
        return message;
    }

    public String toKey() {
        return message_type.name() + file_id;
    }

    /**
     * For usage in reading the body of the message for storing in PUTCHUNK and CHUNK messages for example
     *
     * @return The message body
     */
    public byte[] getBody() throws InvalidMessageFormatException {
        if (this.last_crlf_index == -1 || this.last_crlf_index == this.crlf_index) {
            throw new InvalidMessageFormatException("Missing Body");
        }

        return Arrays.copyOfRange(this.message, this.last_crlf_index + 2, this.msg_length);
    }

}
