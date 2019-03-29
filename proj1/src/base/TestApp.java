package base;

import java.rmi.NotBoundException;
import java.rmi.RemoteException;
import java.rmi.registry.LocateRegistry;
import java.rmi.registry.Registry;
import java.util.Arrays;

public class TestApp {
    public static void main(String[] args) {
        if (args.length < 2) {
            System.err.println("Not enough arguments");
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
                    peer.backup(operands[0], Integer.parseInt(operands[1]));
                    break;
                case "RESTORE":
                    peer.restore(operands[0]);
                    break;
                case "DELETE":
                    peer.delete(operands[0]);
                    break;
                case "RECLAIM":
                    peer.setMaxDiskSpace(Integer.parseInt(operands[0]));
                    break;
                case "STATE":
                    peer.getServiceState();
                    break;
                default:
                    System.err.println("Uh oh invalid method!");
                    System.exit(2);
            }
        } catch (RemoteException e) {
            System.out.println("uh oh, remote spaghetti-ohs");
            e.printStackTrace();
        } catch (NotBoundException e) {
            e.printStackTrace();
        }
    }
}
