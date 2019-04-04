package base;

import java.util.concurrent.ThreadLocalRandom;

public class ProtocolDefinitions {
    public final static String CRLF = "\r\n";
    public final static byte CR = 0xD;
    public final static byte LF = 0xA;

    public static String VERSION;
    public static String SERVER_ID;

    public enum MessageType {
        PUTCHUNK,
        STORED,
        GETCHUNK,
        CHUNK,
        DELETE
    }

    public static final int[] MESSAGE_DELAYS = {1, 2, 4, 8};

    private static final int MIN_RANDOM_MESSAGE_DELAY_MILIS = 0;
    private static final int MAX_RANDOM_MESSAGE_DELAY_MILIS = 400;

    public static int getRandomMessageDelayMilis() {
        return ThreadLocalRandom.current().nextInt(MIN_RANDOM_MESSAGE_DELAY_MILIS, MAX_RANDOM_MESSAGE_DELAY_MILIS+1);
    }

    public static String calcChunkHash(String file_id, int chunk_no) {
        return String.format("%s_chk%d", file_id, chunk_no);
    }

    public static final String BACKUP_DIRNAME = "backup";
    public static final String RESTORED_DIRNAME = "restored";

    // 10 for MsgType, 3 for Version, 16 for SenderId, 64 for FileId, 6 for ChunkNo, 1 for ReplicationDeg, 6 for spaces, 4 for CRLF (can be 1 or 2) - rounded up to 128
    public static final int HEADER_MAX_BYTES = 128;
    public static final int CHUNK_MAX_SIZE_BYTES = 64000;
    public static final int MAX_CHUNKS = 1000000;
}
