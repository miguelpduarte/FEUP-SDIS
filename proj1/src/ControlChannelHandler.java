import java.io.IOException;

public class ControlChannelHandler extends ChannelHandler {
    public ControlChannelHandler(String hostname, int port) throws IOException {
        super(hostname, port, "Control");
    }

    @Override
    protected void handle() {

    }
}
