package info.kgeorgiy.ja.petrova.i18n.resources;

import java.util.ListResourceBundle;

public class ListResourceBundle_ru extends ListResourceBundle {
    private final Object[][] bundle = {
            {"analyzedFile", "Анализируемый файл"},
            {"Summary", "Сводная"},
            {"statistics", "статистика"},
            {"Number", "Число"},
            {"number", "число"},
            {"of sentences", "предложений"},
            {"of a sentence", "предложения"},
            {"of words", "слов"},
            {"of numbers", "чисел"},
            {"of sums", "сумм"},
            {"of dates", "дат"},
            {"Sentence statistics", "Статистика по предложениям"},
            {"of unique", "различных"},
            {"sentence", "предложение"},
            {"Minimal", "Минимальное"},
            {"Maximal", "Максимальное"},
            {"MinimalFem", "Минимальная"},
            {"MaximalFem", "Максимальная"},
            {"Minimal length", "Минимальная длина"},
            {"Maximal length", "Максимальная длина"},
            {"Average length", "Средняя длина"},
            {"Words statistics", "Статистика по словам"},
            {"word", "слово"},
            {"of a word", "слова"},
            {"Number statistics", "Статистика по числам"},
            {"Number count", "Число чисел"},
            {"Average", "Среднее"},
            {"AverageFem", "Средняя"},
            {"Money statistics", "Статистика по суммам денег"},
            {"Money sum count", "Число сумм"},
            {"sum", "сумма"},
            {"Date statistics", "Статистика по датам"},
            {"Date count", "Число дат"},
            {"date", "дата"}
    };
    @Override
    protected Object[][] getContents() {
        return bundle;
    }
}
