package base.messages;

import base.FileTooLargeException;
import base.ProtocolDefinitions;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.Arrays;

public class MessageFactory {

    public static byte[][] splitFileContents(byte[] file_data) throws FileTooLargeException {
        int num_chunks = (int) Math.ceil((float) file_data.length / ProtocolDefinitions.CHUNK_MAX_SIZE_BYTES);
        if (num_chunks > ProtocolDefinitions.MAX_CHUNKS) {
            throw new FileTooLargeException();
        }

        byte needs_empty_chunk = 0;

        if (file_data.length % ProtocolDefinitions.CHUNK_MAX_SIZE_BYTES == 0) {
            needs_empty_chunk = 1;
        }

        byte[][] split_file_contents = new byte[num_chunks + needs_empty_chunk][];

        int current_byte = 0;
        int chunk_size;

        for (int i = 0; i < num_chunks; ++i) {
            chunk_size = Math.min(ProtocolDefinitions.CHUNK_MAX_SIZE_BYTES, file_data.length - current_byte);

            split_file_contents[i] = Arrays.copyOfRange(file_data, current_byte, current_byte + chunk_size
            );

            current_byte += chunk_size;
        }

        if (needs_empty_chunk == 1) {
            split_file_contents[num_chunks] = new byte[0];
        }

        System.out.println("needs_empty_chunk = " + needs_empty_chunk);

        return split_file_contents;
    }

    public static byte[] createGetchunkTask(String file_id, int chunk_no) {
        StringBuilder sb = new StringBuilder();

        sb.append("GETCHUNK").append(" ");
        sb.append(ProtocolDefinitions.VERSION).append(" ");
        sb.append(ProtocolDefinitions.SERVER_ID).append(" ");
        sb.append(file_id).append(" ");
        sb.append(chunk_no).append(" ");
        sb.append(ProtocolDefinitions.CRLF).append(ProtocolDefinitions.CRLF);

        return sb.toString().getBytes();
    }

    public static byte[] createPutchunkMessage(String file_id, int chunk_no, int replication_degree, byte[] body) {
        StringBuilder sb = new StringBuilder();

        sb.append("PUTCHUNK").append(" ");
        sb.append(ProtocolDefinitions.VERSION).append(" ");
        sb.append(ProtocolDefinitions.SERVER_ID).append(" ");
        sb.append(file_id).append(" ");
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
        final String text_to_encode = file_name + ProtocolDefinitions.SERVER_ID;

        MessageDigest digest;
        byte[] hash = new byte[0];
        try {
            digest = MessageDigest.getInstance("SHA-256");
            hash = digest.digest(text_to_encode.getBytes(StandardCharsets.UTF_8));
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

    public static byte[] createChunkMessage(String file_id, int chunk_no, byte[] body) {
        StringBuilder sb = new StringBuilder();

        sb.append("CHUNK").append(" ");
        sb.append(ProtocolDefinitions.VERSION).append(" ");
        sb.append(ProtocolDefinitions.SERVER_ID).append(" ");
        sb.append(file_id).append(" ");
        sb.append(chunk_no).append(" ");
        sb.append(ProtocolDefinitions.CRLF).append(ProtocolDefinitions.CRLF);

        byte[] header = sb.toString().getBytes();

        byte[] output = new byte[header.length + body.length];
        System.arraycopy(header, 0, output, 0, header.length);
        System.arraycopy(body, 0, output, header.length, body.length);

        return output;
    }

    public static CommonMessage getBasicInfo(byte[] message, int msg_length) {
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
                message,
                msg_length
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
