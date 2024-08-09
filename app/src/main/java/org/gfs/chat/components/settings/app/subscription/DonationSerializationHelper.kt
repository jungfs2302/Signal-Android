/*
 * Copyright 2023 Signal Messenger, LLC
 * SPDX-License-Identifier: AGPL-3.0-only
 */

package org.gfs.chat.components.settings.app.subscription

import okio.ByteString
import org.signal.core.util.money.FiatMoney
import org.signal.donations.PaymentSourceType
import org.gfs.chat.badges.Badges
import org.gfs.chat.badges.models.Badge
import org.gfs.chat.database.model.databaseprotos.DecimalValue
import org.gfs.chat.database.model.databaseprotos.FiatValue
import org.gfs.chat.database.model.databaseprotos.PendingOneTimeDonation
import java.math.BigDecimal
import java.math.BigInteger
import java.math.MathContext
import java.util.Currency

object DonationSerializationHelper {
  fun createPendingOneTimeDonationProto(
    badge: Badge,
    paymentSourceType: PaymentSourceType,
    amount: FiatMoney
  ): PendingOneTimeDonation {
    return PendingOneTimeDonation(
      badge = Badges.toDatabaseBadge(badge),
      paymentMethodType = when (paymentSourceType) {
        PaymentSourceType.PayPal -> PendingOneTimeDonation.PaymentMethodType.PAYPAL
        PaymentSourceType.Stripe.CreditCard, PaymentSourceType.Stripe.GooglePay, PaymentSourceType.Unknown -> PendingOneTimeDonation.PaymentMethodType.CARD
        PaymentSourceType.Stripe.SEPADebit -> PendingOneTimeDonation.PaymentMethodType.SEPA_DEBIT
        PaymentSourceType.Stripe.IDEAL -> PendingOneTimeDonation.PaymentMethodType.IDEAL
      },
      amount = amount.toFiatValue(),
      timestamp = System.currentTimeMillis()
    )
  }

  fun FiatValue.toFiatMoney(): FiatMoney {
    return FiatMoney(
      amount!!.toBigDecimal(),
      Currency.getInstance(currencyCode)
    )
  }

  fun DecimalValue.toBigDecimal(): BigDecimal {
    return BigDecimal(
      BigInteger(value_.toByteArray()),
      scale,
      MathContext(precision)
    )
  }

  fun FiatMoney.toFiatValue(): FiatValue {
    return FiatValue(
      currencyCode = currency.currencyCode,
      amount = amount.toDecimalValue()
    )
  }

  fun BigDecimal.toDecimalValue(): DecimalValue {
    return DecimalValue(
      scale = scale(),
      precision = precision(),
      value_ = ByteString.of(*this.unscaledValue().toByteArray())
    )
  }
}
