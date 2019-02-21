import java.net.DatagramPacket;
import java.util.Arrays;

public class PlateRequestHandler extends AbstractRequestHandler {
    private final PlateDatabase plateDatabase = new PlateDatabase();

    public PlateRequestHandler() {
        super(274);
    }

    @Override
    byte[] handleRequest(DatagramPacket received_packet) {
        String s_temp = new String(received_packet.getData(), 0, received_packet.getLength());

        String[] request = s_temp.split(" ");

        switch (request[0]) {
            case "REGISTER":
                return this.handleRegister(Arrays.copyOfRange(request, 1, request.length));
            case "LOOKUP":
                return this.handleLookup(Arrays.copyOfRange(request, 1, request.length));
            default:
                System.out.println("DBG:Invalid request type received");
                return "-1".getBytes();
        }
    }

    private byte[] handleRegister(String[] data) {
        if (data.length != 2) {
            System.out.println("DBG:REGISTER:Not enough arguments");
            return "-1".getBytes();
        }

        if (plateDatabase.insertKeyValue(data[0], data[1])) {
            System.out.println("DBG:REGISTER:inserted " + data[0] + "->" + data[1]);
            System.out.println("DBG:REGISTER:" + plateDatabase.getNrPlates() + " vehicles in the database.");
            return String.valueOf(plateDatabase.getNrPlates()).getBytes();
        }

        System.out.println("DBG:REGISTER:Error in registering vehicle");
        return "-1".getBytes();
    }

    private byte[] handleLookup(String[] data) {
        if (data.length != 1) {
            System.out.println("DBG:LOOKUP:Not enough arguments");
            return "-1".getBytes();
        }

        String result = plateDatabase.queryPlateOwner(data[0]);

        System.out.println("DBG:LOOKUP:query: " + data[0] + " result:" + result);

        if (result == null) {
            return "-1".getBytes();
        }

        return result.getBytes();
    }
}
