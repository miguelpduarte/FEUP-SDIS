package base.channels;

import base.ProtocolDefinitions;
import base.messages.CommonMessage;
import base.messages.MessageFactory;
import base.ThreadManager;
import base.messages.MessageWithChunkNo;
import base.protocol.task.extendable.Task;
import base.protocol.task.TaskManager;

import java.io.IOException;
import java.net.DatagramPacket;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.Future;

public class RestoreChannelHandler extends ChannelHandler {
    public RestoreChannelHandler(String hostname, int port) throws IOException {
        super(hostname, port);
    }

    private final ConcurrentHashMap<String, Future> chunkMessagesToSend = new ConcurrentHashMap<String, Future>();

    public void registerChunkToSend(String file_id, int chunk_no, Future f) {
        this.chunkMessagesToSend.put(ProtocolDefinitions.calcChunkHash(file_id, chunk_no), f);
    }

    @Override
    protected void handle(DatagramPacket dp) {
        final byte[] packet_data = dp.getData();
        final int packet_length = dp.getLength();


        ThreadManager.getInstance().executeLater(() -> {
            CommonMessage info = MessageFactory.getBasicInfo(packet_data, packet_length);
            if (info == null) {
                System.out.println("MDR: Message couldn't be parsed");
                return;
            }

            if (info.getSenderId().equals(ProtocolDefinitions.SERVER_ID)) {
                // Own Message, ignoring
                return;
            }

            System.out.printf("\t\tMDR: Received message of type %s\n", info.getMessageType().name());
            switch (info.getMessageType()) {
                case PUTCHUNK:
                    break;
                case STORED:
                    break;
                case GETCHUNK:
                    break;
                case CHUNK:
                    handleChunk(info);
                    break;
            }
        });
    }

    private void handleChunk(CommonMessage info) {
        stopRepeatedChunkSending(info);

        Task t = TaskManager.getInstance().getTask(info);
        if (t != null) {
            t.notify(info);
        }
    }

    private void stopRepeatedChunkSending(CommonMessage info) {
        final String chunk_hash = ProtocolDefinitions.calcChunkHash(info.getFileId(), ((MessageWithChunkNo)info).getChunkNo());
        Future f = this.chunkMessagesToSend.get(chunk_hash);
        if (f == null) {
            return;
        }
        System.out.println("REPEATED CHUNK DETECTED - STOPPING THE SENDING");
        f.cancel(true);
        this.chunkMessagesToSend.remove(chunk_hash);
    }
}
