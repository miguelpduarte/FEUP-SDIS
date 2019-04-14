package base.messages;

import base.ProtocolDefinitions;

public class QueryDeletedMessage extends CommonMessage {
    private final int port;

    public QueryDeletedMessage(ProtocolDefinitions.MessageType message_type, String version, String sender_id, byte[] message, int msg_length, int crlf_index) throws InvalidMessageFormatException {
        super(message_type, version, sender_id, "", message, msg_length, crlf_index);
        this.port = parsePort();
    }

    private int parsePort() throws InvalidMessageFormatException {
        // Must find the next CRLF (to get the second line)

        if (this.crlf_index + 4 > this.msg_length) {
            // Message can't have a single digit, space and CRLF so it does not have a PASV_PORT
            throw new InvalidMessageFormatException("Missing PORT in QUERYDELETE message");
        }

        int end_of_second_line_idx = MessageFactory.getCRLFIndex(this.message, this.msg_length, this.crlf_index + 1);

        if (end_of_second_line_idx == -1) {
            throw new InvalidMessageFormatException("No second line found for expected QUERYDELETE message");
        }

        String pasv_port_raw = new String(this.message, this.crlf_index + 2, end_of_second_line_idx - this.crlf_index - 2 - 1);

        return Integer.parseInt(pasv_port_raw);
    }

    public int getPort() {
        return port;
    }
}