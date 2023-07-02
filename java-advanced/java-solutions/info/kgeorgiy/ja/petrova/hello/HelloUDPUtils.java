package info.kgeorgiy.ja.petrova.hello;

import java.io.IOException;
import java.net.StandardSocketOptions;
import java.nio.channels.DatagramChannel;
import java.nio.channels.SelectionKey;
import java.nio.channels.Selector;
import java.util.Arrays;
import java.util.concurrent.ExecutorService;

public class HelloUDPUtils {
    protected static void checkArgs(String[] args, int len) {
        if (args.length != len) {
            throw new RuntimeException("Incorrect number of arguments");
        }
        Arrays.stream(args).forEach(arg -> {
            if (arg == null) {
                throw new RuntimeException("Some arguments are missing");
            }
        });
    }

    protected static void shutdownAndAwaitTermination(ExecutorService pool) {
        pool.shutdown();
        pool.close();
    }

    protected static DatagramChannel openChannel(Selector selector) throws IOException {
        DatagramChannel channel = DatagramChannel.open();
        channel.configureBlocking(false);
        channel.register(selector, SelectionKey.OP_READ);
        channel.setOption(StandardSocketOptions.SO_REUSEADDR, true);
        return channel;
    }
}
