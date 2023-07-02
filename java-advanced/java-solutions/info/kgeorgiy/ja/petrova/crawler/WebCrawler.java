package info.kgeorgiy.ja.petrova.crawler;

import info.kgeorgiy.java.advanced.crawler.*;

import java.io.IOException;
import java.util.Arrays;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.*;
import java.util.stream.Collectors;

public class WebCrawler implements Crawler {
    private final Downloader downloader;
    public int perHost;
    private final ExecutorService downloadExecutor;
    private final ExecutorService extractExecutor;

    public static void main(String[] args) {
        checkArgs(args);
        String url = args[0];
        try {
            int depth = Integer.parseInt(args[1]);
            int downloaders = Integer.parseInt(args[2]);
            int extractors = Integer.parseInt(args[3]);
            int perHost = Integer.parseInt(args[4]);

            try (WebCrawler crawler = new WebCrawler(new CachingDownloader(0.1), downloaders, extractors, perHost)) {
                Result result = crawler.download(url, depth);
                System.out.println("Downloaded: ");
                result.getDownloaded().forEach(System.out::println);
                System.out.println("Errors: ");
                result.getErrors().keySet().forEach(System.out::println);
            } catch (IOException e) {
                System.out.println("IOException occurred: " + e.getMessage());
            }
        } catch (NumberFormatException e) {
            throw new RuntimeException("Last 4 arguments must be integers");
        }
    }

    private static void checkArgs(String[] args) {
        if (args.length != 5) {
            throw new RuntimeException("Incorrect number of arguments");
        }
        Arrays.stream(args).forEach(arg -> {
            if (arg == null) {
                throw new RuntimeException("Some arguments are missing");
            }
        });
    }

    public WebCrawler(Downloader downloader, int downloaders, int extractors, int perHost) {
        this.downloader = downloader;
        downloadExecutor = Executors.newFixedThreadPool(downloaders);
        extractExecutor = Executors.newFixedThreadPool(extractors);
        this.perHost = perHost;
    }

    @Override
    public Result download(String rootUrl, int maxDepth) {
        Set<String> urls = Set.of(rootUrl);
        Set<String> downloaded = ConcurrentHashMap.newKeySet();
        Map<String, IOException> errors = new ConcurrentHashMap<>();

        for (int depth = maxDepth; depth >= 1; depth--) {
            Phaser phaser = new Phaser(1);
            Set<String> newUrls = ConcurrentHashMap.newKeySet();
            urls.forEach(url -> {
                phaser.register();
                downloadExecutor.submit(() -> downloadTask(downloaded, phaser, url, newUrls, errors));
            });
            phaser.arriveAndAwaitAdvance();
            urls = newUrls.stream()
                    .filter(url -> !downloaded.contains(url) && !errors.containsKey(url))
                    .collect(Collectors.toSet());
        }
        return new Result(downloaded.stream().toList(), errors);
    }

    private void downloadTask(Set<String> downloaded, Phaser phaser, String url, Set<String> newUrls,
                              Map<String, IOException> errors) {
        try {
            Document document = downloader.download(url);
            downloaded.add(url);
            phaser.register();
            extractExecutor.submit(() -> extractTask(document, phaser, url, newUrls, errors));
        } catch (IOException e) {
            errors.put(url, e);
        } finally {
            phaser.arrive();
        }
    }

    private void extractTask(Document document, Phaser phaser, String url, Set<String> newUrls,
                             Map<String, IOException> errors) {
        try {
            newUrls.addAll(document.extractLinks());
        } catch (IOException e) {
            errors.put(url, e);
        } finally {
            phaser.arrive();
        }
    }

    @Override
    public void close() {
        shutdownAndAwaitTermination(downloadExecutor);
        shutdownAndAwaitTermination(extractExecutor);
    }

    private void shutdownAndAwaitTermination(ExecutorService pool) {
        pool.shutdown();
        try {
            if (!pool.awaitTermination(60, TimeUnit.SECONDS)) {
                pool.shutdownNow();
                if (!pool.awaitTermination(60, TimeUnit.SECONDS))
                    System.err.println("Pool did not terminate");
            }
        } catch (InterruptedException ie) {
            pool.shutdownNow();
            Thread.currentThread().interrupt();
        }
    }
}
