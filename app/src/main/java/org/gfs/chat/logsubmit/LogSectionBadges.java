package org.gfs.chat.logsubmit;

import android.content.Context;

import androidx.annotation.NonNull;

import org.signal.donations.InAppPaymentType;
import org.gfs.chat.components.settings.app.subscription.InAppPaymentsRepository;
import org.gfs.chat.database.InAppPaymentTable;
import org.gfs.chat.database.SignalDatabase;
import org.gfs.chat.database.model.InAppPaymentSubscriberRecord;
import org.gfs.chat.database.model.databaseprotos.InAppPaymentData;
import org.gfs.chat.keyvalue.SignalStore;
import org.gfs.chat.recipients.Recipient;

final class LogSectionBadges implements LogSection {

  @Override
  public @NonNull String getTitle() {
    return "BADGES";
  }

  @Override
  public @NonNull CharSequence getContent(@NonNull Context context) {
    if (!SignalStore.account().isRegistered()) {
      return "Unregistered";
    }

    if (SignalStore.account().getE164() == null || SignalStore.account().getAci() == null) {
      return "Self not yet available!";
    }

    InAppPaymentTable.InAppPayment latestRecurringDonation = SignalDatabase.inAppPayments().getLatestInAppPaymentByType(InAppPaymentType.RECURRING_DONATION);

    if (latestRecurringDonation != null) {
      return new StringBuilder().append("Badge Count                     : ").append(Recipient.self().getBadges().size()).append("\n")
                                .append("ExpiredBadge                    : ").append(SignalStore.inAppPayments().getExpiredBadge() != null).append("\n")
                                .append("LastKeepAliveLaunchTime         : ").append(SignalStore.inAppPayments().getLastKeepAliveLaunchTime()).append("\n")
                                .append("LastEndOfPeriod                 : ").append(SignalStore.inAppPayments().getLastEndOfPeriod()).append("\n")
                                .append("InAppPayment.State              : ").append(latestRecurringDonation.getState()).append("\n")
                                .append("InAppPayment.EndOfPeriod        : ").append(latestRecurringDonation.getEndOfPeriodSeconds()).append("\n")
                                .append("InAppPaymentData.RedemptionState: ").append(getRedemptionStage(latestRecurringDonation.getData())).append("\n")
                                .append("InAppPaymentData.Error          : ").append(getError(latestRecurringDonation.getData())).append("\n")
                                .append("InAppPaymentData.Cancellation   : ").append(getCancellation(latestRecurringDonation.getData())).append("\n")
                                .append("DisplayBadgesOnProfile          : ").append(SignalStore.inAppPayments().getDisplayBadgesOnProfile()).append("\n")
                                .append("ShouldCancelBeforeNextAttempt   : ").append(InAppPaymentsRepository.getShouldCancelSubscriptionBeforeNextSubscribeAttempt(InAppPaymentSubscriberRecord.Type.DONATION)).append("\n")
                                .append("IsUserManuallyCancelledDonation : ").append(SignalStore.inAppPayments().isDonationSubscriptionManuallyCancelled()).append("\n");

    } else {
      return new StringBuilder().append("Badge Count                             : ").append(Recipient.self().getBadges().size()).append("\n")
                                .append("ExpiredBadge                            : ").append(SignalStore.inAppPayments().getExpiredBadge() != null).append("\n")
                                .append("LastKeepAliveLaunchTime                 : ").append(SignalStore.inAppPayments().getLastKeepAliveLaunchTime()).append("\n")
                                .append("LastEndOfPeriod                         : ").append(SignalStore.inAppPayments().getLastEndOfPeriod()).append("\n")
                                .append("SubscriptionEndOfPeriodConversionStarted: ").append(SignalStore.inAppPayments().getSubscriptionEndOfPeriodConversionStarted()).append("\n")
                                .append("SubscriptionEndOfPeriodRedemptionStarted: ").append(SignalStore.inAppPayments().getSubscriptionEndOfPeriodRedemptionStarted()).append("\n")
                                .append("SubscriptionEndOfPeriodRedeemed         : ").append(SignalStore.inAppPayments().getSubscriptionEndOfPeriodRedeemed()).append("\n")
                                .append("IsUserManuallyCancelledDonation         : ").append(SignalStore.inAppPayments().isDonationSubscriptionManuallyCancelled()).append("\n")
                                .append("DisplayBadgesOnProfile                  : ").append(SignalStore.inAppPayments().getDisplayBadgesOnProfile()).append("\n")
                                .append("SubscriptionRedemptionFailed            : ").append(SignalStore.inAppPayments().getSubscriptionRedemptionFailed()).append("\n")
                                .append("ShouldCancelBeforeNextAttempt           : ").append(SignalStore.inAppPayments().getShouldCancelSubscriptionBeforeNextSubscribeAttempt()).append("\n")
                                .append("Has unconverted request context         : ").append(SignalStore.inAppPayments().getSubscriptionRequestCredential() != null).append("\n")
                                .append("Has unredeemed receipt presentation     : ").append(SignalStore.inAppPayments().getSubscriptionReceiptCredential() != null).append("\n");
    }
  }

  private @NonNull String getRedemptionStage(@NonNull InAppPaymentData inAppPaymentData) {
    if (inAppPaymentData.redemption == null) {
      return "null";
    } else {
      return inAppPaymentData.redemption.stage.name();
    }
  }

  private @NonNull String getError(@NonNull InAppPaymentData inAppPaymentData) {
    if (inAppPaymentData.error == null) {
      return "none";
    } else {
      return inAppPaymentData.error.toString();
    }
  }

  private @NonNull String getCancellation(@NonNull InAppPaymentData inAppPaymentData) {
    if (inAppPaymentData.cancellation == null) {
      return "none";
    } else {
      return inAppPaymentData.cancellation.reason.name();
    }
  }
}
