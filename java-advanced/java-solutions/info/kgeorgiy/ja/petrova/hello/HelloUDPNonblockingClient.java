package info.kgeorgiy.ja.petrova.hello;

import info.kgeorgiy.java.advanced.hello.HelloClient;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.nio.ByteBuffer;
import java.nio.channels.DatagramChannel;
import java.nio.channels.SelectionKey;
import java.nio.channels.Selector;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.Objects;
import java.util.stream.IntStream;

public class HelloUDPNonblockingClient extends AbstractHelloUDPClient implements HelloClient {
    public static final Charset UTF_8 = StandardCharsets.UTF_8;
    private InetSocketAddress address;
    Map<DatagramChannel, Integer> channels = new HashMap<>();

    @Override
    public void run(String host, int port, String prefix, int threads, int requests) {
        address = new InetSocketAddress(host, port);
        int[] requestsPerChannel = new int[threads + 1];

        try (Selector selector = Selector.open()) {
            IntStream.range(1, threads + 1).forEach((thread -> createChannel(selector, thread)));

            int threadCounter = 0;
            while (threadCounter < threads && !Thread.interrupted()) {
                selector.select(100);

                if (selector.selectedKeys().isEmpty()) {
                    for (SelectionKey key : selector.keys()) {
                        key.interestOps(SelectionKey.OP_WRITE);
                    }
                }

                Iterator<SelectionKey> it = selector.selectedKeys().iterator();
                while (it.hasNext()) {
                    SelectionKey key = it.next();
                    it.remove();

                    DatagramChannel channel = (DatagramChannel) key.channel();
                    int thread = channels.get(channel);
                    String request = buildRequest(prefix, thread, requestsPerChannel[thread] + 1);

                    if (key.isWritable()) {
                        key.interestOps(SelectionKey.OP_READ);
                        if (requestsPerChannel[thread] >= requests) {
                            break;
                        }
                        channel.write(ByteBuffer.wrap(request.getBytes(UTF_8)));
                    }
                    if (key.isReadable() && responseIsCorrect(channel, request)) {
                        key.interestOps(SelectionKey.OP_WRITE);
                        if (++requestsPerChannel[thread] >= requests) {
                            threadCounter++;
                        }
                    }
                }
            }
        } catch (IOException e) {
            System.err.println("I/O error occurred: " + e.getMessage());
        } finally {
            for (DatagramChannel channel : channels.keySet()) {
                try {
                    channel.close();
                } catch (IOException e) {
                    System.err.println("I/O error occurred: " + e.getMessage());
                }
            }
        }
    }

    private boolean responseIsCorrect(DatagramChannel channel, String requestData) {
        ByteBuffer buffer = null;
        try {
            buffer = ByteBuffer.allocate(channel.socket().getReceiveBufferSize());
        } catch (IOException e) {
            System.err.println("UDP error occurred, cannot get buffer size: " + e.getMessage());
        }
        try {
            channel.receive(buffer);
        } catch (IOException e) {
            System.err.println("I/O error occurred, cannot receive data: " + e.getMessage());
        }
        return new String(Objects.requireNonNull(buffer).array(), UTF_8).trim().contains(requestData);
    }

    private void createChannel(Selector selector, int thread) {
        try {
            DatagramChannel channel = HelloUDPUtils.openChannel(selector);
            channel.connect(address);
            channels.put(channel, thread);
        } catch (IOException e) {
            System.err.println("I/O error occurred, cannot open a datagram channel: " + e.getMessage());
        }
    }
}