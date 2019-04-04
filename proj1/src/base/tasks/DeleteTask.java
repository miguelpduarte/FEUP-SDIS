package base.tasks;

import base.channels.ChannelHandler;
import base.channels.ChannelManager;
import base.messages.CommonMessage;
import base.messages.MessageFactory;

public class DeleteTask extends Task {
    public DeleteTask(String file_name) {
        super(file_name);
    }

    @Override
    public void notify(CommonMessage msg) {
        // Do nothing, no replies to DELETE messages are predicted in the protocol
        // TODO: Discuss possibility of extracting this method to an intermediate class
    }

    @Override
    protected byte[] createMessage() {
        return MessageFactory.createDeleteMessage(this.file_id);
    }

    @Override
    protected void handleMaxRetriesReached() {
        super.handleMaxRetriesReached();
        //TODO: Delete later, only being used for debug printing
        System.out.printf("DELETE for fileid '%s' reached the maximum number of retries\n", this.file_id);
    }

    @Override
    protected ChannelHandler getChannel() {
        return ChannelManager.getInstance().getControl();
    }

    @Override
    protected void printSendingMessage() {
        System.out.printf("Sending DELETE message for fileid '%s' - attempt #%d\n", this.file_id, this.current_attempt + 1);
    }

    @Override
    public String toKey() {
        // "EXTRA_DELETE_PREFIX" is added to ensure that there are no collisions with hashes of just file_ids
        return "EXTRA_DELETE_PREFIX" + this.file_id;
    }
}
