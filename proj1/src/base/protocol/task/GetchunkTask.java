package base.protocol.task;

import base.ProtocolDefinitions;
import base.channels.ChannelHandler;
import base.channels.ChannelManager;
import base.messages.CommonMessage;
import base.messages.InvalidMessageFormatException;
import base.messages.MessageFactory;
import base.messages.MessageWithChunkNo;
import base.protocol.task.extendable.ObservableTask;
import base.storage.StorageManager;

public class GetchunkTask extends ObservableTask {
    private final String file_name;

    public GetchunkTask(String file_id, String file_name, int chunk_no) {
        super(file_id, chunk_no);
        this.file_name = file_name;
        prepareMessage();
    }

    @Override
    public void notify(CommonMessage msg) {
        synchronized (this) {
            if (!this.isRunning()) {
                return;
            }

            if (msg.getMessageType() != ProtocolDefinitions.MessageType.CHUNK) {
                System.out.println("DBG:RestoreTask.notify::Message was not of type CHUNK!");
                return;
            }

            if (((MessageWithChunkNo) msg).getChunkNo() != this.chunk_no || !msg.getFileId().equals(this.file_id)) {
                System.out.println("DBG:RestoreTask.notify::Message target was not this specific task");
                return;
            }

            try {
                this.pauseCommunication();
                byte[] msg_body = msg.getBody();

                if (this.storeInFile(msg_body)) {
                    this.stopTask();
                    this.notifyObserver(true);
                } else {
                    this.resumeCommuncation();
                }
            } catch (InvalidMessageFormatException e) {
                e.printStackTrace();
                System.out.println("Message body was not of the correct format");
                this.resumeCommuncation();
            }
        }
    }

    protected boolean storeInFile(byte[] msg_body) {
        return StorageManager.getInstance().writeChunkToFullFile(file_name, msg_body, this.chunk_no);
    }

    @Override
    protected byte[] createMessage() {
        return MessageFactory.createGetchunkMessage(file_id, this.chunk_no);
    }

    @Override
    protected void handleMaxRetriesReached() {
        System.out.printf("Maximum retries reached for GetchunkTask for fileid '%s', at chunk_no '%d'\n", this.file_id, this.chunk_no);
        this.stopTask();
        this.notifyObserver(false);
    }

    @Override
    protected void printSendingMessage() {
        System.out.printf("Sending GETCHUNK message for fileid '%s' and chunk_no '%d' - attempt #%d\n", this.file_id, this.chunk_no, this.getCurrentAttempt() + 1);
    }

    @Override
    public String toKey() {
        return ProtocolDefinitions.MessageType.CHUNK.name() + file_id + this.chunk_no;
    }

    @Override
    protected ChannelHandler getChannel() {
        return ChannelManager.getInstance().getControl();
    }
}
