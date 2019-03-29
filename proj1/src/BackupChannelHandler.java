import java.io.IOException;

public class BackupChannelHandler extends ChannelHandler {
    public BackupChannelHandler(String hostname, int port) throws IOException {
        super(hostname, port, "Backup");
    }

    @Override
    protected void handle() {
        final byte[] packet_data = this.packet.getData();

        ThreadManager.getInstance().executeLater(() -> {
            System.out.println("\t\tMDB: Starting message handling");
            CommonMessage info = MessageFactory.getBasicInfo(packet_data);
            if (info == null) {
                System.out.println("MDB: Message couldn't be parsed");
                return;
            }

            System.out.printf("\t\tMDB: Received message of type %s\n", info.getMessageType().name());
            /*Task t = TaskManager.getInstance().getTask(info);
            if (t != null) {
                t.notify(info);
            }*/
        });
    }
}
