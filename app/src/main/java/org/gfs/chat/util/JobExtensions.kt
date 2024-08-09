/*
 * Copyright 2024 Signal Messenger, LLC
 * SPDX-License-Identifier: AGPL-3.0-only
 */

package org.gfs.chat.util

import org.gfs.chat.dependencies.AppDependencies
import org.gfs.chat.jobmanager.Job
import org.gfs.chat.jobmanager.JobManager

/** Starts a new chain with this job. */
fun Job.asChain(): JobManager.Chain {
  return AppDependencies.jobManager.startChain(this)
}
