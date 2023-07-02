package info.kgeorgiy.ja.petrova.hello;

import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;

public abstract class AbstractHelloUDPServer {
    private final Charset UTF_8 = StandardCharsets.UTF_8;
    public static void main(String[] args) {
        HelloUDPUtils.checkArgs(args, 2);
        try {
            int port = Integer.parseInt(args[0]);
            int threads = Integer.parseInt(args[1]);
            try (HelloUDPNonblockingServer server = new HelloUDPNonblockingServer()) {
                server.start(port, threads);
            }
        } catch (NumberFormatException e) {
            System.err.println("Arguments must be integers");
        }
    }

    protected byte[] buildResponse(String data) {
        return ("Hello, " + data).getBytes(UTF_8);
    }
}
