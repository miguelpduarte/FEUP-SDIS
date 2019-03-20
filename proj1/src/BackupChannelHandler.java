import java.io.IOException;

public class BackupChannelHandler extends ChannelHandler {
    public BackupChannelHandler(String hostname, int port) throws IOException {
        super(hostname, port, "Backup");
    }

    @Override
    protected void handle() {
        System.out.println(new String(this.packet.getData(), 0, this.packet.getLength()));
    }
}
