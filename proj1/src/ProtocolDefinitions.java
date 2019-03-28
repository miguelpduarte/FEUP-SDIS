public class ProtocolDefinitions {
    public static String VERSION;
    public static String SERVER_ID;

    public enum MessageType {
        PUTCHUNK,
        STORED,
        GETCHUNK,
        CHUNK
    }

    public static final int[] MESSAGE_DELAYS = {1, 2, 4, 8};
}
