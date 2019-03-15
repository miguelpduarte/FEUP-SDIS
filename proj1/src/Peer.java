import javax.xml.crypto.Data;
import java.io.IOException;
import java.net.*;
import java.rmi.RemoteException;
import java.rmi.registry.LocateRegistry;
import java.rmi.registry.Registry;
import java.rmi.server.UnicastRemoteObject;
import java.util.Arrays;

public class Peer extends UnicastRemoteObject implements IPeer {
    private final String protocol_version;
    private final String server_id;
    private MulticastSocket mc;
    private MulticastSocket mdb;
    private MulticastSocket mdr;
    private static final int PACKET_SIZE = 1024;

    public Peer(String protocol_version, String server_id, String mc_hostname, int mc_port, String mdb_hostname, int mdb_port, String mdr_hostname, int mdr_port) throws IOException {
        super(0); // required to avoid the 'rmic' step, see below

        this.protocol_version = protocol_version;
        this.server_id = server_id;

        this.mc = new MulticastSocket(mc_port);
        this.mc.joinGroup(InetAddress.getByName(mc_hostname));
        this.mc.setTimeToLive(1);
        this.mc.setLoopbackMode(false); // Change?

        this.mdb = new MulticastSocket(mdb_port);
        this.mdb.joinGroup(InetAddress.getByName(mdb_hostname));
        this.mdb.setTimeToLive(1);
        this.mdb.setLoopbackMode(false); // Change?

        this.mdr = new MulticastSocket(mdr_port);
        this.mdr.joinGroup(InetAddress.getByName(mdr_hostname));
        this.mdr.setTimeToLive(1);
        this.mdr.setLoopbackMode(false); // Change?

        listenCommunicationChannel();
        listenDataBackupChannel();
        listenDataRestoreChannel();
    }

    private void listenCommunicationChannel() {
        DatagramPacket packet = new DatagramPacket(new byte[PACKET_SIZE], PACKET_SIZE); // TODO: Update values
        new Thread(new Runnable() {
            @Override
            public void run() {
                while (true) {
                    try {
                        mc.receive(packet);

                        System.out.println("Communication Channel message");
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                }
            }
        }).start();
    }

    private void listenDataBackupChannel() {
        DatagramPacket packet = new DatagramPacket(new byte[PACKET_SIZE], PACKET_SIZE); // TODO: Update values
        new Thread(new Runnable() {
            @Override
            public void run() {
                while (true) {
                    try {
                        mdb.receive(packet);

                        System.out.println("Databackup Channel message");
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                }
            }
        }).start();
    }

    private void listenDataRestoreChannel() {
        DatagramPacket packet = new DatagramPacket(new byte[PACKET_SIZE], PACKET_SIZE); // TODO: Update values
        new Thread(new Runnable() {
            @Override
            public void run() {
                while (true) {
                    try {
                        mdr.receive(packet);

                        System.out.println("Datarestore Channel message");
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                }
            }
        }).start();
    }

    public static void main(String args[]) throws Exception {
        System.out.println("My args");
        System.out.println("args = [" + Arrays.toString(args) + "]");

        if (args.length != 9) {
            System.err.println("Wrong no. of arguments");
            System.exit(1);
        }

        String protocol_version = args[0];
        String server_id = args[1];
        String service_access_point = args[2];

        String mc_hostname = args[3];
        int mc_port = Integer.parseInt(args[4]);

        String mdb_hostname = args[5];
        int mdb_port = Integer.parseInt(args[6]);

        String mdr_hostname = args[7];
        int mdr_port = Integer.parseInt(args[8]);

        Peer obj = new Peer(protocol_version, server_id, mc_hostname, mc_port, mdb_hostname, mdb_port, mdr_hostname, mdr_port);

        System.out.printf("Peer with id '%s' created.\n", server_id);

        /// Create a registry if not yet running. If it is already running, just use the existing one.
        try {
            Registry reg = LocateRegistry.getRegistry();

            // Bind this object instance to a name
            reg.bind(service_access_point, obj);

            System.out.println("Bound with gotten registry");
        } catch(RemoteException e) {
            Registry reg = LocateRegistry.createRegistry(1099);

            // Bind this object instance to a name
            reg.bind(service_access_point, obj);

            System.out.println("Bound with created registry");
        }

        System.out.printf("Registered object instance to access point '%s'.\n", service_access_point);
    }


    @Override
    public int backup(String filename, int replication_factor) {
        System.out.println("Peer.backup");
        System.out.println("filename = [" + filename + "], replication_factor = [" + replication_factor + "]");
        


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
