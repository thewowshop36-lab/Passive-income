package com.example

import com.example.data.model.InvestmentPlan
import org.junit.Assert.*
import org.junit.Test

class ExampleUnitTest {

  @Test
  fun testFixedAdEarnings() {
    val singleAdReward = 0.05
    assertEquals(0.05, singleAdReward, 0.0001)

    // Plan 1: 5 ads * 0.05 = $0.25
    assertEquals(0.25, 5 * singleAdReward, 0.0001)

    // Plan 10: 35 ads * 0.05 = $1.75
    assertEquals(1.75, 35 * singleAdReward, 0.0001)
  }

  @Test
  fun testWithdrawalMinimumThresholds() {
    val usdtMinLimitUsd = 12.0
    val jazzCashMinLimitPkr = 4000.0
    val easyPaisaMinLimitPkr = 4000.0

    assertTrue(12.0 >= usdtMinLimitUsd)
    assertFalse(11.99 >= usdtMinLimitUsd)

    assertTrue(4000.0 >= jazzCashMinLimitPkr)
    assertFalse(3999.0 >= jazzCashMinLimitPkr)

    assertTrue(4000.0 >= easyPaisaMinLimitPkr)
    assertFalse(3500.0 >= easyPaisaMinLimitPkr)
  }

  @Test
  fun testAllTenInvestmentPlans() {
    val plans = InvestmentPlan.ALL_PLANS
    assertEquals(10, plans.size)

    assertEquals(1000.0, plans[0].pricePkr, 0.01)
    assertEquals(5, plans[0].dailyAdsLimit)

    assertEquals(2000.0, plans[1].pricePkr, 0.01)
    assertEquals(7, plans[1].dailyAdsLimit)

    assertEquals(3000.0, plans[2].pricePkr, 0.01)
    assertEquals(10, plans[2].dailyAdsLimit)

    assertEquals(4000.0, plans[3].pricePkr, 0.01)
    assertEquals(12, plans[3].dailyAdsLimit)

    assertEquals(5000.0, plans[4].pricePkr, 0.01)
    assertEquals(15, plans[4].dailyAdsLimit)

    assertEquals(6000.0, plans[5].pricePkr, 0.01)
    assertEquals(18, plans[5].dailyAdsLimit)

    assertEquals(7000.0, plans[6].pricePkr, 0.01)
    assertEquals(20, plans[6].dailyAdsLimit)

    assertEquals(8000.0, plans[7].pricePkr, 0.01)
    assertEquals(25, plans[7].dailyAdsLimit)

    assertEquals(9000.0, plans[8].pricePkr, 0.01)
    assertEquals(30, plans[8].dailyAdsLimit)

    assertEquals(10000.0, plans[9].pricePkr, 0.01)
    assertEquals(35, plans[9].dailyAdsLimit)
  }
}
