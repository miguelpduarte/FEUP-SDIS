import java.rmi.NotBoundException;
import java.rmi.RemoteException;
import java.rmi.registry.LocateRegistry;
import java.rmi.registry.Registry;
import java.util.Arrays;

public class RmiClient {
    public static void main(String[] args) {
        if (args.length != 2 && args.length != 3) {
            System.out.println("Invalid number of arguments. Usage:\n" +
                    "RmiClient REGISTER <plate> <name>\n" +
                    "RmiClient LOOKUP <plate>");
            return;
        }

        String operation = args[0];
        String[] operands = Arrays.copyOfRange(args, 1, args.length);

        try {
            Registry reg = LocateRegistry.getRegistry();
            IRmiServer server = (IRmiServer) reg.lookup("//localhost/RmiServer");

            switch (operation) {
                case "LOOKUP":
                    String res1 = server.lookup(operands[0]);
                    System.out.println("LOOKUP " + Arrays.toString(operands) + " :: " + res1);
                    break;
                case "REGISTER":
                    int res2 = server.register(operands[0], operands[1]);
                    System.out.println("REGISTER " + Arrays.toString(operands) + " :: " + res2);
                    break;
                default:
                    System.err.println("Uh oh invalid method!");
                    System.exit(801);
            }
        } catch (RemoteException e) {
            System.out.println("uh oh, remote spaghetti-ohs");
            e.printStackTrace();
        } catch (NotBoundException e) {
            e.printStackTrace();
        }
    }
}
