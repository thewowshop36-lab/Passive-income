package com.example.data.repository

import com.example.data.local.AdWatchDao
import com.example.data.local.DepositDao
import com.example.data.local.UserDao
import com.example.data.local.WithdrawalDao
import com.example.data.model.AdWatchLogEntity
import com.example.data.model.DepositRecordEntity
import com.example.data.model.InvestmentPlan
import com.example.data.model.UserEntity
import com.example.data.model.WithdrawalRecordEntity
import kotlinx.coroutines.flow.Flow
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

class PassiveIncomeRepository(
    private val userDao: UserDao,
    private val depositDao: DepositDao,
    private val withdrawalDao: WithdrawalDao,
    private val adWatchDao: AdWatchDao
) {

    private val dateFormat = SimpleDateFormat("yyyy-MM-dd", Locale.US)

    private fun getTodayDateString(): String = dateFormat.format(Date())

    suspend fun seedInitialDataIfEmpty() {
        if (userDao.getUserCount() == 0) {
            // Seed Admin
            val adminUser = UserEntity(
                identifier = "admin@passiveincome.com",
                fullName = "System Administrator",
                passwordHash = "admin123",
                balanceUsd = 500.0,
                balancePkr = 500.0 * 280.0,
                isAdmin = true
            )
            userDao.insertUser(adminUser)

            // Seed Demo User with starting trial balance and VIP Plan 1 ready for testing
            val demoUser = UserEntity(
                identifier = "demo@passiveincome.com",
                fullName = "Demo Investor",
                passwordHash = "demo123",
                balanceUsd = 15.0,
                balancePkr = 4200.0,
                activePlanId = 1,
                activePlanName = "VIP Plan 1",
                planDailyAdLimit = 5,
                planPrice = 1000.0,
                planActivatedAt = System.currentTimeMillis() - 86400000L,
                adsWatchedToday = 2,
                lastAdWatchDate = getTodayDateString(),
                lastAdWatchTimestamp = System.currentTimeMillis() - 3600000L,
                totalEarnedUsd = 0.50,
                isAdmin = false
            )
            val demoUserId = userDao.insertUser(demoUser)

            // Seed initial sample deposit for demonstration
            depositDao.insertDeposit(
                DepositRecordEntity(
                    userId = demoUserId,
                    userIdentifier = "demo@passiveincome.com",
                    method = "USDT (TRC20)",
                    amount = 15.0,
                    currency = "USDT",
                    creditedUsd = 15.0,
                    creditedPkr = 4200.0,
                    transactionId = "TXN_SAMPLE_7829104",
                    senderDetails = "0x7a3...c91e",
                    screenshotUri = null,
                    status = "APPROVED",
                    adminNotes = "Initial verified deposit",
                    createdAt = System.currentTimeMillis() - 86400000L,
                    reviewedAt = System.currentTimeMillis() - 85000000L
                )
            )
        }
    }

    suspend fun registerUser(identifier: String, fullName: String, password: String): Result<UserEntity> {
        val trimmedId = identifier.trim()
        val trimmedName = fullName.trim()
        if (trimmedId.isEmpty() || password.isEmpty() || trimmedName.isEmpty()) {
            return Result.failure(IllegalArgumentException("All fields are required"))
        }

        val existing = userDao.getUserByIdentifier(trimmedId)
        if (existing != null) {
            return Result.failure(IllegalArgumentException("An account with this email/phone already exists"))
        }

        val newUser = UserEntity(
            identifier = trimmedId,
            fullName = trimmedName,
            passwordHash = password,
            balanceUsd = 0.0,
            balancePkr = 0.0,
            isAdmin = trimmedId.lowercase().contains("admin")
        )
        val newId = userDao.insertUser(newUser)
        val created = userDao.getUserById(newId)
            ?: return Result.failure(IllegalStateException("User creation failed"))
        return Result.success(created)
    }

    suspend fun loginUser(identifier: String, password: String): Result<UserEntity> {
        val trimmedId = identifier.trim()
        if (trimmedId.isEmpty() || password.isEmpty()) {
            return Result.failure(IllegalArgumentException("Please enter your email/phone and password"))
        }

        val user = userDao.getUserByIdentifier(trimmedId)
            ?: return Result.failure(IllegalArgumentException("No account found with this email or phone number"))

        if (user.passwordHash != password) {
            return Result.failure(IllegalArgumentException("Incorrect password"))
        }

        // Check daily reset on login
        val checkedUser = checkAndResetDailyAds(user)
        return Result.success(checkedUser)
    }

    suspend fun checkAndResetDailyAds(user: UserEntity): UserEntity {
        val today = getTodayDateString()
        val now = System.currentTimeMillis()
        val elapsed = now - user.lastAdWatchTimestamp

        // Reset if date changed or 24 hours (86400000 ms) passed
        if (user.lastAdWatchDate != today || (user.lastAdWatchTimestamp > 0 && elapsed >= 86_400_000L)) {
            val updated = user.copy(
                adsWatchedToday = 0,
                lastAdWatchDate = today
            )
            userDao.updateUser(updated)
            return updated
        }
        return user
    }

    fun getUserFlow(userId: Long): Flow<UserEntity?> = userDao.getUserByIdFlow(userId)

    suspend fun getUserById(userId: Long): UserEntity? = userDao.getUserById(userId)

    fun getAllUsersFlow(): Flow<List<UserEntity>> = userDao.getAllUsersFlow()

    // 10 Investment Plans Purchase
    suspend fun purchasePlan(userId: Long, planId: Int): Result<UserEntity> {
        val user = userDao.getUserById(userId) ?: return Result.failure(IllegalArgumentException("User not found"))
        val plan = InvestmentPlan.getById(planId) ?: return Result.failure(IllegalArgumentException("Invalid plan selected"))

        // Check balance: Plan prices are 1,000 to 10,000 PKR (or equivalent in USD)
        val hasPkrSufficient = user.balancePkr >= plan.pricePkr
        val hasUsdSufficient = user.balanceUsd >= plan.priceUsd

        if (!hasPkrSufficient && !hasUsdSufficient) {
            return Result.failure(
                IllegalStateException("Insufficient balance. Required: ${plan.pricePkr.toInt()} PKR or $${String.format(Locale.US, "%.2f", plan.priceUsd)} USD. Please deposit funds first.")
            )
        }

        var newBalancePkr = user.balancePkr
        var newBalanceUsd = user.balanceUsd

        if (hasPkrSufficient) {
            newBalancePkr -= plan.pricePkr
            newBalanceUsd = maxOf(0.0, newBalanceUsd - (plan.pricePkr / InvestmentPlan.USD_TO_PKR_RATE))
        } else {
            newBalanceUsd -= plan.priceUsd
            newBalancePkr = maxOf(0.0, newBalancePkr - (plan.priceUsd * InvestmentPlan.USD_TO_PKR_RATE))
        }

        val updated = user.copy(
            balancePkr = newBalancePkr,
            balanceUsd = newBalanceUsd,
            activePlanId = plan.id,
            activePlanName = plan.name,
            planDailyAdLimit = plan.dailyAdsLimit,
            planPrice = plan.pricePkr,
            planActivatedAt = System.currentTimeMillis(),
            adsWatchedToday = 0,
            lastAdWatchDate = getTodayDateString()
        )
        userDao.updateUser(updated)
        return Result.success(updated)
    }

    // Micro-task ad earnings
    suspend fun watchAdAndEarn(userId: Long, adTitle: String): Result<Pair<Double, UserEntity>> {
        val initialUser = userDao.getUserById(userId) ?: return Result.failure(IllegalArgumentException("User not found"))
        val user = checkAndResetDailyAds(initialUser)

        if (user.activePlanId == null || user.planDailyAdLimit <= 0) {
            return Result.failure(IllegalStateException("No active plan. Please purchase a VIP Plan to unlock daily ads!"))
        }

        if (user.adsWatchedToday >= user.planDailyAdLimit) {
            return Result.failure(IllegalStateException("Daily limit reached (${user.planDailyAdLimit}/${user.planDailyAdLimit}). Your counter will automatically reset every 24 hours."))
        }

        val rewardUsd = InvestmentPlan.AD_REWARD_USD
        val rewardPkr = rewardUsd * InvestmentPlan.USD_TO_PKR_RATE

        val newAdsWatched = user.adsWatchedToday + 1
        val updatedUser = user.copy(
            balanceUsd = user.balanceUsd + rewardUsd,
            balancePkr = user.balancePkr + rewardPkr,
            totalEarnedUsd = user.totalEarnedUsd + rewardUsd,
            adsWatchedToday = newAdsWatched,
            lastAdWatchDate = getTodayDateString(),
            lastAdWatchTimestamp = System.currentTimeMillis()
        )
        userDao.updateUser(updatedUser)

        adWatchDao.insertLog(
            AdWatchLogEntity(
                userId = userId,
                adTitle = adTitle,
                rewardUsd = rewardUsd,
                rewardPkr = rewardPkr,
                watchedAt = System.currentTimeMillis()
            )
        )

        return Result.success(Pair(rewardUsd, updatedUser))
    }

    fun getAdLogsForUser(userId: Long): Flow<List<AdWatchLogEntity>> = adWatchDao.getLogsForUser(userId)

    // Manual Deposit
    suspend fun submitDeposit(
        userId: Long,
        method: String,
        amount: Double,
        currency: String,
        transactionId: String,
        senderDetails: String,
        screenshotUri: String?
    ): Result<Long> {
        val user = userDao.getUserById(userId) ?: return Result.failure(IllegalArgumentException("User not found"))

        if (amount <= 0) {
            return Result.failure(IllegalArgumentException("Deposit amount must be greater than zero"))
        }
        if (transactionId.trim().isEmpty()) {
            return Result.failure(IllegalArgumentException("Transaction ID / Reference is required"))
        }

        val creditedUsd = if (currency.equals("USDT", ignoreCase = true)) amount else (amount / InvestmentPlan.USD_TO_PKR_RATE)
        val creditedPkr = if (currency.equals("PKR", ignoreCase = true)) amount else (amount * InvestmentPlan.USD_TO_PKR_RATE)

        val deposit = DepositRecordEntity(
            userId = userId,
            userIdentifier = user.identifier,
            method = method,
            amount = amount,
            currency = currency,
            creditedUsd = creditedUsd,
            creditedPkr = creditedPkr,
            transactionId = transactionId.trim(),
            senderDetails = senderDetails.trim(),
            screenshotUri = screenshotUri,
            status = "PENDING",
            createdAt = System.currentTimeMillis()
        )
        val depositId = depositDao.insertDeposit(deposit)
        return Result.success(depositId)
    }

    suspend fun approveDeposit(depositId: Long, adminNotes: String? = null): Result<Unit> {
        val deposit = depositDao.getDepositById(depositId)
            ?: return Result.failure(IllegalArgumentException("Deposit record not found"))

        if (deposit.status != "PENDING") {
            return Result.failure(IllegalStateException("Deposit is already ${deposit.status}"))
        }

        depositDao.updateDepositStatus(
            depositId = depositId,
            status = "APPROVED",
            adminNotes = adminNotes ?: "Approved by admin",
            reviewedAt = System.currentTimeMillis()
        )

        // Credit user balance
        userDao.creditBalance(deposit.userId, deposit.creditedUsd, deposit.creditedPkr)
        return Result.success(Unit)
    }

    suspend fun rejectDeposit(depositId: Long, reason: String? = null): Result<Unit> {
        val deposit = depositDao.getDepositById(depositId)
            ?: return Result.failure(IllegalArgumentException("Deposit record not found"))

        if (deposit.status != "PENDING") {
            return Result.failure(IllegalStateException("Deposit is already ${deposit.status}"))
        }

        depositDao.updateDepositStatus(
            depositId = depositId,
            status = "REJECTED",
            adminNotes = reason ?: "Rejected by admin",
            reviewedAt = System.currentTimeMillis()
        )
        return Result.success(Unit)
    }

    fun getDepositsForUser(userId: Long): Flow<List<DepositRecordEntity>> = depositDao.getDepositsForUser(userId)
    fun getAllDeposits(): Flow<List<DepositRecordEntity>> = depositDao.getAllDeposits()

    // Withdrawal Section
    // Payout methods & thresholds:
    // USDT TRC20: Minimum $12 USD
    // JazzCash: Minimum 4,000 PKR
    // EasyPaisa: Minimum 4,000 PKR
    suspend fun submitWithdrawal(
        userId: Long,
        method: String,
        amount: Double,
        accountTitle: String,
        accountIdentifier: String
    ): Result<Long> {
        val user = userDao.getUserById(userId) ?: return Result.failure(IllegalArgumentException("User not found"))

        if (accountTitle.trim().isEmpty() || accountIdentifier.trim().isEmpty()) {
            return Result.failure(IllegalArgumentException("Account Title and Account Number/Wallet are required"))
        }

        var currency = "PKR"
        var minLimit = 4000.0

        if (method.contains("USDT", ignoreCase = true)) {
            currency = "USD"
            minLimit = 12.0
            if (amount < minLimit) {
                return Result.failure(IllegalArgumentException("USDT TRC20 minimum withdrawal limit is $12 USD"))
            }
            if (user.balanceUsd < amount) {
                return Result.failure(IllegalStateException("Insufficient USD balance. Current balance: $${String.format(Locale.US, "%.2f", user.balanceUsd)}"))
            }
            // Deduct balance and hold
            val pkrDeduct = amount * InvestmentPlan.USD_TO_PKR_RATE
            userDao.debitBalance(userId, amount, pkrDeduct)
        } else {
            currency = "PKR"
            minLimit = 4000.0
            if (amount < minLimit) {
                return Result.failure(IllegalArgumentException("$method minimum withdrawal limit is 4,000 PKR"))
            }
            if (user.balancePkr < amount) {
                return Result.failure(IllegalStateException("Insufficient PKR balance. Current balance: ${user.balancePkr.toInt()} PKR"))
            }
            // Deduct balance and hold
            val usdDeduct = amount / InvestmentPlan.USD_TO_PKR_RATE
            userDao.debitBalance(userId, usdDeduct, amount)
        }

        val withdrawal = WithdrawalRecordEntity(
            userId = userId,
            userIdentifier = user.identifier,
            method = method,
            amount = amount,
            currency = currency,
            accountTitle = accountTitle.trim(),
            accountIdentifier = accountIdentifier.trim(),
            status = "PENDING",
            createdAt = System.currentTimeMillis()
        )
        val withdrawalId = withdrawalDao.insertWithdrawal(withdrawal)
        return Result.success(withdrawalId)
    }

    suspend fun completeWithdrawal(withdrawalId: Long, adminNotes: String? = null): Result<Unit> {
        val withdrawal = withdrawalDao.getWithdrawalById(withdrawalId)
            ?: return Result.failure(IllegalArgumentException("Withdrawal record not found"))

        if (withdrawal.status != "PENDING") {
            return Result.failure(IllegalStateException("Withdrawal is already ${withdrawal.status}"))
        }

        withdrawalDao.updateWithdrawalStatus(
            withdrawalId = withdrawalId,
            status = "COMPLETED",
            adminNotes = adminNotes ?: "Processed and transferred successfully",
            completedAt = System.currentTimeMillis()
        )
        return Result.success(Unit)
    }

    suspend fun rejectWithdrawal(withdrawalId: Long, reason: String? = null): Result<Unit> {
        val withdrawal = withdrawalDao.getWithdrawalById(withdrawalId)
            ?: return Result.failure(IllegalArgumentException("Withdrawal record not found"))

        if (withdrawal.status != "PENDING") {
            return Result.failure(IllegalStateException("Withdrawal is already ${withdrawal.status}"))
        }

        withdrawalDao.updateWithdrawalStatus(
            withdrawalId = withdrawalId,
            status = "REJECTED",
            adminNotes = reason ?: "Rejected by admin",
            completedAt = System.currentTimeMillis()
        )

        // Refund held balance back to user
        if (withdrawal.currency == "USD") {
            val usdAmount = withdrawal.amount
            val pkrAmount = usdAmount * InvestmentPlan.USD_TO_PKR_RATE
            userDao.creditBalance(withdrawal.userId, usdAmount, pkrAmount)
        } else {
            val pkrAmount = withdrawal.amount
            val usdAmount = pkrAmount / InvestmentPlan.USD_TO_PKR_RATE
            userDao.creditBalance(withdrawal.userId, usdAmount, pkrAmount)
        }

        return Result.success(Unit)
    }

    fun getWithdrawalsForUser(userId: Long): Flow<List<WithdrawalRecordEntity>> = withdrawalDao.getWithdrawalsForUser(userId)
    fun getAllWithdrawals(): Flow<List<WithdrawalRecordEntity>> = withdrawalDao.getAllWithdrawals()
}
