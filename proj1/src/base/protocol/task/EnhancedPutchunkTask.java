package base.protocol.task;

import base.messages.MessageFactory;

public class EnhancedPutchunkTask extends PutchunkTask {
    public EnhancedPutchunkTask(String file_id, int chunk_no, int replication_deg, byte[] body) {
        super(file_id, chunk_no, replication_deg, body);
    }

    @Override
    protected byte[] createMessage() {
        return MessageFactory.createPutchunkMessage(file_id, chunk_no, replication_deg, body, false);
    }

    @Override
    protected void printSendingMessage() {
        System.out.printf("Sending PUTCHUNK message for fileid '%s' and chunk_no '%d' - attempt #%d\n", this.file_id, this.chunk_no, this.current_attempt + 1);
    }
}
