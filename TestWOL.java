import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;

public class TestWOL {
    public static void main(String[] args) throws Exception {
        String macStr = "00:1A:2B:3C:4D:5E";
        String host = "127.0.0.1";
        int port = 9;

        byte[] macBytes = new byte[6];
        String[] hex = macStr.split("(:|-)");
        for (int i = 0; i < 6; i++) {
            macBytes[i] = (byte) Integer.parseInt(hex[i], 16);
        }

        byte[] bytes = new byte[6 + 16 * macBytes.length];
        for (int i = 0; i < 6; i++) {
            bytes[i] = (byte) 0xff;
        }
        for (int i = 6; i < bytes.length; i += macBytes.length) {
            System.arraycopy(macBytes, 0, bytes, i, macBytes.length);
        }

        InetAddress address = InetAddress.getByName(host);
        DatagramPacket packet = new DatagramPacket(bytes, bytes.length, address, port);

        try (DatagramSocket socket = new DatagramSocket()) {
            socket.setBroadcast(true);
            socket.send(packet);
            System.out.println("Packet sent successfully to " + host + ":" + port);
        }
    }
}
