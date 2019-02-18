package uk.gov.ida.notification.contracts.verifyserviceprovider;

import java.util.Collections;

import org.joda.time.DateTime;

public class TranslatedHubResponseBuilder {

    public static TranslatedHubResponse getTranslatedHubResponseAuthenticationFailed() {
        return new TranslatedHubResponse(VspScenario.AUTHENTICATION_FAILED, "pid1234", VspLevelOfAssurance.LEVEL_2, null);
    }

    public static TranslatedHubResponse getTranslatedHubResponseIdentityVerified() {
        return new TranslatedHubResponse(VspScenario.IDENTITY_VERIFIED, "123456", VspLevelOfAssurance.LEVEL_2, buildAttributes());
    }

    private static Attributes buildAttributes() {
        return new Attributes(
                new Attribute<>("Jean Paul", true, createDateTime(2001, 1, 1, 12, 0), null),
                null,
                Collections.singletonList(new Attribute<>("Smith", true, createDateTime(2001, 1, 1, 12, 0), null)),
                new Attribute<>(createDateTime(1990, 1, 1, 0, 0), true, createDateTime(2001, 1, 1, 12, 0), null),
                new Attribute<>("NOT_SPECIFIED", true, createDateTime(2001, 1, 1, 12, 0), null),
                Collections.singletonList(new Attribute<>(new Address(Collections.singletonList("1 Acacia Avenue"), "SW1A 1AA", null, null),
                        true, createDateTime(2001, 1, 1, 12, 0), null)));
    }

    private static DateTime createDateTime(int year, int month, int day, int hour, int minute) {
        return new DateTime().withYear(year).withMonthOfYear(month).withDayOfMonth(day).withHourOfDay(hour).withMinuteOfHour(minute);
    }
}