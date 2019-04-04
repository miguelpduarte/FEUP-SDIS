package base;

import base.channels.BackupChannelHandler;
import base.channels.ChannelManager;
import base.channels.ControlChannelHandler;
import base.channels.RestoreChannelHandler;
import base.messages.MessageFactory;
import base.storage.StorageManager;
import base.tasks.DeleteTask;
import base.tasks.PutchunkTask;
import base.tasks.RestoreTask;
import base.tasks.TaskManager;

import java.io.File;
import java.io.IOException;
import java.rmi.server.UnicastRemoteObject;

public class Peer extends UnicastRemoteObject implements IPeer {
    public Peer(String mc_hostname, int mc_port, String mdb_hostname, int mdb_port, String mdr_hostname, int mdr_port) throws IOException {
        super(0); // required to avoid the 'rmic' step, see PeerMain.main

        createChannelHandlers(mc_hostname, mc_port, mdb_hostname, mdb_port, mdr_hostname, mdr_port);
    }

    private void createChannelHandlers(String mc_hostname, int mc_port, String mdb_hostname, int mdb_port, String mdr_hostname, int mdr_port) throws IOException {
        ChannelManager.getInstance().setChannelHandlers(
                createMcHandler(mc_hostname, mc_port),
                createMdbChannel(mdb_hostname, mdb_port),
                createMdrChannel(mdr_hostname, mdr_port)
        );
    }

    private ControlChannelHandler createMcHandler(String mc_hostname, int mc_port) throws IOException {
        ControlChannelHandler control = new ControlChannelHandler(mc_hostname, mc_port);
        new Thread(control).start();
        return control;
    }

    private BackupChannelHandler createMdbChannel(String mdb_hostname, int mdb_port) throws IOException {
        BackupChannelHandler backup = new BackupChannelHandler(mdb_hostname, mdb_port);
        new Thread(backup).start();
        return backup;
    }

    private RestoreChannelHandler createMdrChannel(String mdr_hostname, int mdr_port) throws IOException {
        RestoreChannelHandler restore = new RestoreChannelHandler(mdr_hostname, mdr_port);
        new Thread(restore).start();
        return restore;
    }

    @Override
    public int backup(String file_path, int replication_factor) {
        System.out.println("Peer.backup");
        System.out.println("file_path = [" + file_path + "], replication_factor = [" + replication_factor + "]");

        final String file_name = new File(file_path).getName();

        // Testing by creating a dummy tasksPutchunkTask that will autonomously communicate:
        try {
            byte[] file_data = StorageManager.readFromFile(file_path);
            byte[][] split_file_data = MessageFactory.splitFileContents(file_data);

            for (int i = 0; i < split_file_data.length; ++i) {
                // System.out.println("i = " + i);
                TaskManager.getInstance().registerTask(new PutchunkTask(file_name, i, replication_factor, split_file_data[i]));
            }
        } catch (IOException e) {
            e.printStackTrace();
            return -1;
        } catch (FileTooLargeException e) {
            System.err.println("File is too large for storage in this distributed backup system!");
            return -2;
        }

        return 0;
    }

    @Override
    public int restore(String file_path) {
        System.out.println("Peer.restore");
        System.out.println("file_path = [" + file_path + "]");

        final String file_name = new File(file_path).getName();

        TaskManager.getInstance().registerTask(new RestoreTask(file_name));

        return 0;
    }

    @Override
    public int delete(String file_path) {
        System.out.println("Peer.delete");
        System.out.println("file_path = [" + file_path + "]");

        final String file_name = new File(file_path).getName();

        TaskManager.getInstance().registerTask(new DeleteTask(file_name));

        return 0;
    }

    @Override
    public int setMaxDiskSpace(int disk_space_kbs) {
        return 0;
    }

    @Override
    public Object getServiceState() {
        return null;
    }
}
