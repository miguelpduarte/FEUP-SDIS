package base.protocol.task;

import base.ProtocolDefinitions;
import base.channels.ChannelHandler;
import base.channels.ChannelManager;
import base.messages.CommonMessage;
import base.messages.MessageFactory;
import base.messages.MessageWithChunkNo;
import base.protocol.task.extendable.ObservableTask;

import java.util.HashSet;

public class PutchunkTask extends ObservableTask {
    protected final int replication_deg;
    protected final HashSet<String> replicators = new HashSet<>();
    protected final byte[] body;

    public PutchunkTask(String file_id, int chunk_no, int replication_deg, byte[] body, boolean self_replicated) {
        super(file_id, chunk_no);
        this.body = body;
        this.replication_deg = replication_deg;
        prepareMessage();
        if (self_replicated) {
            this.replicators.add(ProtocolDefinitions.SERVER_ID);
        }
    }

    public PutchunkTask(String file_id, int chunk_no, int replication_deg, byte[] body) {
        this(file_id, chunk_no, replication_deg, body, false);
    }

    @Override
    protected byte[] createMessage() {
        return MessageFactory.createPutchunkMessage(file_id, chunk_no, replication_deg, body);
    }

    @Override
    protected void handleMaxRetriesReached() {
        this.stopTask();
        System.out.printf("Maximum retries reached for PutchunkTask for fileid '%s' and chunk_no '%d'\n", this.file_id, this.chunk_no);
        this.notifyObserver(false);
    }

    @Override
    public void notify(CommonMessage msg) {
        if (!this.isRunning()) {
            return;
        }

        if (msg.getMessageType() != ProtocolDefinitions.MessageType.STORED) {
            System.out.println("DBG:PutchunkTask.notify::Message was not of type STORED!");
            return;
        }

        if (((MessageWithChunkNo) msg).getChunkNo() != this.chunk_no || !msg.getFileId().equals(this.file_id)) {
            System.out.println("DBG:PutchunkTask.notify::Message target was not this specific task");
            return;
        }

        synchronized (this) {
            this.replicators.add(msg.getSenderId());
            System.out.printf("DBG: Registered %s as a replicator successfully\n#Replicators: %d\tReplication Degree: %d\n", msg.getSenderId(), this.replicators.size(), this.replication_deg);
            if (this.replicators.size() >= this.replication_deg) {
                // System.out.println("DBG: Replication minimum reached! Stopping future messages and unregistering task!");
                System.out.printf("Chunk '%d' for fileid '%s' successfully replicated with a factor of at least '%d'\n", this.chunk_no, this.file_id, this.replication_deg);
                this.stopTask();
                // Had success!!
                this.notifyObserver(true);
            }
        }
    }

    @Override
    public String toKey() {
        return ProtocolDefinitions.MessageType.STORED.name() + file_id + chunk_no;
    }

    @Override
    protected void printSendingMessage() {
        System.out.printf("Sending PUTCHUNK message for fileid '%s' and chunk_no '%d' - attempt #%d\n", this.file_id, this.chunk_no, this.current_attempt + 1);
    }

    @Override
    protected ChannelHandler getChannel() {
        return ChannelManager.getInstance().getBackup();
    }
}
