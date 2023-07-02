package info.kgeorgiy.ja.petrova.hello;

import info.kgeorgiy.java.advanced.hello.HelloServer;

import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.SocketException;
import java.nio.charset.StandardCharsets;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class HelloUDPServer extends AbstractHelloUDPServer implements HelloServer {
    private DatagramSocket socket;
    private ExecutorService requestExecutor;
    private ExecutorService receiver;

    @Override
    public void start(int port, int threads) {
        try {
            socket = new DatagramSocket(port);
            requestExecutor = Executors.newFixedThreadPool(threads);
            receiver = Executors.newSingleThreadExecutor();
        } catch (SocketException e) {
            System.err.println("Socket cannot be opened or bind to the specified port");
        }

        receiver.submit(() -> {
            while (!socket.isClosed()) {
                try {
                    byte[] requestBuf = new byte[socket.getReceiveBufferSize()];
                    DatagramPacket request = new DatagramPacket(requestBuf, requestBuf.length);
                    socket.receive(request);

                    requestExecutor.submit(() -> {
                        byte[] responseBuf = new byte[0];
                        DatagramPacket response = new DatagramPacket(responseBuf, responseBuf.length,
                                request.getSocketAddress());
                        String data = new String(request.getData(), request.getOffset(), request.getLength(),
                                StandardCharsets.UTF_8);
                        response.setData(buildResponse(data));
                        try {
                            socket.send(response);
                        } catch (IOException e) {
                            System.err.println("IOException occurred: " + e.getMessage());
                        }
                    });
                } catch (SocketException e) {
                    System.err.println("UDP error occurred");
                } catch (IOException e) {
                    System.err.println("IOException occurred: " + e.getMessage());
                }
            }
        });
    }

    @Override
    public void close() {
        socket.close();
        HelloUDPUtils.shutdownAndAwaitTermination(requestExecutor);
        HelloUDPUtils.shutdownAndAwaitTermination(receiver);
    }
}