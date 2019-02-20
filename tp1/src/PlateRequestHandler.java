import java.net.DatagramPacket;
import java.util.Arrays;
import java.util.HashMap;

public class PlateRequestHandler extends AbstractRequestHandler {
    protected PlateRequestHandler() {
        super(274);
    }

    private final HashMap<String,String> database = new HashMap<>();


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
                System.out.println("Invalid request type received");
                return "-1".getBytes();
        }
    }

    private byte[] handleRegister(String[] data) {
        if (data.length != 2) {
            System.out.println("REGISTER:Not enough arguments");
            return new byte[0];
        }

        this.database.put(data[0], data[1]);
        System.out.println("REGISTER:inserted " + data[0] + "->" + data[1]);
        System.out.println("REGISTER:" + this.database.size() + " vehicles in the database.");
        return new byte[0];
    }

    private byte[] handleLookup(String[] data) {
        if (data.length != 1) {
            System.out.println("LOOKUP:Not enough arguments");
            return new byte[0];
        }

        String result = this.database.get(data[0]);

        System.out.println("LOOKUP:query: " + data[0] + " result:" + result);
        return new byte[0];
    }
}
