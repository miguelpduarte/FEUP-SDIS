package base.messages;

import base.ProtocolDefinitions;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;

public class MessageFactory {

    public static byte[] createPutchunkMessage(String file_name, int chunk_no, int replication_degree, byte[] body) {
        StringBuilder sb = new StringBuilder();

        sb.append("PUTCHUNK").append(" ");
        sb.append(ProtocolDefinitions.VERSION).append(" ");
        sb.append(ProtocolDefinitions.SERVER_ID).append(" ");
        sb.append(filenameEncode(file_name)).append(" ");
        sb.append(chunk_no).append(" ");
        sb.append(replication_degree).append(" ");
        sb.append(ProtocolDefinitions.CRLF).append(ProtocolDefinitions.CRLF);

        byte[] header = sb.toString().getBytes();

        byte[] output = new byte[header.length + body.length];
        System.arraycopy(header, 0, output, 0, header.length);
        System.arraycopy(body, 0, output, header.length, body.length);

        return output;
    }

    public static String filenameEncode(String file_name) {
        MessageDigest digest;
        byte[] hash = new byte[0];
        try {
            digest = MessageDigest.getInstance("SHA-256");
            hash = digest.digest(file_name.getBytes(StandardCharsets.UTF_8));
        } catch (NoSuchAlgorithmException ignored) {
        }

        byte[] processed_hash = new byte[64];

        // why not < 64 and += 2?
        for (int i = 0; i < 32; i++) {
            processed_hash[2 * i] = (byte) ((hash[i] & 0xF0) >> 4);
            processed_hash[(2 * i) + 1] = (byte) (hash[i] & 0x0F);
        }

        StringBuilder sb = new StringBuilder();

        for (byte b : processed_hash) {
            sb.append(String.format("%x", b));
        }

        return sb.toString();
    }

    public static byte[] createStoredMessage(String file_id, int chunk_no) {
        // NOTE: The argument is file_id and not file_name, thus it is considered pre-processed (via filenameEncode)
        // This consideration is due to the fact that this message is always created as a reply so the original filename does not even exist

        StringBuilder sb = new StringBuilder();

        sb.append("STORED").append(" ");
        sb.append(ProtocolDefinitions.VERSION).append(" ");
        sb.append(ProtocolDefinitions.SERVER_ID).append(" ");
        sb.append(file_id).append(" ");
        sb.append(chunk_no).append(" ");
        sb.append(ProtocolDefinitions.CRLF).append(ProtocolDefinitions.CRLF);

        return sb.toString().getBytes();
    }

    public static CommonMessage getBasicInfo(byte[] message) {
        int crlf_index = getCRLFIndex(message);
        if (crlf_index == -1) {
            return null;
        }

        String message_header = new String(message, 0, crlf_index);
        String[] header_fields = message_header.split(" ");

        return new CommonMessage(
                ProtocolDefinitions.MessageType.valueOf(header_fields[0]),
                header_fields[1],
                header_fields[2],
                header_fields[3],
                Integer.parseInt(header_fields[4]),
                crlf_index,
                message
        );
    }

    private static int getCRLFIndex(byte[] array) {
        for (int i = 0; i < array.length - 1; ++i) {
            if (array[i] == ProtocolDefinitions.CR && array[i + 1] == ProtocolDefinitions.LF) {
                return i;
            }
        }

        return -1;
    }
}
