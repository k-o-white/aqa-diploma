package ru.netology.tour.data;

import lombok.Value;
import lombok.With;

import java.time.YearMonth;
import java.util.Locale;

public final class DataHelper {

    private DataHelper() {}

    public static final String APPROVED_CARD_NUMBER = "1111 2222 3333 4444";
    public static final String DECLINED_CARD_NUMBER = "5555 6666 7777 8888";
    public static final String UNSUPPORTED_CARD_NUMBER = "6666 7777 8888 9999";

    public static final String VALID_HOLDER = "IVAN IVANOV";
    public static final String VALID_CVC = "123";

    public static CardData approvedCard() {
        return new CardData(
                APPROVED_CARD_NUMBER,
                getNextMonth(),
                getNextMonthYear(),
                VALID_HOLDER,
                VALID_CVC
        );
    }

    public static CardData declinedCard() {
        return approvedCard().withNumber(DECLINED_CARD_NUMBER);
    }

    public static CardData unsupportedCard() {
        return approvedCard().withNumber(UNSUPPORTED_CARD_NUMBER);
    }

    public static CardData approvedCardWithExpiry(String month, String year) {
        return approvedCard().withMonth(month).withYear(year);
    }

    public static CardData expiredInPreviousYearCard() {
        return approvedCard().withExpiration(YearMonth.now().minusYears(1));
    }

    public static CardData expiredInPreviousMonthCard() {
        return approvedCard().withExpiration(YearMonth.now().minusMonths(1));
    }

    public static CardData cardWithEmptyNumber() {
        return approvedCard().withNumber("");
    }

    public static CardData cardWithEmptyMonth() {
        return approvedCard().withMonth("");
    }

    public static CardData cardWithEmptyYear() {
        return approvedCard().withYear("");
    }

    public static CardData cardWithEmptyHolder() {
        return approvedCard().withHolder("");
    }

    public static CardData cardWithEmptyCvc() {
        return approvedCard().withCvc("");
    }

    public static CardData cardWithInvalidNumberLength() {
        return approvedCard().withNumber("1111 2222 3333 444");
    }

    public static CardData cardWithShortCvc() {
        return approvedCard().withCvc("12");
    }

    public static String getNextMonth() {
        return formatMonth(YearMonth.now().plusMonths(1));
    }

    public static String getNextMonthYear() {
        return formatYear(YearMonth.now().plusMonths(1));
    }

    public static String getNextYear() {
        return formatYear(YearMonth.now().plusYears(1));
    }

    private static String formatMonth(YearMonth date) {
        return String.format(Locale.ROOT, "%02d", date.getMonthValue());
    }

    private static String formatYear(YearMonth date) {
        return String.format(Locale.ROOT, "%02d", date.getYear() % 100);
    }

    @Value
    @With
    public static class CardData {
        String number;
        String month;
        String year;
        String holder;
        String cvc;

        public CardData withExpiration(YearMonth expiration) {
            return this.withMonth(formatMonth(expiration)).withYear(formatYear(expiration));
        }
    }
}