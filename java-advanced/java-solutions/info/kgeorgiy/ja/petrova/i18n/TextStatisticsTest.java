package info.kgeorgiy.ja.petrova.i18n;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.nio.file.Path;
import java.util.Arrays;
import java.util.Locale;

class TextStatisticsTest {
    Locale[] locales = {
            Locale.ENGLISH,
            Arrays.stream(Locale.getAvailableLocales())
                    .filter(locale -> locale.toString().equals("ru_RU"))
                    .findFirst().orElseThrow()
    };

    @Test
    @DisplayName("Empty files")
    void emptyFilesTest() {
        testEmptyFiles(Path.of("java-advanced/java-solutions/info/kgeorgiy/ja/petrova/i18n/tests/emptyFile.txt"));
        testEmptyFiles(Path.of("java-advanced/java-solutions/info/kgeorgiy/ja/petrova/i18n/tests/blankFile.txt"));
    }

    void testEmptyFiles(Path path) {
        for (Locale locale : locales) {
            TextAnalyzer analyzer = new TextAnalyzer(locale, path);
            Statistics statistics = analyzer.analyzeText();
            Assertions.assertEquals(statistics.numbers, 0);
            Assertions.assertEquals(statistics.uniqueNumbers, 0);
            Assertions.assertEquals(statistics.sums, 0);
            Assertions.assertEquals(statistics.uniqueSums, 0);
            Assertions.assertEquals(statistics.dates, 0);
            Assertions.assertEquals(statistics.uniqueDates, 0);
            Assertions.assertEquals(statistics.minNumber, 0);
            Assertions.assertEquals(statistics.maxNumber, 0);
            Assertions.assertEquals(statistics.avgNumber, 0);
            Assertions.assertEquals(statistics.avgSum, 0);
            Assertions.assertNull(statistics.minSum);
            Assertions.assertNull(statistics.maxSum);
            Assertions.assertNull(statistics.minDate);
            Assertions.assertNull(statistics.maxDate);
            Assertions.assertNull(statistics.avgDate);
            Assertions.assertEquals(statistics.sentenceStatistics.instances, 0);
            Assertions.assertEquals(statistics.sentenceStatistics.uniqueInstances, 0);
            Assertions.assertEquals(statistics.sentenceStatistics.minInstanceLength, 0);
            Assertions.assertEquals(statistics.sentenceStatistics.maxInstanceLength, 0);
            Assertions.assertEquals(statistics.sentenceStatistics.avgInstanceLength, 0);
            Assertions.assertNull(statistics.sentenceStatistics.minInstance);
            Assertions.assertNull(statistics.sentenceStatistics.maxInstance);
            Assertions.assertNull(statistics.sentenceStatistics.shortestInstance);
            Assertions.assertNull(statistics.sentenceStatistics.longestInstance);
            Assertions.assertEquals(statistics.wordStatistics.instances, 0);
            Assertions.assertEquals(statistics.wordStatistics.uniqueInstances, 0);
            Assertions.assertEquals(statistics.wordStatistics.minInstanceLength, 0);
            Assertions.assertEquals(statistics.wordStatistics.maxInstanceLength, 0);
            Assertions.assertEquals(statistics.wordStatistics.avgInstanceLength, 0);
            Assertions.assertNull(statistics.wordStatistics.minInstance);
            Assertions.assertNull(statistics.wordStatistics.maxInstance);
            Assertions.assertNull(statistics.wordStatistics.shortestInstance);
            Assertions.assertNull(statistics.wordStatistics.longestInstance);
        }
    }

    @Test
    @DisplayName("No words file")
    void noWordsFileTest() {
        for (Locale locale : locales) {
            TextAnalyzer analyzer = new TextAnalyzer(locale, Path.of("java-advanced/java-solutions/info/kgeorgiy/ja/petrova/i18n/tests/noWordsFile.txt"));
            Statistics statistics = analyzer.analyzeText();
            Assertions.assertEquals(statistics.numbers, 0);
            Assertions.assertEquals(statistics.uniqueNumbers, 0);
            Assertions.assertEquals(statistics.sums, 0);
            Assertions.assertEquals(statistics.uniqueSums, 0);
            Assertions.assertEquals(statistics.dates, 0);
            Assertions.assertEquals(statistics.uniqueDates, 0);
            Assertions.assertEquals(statistics.minNumber, 0);
            Assertions.assertEquals(statistics.maxNumber, 0);
            Assertions.assertEquals(statistics.avgNumber, 0);
            Assertions.assertEquals(statistics.avgSum, 0);
            Assertions.assertNull(statistics.minSum);
            Assertions.assertNull(statistics.maxSum);
            Assertions.assertNull(statistics.minDate);
            Assertions.assertNull(statistics.maxDate);
            Assertions.assertNull(statistics.avgDate);
            Assertions.assertEquals(statistics.sentenceStatistics.instances, 1);
            Assertions.assertEquals(statistics.sentenceStatistics.uniqueInstances, 1);
            Assertions.assertEquals(statistics.sentenceStatistics.minInstanceLength, 59);
            Assertions.assertEquals(statistics.sentenceStatistics.maxInstanceLength, 59);
            Assertions.assertEquals(statistics.sentenceStatistics.avgInstanceLength, 59);
            Assertions.assertEquals(statistics.wordStatistics.instances, 0);
            Assertions.assertEquals(statistics.wordStatistics.uniqueInstances, 0);
            Assertions.assertEquals(statistics.wordStatistics.minInstanceLength, 0);
            Assertions.assertEquals(statistics.wordStatistics.maxInstanceLength, 0);
            Assertions.assertEquals(statistics.wordStatistics.avgInstanceLength, 0);
            Assertions.assertNull(statistics.wordStatistics.minInstance);
            Assertions.assertNull(statistics.wordStatistics.maxInstance);
            Assertions.assertNull(statistics.wordStatistics.shortestInstance);
            Assertions.assertNull(statistics.wordStatistics.longestInstance);
        }
    }

    @Test
    @DisplayName("Russian file")
    void russianFileTest() {
        TextAnalyzer analyzer = new TextAnalyzer(locales[1], Path.of("java-advanced/java-solutions/info/kgeorgiy/ja/petrova/i18n/tests/russian.txt"));
        Statistics statistics = analyzer.analyzeText();
        Assertions.assertEquals(statistics.numbers, 5);
            Assertions.assertEquals(statistics.uniqueNumbers, 4);
            Assertions.assertEquals(statistics.sums, 0);
            Assertions.assertEquals(statistics.uniqueSums, 0);
            Assertions.assertEquals(statistics.dates, 3);
            Assertions.assertEquals(statistics.uniqueDates, 2);
            Assertions.assertEquals(statistics.minNumber, 0.01);
            Assertions.assertEquals(statistics.maxNumber, 400);
            Assertions.assertEquals(statistics.avgNumber, 200.388);
            Assertions.assertEquals(statistics.avgSum, 0);
            Assertions.assertNull(statistics.minSum);
            Assertions.assertNull(statistics.maxSum);
            Assertions.assertEquals(statistics.sentenceStatistics.instances, 33);
            Assertions.assertEquals(statistics.sentenceStatistics.uniqueInstances, 31);
            Assertions.assertEquals(statistics.sentenceStatistics.minInstanceLength, 4);
            Assertions.assertEquals(statistics.sentenceStatistics.maxInstanceLength, 178);
            Assertions.assertEquals(statistics.sentenceStatistics.avgInstanceLength, 66);
            Assertions.assertEquals(statistics.wordStatistics.instances, 336);
            Assertions.assertEquals(statistics.wordStatistics.uniqueInstances, 222);
            Assertions.assertEquals(statistics.wordStatistics.minInstanceLength, 1);
            Assertions.assertEquals(statistics.wordStatistics.maxInstanceLength, 16);
            Assertions.assertEquals(statistics.wordStatistics.avgInstanceLength, 5.321429, 0.01);
    }
}