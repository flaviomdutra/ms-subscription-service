package com.fullcycle.subscription.domain.subscription;

import com.fullcycle.subscription.domain.AggregateRoot;
import com.fullcycle.subscription.domain.account.AccountId;
import com.fullcycle.subscription.domain.plan.Plan;
import com.fullcycle.subscription.domain.plan.PlanId;
import com.fullcycle.subscription.domain.subscription.SubscriptionCommand.CancelSubscription;
import com.fullcycle.subscription.domain.subscription.SubscriptionCommand.IncompleteSubscription;
import com.fullcycle.subscription.domain.subscription.SubscriptionCommand.RenewSubscription;
import com.fullcycle.subscription.domain.subscription.status.ActiveSubscriptionStatus;
import com.fullcycle.subscription.domain.subscription.status.SubscriptionStatus;
import com.fullcycle.subscription.domain.subscription.status.TrialingSubscriptionStatus;
import com.fullcycle.subscription.domain.utils.InstantUtils;

import java.time.Instant;
import java.time.LocalDate;

import static com.fullcycle.subscription.domain.subscription.SubscriptionCommand.ChangeStatus;

public class Subscription extends AggregateRoot<SubscriptionId> {

    private int version;
    private AccountId accountId;
    private PlanId planId;
    private LocalDate dueDate;
    private SubscriptionStatus status;
    private Instant lastRenewDate;
    private String lastTransactionId;
    private Instant createdAt;
    private Instant updatedAt;

    private Subscription(
            final SubscriptionId subscriptionId,
            final int version,
            final AccountId accountId,
            final PlanId planId,
            final LocalDate dueDate,
            final String status,
            final Instant lastRenewDate,
            final String lastTransactionId,
            final Instant createdAt,
            final Instant updatedAt
    ) {
        super(subscriptionId);
        this.setVersion(version);
        this.setAccountId(accountId);
        this.setPlanId(planId);
        this.setDueDate(dueDate);
        this.setStatus(SubscriptionStatus.create(status, this));
        this.setLastRenewDate(lastRenewDate);
        this.setLastTransactionId(lastTransactionId);
        this.setCreatedAt(createdAt);
        this.setUpdatedAt(updatedAt);
    }

    public static Subscription newSubscription(
            final SubscriptionId anId,
            final AccountId anAccountId,
            final Plan selectedPlan
    ) {
        final var now = InstantUtils.now();
        final var aNewSubscription = new Subscription(anId, 0, anAccountId, selectedPlan.id(), LocalDate.now().plusMonths(1), SubscriptionStatus.TRIALING, null, null, now, now);
        aNewSubscription.registerEvent(new SubscriptionCreated(aNewSubscription));

        return aNewSubscription;
    }

    public static Subscription with(
            final SubscriptionId subscriptionId,
            final int version,
            final AccountId accountId,
            final PlanId planId,
            final LocalDate dueDate,
            final String status,
            final Instant lastRenewDate,
            final String lastTransactionId,
            final Instant createdAt,
            final Instant updatedAt
    ) {
        return new Subscription(
                subscriptionId,
                version,
                accountId,
                planId,
                dueDate,
                status,
                lastRenewDate,
                lastTransactionId,
                createdAt,
                updatedAt
        );
    }

    public void execute(final SubscriptionCommand... cmds) {
        if (cmds == null || cmds.length == 0) {
            return;
        }

        for (var cmd : cmds) {
            switch (cmd) {
                case ChangeStatus c -> apply(c);
                case IncompleteSubscription c -> apply(c);
                case RenewSubscription c -> apply(c);
                case CancelSubscription c -> apply(c);
            }
        }

        this.setUpdatedAt(InstantUtils.now());
    }

    private void apply(final CancelSubscription cmd) {
        this.status.cancel();
        this.registerEvent(new SubscriptionCanceled(this));

    }

    private void apply(final RenewSubscription cmd) {
        this.status.active();
        this.setLastTransactionId(cmd.aTransactionId());
        this.setDueDate(dueDate.plusMonths(1));
        this.setLastRenewDate(InstantUtils.now());
        this.registerEvent(new SubscriptionRenewed(this, cmd.selectedPlan()));
    }


    private void apply(final IncompleteSubscription cmd) {
        this.status.incomplete();
        this.setLastTransactionId(cmd.aTransactionId());
        this.registerEvent(new SubscriptionIncomplete(this, cmd.aReason()));
    }

    private void apply(final ChangeStatus cmd) {
        this.setStatus(SubscriptionStatus.create(cmd.status(), this));
    }

    public int version() {
        return version;
    }

    public AccountId accountId() {
        return accountId;
    }

    public PlanId planId() {
        return planId;
    }

    public LocalDate dueDate() {
        return dueDate;
    }

    public SubscriptionStatus status() {
        return status;
    }

    public Instant lastRenewDate() {
        return lastRenewDate;
    }

    public String lastTransactionId() {
        return lastTransactionId;
    }

    public Instant createdAt() {
        return createdAt;
    }

    public Instant updatedAt() {
        return updatedAt;
    }

    private void setVersion(int version) {
        this.version = version;
    }

    private void setAccountId(final AccountId accountId) {
        this.assertArgumentNotNull(accountId, "'accountId' should not be null");
        this.accountId = accountId;
    }

    private void setPlanId(final PlanId planId) {
        this.assertArgumentNotNull(planId, "'planId' should not be null");
        this.planId = planId;
    }

    private void setDueDate(final LocalDate dueDate) {
        this.assertArgumentNotNull(dueDate, "'dueDate' should not be null");
        this.dueDate = dueDate;
    }

    private void setStatus(final SubscriptionStatus status) {
        this.assertArgumentNotNull(status, "'status' should not be null");
        this.status = status;
    }

    private void setLastRenewDate(final Instant lastRenewDate) {
        this.lastRenewDate = lastRenewDate;
    }

    private void setLastTransactionId(final String lastTransactionId) {
        this.lastTransactionId = lastTransactionId;
    }

    private void setCreatedAt(final Instant createdAt) {
        this.assertArgumentNotNull(createdAt, "'createdAt' should not be null");
        this.createdAt = createdAt;
    }

    private void setUpdatedAt(final Instant updatedAt) {
        this.assertArgumentNotNull(updatedAt, "'updatedAt' should not be null");
        this.updatedAt = updatedAt;
    }

    public boolean isTrial() {
        return this.status instanceof TrialingSubscriptionStatus;
    }

    public boolean isActive() {
        return this.status instanceof ActiveSubscriptionStatus;
    }
}
