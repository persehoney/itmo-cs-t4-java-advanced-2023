package info.kgeorgiy.ja.petrova.i18n;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.text.*;
import java.util.*;
import java.util.regex.Pattern;

public class TextAnalyzer {
    private final Locale locale;
    private final Path textPath;
    private String text;
    private final Statistics statistics = new Statistics();
    private final NumberFormat numberParser;
    private final NumberFormat currencyParser;
    private final DateFormat dateParser;
    private final List<Double> numbers = new ArrayList<>();
    private final List<MoneyStatistics> sums = new ArrayList<>();
    private final List<DateStatistics> dates = new ArrayList<>();

    public TextAnalyzer(Locale locale, Path textPath) {
        this.locale = locale;
        this.textPath = textPath;

        this.currencyParser = NumberFormat.getCurrencyInstance(locale);
        this.numberParser = NumberFormat.getNumberInstance(locale);
        this.dateParser = DateFormat.getDateInstance(DateFormat.SHORT, locale);
    }

    protected Statistics analyzeText() {
        try {
            this.text = Files.readString(textPath);
        } catch (IOException ignored) {
        }

        getTextStatistic(BreakIterator.getSentenceInstance(locale), "sentence");
        getTextStatistic(BreakIterator.getWordInstance(locale), "word");

        buildStatistics();

        return statistics;
    }

    private void getTextStatistic(BreakIterator it, String mode) {
        Statistics.TextInstanceStatistic statistic = new Statistics.TextInstanceStatistic();
        List<String> instances = new ArrayList<>();
        Collator comparator = Collator.getInstance(locale);
        double textLength = 0;

        it.setText(text);
        int begin = it.first();
        int end = it.next();

        while (end != BreakIterator.DONE) {
            String instance = text.substring(begin, end).trim();

            if (!instance.isEmpty() && !(mode.equals("word") && Pattern.matches("\\p{Punct}", instance))) {
                instances.add(instance);
                textLength += instance.length();

                if (statistic.minInstance == null || comparator.compare(instance, statistic.minInstance) < 0) {
                    statistic.minInstance = instance;
                }

                if (statistic.maxInstance == null || comparator.compare(statistic.maxInstance, instance) < 0) {
                    statistic.maxInstance = instance;
                }

                if (statistic.shortestInstance == null || instance.length() < statistic.minInstanceLength) {
                    statistic.setShortestInstance(instance);
                }

                if (statistic.longestInstance == null || instance.length() > statistic.maxInstanceLength) {
                    statistic.setLongestInstance(instance);
                }

                if (mode.equals("word")) {
                    try {
                        Number number = numberParser.parse(instance);
                        try {
                            Calendar date = Calendar.getInstance(locale);
                            date.setTime(dateParser.parse(instance));
                            dates.add(new DateStatistics(date, instance));
                        } catch (ParseException e) {
                            numbers.add(number.doubleValue());
                        }
                    } catch (ParseException ignored) {
                    }
                    try {
                        sums.add(new MoneyStatistics(currencyParser.parse(instance), instance));
                    } catch (ParseException ignored) {
                    }
                }
            }

            begin = end;
            end = it.next();
        }
        if (!instances.isEmpty()) {
            statistic.instances = instances.size();
            statistic.uniqueInstances = instances.stream().distinct().count();
            statistic.avgInstanceLength = textLength / statistic.instances;
        }
        switch (mode) {
            case "sentence":
                statistics.sentenceStatistics = statistic;
            case "word":
                statistics.wordStatistics = statistic;
        }
    }

    private void buildStatistics() {
        if (!numbers.isEmpty()) {
            statistics.numbers = numbers.size();
            statistics.uniqueNumbers = numbers.stream().distinct().count();
            statistics.minNumber = numbers.stream().min(Double::compare).orElseThrow();
            statistics.maxNumber = numbers.stream().max(Double::compare).orElseThrow();
            statistics.avgNumber = numbers.stream().reduce(Double::sum).orElseThrow() / numbers.size();
        }

        if (!sums.isEmpty()) {
            statistics.sums = sums.size();
            statistics.uniqueSums = sums.stream().distinct().count();
            statistics.minSum = sums.stream()
                    .reduce((e1, e2) -> (e1.sum().doubleValue() < e2.sum().doubleValue()) ? e1 : e2)
                    .orElseThrow().source();
            statistics.maxSum = sums.stream()
                    .reduce((e1, e2) -> (e1.sum().doubleValue() < e2.sum().doubleValue()) ? e2 : e1)
                    .orElseThrow().source();
            statistics.avgSum = sums.stream().map(e -> e.sum().doubleValue())
                    .reduce(Double::sum).orElseThrow() / sums.size();
        }

        if (!dates.isEmpty()) {
            statistics.dates = dates.size();
            statistics.uniqueDates = dates.stream().distinct().count();
            statistics.minDate = dates.stream()
                    .reduce((e1, e2) -> (e1.date().before(e2.date())) ? e1 : e2).orElseThrow().source();
            statistics.maxDate = dates.stream()
                    .reduce((e1, e2) -> (e1.date().before(e2.date())) ? e2 : e1).orElseThrow().source();
            statistics.avgDate = dateParser.format(new Date(dates.stream().map(e -> e.date().getTime().getTime())
                    .reduce(Long::sum).orElseThrow() / dates.size()));
        }
    }

    private record MoneyStatistics(Number sum, String source) {
    }

    private record DateStatistics(Calendar date, String source) {
    }
}
