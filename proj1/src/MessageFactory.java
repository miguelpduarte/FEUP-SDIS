import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.Arrays;

public class MessageFactory {
    private final static String CRLF = "\r\n";

    public static byte[] createPutchunkMessage(String file_name, int chunk_no, int replication_degree, byte[] body) {
        StringBuilder sb = new StringBuilder();

        sb.append("PUTCHUNK").append(" ");
        sb.append(ProtocolDefinitions.VERSION).append(" ");
        sb.append(ProtocolDefinitions.SERVER_ID).append(" ");
        sb.append(filenameEncode(file_name)).append(" ");
        sb.append(chunk_no).append(" ");
        sb.append(replication_degree).append(" ");
        sb.append(CRLF).append(CRLF);

        byte[] header = sb.toString().getBytes();

        byte[] output = new byte[header.length + body.length];
        System.arraycopy(header, 0, output, 0, header.length);
        System.arraycopy(body, 0, output, header.length, body.length);

        return output;
    }

    private static String filenameEncode(String file_name) {
        MessageDigest digest;
        byte[] hash = new byte[0];
        try {
            digest = MessageDigest.getInstance("SHA-256");
            hash = digest.digest(file_name.getBytes(StandardCharsets.UTF_8));
        } catch (NoSuchAlgorithmException ignored) {
        }

        byte[] processed_hash = new byte[64];

        for (int i = 0; i < 32; i++) {
            processed_hash[2*i] = (byte) ((hash[i] & 0xF0) >> 4);
            processed_hash[(2*i)+1] = (byte) (hash[i] & 0x0F);
        }

        return new String(processed_hash, 0, processed_hash.length);
    }
}
