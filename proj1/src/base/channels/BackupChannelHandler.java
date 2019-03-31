package base.channels;

import base.ProtocolDefinitions;
import base.ThreadManager;
import base.messages.CommonMessage;
import base.messages.InvalidMessageFormatException;
import base.messages.MessageFactory;
import base.storage.StorageManager;

import java.io.IOException;

public class BackupChannelHandler extends ChannelHandler {
    public BackupChannelHandler(String hostname, int port) throws IOException {
        super(hostname, port, "Backup");
    }

    @Override
    protected void handle() {
        final byte[] packet_data = this.packet.getData();

        ThreadManager.getInstance().executeLater(() -> {
            CommonMessage info = MessageFactory.getBasicInfo(packet_data);
            if (info == null) {
                System.out.println("MDB: Message couldn't be parsed");
                return;
            }

            if (info.getSenderId().equals(ProtocolDefinitions.SERVER_ID)) {
                // Own Message, ignoring
                return;
            }

            System.out.printf("\t\tMDB: Received message of type %s\n", info.getMessageType().name());

            // TODO: Refactor later
            // TODO: Add size constraints, etc
            // Was thinking of creating a separate task that would run itself later with executeLater but it seemed pointless because this code is self contained in terms of processing it seems to me
            // It is already in a thread separate from the listening of messages and it must block until the file is backed up anyway...
            switch (info.getMessageType()) {
                case PUTCHUNK:
                    try {
                        final byte[] body = info.getBody();
                        final String file_id = info.getFileId();
                        final int chunk_no = info.getChunkNo();

                        if (!StorageManager.getInstance().storeChunk(file_id, chunk_no, body)) {
                            System.out.printf("Storage of file id '%s' and chunk no '%d' was unsuccessful, aborting\n", file_id, chunk_no);
                            return;
                        }
                        final byte[] stored_message = MessageFactory.createStoredMessage(file_id, chunk_no);

                        System.out.printf("Stored file id '%s' - chunk no '%d' -> prepared reply STORED message and will make it broadcast after a random delay\n", file_id, chunk_no);

                        ThreadManager.getInstance().executeLaterMilis(() -> {
                            try {
                                System.out.printf("Broadcasting STORED for file id '%s' and chunk no '%d'\n", file_id, chunk_no);
                                ChannelManager.getInstance().getControl().broadcast(stored_message);
                            } catch (IOException e) {
                                e.printStackTrace();
                            }
                        }, ProtocolDefinitions.getRandomMessageDelayMilis());
                    } catch (InvalidMessageFormatException e) {
                        e.printStackTrace();
                    }
                    break;
            }
        });
    }
}
