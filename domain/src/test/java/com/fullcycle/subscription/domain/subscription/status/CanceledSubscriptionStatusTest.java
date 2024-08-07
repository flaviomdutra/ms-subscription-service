package com.fullcycle.subscription.domain.subscription.status;

import com.fullcycle.subscription.domain.Fixture;
import com.fullcycle.subscription.domain.account.AccountId;
import com.fullcycle.subscription.domain.exceptions.DomainException;
import com.fullcycle.subscription.domain.subscription.Subscription;
import com.fullcycle.subscription.domain.subscription.SubscriptionId;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

class CanceledSubscriptionStatusTest {

    private static Subscription canceledSubscription() {
        var expectedSubscription = Subscription.newSubscription(new SubscriptionId("SUB"), new AccountId("ACC123"), Fixture.Plans.plus());
        expectedSubscription.status().cancel();
        return expectedSubscription;
    }

    @Test
    public void givenInstances_whenCallsToString_shouldReturnValue() {
        // given
        var expectedString = "canceled";
        var one = new CanceledSubscriptionStatus(Subscription.newSubscription(new SubscriptionId("SUB"), new AccountId("ACC123"), Fixture.Plans.plus()));

        // when
        var actualString = one.toString();

        // then
        assertEquals(expectedString, actualString);
    }

    @Test
    public void givenTwoInstances_whenCallsEquals_shouldCompareClasses() {
        // given
        var expectedEquals = true;
        var one = new CanceledSubscriptionStatus(Subscription.newSubscription(new SubscriptionId("SUB"), new AccountId("ACC123"), Fixture.Plans.plus()));
        var two = new CanceledSubscriptionStatus(Subscription.newSubscription(new SubscriptionId("SUB2"), new AccountId("ACC123"), Fixture.Plans.plus()));

        // when
        var actualEquals = one.equals(two);

        // then
        assertEquals(expectedEquals, actualEquals, "O equals deveria levar em conta apenas a classe do status e não a subscription");
    }

    @Test
    public void givenTwoInstances_whenCallsHashCode_shouldBeEquals() {
        // given
        var expectedEquals = true;
        var one = new CanceledSubscriptionStatus(Subscription.newSubscription(new SubscriptionId("SUB"), new AccountId("ACC123"), Fixture.Plans.plus()));
        var two = new CanceledSubscriptionStatus(Subscription.newSubscription(new SubscriptionId("SUB2"), new AccountId("ACC123"), Fixture.Plans.plus()));

        // then
        assertEquals(one.hashCode(), two.hashCode(), "O hashCode deveria levar em conta apenas a classe do status e não a subscription");
    }

    @Test
    public void givenCanceledStatus_whenCallsTrialing_shouldExpectError() {
        // given
        var expectedError = "Subscription with status canceled can't transit to trialing";
        var expectedStatusClass = CanceledSubscriptionStatus.class;
        var expectedSubscription = canceledSubscription();

        var target = new CanceledSubscriptionStatus(expectedSubscription);

        // when
        var actualError = assertThrows(DomainException.class, () -> target.trialing());

        // then
        assertEquals(expectedError, actualError.getMessage());
        assertEquals(expectedStatusClass, expectedSubscription.status().getClass());
    }

    @Test
    public void givenCanceledStatus_whenCallsCancel_shouldDoNothing() {
        // given
        var expectedStatusClass = CanceledSubscriptionStatus.class;
        var expectedSubscription = canceledSubscription();
        var target = new CanceledSubscriptionStatus(expectedSubscription);

        // when
        target.cancel();

        // then
        assertEquals(expectedStatusClass, expectedSubscription.status().getClass());
    }

    @Test
    public void givenCanceledStatus_whenCallsActive_shouldExpectError() {
        // given
        var expectedError = "Subscription with status canceled can't transit to active";
        var expectedStatusClass = CanceledSubscriptionStatus.class;
        var expectedSubscription = canceledSubscription();

        var target = new CanceledSubscriptionStatus(expectedSubscription);

        // when
        var actualError = assertThrows(DomainException.class, () -> target.active());

        // then
        assertEquals(expectedError, actualError.getMessage());
        assertEquals(expectedStatusClass, expectedSubscription.status().getClass());
    }

    @Test
    public void givenCanceledStatus_whenCallsIncomplete_shouldExpectError() {
        // given
        var expectedError = "Subscription with status canceled can't transit to incomplete";
        var expectedStatusClass = CanceledSubscriptionStatus.class;
        var expectedSubscription = canceledSubscription();

        var target = new CanceledSubscriptionStatus(expectedSubscription);

        // when
        var actualError = assertThrows(DomainException.class, () -> target.incomplete());

        // then
        assertEquals(expectedError, actualError.getMessage());
        assertEquals(expectedStatusClass, expectedSubscription.status().getClass());
    }
}