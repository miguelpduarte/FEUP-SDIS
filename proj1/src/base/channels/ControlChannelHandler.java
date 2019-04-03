package base.channels;

import base.ProtocolDefinitions;
import base.ThreadManager;
import base.messages.CommonMessage;
import base.messages.MessageFactory;
import base.tasks.Task;
import base.tasks.TaskManager;

import java.io.IOException;

public class ControlChannelHandler extends ChannelHandler {
    public ControlChannelHandler(String hostname, int port) throws IOException {
        super(hostname, port);
    }

    @Override
    protected void handle() {
        final byte[] packet_data = this.packet.getData();
        final int packet_length = this.packet.getLength();


        ThreadManager.getInstance().executeLater(() -> {
            CommonMessage info = MessageFactory.getBasicInfo(packet_data, packet_length);
            if (info == null) {
                System.out.println("MDC: Message couldn't be parsed");
                return;
            }

            if (info.getSenderId().equals(ProtocolDefinitions.SERVER_ID)) {
                // Own Message, ignoring
                return;
            }

            System.out.printf("\t\tMDC: Received message of type %s\n", info.getMessageType().name());

            switch (info.getMessageType()) {
                case PUTCHUNK:
                    break;
                case STORED:
                    handleStored(info);
                    break;
                case GETCHUNK:
                    handleGetchunk(info);
                    break;
                case CHUNK:
                    break;
            }
        });
    }

    private void handleGetchunk(CommonMessage info) {
        System.out.println("Received GETCHUNK messsage");
    }

    private void handleStored(CommonMessage info) {
        Task t = TaskManager.getInstance().getTask(info);
        if (t != null) {
            t.notify(info);
        }
    }
}
