import java.rmi.AlreadyBoundException;
import java.rmi.RemoteException;
import java.rmi.registry.LocateRegistry;
import java.rmi.registry.Registry;
import java.rmi.server.UnicastRemoteObject;

public class RmiServer extends UnicastRemoteObject implements IRmiServer {
    private final PlateDatabase plateDatabase = new PlateDatabase();

    public RmiServer() throws RemoteException {
        super(0); // required to avoid the 'rmic' step, see below
    }

    public static void main(String args[]) {
        System.out.println("RMI server started");

        try {
            RmiServer obj = new RmiServer();
            Registry reg = LocateRegistry.createRegistry(1099);
            // Bind this object instance to the name "RmiServer"
            reg.bind("//localhost/RmiServer", obj);

            System.out.println("RmiServer bound in registry");
        } catch (RemoteException e) {
            e.printStackTrace();
        } catch (AlreadyBoundException e) {
            e.printStackTrace();
        }

    }

    @Override
    public int register(String owner, String plate_nr) {
        if (plateDatabase.insertKeyValue(plate_nr, owner)) {
            System.out.println("DBG:REGISTER:inserted " + plate_nr + "->" + owner);

            final int nr_plates = plateDatabase.getNrPlates();
            System.out.println("DBG:REGISTER:" + nr_plates + " vehicles in the database.");
            return nr_plates;
        }

        System.out.println("DBG:REGISTER:Error in registering vehicle");
        return -1;
    }

    @Override
    public String lookup(String plate_nr) {
        String result = plateDatabase.queryPlateOwner(plate_nr);

        System.out.println("DBG:LOOKUP:query: " + plate_nr + " result:" + result);

        if (result == null) {
            return "-1";
        }

        return result;
    }
}
