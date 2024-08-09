/*
 * Copyright 2023 Signal Messenger, LLC
 * SPDX-License-Identifier: AGPL-3.0-only
 */

package org.gfs.chat.components.settings.models

import androidx.annotation.StringRes
import org.gfs.chat.databinding.DslBannerBinding
import org.gfs.chat.util.adapter.mapping.BindingFactory
import org.gfs.chat.util.adapter.mapping.BindingViewHolder
import org.gfs.chat.util.adapter.mapping.MappingAdapter
import org.gfs.chat.util.adapter.mapping.MappingModel

/**
 * Displays a banner to notify the user of certain state or action that needs to be taken.
 */
object Banner {
  fun register(mappingAdapter: MappingAdapter) {
    mappingAdapter.registerFactory(Model::class.java, BindingFactory(::ViewHolder, DslBannerBinding::inflate))
  }

  class Model(
    @StringRes val textId: Int,
    @StringRes val actionId: Int,
    val onClick: () -> Unit
  ) : MappingModel<Model> {
    override fun areItemsTheSame(newItem: Model): Boolean {
      return true
    }

    override fun areContentsTheSame(newItem: Model): Boolean {
      return textId == newItem.textId && actionId == newItem.actionId
    }
  }

  private class ViewHolder(binding: DslBannerBinding) : BindingViewHolder<Model, DslBannerBinding>(binding) {
    override fun bind(model: Model) {
      binding.bannerText.setText(model.textId)
      binding.bannerAction.setText(model.actionId)
      binding.bannerAction.setOnClickListener { model.onClick() }
    }
  }
}
