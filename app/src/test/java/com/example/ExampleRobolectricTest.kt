package com.example

import android.content.Context
import androidx.test.core.app.ApplicationProvider
import com.example.service.BatteryManagerHelper
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config

@RunWith(RobolectricTestRunner::class)
@Config(sdk = [34])
class ExampleRobolectricTest {

  @Test
  fun `read string from context`() {
    val context = ApplicationProvider.getApplicationContext<Context>()
    val appName = context.getString(R.string.app_name)
    assertEquals("Ampere Meter", appName)
  }

  @Test
  fun `battery helper returns valid state`() {
    val context = ApplicationProvider.getApplicationContext<Context>()
    val helper = BatteryManagerHelper(context)
    val state = helper.getBatteryState()
    assertNotNull(state)
    assertNotNull(state.health)
    assertNotNull(state.status)
  }
}
