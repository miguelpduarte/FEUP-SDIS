package base.protocol.task.extendable;

import base.Keyable;
import base.ProtocolDefinitions;
import base.ThreadManager;
import base.channels.ChannelHandler;
import base.messages.CommonMessage;
import base.protocol.SynchronizedRunner;
import base.protocol.task.TaskManager;

import java.io.IOException;
import java.util.concurrent.Future;

public abstract class Task extends SynchronizedRunner implements Keyable {
    private byte[] message;
    protected final String file_id;
    protected int current_attempt;
    private Future next_action;
    // Must have synchronized access
    private boolean is_communicating = true;

    public Task(String file_id) {
        this.file_id = file_id;
        this.current_attempt = 0;
    }

    protected final void prepareMessage() {
        this.message = this.createMessage();
    }

    public abstract void notify(CommonMessage msg);

    protected final void resumeCommuncation() {
        // The commented situation below was the problem in tasks that needed to reset the delay between messages (Restore before the Subprotocols refactor)
        // It was fixed by adding a call to startCommunication
        // The problem might still arise when the communication is paused for more time than the current delay (TODO test stuff)
        /*if (this.next_action == null || this.next_action.isCancelled()) {
            // No action for continuation of communication scheduled, schedule one
            // Scheduling in another thread to ensure that the caller doesn't block unwillingly
            // System.out.println("this was the problem");
            // this.next_action = ThreadManager.getInstance().executeLater(this::communicate);

        }
        // This was the temporary test if for testing
        if (this.next_action == null || this.next_action.isDone() || this.next_action.isCancelled()) {
            System.out.println("DEBUG PRINT1 -------&&&&&&&&&"); // TODO Investigation
        } else {
            System.out.println("%%%&&Different print2!!!!! %%%%$$$$$$$$$$$$$$4"); // TODO Investigation
        }
        */


        if (this.isCommunicating()) {
            System.out.println("Resumed twice but ok!");
            return;
        }
        this.setIsCommunicating(true);
    }

    protected final void startCommuncation() {
        // Kickstarting the channels "loop"
        // (no need for separate thread for first launch of message as it can run synchronously, after the creation of the message to send)
        this.communicate();
    }

    private synchronized void setIsCommunicating(boolean is_communicating) {
        this.is_communicating = is_communicating;
    }

    private synchronized boolean isCommunicating() {
        return this.is_communicating;
    }

    protected abstract byte[] createMessage();

    protected abstract ChannelHandler getChannel();

    protected abstract void handleMaxRetriesReached();

    private void communicate() {
        if (!this.isRunning()) {
            System.out.println("Task not running!");
            return;
        }

        if (!this.isCommunicating()) {
            System.out.println("Not communicating atm, trying again later"); // TODO REMOVE? handling repeated calls (?)
            this.next_action = ThreadManager.getInstance().executeLater(this::communicate, ProtocolDefinitions.MESSAGE_DELAYS[this.getCurrentAttempt()]);
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

    protected final void pauseCommunication() {
        if (!this.isCommunicating()) {
            System.out.println("Cancelling non-running communication");
            return;
        }
        this.setIsCommunicating(false);
        this.next_action.cancel(true);
    }

    protected abstract void printSendingMessage();

    public final void stopTask() {
        this.unregister();
        this.cancelCommunication();
        this.stopRunning();
    }

    private void cancelCommunication() {
        this.next_action.cancel(true);
        this.setIsCommunicating(false);
    }

    protected final void unregister() {
        TaskManager.getInstance().unregisterTask(this);
    }

    protected synchronized final void resetAttemptNumber() {
        this.current_attempt = 0;
    }

    protected synchronized final int getCurrentAttempt() {
        return current_attempt;
    }

    private synchronized void incrementAttemptNumber() {
        this.current_attempt++;
    }
}
