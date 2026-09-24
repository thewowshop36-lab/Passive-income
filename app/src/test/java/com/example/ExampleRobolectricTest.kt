package com.example

import android.content.Context
import androidx.test.core.app.ApplicationProvider
import com.example.data.model.InvestmentPlan
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
    assertEquals("Passive Income", appName)
  }

  @Test
  fun `verify 10 investment plans structure and limits`() {
    assertEquals(10, InvestmentPlan.ALL_PLANS.size)

    val expectedLimits = listOf(5, 7, 10, 12, 15, 18, 20, 25, 30, 35)
    val expectedPrices = listOf(1000.0, 2000.0, 3000.0, 4000.0, 5000.0, 6000.0, 7000.0, 8000.0, 9000.0, 10000.0)

    for (i in 0 until 10) {
      val plan = InvestmentPlan.ALL_PLANS[i]
      assertEquals(i + 1, plan.id)
      assertEquals("VIP Plan ${i + 1}", plan.name)
      assertEquals(expectedPrices[i], plan.pricePkr, 0.01)
      assertEquals(expectedLimits[i], plan.dailyAdsLimit)
      assertEquals(0.05, plan.rewardPerAdUsd, 0.001)
    }

    // Verify Plan 1 and Plan 10 specifics
    val plan1 = InvestmentPlan.getById(1)
    assertNotNull(plan1)
    assertEquals(1000.0, plan1!!.pricePkr, 0.01)
    assertEquals(5, plan1.dailyAdsLimit)
    assertEquals(0.25, plan1.dailyEarningsUsd, 0.01) // 5 * $0.05

    val plan10 = InvestmentPlan.getById(10)
    assertNotNull(plan10)
    assertEquals(10000.0, plan10!!.pricePkr, 0.01)
    assertEquals(35, plan10.dailyAdsLimit)
    assertEquals(1.75, plan10.dailyEarningsUsd, 0.01) // 35 * $0.05
  }
}
