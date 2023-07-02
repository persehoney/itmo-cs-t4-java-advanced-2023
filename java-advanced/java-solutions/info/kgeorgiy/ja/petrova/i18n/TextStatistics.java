package info.kgeorgiy.ja.petrova.i18n;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.*;

public class TextStatistics {
    private static final Locale[] textLocales = Locale.getAvailableLocales();
    private static final Locale[] reportLocales = Arrays.stream(textLocales)
            .filter(locale -> locale.toString().matches("^(ru).*") || locale.toString().matches("^(en).*"))
            .toArray(Locale[]::new);
    private static final String bundlePrefixName = "info.kgeorgiy.ja.petrova.i18n.resources.ListResourceBundle_";

    public static void main(String[] args) {
        if (checkArgs(args)) {
            try {
                Locale textLocale = Objects.requireNonNull(getLocale(args[0], "text"));
                Locale reportLocale = Objects.requireNonNull(getLocale(args[1], "report"));
                Path textPath = Objects.requireNonNull(getTextPath(args[2]));
                Path reportPath = Objects.requireNonNull(getReportPath(args[3], textPath));

                TextAnalyzer analyzer = new TextAnalyzer(textLocale, textPath);
                String report = formReport(analyzer.analyzeText(), reportLocale, textPath.toString());
                System.out.println(report);
                try {
                    BufferedWriter writer = new BufferedWriter(new FileWriter(reportPath.toFile(),
                            StandardCharsets.UTF_8));
                    writer.write(report);
                    writer.close();
                } catch (IOException e) {
                    System.err.println("Cannot form a report: " + e.getMessage());
                }
            } catch (NullPointerException ignored) {
            }
        }
    }

    private static boolean checkArgs(String[] args) {
        boolean result = true;
        if (args.length != 4) {
            result = false;
            System.err.println("Usage: text locale, report locale, text file, report file");
        }
        for (String arg : args) {
            if (arg == null) {
                result = false;
                System.err.println("Some arguments are missing");
            }
        }
        return result;
    }

    private static Locale getLocale(String localeName, String fileType) {
        Locale resultLocale = null;
        Locale[] locales = textLocales;
        if (fileType.equals("report")) {
            locales = reportLocales;
        }
        try {
            resultLocale = Arrays.stream(locales)
                    .filter(locale -> locale.toString().equals(localeName))
                    .findFirst().orElseThrow();
            if (localeName.contains("en")) {
                resultLocale = Locale.US;
            }
        } catch (NoSuchElementException e) {
            System.err.printf("Incorrect %s locale%n", fileType);
            System.err.println("Available locales:");
            Arrays.stream(locales).forEach(System.err::println);
        }
        return resultLocale;
    }

    private static Path getTextPath(String text) {
        Path textPath = Path.of(text);
        if (!textPath.toFile().exists()) {
            System.err.println("Input file doesn't exist");
            return null;
        }
        return textPath;
    }

    private static Path getReportPath(String report, Path textPath) {
        Path reportPath = Path.of(report);
        if (!reportPath.toFile().exists()) {
            try {
                Files.createDirectories(reportPath.getParent());
            } catch (IOException e) {
                System.err.println("Cannot create report directories");
                return null;
            } catch (NullPointerException e) {
                Path textDirectories = textPath.getParent();
                reportPath = Path.of(textDirectories + File.separator + report);
            }
        }
        return reportPath;
    }

    private static String formReport(Statistics statistics, Locale reportLocale, String textFile) {
        ResourceBundle bundle = ResourceBundle.getBundle(bundlePrefixName + "ru");
        if (reportLocale.getLanguage().equals("en")) {
            bundle = ResourceBundle.getBundle(bundlePrefixName + "en");
        }

        return String.format(
                """
                        %s: "%s"
                        %s %s
                            %s %s: %d.
                            %s %s: %d.
                            %s %s: %d.
                            %s %s: %d.
                            %s %s: %d.
                        %s
                            %s %s: %d (%d %s).
                            %s %s: "%s".
                            %s %s: "%s".
                            %s %s: %d ("%s").
                            %s %s: %d ("%s").
                            %s %s: %f.
                        %s
                            %s %s: %d (%d %s).
                            %s %s: "%s".
                            %s %s: "%s".
                            %s %s: %d ("%s").
                            %s %s: %d ("%s").
                            %s %s: %f.
                        %s
                            %s: %d (%d %s).
                            %s %s: %f.
                            %s %s: %f.
                            %s %s: %f.
                        %s
                            %s: %d (%d %s).
                            %s %s: %s.
                            %s %s: %s.
                            %s %s: %f.
                        %s
                            %s: %d (%d %s).
                            %s %s: %s.
                            %s %s: %s.
                            %s %s: %s.
                        """,
                bundle.getString("analyzedFile"),
                textFile,

                bundle.getString("Summary"),
                bundle.getString("statistics"),
                bundle.getString("Number"),
                bundle.getString("of sentences"),
                statistics.sentenceStatistics.instances,
                bundle.getString("Number"),
                bundle.getString("of words"),
                statistics.wordStatistics.instances,
                bundle.getString("Number"),
                bundle.getString("of numbers"),
                statistics.numbers,
                bundle.getString("Number"),
                bundle.getString("of sums"),
                statistics.sums,
                bundle.getString("Number"),
                bundle.getString("of dates"),
                statistics.dates,

                bundle.getString("Sentence statistics"),
                bundle.getString("Number"),
                bundle.getString("of sentences"),
                statistics.sentenceStatistics.instances,
                statistics.sentenceStatistics.uniqueInstances,
                bundle.getString("of unique"),
                bundle.getString("Minimal"),
                bundle.getString("sentence"),
                statistics.sentenceStatistics.minInstance,
                bundle.getString("Maximal"),
                bundle.getString("sentence"),
                statistics.sentenceStatistics.maxInstance,
                bundle.getString("Minimal length"),
                bundle.getString("of a sentence"),
                statistics.sentenceStatistics.minInstanceLength,
                statistics.sentenceStatistics.shortestInstance,
                bundle.getString("Maximal length"),
                bundle.getString("of a sentence"),
                statistics.sentenceStatistics.maxInstanceLength,
                statistics.sentenceStatistics.longestInstance,
                bundle.getString("Average length"),
                bundle.getString("of a sentence"),
                statistics.sentenceStatistics.avgInstanceLength,

                bundle.getString("Words statistics"),
                bundle.getString("Number"),
                bundle.getString("of words"),
                statistics.wordStatistics.instances,
                statistics.wordStatistics.uniqueInstances,
                bundle.getString("of unique"),
                bundle.getString("Minimal"),
                bundle.getString("word"),
                statistics.wordStatistics.minInstance,
                bundle.getString("Maximal"),
                bundle.getString("word"),
                statistics.wordStatistics.maxInstance,
                bundle.getString("Minimal length"),
                bundle.getString("of a word"),
                statistics.wordStatistics.minInstanceLength,
                statistics.wordStatistics.shortestInstance,
                bundle.getString("Maximal length"),
                bundle.getString("of a word"),
                statistics.wordStatistics.maxInstanceLength,
                statistics.wordStatistics.longestInstance,
                bundle.getString("Average length"),
                bundle.getString("of a word"),
                statistics.wordStatistics.avgInstanceLength,

                bundle.getString("Number statistics"),
                bundle.getString("Number count"),
                statistics.numbers,
                statistics.uniqueNumbers,
                bundle.getString("of unique"),
                bundle.getString("Minimal"),
                bundle.getString("number"),
                statistics.minNumber,
                bundle.getString("Maximal"),
                bundle.getString("number"),
                statistics.maxNumber,
                bundle.getString("Average"),
                bundle.getString("number"),
                statistics.avgNumber,

                bundle.getString("Money statistics"),
                bundle.getString("Money sum count"),
                statistics.sums,
                statistics.uniqueSums,
                bundle.getString("of unique"),
                bundle.getString("MinimalFem"),
                bundle.getString("sum"),
                statistics.minSum,
                bundle.getString("MaximalFem"),
                bundle.getString("sum"),
                statistics.maxSum,
                bundle.getString("AverageFem"),
                bundle.getString("sum"),
                statistics.avgSum,

                bundle.getString("Date statistics"),
                bundle.getString("Date count"),
                statistics.dates,
                statistics.uniqueDates,
                bundle.getString("of unique"),
                bundle.getString("MinimalFem"),
                bundle.getString("date"),
                statistics.minDate,
                bundle.getString("MaximalFem"),
                bundle.getString("date"),
                statistics.maxDate,
                bundle.getString("AverageFem"),
                bundle.getString("date"),
                statistics.avgDate
        );
    }
}
