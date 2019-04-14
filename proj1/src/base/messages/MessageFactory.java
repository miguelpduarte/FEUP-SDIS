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

        return split_file_contents;
    }

    public static byte[] createGetchunkMessage(String file_id, int chunk_no) {
        StringBuilder sb = new StringBuilder();

        sb.append("GETCHUNK").append(" ");
        sb.append(ProtocolDefinitions.INITIAL_VERSION).append(" ");
        sb.append(ProtocolDefinitions.SERVER_ID).append(" ");
        sb.append(file_id).append(" ");
        sb.append(chunk_no).append(" ");
        sb.append(ProtocolDefinitions.CRLF).append(ProtocolDefinitions.CRLF);

        return sb.toString().getBytes();
    }

    public static byte[] createEnhancedGetchunkMessage(String file_id, int chunk_no, int port) {
        StringBuilder sb = new StringBuilder();

        sb.append("GETCHUNK").append(" ");
        sb.append(ProtocolDefinitions.VERSION).append(" ");
        sb.append(ProtocolDefinitions.SERVER_ID).append(" ");
        sb.append(file_id).append(" ");
        sb.append(chunk_no).append(" ");
        sb.append(ProtocolDefinitions.CRLF);
        sb.append(port).append(" ");
        sb.append(ProtocolDefinitions.CRLF).append(ProtocolDefinitions.CRLF);

        return sb.toString().getBytes();
    }

    public static byte[] createPutchunkMessage(String file_id, int chunk_no, int replication_degree, byte[] body) {
        return createPutchunkMessage(file_id, chunk_no, replication_degree, body, true);
    }

    public static byte[] createPutchunkMessage(String file_id, int chunk_no, int replication_degree, byte[] body, boolean is_basic) {
        StringBuilder sb = new StringBuilder();

        sb.append("PUTCHUNK").append(" ");
        sb.append(is_basic ? ProtocolDefinitions.INITIAL_VERSION : ProtocolDefinitions.VERSION).append(" ");
        sb.append(ProtocolDefinitions.SERVER_ID).append(" ");
        sb.append(file_id).append(" ");
        sb.append(chunk_no).append(" ");
        sb.append(replication_degree).append(" ");

        if (!is_basic) {
            sb.append(ProtocolDefinitions.CRLF);
            sb.append(body.length).append(" ");
        }

        sb.append(ProtocolDefinitions.CRLF).append(ProtocolDefinitions.CRLF);

        byte[] header = sb.toString().getBytes();

        if (is_basic) {
            byte[] output = new byte[header.length + body.length];
            System.arraycopy(header, 0, output, 0, header.length);
            System.arraycopy(body, 0, output, header.length, body.length);
            return output;
        } else {
            // Enhanced version, does not send message body
            return header;
        }
    }

    public static String filenameEncode(String file_name) {
        final String text_to_encode = file_name + System.currentTimeMillis();

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
        return createStoredMessage(file_id, chunk_no, true);
    }

    public static byte[] createStoredMessage(String file_id, int chunk_no, boolean is_basic) {
        // NOTE: The argument is file_id and not file_name, thus it is considered pre-processed (via filenameEncode)
        // This consideration is due to the fact that this message is always created as a reply so the original filename does not even exist

        StringBuilder sb = new StringBuilder();

        sb.append("STORED").append(" ");
        sb.append(is_basic ? ProtocolDefinitions.INITIAL_VERSION : ProtocolDefinitions.VERSION).append(" ");
        sb.append(ProtocolDefinitions.SERVER_ID).append(" ");
        sb.append(file_id).append(" ");
        sb.append(chunk_no).append(" ");
        sb.append(ProtocolDefinitions.CRLF).append(ProtocolDefinitions.CRLF);

        return sb.toString().getBytes();
    }

    public static byte[] createChunkMessage(String file_id, int chunk_no, byte[] body) {
        return createChunkMessage(file_id, chunk_no, body, true);
    }

    public static byte[] createChunkMessage(String file_id, int chunk_no, byte[] body, boolean is_basic) {
        StringBuilder sb = new StringBuilder();

        sb.append("CHUNK").append(" ");
        sb.append(is_basic ? ProtocolDefinitions.INITIAL_VERSION : ProtocolDefinitions.VERSION).append(" ");
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

    public static byte[] createDeleteMessage(String file_id) {
        return createDeleteMessage(file_id, true);
    }

    public static byte[] createDeleteMessage(String file_id, boolean is_basic) {
        StringBuilder sb = new StringBuilder();

        sb.append("DELETE").append(" ");
        sb.append(is_basic ? ProtocolDefinitions.INITIAL_VERSION : ProtocolDefinitions.VERSION).append(" ");
        sb.append(ProtocolDefinitions.SERVER_ID).append(" ");
        sb.append(file_id).append(" ");
        sb.append(ProtocolDefinitions.CRLF).append(ProtocolDefinitions.CRLF);

        return sb.toString().getBytes();
    }

    public static byte[] createRemovedMessage(String file_id, int chunk_no) {
        return createRemovedMessage(file_id, chunk_no, true);
    }

    public static byte[] createRemovedMessage(String file_id, int chunk_no, boolean is_basic) {
        // NOTE: The argument is file_id and not file_name, thus it is considered pre-processed (via filenameEncode)
        // This consideration is due to the fact that this message is always created as a reply so the original filename does not even exist

        StringBuilder sb = new StringBuilder();

        sb.append("REMOVED").append(" ");
        sb.append(is_basic ? ProtocolDefinitions.INITIAL_VERSION : ProtocolDefinitions.VERSION).append(" ");
        sb.append(ProtocolDefinitions.SERVER_ID).append(" ");
        sb.append(file_id).append(" ");
        sb.append(chunk_no).append(" ");
        sb.append(ProtocolDefinitions.CRLF).append(ProtocolDefinitions.CRLF);

        return sb.toString().getBytes();
    }

    public static byte[] createCanStoreMessage(String file_id, int chunk_no, int pasv_port) {
        StringBuilder sb = new StringBuilder();

        sb.append("CANSTORE").append(" ");
        sb.append(ProtocolDefinitions.VERSION).append(" ");
        sb.append(ProtocolDefinitions.SERVER_ID).append(" ");
        sb.append(file_id).append(" ");
        sb.append(chunk_no).append(" ");
        sb.append(ProtocolDefinitions.CRLF);
        sb.append(pasv_port).append(" ");
        sb.append(ProtocolDefinitions.CRLF).append(ProtocolDefinitions.CRLF);

        return sb.toString().getBytes();
    }

    public static byte[] createQueryDeletedMessage(int pasv_port) {
        StringBuilder sb = new StringBuilder();

        sb.append("QUERYDELETED").append(" ");
        sb.append(ProtocolDefinitions.VERSION).append(" ");
        sb.append(ProtocolDefinitions.SERVER_ID).append(" ");
        sb.append(ProtocolDefinitions.CRLF);
        sb.append(pasv_port).append(" ");
        sb.append(ProtocolDefinitions.CRLF).append(ProtocolDefinitions.CRLF);

        return sb.toString().getBytes();
    }

    public static CommonMessage getBasicInfo(byte[] message, int msg_length) {
        int crlf_index = getCRLFIndex(message, message.length);
        if (crlf_index == -1) {
            return null;
        }

        // The "header" mentioned below is simply the first line, according to protocol 1.0

        String message_header = new String(message, 0, crlf_index);
        String[] header_fields = message_header.split(" ");

        if (header_fields.length < 2) {
            return null;
        }

        try {
            ProtocolDefinitions.MessageType msg_type = ProtocolDefinitions.MessageType.valueOf(header_fields[0]);

            if (!header_fields[1].equals(ProtocolDefinitions.INITIAL_VERSION) && !header_fields[1].equals(ProtocolDefinitions.VERSION)) {
                return null;
            }

            switch (msg_type) {
                // with chunk no and replication deg
                case PUTCHUNK:
                    if (header_fields[1].equals(ProtocolDefinitions.INITIAL_VERSION)) {
                        return new MessageWithReplicationDegree(
                                msg_type,
                                header_fields[1],
                                header_fields[2],
                                header_fields[3],
                                Integer.parseInt(header_fields[4]),
                                Integer.parseInt(header_fields[5]),
                                message,
                                msg_length,
                                crlf_index
                        );
                    } else {
                        return new MessageWithChunkSize(
                                msg_type,
                                header_fields[1],
                                header_fields[2],
                                header_fields[3],
                                Integer.parseInt(header_fields[4]),
                                Integer.parseInt(header_fields[5]),
                                message,
                                msg_length,
                                crlf_index
                        );
                    }
                case GETCHUNK:
                    if (header_fields[1].equals(ProtocolDefinitions.INITIAL_VERSION)) {
                        return new MessageWithChunkNo(
                                msg_type,
                                header_fields[1],
                                header_fields[2],
                                header_fields[3],
                                Integer.parseInt(header_fields[4]),
                                message,
                                msg_length,
                                crlf_index
                        );
                    } else {
                        return new MessageWithPasvPort(
                                msg_type,
                                header_fields[1],
                                header_fields[2],
                                header_fields[3],
                                Integer.parseInt(header_fields[4]),
                                message,
                                msg_length,
                                crlf_index
                        );
                    }
                // with chunk no and without replication deg
                case STORED:
                case CHUNK:
                case REMOVED:
                    return new MessageWithChunkNo(
                            msg_type,
                            header_fields[1],
                            header_fields[2],
                            header_fields[3],
                            Integer.parseInt(header_fields[4]),
                            message,
                            msg_length,
                            crlf_index
                    );
                // without chunk no and without replication deg
                case DELETE:
                    return new CommonMessage(
                            msg_type,
                            header_fields[1],
                            header_fields[2],
                            header_fields[3],
                            message,
                            msg_length,
                            crlf_index
                    );
                case CANSTORE:
                    return new MessageWithPasvPort(
                            msg_type,
                            header_fields[1],
                            header_fields[2],
                            header_fields[3],
                            Integer.parseInt(header_fields[4]),
                            message,
                            msg_length,
                            crlf_index
                    );
                case QUERYDELETED:
                    return new QueryDeletedMessage(
                            msg_type,
                            header_fields[1],
                            header_fields[2],
                            message,
                            msg_length,
                            crlf_index
                    );
                // unexpected message type
                default:
                    return null;
            }

        } catch (IllegalArgumentException ignored) {
            return null;
        } catch (InvalidMessageFormatException e) {
            e.printStackTrace();
            return null;
        }
    }

    public static int getCRLFIndex(byte[] array, int array_len, int start_idx) {
        for (int i = start_idx; i < array_len - 1; ++i) {
            if (array[i] == ProtocolDefinitions.CR && array[i + 1] == ProtocolDefinitions.LF) {
                return i;
            }
        }

        return -1;
    }

    public static int getCRLFIndex(byte[] array, int array_len) {
        return getCRLFIndex(array, array_len, 0);
    }
}
