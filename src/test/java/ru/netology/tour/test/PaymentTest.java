package ru.netology.tour.test;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import ru.netology.tour.data.DataHelper;
import ru.netology.tour.data.DataHelper.CardData;
import ru.netology.tour.data.SQLHelper;
import ru.netology.tour.page.PaymentPage;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

public class PaymentTest {

    private final PaymentPage paymentPage = new PaymentPage();

    private static final String APPROVED_NOTIFICATION_TITLE = "Успешно";
    private static final String APPROVED_NOTIFICATION_CONTENT = "Операция одобрена Банком.";

    private static final String DECLINED_NOTIFICATION_TITLE = "Ошибка";
    private static final String DECLINED_NOTIFICATION_CONTENT = "Ошибка! Банк отказал в проведении операции.";

    private static final String INVALID_FORMAT = "Неверный формат";
    private static final String REQUIRED_FIELD = "Поле обязательно для заполнения";
    private static final String INVALID_EXPIRY = "Неверно указан срок действия карты";
    private static final String EXPIRED_CARD = "Истёк срок действия карты";

    private static final int EXPECTED_PAYMENT_AMOUNT = 45_000;

    @BeforeEach
    void cleanDatabase() {
        SQLHelper.cleanDatabase();
    }

    private String completeApprovedPayment(CardData card) {
        paymentPage.open().selectPayment().fillForm(card).submit()
                .shouldHaveOnlyOneNotification(APPROVED_NOTIFICATION_TITLE, APPROVED_NOTIFICATION_CONTENT);
        assertEquals(
                1,
                SQLHelper.getPaymentCount(),
                "Payment record was not created");
        String transactionId = SQLHelper.getLatestTransactionId();
        assertNotNull(
                transactionId,
                "Payment transaction ID is missing in the database");
        assertEquals(
                "APPROVED",
                SQLHelper.getPaymentStatus(transactionId),
                "Payment status is incorrect");
        assertEquals(
                1,
                SQLHelper.getOrderCount(transactionId),
                "Related order was not created");
        assertEquals(
                0,
                SQLHelper.getCreditRequestCount(),
                "Unexpected credit request was created");
        return transactionId;
    }

    private void completeDeclinedPayment(CardData card) {
        paymentPage.open().selectPayment().fillForm(card).submit()
                .shouldHaveOnlyOneErrorNotification(DECLINED_NOTIFICATION_TITLE, DECLINED_NOTIFICATION_CONTENT);
        assertEquals(
                1,
                SQLHelper.getPaymentCount(),
                "Payment record was not created");
        String transactionId = SQLHelper.getLatestTransactionId();
        assertNotNull(
                transactionId,
                "Payment transaction ID is missing in the database");
        assertEquals(
                "DECLINED",
                SQLHelper.getPaymentStatus(transactionId),
                "Payment status is incorrect");
        assertEquals(
                1,
                SQLHelper.getOrderCount(transactionId),
                "Related order was not created");
        assertEquals(
                0,
                SQLHelper.getCreditRequestCount(),
                "Unexpected credit request was created");
        assertEquals(
                EXPECTED_PAYMENT_AMOUNT,
                SQLHelper.getPaymentAmount(transactionId),
                "Payment amount does not match the tour price");
    }

    private void assertNoRecordsInDatabase() {
        assertEquals(
                0,
                SQLHelper.getPaymentCount(),
                "Unexpected payment record was created");
        assertEquals(
                0,
                SQLHelper.getOrderCount(),
                "Unexpected order record was created");
        assertEquals(
                0,
                SQLHelper.getCreditRequestCount(),
                "Unexpected credit request was created");
    }

    private void shouldRejectForm(CardData card, String fieldName, String expectedError) {
        paymentPage.open().selectPayment().fillForm(card).submit()
                .shouldHaveOnlyFieldError(fieldName, expectedError);
        assertNoRecordsInDatabase();
    }

    private void shouldRejectUnsupportedCard(CardData card) {
        paymentPage.open().selectPayment().fillForm(card).submit()
                .shouldHaveOnlyOneErrorNotification(DECLINED_NOTIFICATION_TITLE, DECLINED_NOTIFICATION_CONTENT);
        assertNoRecordsInDatabase();
    }

    // Позитивные сценарии

    @Test
    @DisplayName("No. 1. Successful payment with an APPROVED card")
    void shouldSuccessfullyPayWithApprovedCard() {
        String transactionId = completeApprovedPayment(DataHelper.approvedCard());
        assertEquals(
                EXPECTED_PAYMENT_AMOUNT,
                SQLHelper.getPaymentAmount(transactionId),
                "Payment amount does not match the tour price");
    }

    @Test
    @DisplayName("No. 2. Expiration month 01 in the following calendar year")
    void shouldSuccessfullyPayWithJanuaryExpirationDate() {
        CardData card = DataHelper.approvedCardWithExpiry("01", DataHelper.getNextYear());
        completeApprovedPayment(card);
    }

    @Test
    @DisplayName("No. 3. Expiration month 12 in the following calendar year")
    void shouldSuccessfullyPayWithDecemberExpirationDate() {
        CardData card = DataHelper.approvedCardWithExpiry("12", DataHelper.getNextYear());
        completeApprovedPayment(card);
    }

    @Test
    @DisplayName("No. 4. Hyphen inside the cardholder's first name")
    void shouldSuccessfullyPayWithHyphenInFirstName() {
        CardData card = DataHelper.approvedCard().withHolder("ANNA-MARIA IVANOVA");
        completeApprovedPayment(card);
    }

    @Test
    @DisplayName("No. 5. Hyphen inside the cardholder's last name")
    void shouldSuccessfullyPayWithHyphenInLastName() {
        CardData card = DataHelper.approvedCard().withHolder("ANNA IVANOVA-PETROVA");
        completeApprovedPayment(card);
    }

    // Негативные сценарии

    @Test
    @DisplayName("No. 1. Payment with a DECLINED card")
    void shouldDeclinePaymentWithDeclinedCard() {
        completeDeclinedPayment(DataHelper.declinedCard());
    }

    @Test
    @DisplayName("No. 2. Empty card number field")
    void shouldRejectEmptyCardNumber() {
        shouldRejectForm(DataHelper.cardWithEmptyNumber(), "Номер карты", INVALID_FORMAT);
    }

    @Test
    @DisplayName("No. 3. Empty month field")
    void shouldRejectEmptyMonth() {
        shouldRejectForm(DataHelper.cardWithEmptyMonth(), "Месяц", INVALID_FORMAT);
    }

    @Test
    @DisplayName("No. 4. Empty year field")
    void shouldRejectEmptyYear() {
        shouldRejectForm(DataHelper.cardWithEmptyYear(), "Год", INVALID_FORMAT);
    }

    @Test
    @DisplayName("No. 5. Empty cardholder field")
    void shouldRejectEmptyHolder() {
        shouldRejectForm(DataHelper.cardWithEmptyHolder(), "Владелец", REQUIRED_FIELD);
    }

    @Test
    @DisplayName("No. 6. Empty CVC/CVV field")
    void shouldRejectEmptyCvc() {
        shouldRejectForm(DataHelper.cardWithEmptyCvc(), "CVC/CVV", INVALID_FORMAT);
    }

    @Test
    @DisplayName("No. 7. Card number with invalid length")
    void shouldRejectCardNumberWithInvalidLength() {
        shouldRejectForm(DataHelper.cardWithInvalidNumberLength(), "Номер карты", INVALID_FORMAT);
    }

    @Test
    @DisplayName("No. 8. Month 00")
    void shouldRejectMonthZero() {
        CardData card = DataHelper.approvedCardWithExpiry("00", DataHelper.getNextYear());
        shouldRejectForm(card, "Месяц", INVALID_EXPIRY);
    }

    @Test
    @DisplayName("No. 9. Month 13")
    void shouldRejectMonthThirteen() {
        CardData card = DataHelper.approvedCardWithExpiry("13", DataHelper.getNextYear());
        shouldRejectForm(card, "Месяц", INVALID_EXPIRY);
    }

    @Test
    @DisplayName("No. 10. Card expired in the previous calendar year")
    void shouldRejectCardExpiredLastYear() {
        shouldRejectForm(DataHelper.expiredInPreviousYearCard(), "Год", EXPIRED_CARD);
    }

    @Test
    @DisplayName("No. 11. Card expired in the previous month")
    void shouldRejectCardExpiredPreviousMonth() {
        shouldRejectForm(DataHelper.expiredInPreviousMonthCard(), "Месяц", INVALID_EXPIRY);
    }

    @Test
    @DisplayName("No. 12. CVC/CVV with fewer than three digits")
    void shouldRejectShortCvc() {
        shouldRejectForm(DataHelper.cardWithShortCvc(), "CVC/CVV", INVALID_FORMAT);
    }

    @Test
    @DisplayName("No. 13. Card unsupported by the bank simulator")
    void shouldRejectUnsupportedCard() {
        shouldRejectUnsupportedCard(DataHelper.unsupportedCard());
    }

    @Test
    @DisplayName("No. 14. Cardholder name in Cyrillic")
    void shouldRejectCyrillicHolder() {
        CardData card = DataHelper.approvedCard().withHolder("Иван Иванов");
        shouldRejectForm(card, "Владелец", INVALID_FORMAT);
    }

    @Test
    @DisplayName("No. 15. Cardholder name without a space")
    void shouldRejectHolderWithoutSpace() {
        CardData card = DataHelper.approvedCard().withHolder("IVANIVANOV");
        shouldRejectForm(card, "Владелец", INVALID_FORMAT);
    }

    @Test
    @DisplayName("No. 16. Special character in the first name")
    void shouldRejectSpecialCharacterInFirstName() {
        CardData card = DataHelper.approvedCard().withHolder("IVAN@ IVANOV");
        shouldRejectForm(card, "Владелец", INVALID_FORMAT);
    }

    @Test
    @DisplayName("No. 17. Special character in the last name")
    void shouldRejectSpecialCharacterInLastName() {
        CardData card = DataHelper.approvedCard().withHolder("IVAN IVAN@OV");
        shouldRejectForm(card, "Владелец", INVALID_FORMAT);
    }

    @Test
    @DisplayName("No. 18. Hyphen at the beginning of the first name")
    void shouldRejectHyphenAtStartOfFirstName() {
        CardData card = DataHelper.approvedCard().withHolder("-IVAN IVANOV");
        shouldRejectForm(card, "Владелец", INVALID_FORMAT);
    }

    @Test
    @DisplayName("No. 19. Hyphen at the end of the first name")
    void shouldRejectHyphenAtEndOfFirstName() {
        CardData card = DataHelper.approvedCard().withHolder("IVAN- IVANOV");
        shouldRejectForm(card, "Владелец", INVALID_FORMAT);
    }

    @Test
    @DisplayName("No. 20. Hyphen at the beginning of the last name")
    void shouldRejectHyphenAtStartOfLastName() {
        CardData card = DataHelper.approvedCard().withHolder("IVAN -IVANOV");
        shouldRejectForm(card, "Владелец", INVALID_FORMAT);
    }

    @Test
    @DisplayName("No. 21. Hyphen at the end of the last name")
    void shouldRejectHyphenAtEndOfLastName() {
        CardData card = DataHelper.approvedCard().withHolder("IVAN IVANOV-");
        shouldRejectForm(card, "Владелец", INVALID_FORMAT);
    }

    @Test
    @DisplayName("No. 22. Digits in the cardholder field")
    void shouldRejectDigitsInHolder() {
        CardData card = DataHelper.approvedCard().withHolder("IV1AN IVA2NOV");
        shouldRejectForm(card, "Владелец", INVALID_FORMAT);
    }
}