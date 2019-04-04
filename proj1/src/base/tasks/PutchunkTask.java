package base.tasks;

import base.ProtocolDefinitions;
import base.channels.ChannelHandler;
import base.channels.ChannelManager;
import base.messages.CommonMessage;
import base.messages.MessageFactory;

import java.util.HashSet;

public class PutchunkTask extends Task {
    private final int replication_deg;
    private final HashSet<String> replicators = new HashSet<>();
    private final byte[] body;
    private int chunk_no;

    public PutchunkTask(String file_name, int chunk_no, int replication_deg, byte[] body) {
        super(file_name);
        this.chunk_no = chunk_no;
        this.body = body;
        this.replication_deg = replication_deg;
        prepareMessage();
        startCommuncation();
    }

    @Override
    protected byte[] createMessage() {
        return MessageFactory.createPutchunkMessage(file_id, chunk_no, replication_deg, body);
    }

    @Override
    protected void handleMaxRetriesReached() {
        super.handleMaxRetriesReached();
        System.out.printf("Maximum retries reached for PutchunkTask for fileid '%s' and chunk_no '%d'\n", this.file_id, this.chunk_no);
    }

    @Override
    public void notify(CommonMessage msg) {
        if (msg.getMessageType() != ProtocolDefinitions.MessageType.STORED) {
            System.out.println("DBG:PutchunkTask.notify::Message was not of type STORED!");
            return;
        }

        if (msg.getChunkNo() != this.chunk_no || !msg.getFileId().equals(this.file_id)) {
            System.out.println("DBG:PutchunkTask.notify::Message target was not this specific task");
            return;
        }

        synchronized (this) {
            this.replicators.add(msg.getSenderId());
            System.out.printf("DBG: Registered %s as a replicator successfully\n#Replicators: %d\tReplication Degree: %d\n", msg.getSenderId(), this.replicators.size(), this.replication_deg);
            if (this.replicators.size() >= this.replication_deg) {
                // System.out.println("DBG: Replication minimum reached! Stopping future messages and unregistering task!");
                System.out.printf("Chunk '%d' for fileid '%s' successfully replicated with a factor of at least '%d'\n", this.chunk_no, this.file_id, this.replication_deg);
                cancelCommunication();
                TaskManager.getInstance().unregisterTask(this);
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
