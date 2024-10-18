package com.fullcycle.subscription.domain.subscription;

import com.fullcycle.subscription.domain.utils.InstantUtils;

import java.time.Instant;
import java.time.LocalDate;

public record SubscriptionIncomplete(String subscriptionId, String accountId, Long planId, String reason,
                                     LocalDate dueDate,
                                     Instant occurredOn) implements SubscriptionEvent {

    public SubscriptionIncomplete {
        this.assertArgumentNotEmpty(subscriptionId, "'subscriptionId' should not be empty");
        this.assertArgumentNotEmpty(accountId, "'accountId' should not be empty");
        this.assertArgumentNotNull(planId, "'planId' should not be null");
        this.assertArgumentNotEmpty(reason, "'reason' should not be empty");
        this.assertArgumentNotNull(dueDate, "'dueDate' should not be null");
        this.assertArgumentNotNull(occurredOn, "'occurredOn' should not be null");
    }

    public SubscriptionIncomplete(final Subscription anSubscription, final String aReason) {
        this(
                anSubscription.id().value(),
                anSubscription.accountId().value(),
                anSubscription.planId().value(),
                aReason,
                anSubscription.dueDate(),
                InstantUtils.now()
        );
    }
}