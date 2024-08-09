package org.gfs.chat

import org.signal.core.util.concurrent.SignalExecutors
import org.signal.core.util.logging.AndroidLogger
import org.signal.core.util.logging.Log
import org.signal.libsignal.protocol.logging.SignalProtocolLoggerProvider
import org.gfs.chat.database.LogDatabase
import org.gfs.chat.dependencies.AppDependencies
import org.gfs.chat.dependencies.ApplicationDependencyProvider
import org.gfs.chat.dependencies.InstrumentationApplicationDependencyProvider
import org.gfs.chat.logging.CustomSignalProtocolLogger
import org.gfs.chat.logging.PersistentLogger
import org.gfs.chat.testing.InMemoryLogger

/**
 * Application context for running instrumentation tests (aka androidTests).
 */
class SignalInstrumentationApplicationContext : ApplicationContext() {

  val inMemoryLogger: InMemoryLogger = InMemoryLogger()

  override fun initializeAppDependencies() {
    val default = ApplicationDependencyProvider(this)
    AppDependencies.init(this, InstrumentationApplicationDependencyProvider(this, default))
    AppDependencies.deadlockDetector.start()
  }

  override fun initializeLogging() {
    Log.initialize({ true }, AndroidLogger(), PersistentLogger(this), inMemoryLogger)

    SignalProtocolLoggerProvider.setProvider(CustomSignalProtocolLogger())

    SignalExecutors.UNBOUNDED.execute {
      Log.blockUntilAllWritesFinished()
      LogDatabase.getInstance(this).logs.trimToSize()
    }
  }

  override fun beginJobLoop() = Unit

  /**
   * Some of the jobs can interfere with some of the instrumentation tests.
   *
   * For example, we may try to create a release channel recipient while doing
   * an import/backup test.
   *
   * This can be used to start the job loop if needed for tests that rely on it.
   */
  fun beginJobLoopForTests() {
    super.beginJobLoop()
  }
}
