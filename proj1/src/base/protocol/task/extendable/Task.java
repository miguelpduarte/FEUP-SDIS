package base.protocol.task.extendable;

import base.Keyable;
import base.ProtocolDefinitions;
import base.ThreadManager;
import base.channels.ChannelHandler;
import base.messages.CommonMessage;
import base.protocol.SynchronizedRunner;
import base.protocol.task.TaskManager;

import java.util.concurrent.Future;

public abstract class Task extends SynchronizedRunner implements Keyable {
    private byte[] message;
    protected final String file_id;
    private final boolean will_retry;
    protected int current_attempt;
    private Future next_action;
    // Must have synchronized access
    private boolean is_communicating = true;

    public Task(String file_id, boolean will_retry) {
        this.file_id = file_id;
        this.will_retry = will_retry;
        this.current_attempt = 0;
    }

    public Task(String file_id) {
        this(file_id, true);
    }

    public void start() {
        // Kickstarting the channels "loop"
        // (no need for separate thread for first launch of message as it can run synchronously, after the creation of the message to send)
        this.communicate();
    }

    protected final void prepareMessage() {
        this.message = this.createMessage();
    }

    public abstract void notify(CommonMessage msg);

    private synchronized void setIsCommunicating(boolean is_communicating) {
        this.is_communicating = is_communicating;
    }

    private synchronized boolean isCommunicating() {
        return this.is_communicating;
    }

    protected abstract byte[] createMessage();

    protected abstract ChannelHandler getChannel();

    protected abstract void handleMaxRetriesReached();

    private synchronized void communicate() {
        if (!this.isRunning()) {
            // System.out.println("Task not running!");
            return;
        }

        if (this.getCurrentAttempt() >= ProtocolDefinitions.MESSAGE_DELAYS.length) {
            this.handleMaxRetriesReached();
            return;
        }

        if (!this.isCommunicating()) {
            // System.out.println("Not communicating atm, trying again later");
            if (this.next_action == null || this.next_action.isDone() || this.next_action.isCancelled()) {
                if (!this.will_retry) {
                    return;
                }
                this.next_action = ThreadManager.getInstance().executeLater(this::communicate, ProtocolDefinitions.MESSAGE_DELAYS[this.getCurrentAttempt()]);
            }
            return;
        }

        printSendingMessage();
        getChannel().broadcast(this.message);

        if (!this.will_retry) {
            return;
        }

        this.next_action = ThreadManager.getInstance().executeLater(() -> {
            this.incrementAttemptNumber();
            this.communicate();
        }, ProtocolDefinitions.MESSAGE_DELAYS[this.getCurrentAttempt()]);
    }

    protected final void pauseCommunication() {
        // if (!this.isCommunicating()) {
        //    System.out.println("Cancelling non-running communication");
        // }
        this.next_action.cancel(true);
        this.setIsCommunicating(false);
    }

    protected final void resumeCommuncation() {
        //if (this.isCommunicating()) {
        //    System.out.println("Resumed twice but ok!");
        //}
        this.setIsCommunicating(true);
    }

    protected abstract void printSendingMessage();

    public final void stopTask() {
        this.unregister();
        this.cancelCommunication();
        this.stopRunning();
    }

    private void cancelCommunication() {
        if (this.next_action != null) {
            this.next_action.cancel(true);
        }
        this.setIsCommunicating(false);
    }

    protected final void unregister() {
        TaskManager.getInstance().unregisterTask(this);
    }

    protected synchronized final int getCurrentAttempt() {
        return current_attempt;
    }

    private synchronized void incrementAttemptNumber() {
        this.current_attempt++;
    }
}
