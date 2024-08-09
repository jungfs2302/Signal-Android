/*
 * Copyright 2024 Signal Messenger, LLC
 * SPDX-License-Identifier: AGPL-3.0-only
 */

package org.gfs.chat.jobmanager.impl

import android.app.Application
import android.app.job.JobInfo
import org.gfs.chat.jobmanager.Constraint
import org.gfs.chat.util.NetworkUtil

/**
 * Constraint that, when added, means that a job cannot be performed unless the user has Wifi
 */
class WifiConstraint(private val application: Application) : Constraint {

  companion object {
    const val KEY = "WifiConstraint"
  }

  override fun isMet(): Boolean {
    return NetworkUtil.isConnectedWifi(application)
  }

  override fun getFactoryKey(): String = KEY

  override fun applyToJobInfo(jobInfoBuilder: JobInfo.Builder) = Unit

  class Factory(val application: Application) : Constraint.Factory<WifiConstraint> {
    override fun create(): WifiConstraint {
      return WifiConstraint(application)
    }
  }
}
