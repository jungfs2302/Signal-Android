/*
 * Copyright 2024 Signal Messenger, LLC
 * SPDX-License-Identifier: AGPL-3.0-only
 */

package org.gfs.chat.jobs

/**
 * Denotes that a thrown exception can be retried
 */
class InAppPaymentRetryException(
  cause: Throwable? = null
) : Exception(cause)
