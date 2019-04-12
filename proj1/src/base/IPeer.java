package base;

import java.rmi.Remote;
import java.rmi.RemoteException;

public interface IPeer extends Remote {
    int backup(String filename, int replication_factor) throws RemoteException;

    int backupEnhanced(String filename, int replication_factor) throws RemoteException;

    int restore(String filename) throws RemoteException;

    int restoreEnhanced(String filename) throws RemoteException;

    int delete(String filename) throws RemoteException;

    // When called with 0 empties the Peer's disk space
    int setMaxDiskSpace(int disk_space_kbs) throws RemoteException;

    String getServiceState() throws RemoteException;
}
