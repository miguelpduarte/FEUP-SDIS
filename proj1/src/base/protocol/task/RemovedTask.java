package base.protocol.task;

import base.channels.ChannelHandler;
import base.channels.ChannelManager;
import base.messages.CommonMessage;
import base.messages.MessageFactory;
import base.protocol.task.extendable.Task;

public class RemovedTask extends Task {
    private final int chunk_no;

    public RemovedTask(String file_id, int chunk_no) {
        // This task doesn't need to retry sending
        super(file_id, false);
        this.chunk_no = chunk_no;
        prepareMessage();
    }

    @Override
    public void notify(CommonMessage msg) {
        // Do nothing, no replies to DELETE messages are predicted in the protocol
    }

    @Override
    protected byte[] createMessage() {
        return MessageFactory.createRemovedMessage(this.file_id, this.chunk_no);
    }

    @Override
    protected void handleMaxRetriesReached() {
        this.unregister();
    }

    @Override
    protected ChannelHandler getChannel() {
        return ChannelManager.getInstance().getControl();
    }

    @Override
    protected void printSendingMessage() {
        System.out.printf("Sending REMOVED message for fileid '%s' and chunkno '%d' - attempt #%d\n", this.file_id, this.chunk_no, this.current_attempt + 1);
    }

    @Override
    public String toKey() {
        // "EXTRA_REMOVED_PREFIX" is added to ensure that there are no collisions with hashes of just file_ids and chunk_nos
        return "EXTRA_REMOVED_PREFIX" + this.file_id + this.chunk_no;
    }
}
