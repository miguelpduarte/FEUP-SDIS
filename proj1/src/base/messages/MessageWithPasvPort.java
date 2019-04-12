package base.messages;

import base.ProtocolDefinitions;

public class MessageWithPasvPort extends MessageWithChunkNo {
    private final int pasv_port;

    public MessageWithPasvPort(ProtocolDefinitions.MessageType message_type, String version, String sender_id, String file_id, int chunk_no, byte[] message, int msg_length, int crlf_index) throws InvalidMessageFormatException {
        super(message_type, version, sender_id, file_id, chunk_no, message, msg_length, crlf_index);
        pasv_port = parsePort();
    }

    private int parsePort() throws InvalidMessageFormatException {
        // Must find the next CRLF (to get the second line)

        if (this.crlf_index + 4 > this.msg_length) {
            // Message can't have a single digit, space and CRLF so it does not have a PASV_PORT
            throw new InvalidMessageFormatException("Missing PASV_PORT in PASVCHUNK message");
        }

        int end_of_second_line_idx = MessageFactory.getCRLFIndex(this.message, this.msg_length, this.crlf_index + 1);

        if (end_of_second_line_idx == -1) {
            throw new InvalidMessageFormatException("No second line found for expected PASVCHUNK message");
        }

        String pasv_port_raw = new String(this.message, this.crlf_index + 2, end_of_second_line_idx - this.crlf_index - 2 - 1);

        return Integer.parseInt(pasv_port_raw);
    }

    public int getPasvPort() {
        return pasv_port;
    }
}
