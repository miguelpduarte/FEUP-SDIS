package base.channels;

import base.ProtocolDefinitions;
import base.messages.CommonMessage;
import base.messages.MessageFactory;
import base.ThreadManager;

import java.io.IOException;

public class RestoreChannelHandler extends ChannelHandler {
    public RestoreChannelHandler(String hostname, int port) throws IOException {
        super(hostname, port, "Restore");
    }

    @Override
    protected void handle() {
        final byte[] packet_data = this.packet.getData();

        ThreadManager.getInstance().executeLater(() -> {
            CommonMessage info = MessageFactory.getBasicInfo(packet_data);
            if (info == null) {
                System.out.println("MDR: Message couldn't be parsed");
                return;
            }

            if (info.getSenderId().equals(ProtocolDefinitions.SERVER_ID)) {
                // Own Message, ignoring
                return;
            }

            System.out.printf("\t\tMDR: Received message of type %s\n", info.getMessageType().name());
            /*tasksTask t = TaskManager.getInstance().getTask(info);
            if (t != null) {
                t.notify(info);
            }*/
        });
    }
}
