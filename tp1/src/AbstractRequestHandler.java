import java.net.DatagramPacket;

public abstract class AbstractRequestHandler {
    private final int REQUEST_MAX_SIZE;

    protected AbstractRequestHandler(int request_max_size) {
        REQUEST_MAX_SIZE = request_max_size;
    }

    abstract byte[] handleRequest(DatagramPacket received_packet);

    public int getRequestMaxSize() {
        return REQUEST_MAX_SIZE;
    }
}
