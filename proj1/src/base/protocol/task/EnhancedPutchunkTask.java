package base.protocol.task;

import base.ProtocolDefinitions;
import base.messages.MessageFactory;
import base.messages.MessageWithPasvPort;

import java.io.IOException;
import java.io.ObjectOutputStream;
import java.net.InetAddress;
import java.net.Socket;
import java.util.HashSet;

public class EnhancedPutchunkTask extends PutchunkTask {
    private final HashSet<String> ongoing_replications = new HashSet<>();

    public EnhancedPutchunkTask(String file_id, int chunk_no, int replication_deg, byte[] body) {
        super(file_id, chunk_no, replication_deg, body);
    }

    @Override
    protected void handleMaxRetriesReached() {
        this.stopRunning();
        System.out.printf("Maximum retries reached for EnhancedPutchunkTask for fileid '%s' and chunk_no '%d'\n", this.file_id, this.chunk_no);
        this.notifyObserver(false);
    }

    @Override
    protected byte[] createMessage() {
        return MessageFactory.createPutchunkMessage(file_id, chunk_no, replication_deg, body, false);
    }

    @Override
    protected void printSendingMessage() {
        System.out.printf("Sending enhanced PUTCHUNK message for fileid '%s' and chunk_no '%d' - attempt #%d\n", this.file_id, this.chunk_no, this.current_attempt + 1);
    }

    @Override
    public String toKey() {
        return ProtocolDefinitions.MessageType.CANSTORE.name() + file_id + chunk_no;
    }

    public synchronized void notify(MessageWithPasvPort msg, InetAddress address) {
        if (!this.isRunning()) {
            return;
        }

        if (msg.getMessageType() != ProtocolDefinitions.MessageType.CANSTORE) {
            System.out.println("EnhancedPutchunkTask.notify::Message was not of type CANSTORE!");
            return;
        }

        if (msg.getChunkNo() != this.chunk_no || !msg.getFileId().equals(this.file_id)) {
            System.out.println("EnhancedPutchunkTask.notify::Message target was not this specific task");
            return;
        }

        // Ensuring that the observed replication degree is not larger than the desired one
        if (ongoing_replications.size() + replicators.size() >= this.replication_deg) {
            return;
        }

        // Ensuring that backup is not attempted for the same Peer twice
        if (ongoing_replications.contains(msg.getSenderId()) || replicators.contains(msg.getSenderId())) {
            return;
        }

        // Entering into protocol processing for a certain peer

        this.pauseCommunication();

        // Connecting to the remote peer
        try (Socket s = new Socket(address, msg.getPasvPort())) {
            ObjectOutputStream oos = new ObjectOutputStream(s.getOutputStream());
            oos.writeObject(this.body);
            this.replicators.add(msg.getSenderId());
            this.ongoing_replications.remove(msg.getSenderId());

            if (this.replicators.size() >= this.replication_deg) {
                System.out.printf("Chunk '%d' for fileid '%s' successfully replicated with a factor of '%d'\n", this.chunk_no, this.file_id, this.replication_deg);

                this.stopRunning();

                // Success!
                this.notifyObserver(true);
                return;
            }

            // Resuming communication as the desired replication was not yet reached
            this.resumeCommuncation();
        } catch (IOException e) {
            e.printStackTrace();
            // Failure, resume communication
            this.resumeCommuncation();
        }
    }
}
