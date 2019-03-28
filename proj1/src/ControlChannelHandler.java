import java.io.IOException;

public class ControlChannelHandler extends ChannelHandler {
    public ControlChannelHandler(String hostname, int port) throws IOException {
        super(hostname, port, "Control");
    }

    @Override
    protected void handle() {
        ThreadManager.getInstance().executeLater(() -> {
            CommonMessage info = MessageFactory.getBasicInfo(this.packet.getData());
            if (info == null) {
                System.out.println("MDC: Message couldn't be parsed");
                return;
            }

            System.out.printf("MDC: Received message of type %s\n", info.getMessageType().name());
            Task t = TaskManager.getInstance().getTask(info);
            if (t != null) {
                t.notify(info);
            }
        });
    }
}
