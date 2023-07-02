package info.kgeorgiy.ja.petrova.hello;

public abstract class AbstractHelloUDPClient {
    public static void main(String[] args) {
        HelloUDPUtils.checkArgs(args, 5);
        try {
            String host = args[0];
            int port = Integer.parseInt(args[1]);
            String prefix = args[2];
            int threads = Integer.parseInt(args[3]);
            int requests = Integer.parseInt(args[4]);

            HelloUDPClient client = new HelloUDPClient();
            client.run(host, port, prefix, threads, requests);
        } catch (NumberFormatException e) {
            System.err.println("Port, number of threads and number of requests must be integers");
        }
    }

    protected static String buildRequest(String prefix, int thread, int requestNumber) {
        return prefix + thread + "_" + requestNumber;
    }
}
