package ru.netology.tour.page;

import com.codeborne.selenide.Selenide;
import com.codeborne.selenide.SelenideElement;
import ru.netology.tour.data.DataHelper.CardData;

import java.time.Duration;

import static com.codeborne.selenide.CollectionCondition.size;
import static com.codeborne.selenide.Condition.cssClass;
import static com.codeborne.selenide.Condition.disappear;
import static com.codeborne.selenide.Condition.exactText;
import static com.codeborne.selenide.Condition.text;
import static com.codeborne.selenide.Condition.visible;
import static com.codeborne.selenide.Selenide.$;
import static com.codeborne.selenide.Selenide.$$;

public class PaymentPage {

    private final SelenideElement buyButton =
            $$("button").find(exactText("Купить"));
    private final SelenideElement continueButton =
            $$("button").find(exactText("Продолжить"));
    private final SelenideElement notificationTitle =
            $(".notification .notification__title");
    private final SelenideElement notificationContent =
            $(".notification .notification__content");
    private final SelenideElement errorNotificationTitle =
            $(".notification_status_error .notification__title");
    private final SelenideElement errorNotificationContent =
            $(".notification_status_error .notification__content");

    public PaymentPage() {
        Selenide.open("http://localhost:8080/");
    }

    public PaymentPage selectPayment() {
        buyButton.shouldBe(visible).click();
        return this;
    }

    private SelenideElement getField(String fieldName) {
        return $$(".input").findBy(text(fieldName));
    }

    private SelenideElement getInput(String fieldName) {
        return getField(fieldName).$("input.input__control");
    }

    public PaymentPage fillForm(CardData card) {
        getInput("Номер карты").shouldBe(visible).setValue(card.getNumber());
        getInput("Месяц").shouldBe(visible).setValue(card.getMonth());
        getInput("Год").shouldBe(visible).setValue(card.getYear());
        getInput("Владелец").shouldBe(visible).setValue(card.getHolder());
        getInput("CVC/CVV").shouldBe(visible).setValue(card.getCvc());
        return this;
    }

    public PaymentPage submit() {
        continueButton.shouldBe(visible).click();
        return this;
    }

    public PaymentPage shouldHaveOnlyOneNotification(String expectedTitle, String expectedContent) {
        return checkNotification(
                notificationTitle,
                notificationContent,
                expectedTitle,
                expectedContent
        );
    }

    public PaymentPage shouldHaveOnlyOneErrorNotification(String expectedTitle, String expectedContent) {
        return checkNotification(
                errorNotificationTitle,
                errorNotificationContent,
                expectedTitle,
                expectedContent
        );
    }

    private PaymentPage checkNotification(
            SelenideElement title,
            SelenideElement content,
            String expectedTitle,
            String expectedContent
    ) {
        title.shouldBe(visible, Duration.ofSeconds(10)).shouldHave(exactText(expectedTitle));
        content.shouldBe(visible).shouldHave(exactText(expectedContent));
        $$(".notification.notification_visible").shouldHave(size(1));
        SelenideElement notification = $(".notification.notification_visible");
        notification.$(".notification__closer").click();
        $$(".notification.notification_visible").shouldHave(size(0), Duration.ofSeconds(5));
        return this;
    }

    public PaymentPage shouldHaveOnlyFieldError(String fieldName, String expectedError) {
        SelenideElement field = getField(fieldName);
        field.shouldHave(cssClass("input_invalid"));
        field.$(".input__sub").shouldBe(visible).shouldHave(exactText(expectedError));
        $$(".input.input_invalid").shouldHave(size(1));
        $$(".input__sub").filter(visible).shouldHave(size(1));
        return this;
    }
}