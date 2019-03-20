import java.io.IOException;

public class RestoreChannelHandler extends ChannelHandler {
    public RestoreChannelHandler(String hostname, int port) throws IOException {
        super(hostname, port, "Restore");
    }

    @Override
    protected void handle() {

    }
}
