package info.kgeorgiy.ja.petrova.i18n;

import java.util.Date;

public class Statistics {
    public TextInstanceStatistic sentenceStatistics = new TextInstanceStatistic();
    public TextInstanceStatistic wordStatistics = new TextInstanceStatistic();
    int numbers = 0;
    long uniqueNumbers = 0;
    double minNumber = 0;
    double maxNumber = 0;
    double avgNumber = 0;
    int sums = 0;
    long uniqueSums = 0;
    String minSum;
    String maxSum;
    double avgSum = 0;
    int dates = 0;
    long uniqueDates = 0;
    String minDate;
    String maxDate;
    String avgDate;

    public static class TextInstanceStatistic {
        int instances = 0;
        long uniqueInstances = 0;
        String minInstance;
        String maxInstance;
        String shortestInstance;
        String longestInstance;
        int minInstanceLength = 0;
        int maxInstanceLength = 0;
        double avgInstanceLength = 0;

        public void setShortestInstance(String shortestInstance) {
            this.shortestInstance = shortestInstance;
            this.minInstanceLength = shortestInstance.length();
        }

        public void setLongestInstance(String longestInstance) {
            this.longestInstance = longestInstance;
            this.maxInstanceLength = longestInstance.length();
        }
    }
}
