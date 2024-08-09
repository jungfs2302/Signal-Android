package org.gfs.chat.components.settings.app.subscription.receipts.detail

import android.content.Intent
import androidx.fragment.app.viewModels
import com.google.android.material.button.MaterialButton
import org.gfs.chat.R
import org.gfs.chat.components.SignalProgressDialog
import org.gfs.chat.components.settings.DSLConfiguration
import org.gfs.chat.components.settings.DSLSettingsFragment
import org.gfs.chat.components.settings.DSLSettingsText
import org.gfs.chat.components.settings.app.subscription.receipts.ReceiptImageRenderer
import org.gfs.chat.components.settings.configure
import org.gfs.chat.components.settings.models.SplashImage
import org.gfs.chat.database.model.InAppPaymentReceiptRecord
import org.gfs.chat.payments.FiatMoneyUtil
import org.gfs.chat.util.DateUtils
import org.gfs.chat.util.adapter.mapping.MappingAdapter
import java.util.Locale

class DonationReceiptDetailFragment : DSLSettingsFragment(layoutId = R.layout.donation_receipt_detail_fragment) {

  private lateinit var progressDialog: SignalProgressDialog

  private val viewModel: DonationReceiptDetailViewModel by viewModels(
    factoryProducer = {
      DonationReceiptDetailViewModel.Factory(
        DonationReceiptDetailFragmentArgs.fromBundle(requireArguments()).id,
        DonationReceiptDetailRepository()
      )
    }
  )

  override fun bindAdapter(adapter: MappingAdapter) {
    SplashImage.register(adapter)

    val sharePngButton: MaterialButton = requireView().findViewById(R.id.share_png)
    sharePngButton.isEnabled = false

    viewModel.state.observe(viewLifecycleOwner) { state ->
      if (state.inAppPaymentReceiptRecord != null) {
        adapter.submitList(getConfiguration(state.inAppPaymentReceiptRecord, state.subscriptionName).toMappingModelList())
      }

      if (state.inAppPaymentReceiptRecord != null && state.subscriptionName != null) {
        sharePngButton.isEnabled = true
        sharePngButton.setOnClickListener {
          progressDialog = SignalProgressDialog.show(requireContext())
          ReceiptImageRenderer.renderPng(
            context = requireContext(),
            lifecycleOwner = viewLifecycleOwner,
            record = state.inAppPaymentReceiptRecord,
            subscriptionName = state.subscriptionName,
            callback = object : ReceiptImageRenderer.Callback {
              override fun onBitmapRendered() {
                progressDialog.dismiss()
              }

              override fun onStartActivity(intent: Intent) {
                startActivity(intent)
              }
            }
          )
        }
      }
    }
  }

  private fun getConfiguration(record: InAppPaymentReceiptRecord, subscriptionName: String?): DSLConfiguration {
    return configure {
      customPref(
        SplashImage.Model(
          splashImageResId = R.drawable.ic_signal_logo_type
        )
      )

      textPref(
        title = DSLSettingsText.from(
          charSequence = FiatMoneyUtil.format(resources, record.amount),
          DSLSettingsText.TextAppearanceModifier(R.style.Signal_Text_Giant),
          DSLSettingsText.CenterModifier
        )
      )

      dividerPref()

      textPref(
        title = DSLSettingsText.from(R.string.DonationReceiptDetailsFragment__donation_type),
        summary = DSLSettingsText.from(
          when (record.type) {
            InAppPaymentReceiptRecord.Type.RECURRING_DONATION -> getString(R.string.DonationReceiptDetailsFragment__s_dash_s, subscriptionName, getString(R.string.DonationReceiptListFragment__recurring))
            InAppPaymentReceiptRecord.Type.ONE_TIME_DONATION -> getString(R.string.DonationReceiptListFragment__one_time)
            InAppPaymentReceiptRecord.Type.ONE_TIME_GIFT -> getString(R.string.DonationReceiptListFragment__donation_for_a_friend)
            InAppPaymentReceiptRecord.Type.RECURRING_BACKUP -> error("Not supported in this fragment.")
          }
        )
      )

      textPref(
        title = DSLSettingsText.from(R.string.DonationReceiptDetailsFragment__date_paid),
        summary = record.let { DSLSettingsText.from(DateUtils.formatDateWithYear(Locale.getDefault(), it.timestamp)) }
      )
    }
  }
}
