import java.io.IOException;

public class BackupChannelHandler extends ChannelHandler {
    public BackupChannelHandler(String hostname, int port) throws IOException {
        super(hostname, port, "Backup");
    }

    @Override
    protected void handle() {
        ThreadManager.getInstance().executeLater(new MDBMesssageHandler(this.packet.getData()));
    }
}
