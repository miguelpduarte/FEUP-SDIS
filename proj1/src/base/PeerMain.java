package base;

import base.persistentstate.FileDeletionLog;
import base.persistentstate.FileIdMapper;
import base.persistentstate.ShutdownAndStartupHandler;
import base.protocol.QueryDeletedHandler;
import base.storage.StorageManager;

import java.rmi.RemoteException;
import java.rmi.registry.LocateRegistry;
import java.rmi.registry.Registry;

public class PeerMain {
    public static void main(String args[]) throws Exception {
        if (args.length != 9) {
            System.err.println("Wrong no. of arguments");
            System.exit(1);
        }

        String protocol_version = args[0];
        String server_id = args[1];
        ProtocolDefinitions.VERSION = protocol_version;
        ProtocolDefinitions.SERVER_ID = server_id;

        String service_access_point = args[2];

        String mc_hostname = args[3];
        int mc_port = Integer.parseInt(args[4]);

        String mdb_hostname = args[5];
        int mdb_port = Integer.parseInt(args[6]);

        String mdr_hostname = args[7];
        int mdr_port = Integer.parseInt(args[8]);

        Peer obj = new Peer(mc_hostname, mc_port, mdb_hostname, mdb_port, mdr_hostname, mdr_port);

        System.out.printf("Peer with id '%s' created.\n", server_id);

        /// Create a registry if not yet running. If it is already running, just use the existing one.
        try {
            Registry reg = LocateRegistry.getRegistry();

            // Bind this object instance to a name
            reg.rebind(service_access_point, obj);

            System.out.println("Bound with gotten registry");
        } catch (RemoteException e) {
            Registry reg = LocateRegistry.createRegistry(1099);

            // Bind this object instance to a name
            reg.rebind(service_access_point, obj);

            System.out.println("Bound with created registry");
        }

        System.out.printf("Registered object instance to access point '%s'.\n", service_access_point);

        StorageManager.getInstance().initStorage();
        System.out.println("Storage initialized");
        ShutdownAndStartupHandler.restoreOnStartup();

        ShutdownAndStartupHandler.installShutdownHook();
        ShutdownAndStartupHandler.startPeriodicBackupService();

        // Read file id map from disk
        FileIdMapper.getInstance().readFromDisk();

        // Read file deletion log from disk
        FileDeletionLog.getInstance().readFromDisk();

        // Query file deletions that occured while offline
        new QueryDeletedHandler();
    }
}
