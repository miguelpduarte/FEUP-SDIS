package base.messages;

import base.ProtocolDefinitions;

public class MessageWithChunkSize extends MessageWithReplicationDegree {
    private final int chunk_size;

    public MessageWithChunkSize(ProtocolDefinitions.MessageType message_type, String version, String sender_id, String file_id, int chunk_no, int replication_degree, byte[] message, int msg_length, int crlf_index) throws InvalidMessageFormatException {
        super(message_type, version, sender_id, file_id, chunk_no, replication_degree, message, msg_length, crlf_index);
        chunk_size = parseSize();
    }

    private int parseSize() throws InvalidMessageFormatException {
        // Must find the next CRLF (to get the second line)

        if (this.crlf_index + 4 > this.msg_length) {
            // Message can't have a single digit, space and CRLF so it does not have a PASV_PORT
            throw new InvalidMessageFormatException("Missing CHUNKSIZE in enhanced PUTCHUNK message");
        }

        int end_of_second_line_idx = MessageFactory.getCRLFIndex(this.message, this.msg_length, this.crlf_index + 1);

        if (end_of_second_line_idx == -1) {
            throw new InvalidMessageFormatException("No second line found for expected enhanced PUTCHUNK message");
        }

        String chunk_size_raw = new String(this.message, this.crlf_index + 2, end_of_second_line_idx - this.crlf_index - 2 - 1);

        return Integer.parseInt(chunk_size_raw);
    }

    public int getChunkSize() {
        return chunk_size;
    }
}
