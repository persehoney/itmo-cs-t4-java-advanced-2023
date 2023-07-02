package info.kgeorgiy.ja.petrova.hello;

import info.kgeorgiy.java.advanced.hello.HelloClient;

import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetSocketAddress;
import java.net.SocketException;
import java.nio.charset.StandardCharsets;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.stream.IntStream;

public class HelloUDPClient extends AbstractHelloUDPClient implements HelloClient {
    @Override
    public void run(String host, int port, String prefix, int threads, int requests) {
        try (ExecutorService requestExecutor = Executors.newFixedThreadPool(threads)) {
            IntStream.range(1, threads + 1).forEach((thread) -> requestExecutor.submit(() ->
                    sendRequests(thread, requests, prefix, new InetSocketAddress(host, port))));
            HelloUDPUtils.shutdownAndAwaitTermination(requestExecutor);
        }
    }

    private void sendRequests(int thread, int requests, String prefix, InetSocketAddress address) {
        try (DatagramSocket socket = new DatagramSocket()) {
            socket.setSoTimeout(300);

            byte[] responseBuf = new byte[socket.getReceiveBufferSize()];
            DatagramPacket response = new DatagramPacket(responseBuf, responseBuf.length);

            IntStream.range(1, requests + 1).forEach((requestNumber ->
                    sendRequest(response, socket, requestNumber, thread, prefix, address)));
        } catch (SocketException e) {
            System.err.println("UDP error occurred: " + e.getMessage());
        }
    }

    private void sendRequest(DatagramPacket response, DatagramSocket socket, int requestNumber, int thread, String prefix, InetSocketAddress address) {
        String requestData = buildRequest(prefix, thread, requestNumber);
        DatagramPacket request = new DatagramPacket(requestData.getBytes(StandardCharsets.UTF_8), requestData.length(), address);

        while (!socket.isClosed()) {
            try {
                socket.send(request);
                socket.receive(response);
            } catch (IOException e) {
                System.err.println("IOException occurred: " + e.getMessage());
            }

            String responseData = new String(response.getData(), response.getOffset(), response.getLength(),
                    StandardCharsets.UTF_8);
            if (responseData.contains(requestData)) {
                System.out.println(requestData);
                break;
            }
        }
    }
}
