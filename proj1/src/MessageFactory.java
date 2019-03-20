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

        System.out.println("ripini " + Arrays.toString(output));

        return output;
    }

    private static String filenameEncode(String file_name) {
        return file_name;
    }
}
