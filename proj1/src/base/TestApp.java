package base;

import java.rmi.NotBoundException;
import java.rmi.RemoteException;
import java.rmi.registry.LocateRegistry;
import java.rmi.registry.Registry;
import java.util.Arrays;

public class TestApp {
    public static void main(String[] args) {
        if (args.length < 2) {
            System.err.println("usage: <rmi_peer_ap> <sub_protocol>\n" +
                    "Supported protocols: BACKUP, BACKUPENH, RESTORE, RESTOREENH, DELETE, RECLAIM, STATE");
            System.exit(1);
        }

        String rmi_peer_ap = args[0];
        String sub_protocol = args[1];
        String[] operands = Arrays.copyOfRange(args, 2, args.length);

        try {
            Registry reg = LocateRegistry.getRegistry();
            IPeer peer = (IPeer) reg.lookup(rmi_peer_ap);

            switch (sub_protocol) {
                case "BACKUP":
                    if (args.length != 4) {
                        System.err.println("Wrong no. of arguments");
                        System.err.println("Usage: <peer_id> BACKUP <file_path> <replication_degree>");
                        System.exit(1);
                    }
                    peer.backup(operands[0], Integer.parseInt(operands[1]));
                    break;
                case "BACKUPENH":
                    if (args.length != 4) {
                        System.err.println("Wrong no. of arguments");
                        System.err.println("Usage: <peer_id> BACKUPENH <file_path> <replication_degree>");
                        System.exit(1);
                    }
                    peer.backupEnhanced(operands[0], Integer.parseInt(operands[1]));
                    break;
                case "RESTORE":
                    if (args.length != 3) {
                        System.err.println("Wrong no. of arguments");
                        System.err.println("Usage: <peer_id> RESTORE <file_path>");
                        System.exit(1);
                    }
                    peer.restore(operands[0]);
                    break;
                case "RESTOREENH":
                    if (args.length != 3) {
                        System.err.println("Wrong no. of arguments");
                        System.err.println("Usage: <peer_id> RESTOREENH <file_path>");
                        System.exit(1);
                    }
                    peer.restoreEnhanced(operands[0]);
                    break;
                case "DELETE":
                    if (args.length != 3) {
                        System.err.println("Wrong no. of arguments");
                        System.err.println("Usage: <peer_id> DELETE <file_path>");
                        System.exit(1);
                    }
                    peer.delete(operands[0]);
                    break;
                case "RECLAIM":
                    if (args.length != 3) {
                        System.err.println("Wrong no. of arguments");
                        System.err.println("Usage: <peer_id> RECLAIM <disk_space_kbs>");
                        System.exit(1);
                    }
                    peer.setMaxDiskSpace(Integer.parseInt(operands[0]));
                    break;
                case "STATE":
                    if (args.length != 2) {
                        System.err.println("Wrong no. of arguments");
                        System.err.println("Usage: <peer_id> STATE");
                        System.exit(1);
                    }
                    System.out.println(peer.getServiceState());
                    break;
                default:
                    System.err.println("Invalid subprotocol\n" +
                            "Supported protocols: BACKUP, BACKUPENH, RESTORE, RESTOREENH, DELETE, RECLAIM, STATE");
                    System.exit(2);
            }
        } catch (RemoteException e) {
            System.out.println("Remote exception");
            e.printStackTrace();
        } catch (NotBoundException e) {
            e.printStackTrace();
        }
    }
}
