package base.protocol.task;

import base.ProtocolDefinitions;
import base.channels.ChannelHandler;
import base.channels.ChannelManager;
import base.messages.MessageFactory;
import base.messages.MessageWithPasvPort;

import java.io.IOException;
import java.io.ObjectInputStream;
import java.net.InetAddress;
import java.net.Socket;

public class EnhancedGetchunkTask extends GetchunkTask {
    public EnhancedGetchunkTask(String file_id, String file_name, int chunk_no) {
        super(file_id, file_name, chunk_no);
    }

    public synchronized void notify(MessageWithPasvPort msg, InetAddress address) {
        if (!this.isRunning()) {
            return;
        }

        if (msg.getMessageType() != ProtocolDefinitions.MessageType.PASVCHUNK) {
            System.out.println("DBG:EnhancedGetchunkTask.notify::Message was not of type PASVCHUNK!");
            return;
        }

        if (msg.getChunkNo() != this.chunk_no || !msg.getFileId().equals(this.file_id)) {
            System.out.println("DBG:EnhancedGetchunkTask.notify::Message target was not this specific task");
            return;
        }

        System.out.printf("Now processing PASVCHUNK for %s and %d\n", this.file_id, this.chunk_no);

        this.pauseCommunication();

        try (Socket s = new Socket(address, msg.getPasvPort()); ObjectInputStream ois = new ObjectInputStream(s.getInputStream())) {
            byte[] chunk_data = (byte[]) ois.readObject();

            if (this.storeInFile(chunk_data)) {
                this.stopTask();
                this.notifyObserver(true);
                return;
            }
        } catch (IOException | ClassNotFoundException e) {
            e.printStackTrace();
        }

        this.resumeCommuncation();
    }

    @Override
    protected byte[] createMessage() {
        return MessageFactory.createGetchunkMessage(file_id, this.chunk_no, false);
    }

    @Override
    protected void handleMaxRetriesReached() {
        System.out.printf("Maximum retries reached for EnhancedGetchunkTask for fileid '%s', at chunk_no '%d'\n", this.file_id, this.chunk_no);
        this.stopTask();
        this.notifyObserver(false);
    }

    @Override
    protected void printSendingMessage() {
        System.out.printf("Sending GETCHUNK message for fileid '%s' and chunk_no '%d' - attempt #%d\n", this.file_id, this.chunk_no, this.getCurrentAttempt() + 1);
    }

    @Override
    public String toKey() {
        return ProtocolDefinitions.MessageType.PASVCHUNK.name() + file_id + this.chunk_no;
    }

    @Override
    protected ChannelHandler getChannel() {
        return ChannelManager.getInstance().getControl();
    }
}
