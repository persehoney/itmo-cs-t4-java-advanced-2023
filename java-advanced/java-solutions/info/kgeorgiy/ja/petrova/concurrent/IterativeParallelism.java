package info.kgeorgiy.ja.petrova.concurrent;

import info.kgeorgiy.java.advanced.concurrent.ScalarIP;
import info.kgeorgiy.java.advanced.mapper.ParallelMapper;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.function.Function;
import java.util.function.Predicate;
import java.util.stream.IntStream;

public class IterativeParallelism implements ScalarIP {
    private final ParallelMapper mapper;

    public IterativeParallelism() {
        this.mapper = null;
    }

    public IterativeParallelism(ParallelMapper mapper) {
        this.mapper = mapper;
    }

    @Override
    public <T> T maximum(int threads, List<? extends T> values, Comparator<? super T> comparator)
            throws InterruptedException {
        return mapReduce(threads, values,
                list -> list.stream().max(comparator).orElseThrow(),
                list -> list.stream().max(comparator).orElseThrow());
    }

    @Override
    public <T> T minimum(int threads, List<? extends T> values, Comparator<? super T> comparator)
            throws InterruptedException {
        return maximum(threads, values, Collections.reverseOrder(comparator));
    }

    @Override
    public <T> boolean all(int threads, List<? extends T> values, Predicate<? super T> predicate)
            throws InterruptedException {
        return mapReduce(threads, values,
                list -> list.stream().allMatch(predicate),
                list -> list.stream().allMatch(item -> item));
    }

    @Override
    public <T> boolean any(int threads, List<? extends T> values, Predicate<? super T> predicate)
            throws InterruptedException {
        return !(all(threads, values, predicate.negate()));
    }

    @Override
    public <T> int count(int threads, List<? extends T> values, Predicate<? super T> predicate)
            throws InterruptedException {
        return mapReduce(threads, values,
                list -> (list.stream().filter(predicate).count()),
                list -> (list.stream().mapToInt(Long::intValue).sum()));
    }

    /**
     * Creates given number of threads, splits list of {@code values} between them and applies given {@code function}
     * to each sublist in parallel. Collects return values of threads in a list {@code result} and applies
     * {@code finalFunction} to it. Returns result of {@code finalFunction}.
     *
     * @param threadsCount  number of concurrent threads.
     * @param values        values to operate.
     * @param function      function to apply to values in each thread.
     * @param finalFunction function to apply to return values of threads.
     * @param <T>           value type.
     * @param <G>           operating type.
     * @param <R>           result type.
     * @return result of applying given functions to {@code values}.
     * @throws InterruptedException if executing thread was interrupted.
     */
    private <T, R, G> R mapReduce(int threadsCount,
                                  List<? extends T> values,
                                  Function<List<? extends T>, G> function,
                                  Function<List<G>, R> finalFunction) throws InterruptedException {
        threadsCount = Math.max(1, Math.min(threadsCount, values.size()));
        List<G> result = new ArrayList<>();
        for (int i = 0; i < threadsCount; i++) {
            result.add(null);
        }

        int batch = values.size() / threadsCount;
        final int[] rest = {values.size() % threadsCount};

        final int[] r = {0};
        if (this.mapper == null) {
            List<Thread> threads = new ArrayList<>();

            // :NOTE: dupcode
            IntStream.range(0, threadsCount).forEach(i -> {
                int l = r[0];
                r[0] = l + batch + (rest[0]-- > 0 ? 1 : 0);
                List<? extends T> sub = values.subList(l, r[0]);
                Thread thread = new Thread(() -> result.set(i, function.apply(sub)));
                threads.add(thread);
                thread.start();
            });
            for (Thread thread : threads) {
                thread.join();
            }
            return finalFunction.apply(result);
        }
        List<List<? extends T>> sublists = new ArrayList<>();
        IntStream.range(0, threadsCount).forEach(i -> {
            int l = r[0];
            r[0] = l + batch + (rest[0]-- > 0 ? 1 : 0);
            sublists.add(values.subList(l, r[0]));
        });
        List<G> finalResult = mapper.map(function, sublists);
        return finalFunction.apply(finalResult);
    }
}
