import java.io.IOException;
import java.net.*;
import java.util.Arrays;

public class Client {
    public static void main(String[] args) {
        String hostname = args[0];
        int port = Integer.parseInt(args[1]);
        String operation = args[2];
        String[] operands = Arrays.copyOfRange(args, 3, args.length);

        Client s = new Client(hostname, port, operation, operands);
    }

    public Client(String hostname, int port, String operation, String[] operands) {
        System.out.printf("hostname: %s\nPort: %d\nOperation: %s\n", hostname, port, operation);

        for (String operand : operands) {
            System.out.println(operand);
        }

        try {
            DatagramSocket ds = new DatagramSocket();

            StringBuilder stringBuilder = new StringBuilder(operation);

            for (String operand : operands) {
                stringBuilder.append(" ");
                stringBuilder.append(operand);
            }

            byte[] data = stringBuilder.toString().getBytes();

            try {
                DatagramPacket dp = new DatagramPacket(data, data.length, InetAddress.getByName(hostname), port);

                ds.send(dp);
            } catch (UnknownHostException e) {
                e.printStackTrace();
            } catch (IOException e) {
                e.printStackTrace();
            }

        } catch (SocketException e) {
            e.printStackTrace();
        }
    }
}
