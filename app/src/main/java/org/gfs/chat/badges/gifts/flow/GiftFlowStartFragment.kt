package org.gfs.chat.badges.gifts.flow

import android.view.View
import androidx.fragment.app.viewModels
import androidx.navigation.fragment.findNavController
import io.reactivex.rxjava3.android.schedulers.AndroidSchedulers
import org.signal.core.util.DimensionUnit
import org.signal.core.util.concurrent.LifecycleDisposable
import org.signal.donations.InAppPaymentType
import org.gfs.chat.R
import org.gfs.chat.components.settings.DSLConfiguration
import org.gfs.chat.components.settings.DSLSettingsFragment
import org.gfs.chat.components.settings.DSLSettingsText
import org.gfs.chat.components.settings.app.subscription.models.CurrencySelection
import org.gfs.chat.components.settings.app.subscription.models.NetworkFailure
import org.gfs.chat.components.settings.configure
import org.gfs.chat.components.settings.models.IndeterminateLoadingCircle
import org.gfs.chat.components.settings.models.SplashImage
import org.gfs.chat.util.ViewUtil
import org.gfs.chat.util.adapter.mapping.MappingAdapter
import org.gfs.chat.util.navigation.safeNavigate
import java.util.concurrent.TimeUnit

/**
 * Landing fragment for sending gifts.
 */
class GiftFlowStartFragment : DSLSettingsFragment(
  layoutId = R.layout.gift_flow_start_fragment
) {

  private val viewModel: GiftFlowViewModel by viewModels(
    ownerProducer = { requireActivity() },
    factoryProducer = {
      GiftFlowViewModel.Factory(GiftFlowRepository())
    }
  )

  private val lifecycleDisposable = LifecycleDisposable()

  override fun bindAdapter(adapter: MappingAdapter) {
    CurrencySelection.register(adapter)
    GiftRowItem.register(adapter)
    NetworkFailure.register(adapter)
    IndeterminateLoadingCircle.register(adapter)
    SplashImage.register(adapter)

    val next = requireView().findViewById<View>(R.id.next)
    next.setOnClickListener {
      findNavController().safeNavigate(R.id.action_giftFlowStartFragment_to_giftFlowRecipientSelectionFragment)
    }

    lifecycleDisposable.bindTo(viewLifecycleOwner)
    lifecycleDisposable += viewModel.state.observeOn(AndroidSchedulers.mainThread()).subscribe { state ->
      next.isEnabled = state.stage == GiftFlowState.Stage.READY

      adapter.submitList(getConfiguration(state).toMappingModelList())
    }
  }

  override fun onResume() {
    super.onResume()
    ViewUtil.hideKeyboard(requireContext(), requireView())
  }

  private fun getConfiguration(state: GiftFlowState): DSLConfiguration {
    return configure {
      customPref(
        SplashImage.Model(
          R.drawable.ic_gift_chat
        )
      )

      noPadTextPref(
        title = DSLSettingsText.from(
          R.string.GiftFlowStartFragment__donate_for_a_friend,
          DSLSettingsText.CenterModifier,
          DSLSettingsText.TextAppearanceModifier(R.style.Signal_Text_Headline)
        )
      )

      space(DimensionUnit.DP.toPixels(16f).toInt())

      val days = state.giftBadge?.duration?.let { TimeUnit.MILLISECONDS.toDays(it) } ?: 60L
      noPadTextPref(
        title = DSLSettingsText.from(resources.getQuantityString(R.plurals.GiftFlowStartFragment__support_signal_by, days.toInt(), days), DSLSettingsText.CenterModifier)
      )

      space(DimensionUnit.DP.toPixels(16f).toInt())

      customPref(
        CurrencySelection.Model(
          selectedCurrency = state.currency,
          isEnabled = state.stage == GiftFlowState.Stage.READY,
          onClick = {
            val action = GiftFlowStartFragmentDirections.actionGiftFlowStartFragmentToSetCurrencyFragment(InAppPaymentType.ONE_TIME_GIFT, viewModel.getSupportedCurrencyCodes().toTypedArray())
            findNavController().safeNavigate(action)
          }
        )
      )

      @Suppress("CascadeIf")
      if (state.stage == GiftFlowState.Stage.FAILURE) {
        customPref(
          NetworkFailure.Model(
            onRetryClick = {
              viewModel.retry()
            }
          )
        )
      } else if (state.stage == GiftFlowState.Stage.INIT) {
        customPref(IndeterminateLoadingCircle)
      } else if (state.giftBadge != null) {
        state.giftPrices[state.currency]?.let {
          customPref(
            GiftRowItem.Model(
              giftBadge = state.giftBadge,
              price = it
            )
          )
        }
      }
    }
  }
}
