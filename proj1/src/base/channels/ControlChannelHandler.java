package base.channels;

import base.ProtocolDefinitions;
import base.ThreadManager;
import base.messages.CommonMessage;
import base.messages.MessageFactory;
import base.storage.StorageManager;
import base.tasks.Task;
import base.tasks.TaskManager;

import java.io.IOException;
import java.util.concurrent.Future;

public class ControlChannelHandler extends ChannelHandler {
    public ControlChannelHandler(String hostname, int port) throws IOException {
        super(hostname, port);
    }

    @Override
    protected void handle() {
        final byte[] packet_data = this.packet.getData();
        final int packet_length = this.packet.getLength();


        ThreadManager.getInstance().executeLater(() -> {
            try {
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
                    case STORED:
                        handleStored(info);
                        break;
                    case GETCHUNK:
                        handleGetchunk(info);
                        break;
                    case DELETE:
                        handleDelete(info);
                        break;
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
        });
    }

    private void handleDelete(CommonMessage info) {
        StorageManager.getInstance().removeChunkIfStored(info.getFileId());
    }

    private void handleGetchunk(CommonMessage info) {
        byte[] chunk_data = StorageManager.getInstance().getStoredChunk(info.getFileId(), info.getChunkNo());
        if (chunk_data == null) {
            return;
        }

        final byte[] chunk_message = MessageFactory.createChunkMessage(info.getFileId(), info.getChunkNo(), chunk_data);
        Future f = ThreadManager.getInstance().executeLaterMilis(() -> {
            try {
                System.out.print("Broadcasting CHUNK\n");
                ChannelManager.getInstance().getRestore().broadcast(chunk_message);
            } catch (IOException e) {
                e.printStackTrace();
            }
        }, ProtocolDefinitions.getRandomMessageDelayMilis());

        ChannelManager.getInstance().getRestore().registerChunkToSend(info.getFileId(), info.getChunkNo(), f);
    }

    private void handleStored(CommonMessage info) {
        Task t = TaskManager.getInstance().getTask(info);
        if (t != null) {
            t.notify(info);
        }
    }
}
