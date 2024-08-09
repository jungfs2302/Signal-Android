package org.gfs.chat

import androidx.test.core.app.ApplicationProvider
import org.junit.rules.TestRule
import org.junit.runner.Description
import org.junit.runners.model.Statement
import org.gfs.chat.dependencies.AppDependencies
import org.gfs.chat.dependencies.MockApplicationDependencyProvider
import org.gfs.chat.keyvalue.KeyValueDataSet
import org.gfs.chat.keyvalue.KeyValueStore
import org.gfs.chat.keyvalue.MockKeyValuePersistentStorage
import org.gfs.chat.keyvalue.SignalStore

/**
 * Rule to setup [SignalStore] with a mock [KeyValueDataSet]. Must be used with Roboelectric.
 *
 * Can provide [defaultValues] to set the same values before each test and use [dataSet] directly to add any
 * test specific values.
 *
 * The [dataSet] is reset at the beginning of each test to an empty state.
 */
class SignalStoreRule @JvmOverloads constructor(private val defaultValues: KeyValueDataSet.() -> Unit = {}) : TestRule {
  var dataSet = KeyValueDataSet()
    private set

  override fun apply(base: Statement, description: Description): Statement {
    return object : Statement() {
      @Throws(Throwable::class)
      override fun evaluate() {
        if (!AppDependencies.isInitialized) {
          AppDependencies.init(ApplicationProvider.getApplicationContext(), MockApplicationDependencyProvider())
        }

        dataSet = KeyValueDataSet()
        SignalStore.testInject(KeyValueStore(MockKeyValuePersistentStorage.withDataSet(dataSet)))
        defaultValues.invoke(dataSet)

        base.evaluate()
      }
    }
  }
}
