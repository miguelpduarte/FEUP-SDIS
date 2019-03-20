import java.io.IOException;
import java.rmi.server.UnicastRemoteObject;

public class Peer extends UnicastRemoteObject implements IPeer {
    private ControlChannelHandler control;
    private BackupChannelHandler backup;
    private RestoreChannelHandler restore;

    public Peer(String mc_hostname, int mc_port, String mdb_hostname, int mdb_port, String mdr_hostname, int mdr_port) throws IOException {
        super(0); // required to avoid the 'rmic' step, see below

        createMcHandler(mc_hostname, mc_port);
        createMdbSocket(mdb_hostname, mdb_port);
        createMdrSocket(mdr_hostname, mdr_port);
    }

    private void createMcHandler(String mc_hostname, int mc_port) throws IOException {
        this.control = new ControlChannelHandler(mc_hostname, mc_port);
        new Thread(this.control).start();
    }

    private void createMdbSocket(String mdb_hostname, int mdb_port) throws IOException {
        this.backup = new BackupChannelHandler(mdb_hostname, mdb_port);
        new Thread(this.backup).start();
    }

    private void createMdrSocket(String mdr_hostname, int mdr_port) throws IOException {
        this.restore = new RestoreChannelHandler(mdr_hostname, mdr_port);
        new Thread(this.restore).start();
    }

    @Override
    public int backup(String filename, int replication_factor) {
        System.out.println("Peer.backup");
        System.out.println("filename = [" + filename + "], replication_factor = [" + replication_factor + "]");

        try {
            backup.broadcast(MessageFactory.createPutchunkMessage(filename, 1, replication_factor, new byte[] {'A', 'B', 'Z'}));
        } catch (IOException e) {
            e.printStackTrace();
        }

        return 0;
    }

    @Override
    public int restore(String filename) {
        return 0;
    }

    @Override
    public int delete(String filename) {
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
