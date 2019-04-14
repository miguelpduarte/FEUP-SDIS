/*
package base.protocol.task;

import base.ProtocolDefinitions;
import base.messages.MessageFactory;
import base.messages.MessageWithPasvPort;
import base.storage.Restorer;

import java.io.IOException;
import java.io.ObjectInputStream;
import java.net.InetAddress;
import java.net.Socket;

public class EnhancedRestoreTask extends RestoreTask {
    public EnhancedRestoreTask(String file_id, String file_name) {
        super(file_id, file_name);
    }

    public synchronized void notify(MessageWithPasvPort msg, InetAddress address) {
        if (!this.isRunning()) {
            return;
        }

        if (msg.getMessageType() != ProtocolDefinitions.MessageType.PASVCHUNK) {
            System.out.println("DBG:EnhancedRestoreTask.notify::Message was not of type PASVCHUNK!");
            return;
        }

        if (msg.getChunkNo() != this.getChunkNo() || !msg.getFileId().equals(this.file_id)) {
            // System.out.println("DBG:EnhancedRestoreTask.notify::Message target was not this specific task");
            return;
        }

        System.out.printf("Now processing PASVCHUNK for %s and %d\n", this.file_id, this.getChunkNo());

        this.pauseCommunication();

        try (Socket s = new Socket(address, msg.getPasvPort()); ObjectInputStream ois = new ObjectInputStream(s.getInputStream())) {
            byte[] chunk_data = (byte[]) ois.readObject();
            Restorer r = RestoreManager.getInstance().getRestorer(() -> this.file_id);

            assert r != null;

            if (chunk_data.length < ProtocolDefinitions.CHUNK_MAX_SIZE_BYTES) {
                // Last chunk, unregister this task and eventually stop the Restorer that is running
                this.unregister();
                r.stopWriter();
                r.addChunk(chunk_data, this.getChunkNo());
                System.out.println("a");
                this.incrementChunkNo();
            } else {
                r.addChunk(chunk_data, this.getChunkNo());
                // Still have more chunks, increment chunk_no and reset number of retries.
                // Then, re-key the task (to receive the correct messages), re-generate the message and restart communication
                this.incrementChunkNo();
                this.resetAttemptNumber();
                TaskManager.getInstance().rekeyTask(this);
                this.prepareMessage();
                System.out.println("b");
                // Done explicitly since it is only relevant for this case (and this will be TODO refactored)
                this.resumeCommuncation();
                this.startCommuncation();
            }
        } catch (IOException e) {
            System.out.println("ControlChannelHandler.handlePasvChunk :c");
            e.printStackTrace();
        } catch (ClassNotFoundException e) {
            e.printStackTrace();
        }

    }

    @Override
    protected byte[] createMessage() {
        return MessageFactory.createGetchunkMessage(file_id, this.getChunkNo(), false);
    }

    @Override
    protected void handleMaxRetriesReached() {
        System.out.printf("Maximum retries reached for EnhancedRestoreTask for fileid '%s', at chunk_no '%d'\n", this.file_id, this.getChunkNo());
        this.unregister();
        Restorer r = RestoreManager.getInstance().getRestorer(() -> this.file_id);
        assert r != null;
        r.haltWriter();
    }

    @Override
    protected void printSendingMessage() {
        System.out.printf("Sending GETCHUNK message for fileid '%s' and chunk_no '%d' - attempt #%d\n", this.file_id, this.getChunkNo(), this.getCurrentAttempt() + 1);
    }

    @Override
    public String toKey() {
        return ProtocolDefinitions.MessageType.PASVCHUNK.name() + file_id + this.getChunkNo();
    }
}
*/
