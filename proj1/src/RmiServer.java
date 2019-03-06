import java.rmi.Naming;
import java.rmi.RemoteException;
import java.rmi.registry.LocateRegistry;
import java.rmi.registry.Registry;
import java.rmi.server.UnicastRemoteObject;

public class RmiServer extends UnicastRemoteObject implements IRmiServer {
    public RmiServer() throws RemoteException {
        super(0); // required to avoid the 'rmic' step, see below
    }

    public static void main(String args[]) throws Exception {
        System.out.println("RMI server started");

        Registry reg = LocateRegistry.getRegistry();

        RmiServer obj = new RmiServer();

        // Bind this object instance to the name "RmiServer"
        reg.bind("//localhost/RmiServer", obj);

        System.out.println("RmiServer bound in registry");
    }

    @Override
    public int registry(String owner, String plate_nr) throws RemoteException {
        System.out.println("RmiServer.registry");
        System.out.println("owner = [" + owner + "], plate_nr = [" + plate_nr + "]");
        return 420;
    }

    @Override
    public String lookup(String plate_nr) throws RemoteException {
        return "spaghet";
    }
}
