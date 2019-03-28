import java.io.IOException;

public class RestoreChannelHandler extends ChannelHandler {
    public RestoreChannelHandler(String hostname, int port) throws IOException {
        super(hostname, port, "Restore");
    }

    @Override
    protected void handle() {
        ThreadManager.getInstance().executeLater(() -> {
            CommonMessage info = MessageFactory.getBasicInfo(this.packet.getData());
            if (info == null) {
                System.out.println("MDR: Message couldn't be parsed");
                return;
            }

            System.out.printf("MDR: Received message of type %s\n", info.getMessageType().name());
            /*Task t = TaskManager.getInstance().getTask(info);
            if (t != null) {
                t.notify(info);
            }*/
        });
    }
}
