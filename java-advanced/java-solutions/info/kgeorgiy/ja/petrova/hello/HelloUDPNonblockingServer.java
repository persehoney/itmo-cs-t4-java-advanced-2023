package info.kgeorgiy.ja.petrova.hello;

import info.kgeorgiy.java.advanced.hello.HelloServer;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.net.SocketAddress;
import java.net.SocketException;
import java.nio.ByteBuffer;
import java.nio.channels.DatagramChannel;
import java.nio.channels.SelectionKey;
import java.nio.channels.Selector;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import java.util.Iterator;
import java.util.Queue;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

public class HelloUDPNonblockingServer extends AbstractHelloUDPServer implements HelloServer {
    private final Charset UTF_8 = StandardCharsets.UTF_8;
    private ExecutorService taskExecutor;
    private DatagramChannel channel;
    private Selector selector;
    private Queue<ByteBuffer> buffers;
    private final Queue<Task> tasks = new LinkedBlockingQueue<>();
    private final ExecutorService requestExecutor = Executors.newSingleThreadExecutor();

    @Override
    public void start(int port, int threads) {
        try {
            selector = Selector.open();
            try {
                channel = HelloUDPUtils.openChannel(selector);
                channel.bind(new InetSocketAddress(port));

                try {
                    final int capacity = channel.socket().getReceiveBufferSize();
                    buffers = IntStream.range(0, threads)
                            .mapToObj((i) -> ByteBuffer.allocate(capacity))
                            .collect(Collectors.toCollection(LinkedBlockingQueue::new));
                    taskExecutor = Executors.newFixedThreadPool(threads);
                    requestExecutor.submit(this::run);
                } catch (SocketException e) {
                    System.err.println("UDP error occurred, cannot get buffer size: " + e.getMessage());
                }
            } catch (IOException e) {
                System.err.println("I/O error occurred, cannot open a datagram channel: " + e.getMessage());
            }
        } catch (IOException e) {
            System.err.println("I/O error occurred, cannot open a selector: " + e.getMessage());
        }
    }

    private void run() {
        while (selector.isOpen() && !Thread.interrupted()) {
            try {
                selector.select();
                Iterator<SelectionKey> it = selector.selectedKeys().iterator();
                while (it.hasNext()) {
                    final SelectionKey key = it.next();
                    try {
                        if (key.isReadable()) {
                            read(key);
                        }
                        if (key.isWritable()) {
                            write(key);
                        }
                    } finally {
                        it.remove();
                    }
                }
            } catch (IOException e) {
                System.err.println("I/O error occurred, cannot select key set: " + e.getMessage());
            }
        }
    }

    private void read(SelectionKey key) {
        if (buffers.isEmpty()) {
            key.interestOpsAnd(~SelectionKey.OP_READ);
            return;
        }

        final ByteBuffer buffer = buffers.poll();
        try {
            final SocketAddress address = channel.receive(buffer);
            taskExecutor.submit(buildTask(key, address, buffer));
        } catch (IOException e) {
            System.err.println("I/O error occurred, cannot receive data: " + e.getMessage());
        }
    }

    private Runnable buildTask(SelectionKey key, SocketAddress address, ByteBuffer buffer) {
        return () -> {
            byte[] response = buildResponse(UTF_8.decode(buffer.flip()).toString());
            buffer.clear();
            buffer.put(response);
            tasks.add(new Task(address, buffer));

            key.interestOpsOr(SelectionKey.OP_WRITE);
            selector.wakeup();
        };
    }

    private void write(SelectionKey key) {
        if (tasks.isEmpty()) {
            key.interestOpsAnd(~SelectionKey.OP_WRITE);
            return;
        }

        Task task = tasks.poll();
        try {
            channel.send(task.bytes.flip(), task.address);
        } catch (IOException e) {
            System.err.println("I/O error occurred, cannot send data: " + e.getMessage());
        }
        task.bytes.clear();
        buffers.add(task.bytes);
        key.interestOpsOr(SelectionKey.OP_READ);
    }

    @Override
    public void close() {
        try {
            if (selector != null) {
                selector.close();
            }
            if (channel != null) {
                channel.close();
            }
        } catch (IOException e) {
            System.err.println("I/O error occurred: cannot close the selector");
        }
        HelloUDPUtils.shutdownAndAwaitTermination(requestExecutor);
        HelloUDPUtils.shutdownAndAwaitTermination(taskExecutor);
    }

    private record Task(SocketAddress address, ByteBuffer bytes) {
    }
}