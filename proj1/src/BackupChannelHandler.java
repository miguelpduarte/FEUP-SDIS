import java.io.IOException;

public class BackupChannelHandler extends ChannelHandler {
    public BackupChannelHandler(String hostname, int port) throws IOException {
        super(hostname, port, "Backup");
    }

    @Override
    protected void handle() {
        ThreadManager.getInstance().executeLater(() -> {
            CommonMessage info = MessageFactory.getBasicInfo(this.packet.getData());
            if (info == null) {
                System.out.println("MDB: Message couldn't be parsed");
                return;
            }

            System.out.printf("MDB: Received message of type %s\n", info.getMessageType().name());
            /*Task t = TaskManager.getInstance().getTask(info);
            if (t != null) {
                t.notify(info);
            }*/
        });
    }
}
