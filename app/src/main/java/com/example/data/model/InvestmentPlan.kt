package com.example.data.model

data class InvestmentPlan(
    val id: Int,
    val name: String,
    val subtitle: String,
    val pricePkr: Double,
    val priceUsd: Double,
    val dailyAdsLimit: Int,
    val rewardPerAdUsd: Double = 0.05,
    val badge: String? = null,
    val isPopular: Boolean = false
) {
    val dailyEarningsUsd: Double
        get() = dailyAdsLimit * rewardPerAdUsd

    val dailyEarningsPkr: Double
        get() = dailyEarningsUsd * 280.0

    val monthlyEarningsUsd: Double
        get() = dailyEarningsUsd * 30.0

    val monthlyEarningsPkr: Double
        get() = monthlyEarningsUsd * 280.0

    companion object {
        const val USD_TO_PKR_RATE = 280.0
        const val AD_REWARD_USD = 0.05

        val ALL_PLANS: List<InvestmentPlan> = listOf(
            InvestmentPlan(
                id = 1,
                name = "VIP Plan 1",
                subtitle = "Starter Micro Plan",
                pricePkr = 1000.0,
                priceUsd = 1000.0 / USD_TO_PKR_RATE,
                dailyAdsLimit = 5,
                badge = "Entry Level"
            ),
            InvestmentPlan(
                id = 2,
                name = "VIP Plan 2",
                subtitle = "Basic Earner Plan",
                pricePkr = 2000.0,
                priceUsd = 2000.0 / USD_TO_PKR_RATE,
                dailyAdsLimit = 7,
                badge = null
            ),
            InvestmentPlan(
                id = 3,
                name = "VIP Plan 3",
                subtitle = "Standard Growth Plan",
                pricePkr = 3000.0,
                priceUsd = 3000.0 / USD_TO_PKR_RATE,
                dailyAdsLimit = 10,
                badge = "Popular",
                isPopular = true
            ),
            InvestmentPlan(
                id = 4,
                name = "VIP Plan 4",
                subtitle = "Silver Tier Plan",
                pricePkr = 4000.0,
                priceUsd = 4000.0 / USD_TO_PKR_RATE,
                dailyAdsLimit = 12,
                badge = null
            ),
            InvestmentPlan(
                id = 5,
                name = "VIP Plan 5",
                subtitle = "Gold Tier Plan",
                pricePkr = 5000.0,
                priceUsd = 5000.0 / USD_TO_PKR_RATE,
                dailyAdsLimit = 15,
                badge = "High Value",
                isPopular = true
            ),
            InvestmentPlan(
                id = 6,
                name = "VIP Plan 6",
                subtitle = "Platinum Pro Plan",
                pricePkr = 6000.0,
                priceUsd = 6000.0 / USD_TO_PKR_RATE,
                dailyAdsLimit = 18,
                badge = null
            ),
            InvestmentPlan(
                id = 7,
                name = "VIP Plan 7",
                subtitle = "Diamond Executive Plan",
                pricePkr = 7000.0,
                priceUsd = 7000.0 / USD_TO_PKR_RATE,
                dailyAdsLimit = 20,
                badge = null
            ),
            InvestmentPlan(
                id = 8,
                name = "VIP Plan 8",
                subtitle = "Crown Master Plan",
                pricePkr = 8000.0,
                priceUsd = 8000.0 / USD_TO_PKR_RATE,
                dailyAdsLimit = 25,
                badge = "Super Earner"
            ),
            InvestmentPlan(
                id = 9,
                name = "VIP Plan 9",
                subtitle = "Royal Elite Plan",
                pricePkr = 9000.0,
                priceUsd = 9000.0 / USD_TO_PKR_RATE,
                dailyAdsLimit = 30,
                badge = null
            ),
            InvestmentPlan(
                id = 10,
                name = "VIP Plan 10",
                subtitle = "Max VIP Ultimate",
                pricePkr = 10000.0,
                priceUsd = 10000.0 / USD_TO_PKR_RATE,
                dailyAdsLimit = 35,
                badge = "Max VIP Plan",
                isPopular = true
            )
        )

        fun getById(id: Int): InvestmentPlan? = ALL_PLANS.find { it.id == id }
    }
}
