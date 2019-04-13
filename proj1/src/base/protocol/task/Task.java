package base.protocol.task;

import base.Keyable;
import base.ProtocolDefinitions;
import base.ThreadManager;
import base.channels.ChannelHandler;
import base.messages.CommonMessage;

import java.io.IOException;
import java.util.concurrent.ScheduledFuture;

public abstract class Task implements Keyable {
    private byte[] message;
    protected final String file_id;
    protected int current_attempt;
    private ScheduledFuture next_action;
    private boolean is_communicating = false;

    public Task(String file_id) {
        this.file_id = file_id;
        this.current_attempt = 0;
    }

    protected final void prepareMessage() {
        this.message = this.createMessage();
    }

    public abstract void notify(CommonMessage msg);

    protected final void startCommuncation() {
        if (this.isCommunicating()) {
            return;
        }
        this.setIsCommunicating(true);
        // Kickstarting the channels "loop"
        ThreadManager.getInstance().executeLater(this::communicate);
    }

    private synchronized final void setIsCommunicating(boolean is_communicating) {
        this.is_communicating = is_communicating;
    }

    private synchronized final boolean isCommunicating() {
        return this.is_communicating;
    }

    protected abstract byte[] createMessage();

    protected void handleMaxRetriesReached() {
        this.unregister();
    }

    private void communicate() {
        if (!this.isCommunicating()) {
            System.out.println("Not communicating atm!"); // TODO REMOVE? handling repeated calls (?)
            return;
        }

        if (this.getCurrentAttempt() >= ProtocolDefinitions.MESSAGE_DELAYS.length) {
            this.handleMaxRetriesReached();
            return;
        }

        try {
            printSendingMessage();
            getChannel().broadcast(this.message);
        } catch (IOException e) {
            e.printStackTrace();
        }

        this.next_action = ThreadManager.getInstance().executeLater(() -> {
            this.incrementAttemptNumber();
            this.communicate();
        }, ProtocolDefinitions.MESSAGE_DELAYS[this.getCurrentAttempt()]);
    }

    protected abstract ChannelHandler getChannel();

    protected final void cancelCommunication() {
        if (!this.isCommunicating()) {
            System.out.println("Cancelling non-running communication");
        }
        this.setIsCommunicating(false);
        this.next_action.cancel(true);
    }

    protected abstract void printSendingMessage();

    protected final void unregister() {
        TaskManager.getInstance().unregisterTask(this);
    }

    protected synchronized final void resetAttemptNumber() {
        this.current_attempt = 0;
    }

    protected synchronized final int getCurrentAttempt() {
        return current_attempt;
    }

    protected synchronized final void incrementAttemptNumber() {
        this.current_attempt++;
    }
}
