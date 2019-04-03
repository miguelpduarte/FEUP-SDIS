package base.tasks;

import base.ProtocolDefinitions;
import base.ThreadManager;
import base.channels.ChannelManager;
import base.messages.CommonMessage;
import base.messages.MessageFactory;

import java.io.IOException;
import java.util.HashSet;
import java.util.concurrent.ScheduledFuture;

public class PutchunkTask implements Task {
    private final byte[] message;
    private final String file_id;
    private final int chunk_no;
    private final int replication_deg;
    private final HashSet<String> replicators = new HashSet<>();
    private int current_attempt;
    private ScheduledFuture next_action;

    public PutchunkTask(String file_name, int chunk_no, int replication_deg, byte[] body) {
        this.file_id = MessageFactory.filenameEncode(file_name);
        this.chunk_no = chunk_no;
        this.replication_deg = replication_deg;
        this.current_attempt = 0;
        this.message = MessageFactory.createPutchunkMessage(file_id, chunk_no, replication_deg, body);

        // Kickstarting the channels "loop"
        ThreadManager.getInstance().executeLater(this::communicate);
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
                this.next_action.cancel(true);
                TaskManager.getInstance().unregisterTask(this);
            }
        }
    }

    @Override
    public void communicate() {
        if (this.current_attempt >= ProtocolDefinitions.MESSAGE_DELAYS.length) {
            System.out.printf("Maximum retries reached for PutchunkTaskkTask for fileid '%s' and chunk_no '%d'\n", this.file_id, this.chunk_no);
            TaskManager.getInstance().unregisterTask(this);
            return;
        }

        try {
            System.out.printf("Sending Putchunk message for fileid '%s' and chunk_no '%d' - attempt #%d\n", this.file_id, this.chunk_no, this.current_attempt + 1);
            ChannelManager.getInstance().getBackup().broadcast(this.message);
        } catch (IOException e) {
            e.printStackTrace();
        }

        this.next_action = ThreadManager.getInstance().executeLater(() -> {
            this.current_attempt++;
            this.communicate();
        }, ProtocolDefinitions.MESSAGE_DELAYS[this.current_attempt]);
    }

    @Override
    public String toKey() {
        return ProtocolDefinitions.MessageType.STORED.name() + file_id + chunk_no;
    }
}
