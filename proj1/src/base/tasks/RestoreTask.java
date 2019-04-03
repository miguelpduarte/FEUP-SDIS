package base.tasks;

import base.ProtocolDefinitions;
import base.messages.CommonMessage;
import base.messages.InvalidMessageFormatException;
import base.messages.MessageFactory;
import base.storage.RestoreManager;
import base.storage.Restorer;

public class RestoreTask extends Task {
    public RestoreTask(String file_name, int chunk_no) {
        super(file_name, chunk_no);
    }

    @Override
    public void notify(CommonMessage msg) {
        if (msg.getMessageType() != ProtocolDefinitions.MessageType.CHUNK) {
            System.out.println("DBG:RestoreTask.notify::Message was not of type CHUNK!");
            return;
        }

        if (msg.getChunkNo() != this.chunk_no || !msg.getFileId().equals(this.file_id)) {
            System.out.println("DBG:RestoreTask.notify::Message target was not this specific task");
            return;
        }

        try {
            byte[] msg_body = msg.getBody();
            // Interrupt the next GETCHUNK messages
            this.cancelCommunication();
            Restorer r = RestoreManager.getInstance().getRestorer(() -> this.file_id);

            assert r != null;

            r.addChunk(msg_body);
            if (msg_body.length < ProtocolDefinitions.CHUNK_MAX_SIZE_BYTES) {
                // Last chunk, unregister this task and eventually stop the Restorer that is running
                this.unregister();
                r.stopWriter();
            } else {
                // Still have more chunks, increment chunk_no and reset number of retries.
                // Then, re-key the task (to receive the correct messages) re-generate the message and restart communication
                this.chunk_no++;
                this.current_attempt = 0;
                TaskManager.getInstance().rekeyTask(this);
                this.prepareMessage();
                this.startCommuncation();
            }
        } catch (InvalidMessageFormatException ignored) {
        }
    }

    @Override
    protected byte[] createMessage() {
        return MessageFactory.createGetchunkTask(file_id, chunk_no);
    }

    @Override
    protected void handleMaxRetriesReached() {
        super.handleMaxRetriesReached();
        System.out.printf("Maximum retries reached for RestoreTask for fileid '%s', at chunk_no '%d'\n", this.file_id, this.chunk_no);
        RestoreManager.getInstance().getRestorer(() -> this.file_id).haltWriter();
    }

    @Override
    protected void printSendingMessage() {
        System.out.printf("Sending GETCHUNK message for fileid '%s' and chunk_no '%d' - attempt #%d\n", this.file_id, this.chunk_no, this.current_attempt + 1);
    }

    @Override
    public String toKey() {
        return ProtocolDefinitions.MessageType.CHUNK.name() + file_id + chunk_no;
    }
}
