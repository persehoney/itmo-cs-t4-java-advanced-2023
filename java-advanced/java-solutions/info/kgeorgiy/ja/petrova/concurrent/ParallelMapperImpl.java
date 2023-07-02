package info.kgeorgiy.ja.petrova.concurrent;

import info.kgeorgiy.java.advanced.mapper.ParallelMapper;

import java.util.*;
import java.util.function.Function;
import java.util.stream.IntStream;

public class ParallelMapperImpl implements ParallelMapper {
    private final List<Thread> threadList;
    private final Queue<Task> taskQueue = new ArrayDeque<>();

    public ParallelMapperImpl(int threads) {
        threadList = IntStream.range(0, threads)
                .mapToObj((i) -> new Thread(new Worker()))
                .toList();

        threadList.forEach(Thread::start);
    }

    @Override
    public <T, R> List<R> map(
            Function<? super T, ? extends R> f,
            List<? extends T> args
    ) throws InterruptedException {

        List<R> results = new ArrayList<>(Collections.nCopies(args.size(), null));
        final Waiter waiter = new Waiter(args.size());

        List<Task> newTasks = IntStream.range(0, args.size())
                .mapToObj((i) -> new Task(waiter, () -> results.set(i, f.apply(args.get(i)))))
                .toList();

        synchronized (taskQueue) {
            taskQueue.addAll(newTasks);
            taskQueue.notifyAll();
        }

        synchronized (waiter) {
            while (waiter.counter < waiter.border) {
                waiter.wait();
            }
        }

//        RuntimeException e = null;
        newTasks.forEach(task -> {
            if (task.exception != null) {
//                e = task.exception;
//                e.addSuppressed(task.exception);
                throw task.exception;
            }
        });

        return results;
    }

    private static class Waiter {
        private int counter;

        private final int border;

        private Waiter(int border) {
            this.border = border;
        }

    }

    @Override
    public void close() {
        threadList.forEach(Thread::interrupt);
        threadList.forEach(thread -> {
            while (true) {
                try {
                    thread.join();
                    break;
                } catch (InterruptedException ignored) {
                }
            }
        });
    }

    private static class Task implements Runnable {
        private final Runnable task;
        public RuntimeException exception;
        final Waiter waiter;

        Task(Waiter waiter, Runnable task) {
            this.waiter = waiter;
            this.task = task;
        }

        @Override
        public void run() {
            try {
                task.run();
            } catch (RuntimeException e) {
                exception = e;
            }
        }
    }

    private class Worker implements Runnable {
        @Override
        public void run() {
            try {
                while (!Thread.interrupted()) {
                    Task task;
                    synchronized (taskQueue) {
                        while (taskQueue.isEmpty()) {
                            taskQueue.wait();
                        }

                        task = taskQueue.poll();
                    }

                    task.run();

                    synchronized (task.waiter) {
                        task.waiter.counter++;
                        if (task.waiter.counter >= task.waiter.border) {
                            task.waiter.notify();
                        }
                    }
                }
            } catch (InterruptedException ignored) {
                Thread.currentThread().interrupt();
            }
        }
    }
}
