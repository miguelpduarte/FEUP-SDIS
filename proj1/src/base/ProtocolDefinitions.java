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
        CHUNK
    }

    public static final int[] MESSAGE_DELAYS = {1, 2, 4, 8};

    public static final int MIN_RANDOM_MESSAGE_DELAY_MILIS = 0;
    public static final int MAX_RANDOM_MESSAGE_DELAY_MILIS = 400;

    public static int getRandomMessageDelayMilis() {
        return ThreadLocalRandom.current().nextInt(MIN_RANDOM_MESSAGE_DELAY_MILIS, MAX_RANDOM_MESSAGE_DELAY_MILIS+1);
    }
}
