package com.example.ui

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.data.local.AppDatabase
import com.example.data.model.AdWatchLogEntity
import com.example.data.model.DepositRecordEntity
import com.example.data.model.InvestmentPlan
import com.example.data.model.UserEntity
import com.example.data.model.WithdrawalRecordEntity
import com.example.data.repository.PassiveIncomeRepository
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

sealed class UiEvent {
    data class ShowMessage(val message: String, val isError: Boolean = false) : UiEvent()
    data class AdWatchSuccess(val rewardUsd: Double, val remainingAds: Int) : UiEvent()
    data class PlanPurchasedSuccess(val planName: String) : UiEvent()
}

class PassiveIncomeViewModel(application: Application) : AndroidViewModel(application) {

    private val database = AppDatabase.getInstance(application)
    private val repository = PassiveIncomeRepository(
        userDao = database.userDao(),
        depositDao = database.depositDao(),
        withdrawalDao = database.withdrawalDao(),
        adWatchDao = database.adWatchDao()
    )

    private val _currentUserId = MutableStateFlow<Long?>(null)
    val currentUserId: StateFlow<Long?> = _currentUserId.asStateFlow()

    private val _eventFlow = MutableSharedFlow<UiEvent>()
    val eventFlow: SharedFlow<UiEvent> = _eventFlow.asSharedFlow()

    private val _isLoading = MutableStateFlow(false)
    val isLoading: StateFlow<Boolean> = _isLoading.asStateFlow()

    // Current User reactive flow
    val currentUser: StateFlow<UserEntity?> = _currentUserId
        .flatMapLatest { id ->
            if (id != null) repository.getUserFlow(id) else flowOf(null)
        }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), null)

    // Current user's deposits
    val userDeposits: StateFlow<List<DepositRecordEntity>> = _currentUserId
        .flatMapLatest { id ->
            if (id != null) repository.getDepositsForUser(id) else flowOf(emptyList())
        }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    // Current user's withdrawals
    val userWithdrawals: StateFlow<List<WithdrawalRecordEntity>> = _currentUserId
        .flatMapLatest { id ->
            if (id != null) repository.getWithdrawalsForUser(id) else flowOf(emptyList())
        }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    // Current user's ad watch logs
    val userAdLogs: StateFlow<List<AdWatchLogEntity>> = _currentUserId
        .flatMapLatest { id ->
            if (id != null) repository.getAdLogsForUser(id) else flowOf(emptyList())
        }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    // Admin flows
    val allDepositsForAdmin: StateFlow<List<DepositRecordEntity>> = repository.getAllDeposits()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val allWithdrawalsForAdmin: StateFlow<List<WithdrawalRecordEntity>> = repository.getAllWithdrawals()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val allUsersForAdmin: StateFlow<List<UserEntity>> = repository.getAllUsersFlow()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    init {
        viewModelScope.launch {
            repository.seedInitialDataIfEmpty()
            // Auto login demo user for immediate great experience if no user selected
            val demo = repository.loginUser("demo@passiveincome.com", "demo123")
            demo.onSuccess { user ->
                _currentUserId.value = user.id
            }
        }
    }

    fun login(identifier: String, pass: String) {
        viewModelScope.launch {
            _isLoading.value = true
            val result = repository.loginUser(identifier, pass)
            _isLoading.value = false
            result.onSuccess { user ->
                _currentUserId.value = user.id
                _eventFlow.emit(UiEvent.ShowMessage("Welcome back, ${user.fullName}!"))
            }.onFailure { err ->
                _eventFlow.emit(UiEvent.ShowMessage(err.message ?: "Login failed", isError = true))
            }
        }
    }

    fun register(identifier: String, fullName: String, pass: String) {
        viewModelScope.launch {
            _isLoading.value = true
            val result = repository.registerUser(identifier, fullName, pass)
            _isLoading.value = false
            result.onSuccess { user ->
                _currentUserId.value = user.id
                _eventFlow.emit(UiEvent.ShowMessage("Account created successfully! Welcome ${user.fullName}!"))
            }.onFailure { err ->
                _eventFlow.emit(UiEvent.ShowMessage(err.message ?: "Sign up failed", isError = true))
            }
        }
    }

    fun logout() {
        _currentUserId.value = null
        viewModelScope.launch {
            _eventFlow.emit(UiEvent.ShowMessage("Logged out successfully"))
        }
    }

    fun buyPlan(planId: Int) {
        val uid = _currentUserId.value ?: return
        viewModelScope.launch {
            _isLoading.value = true
            val result = repository.purchasePlan(uid, planId)
            _isLoading.value = false
            result.onSuccess { user ->
                val plan = InvestmentPlan.getById(planId)
                _eventFlow.emit(UiEvent.PlanPurchasedSuccess(plan?.name ?: "Plan"))
                _eventFlow.emit(UiEvent.ShowMessage("Activated ${plan?.name}! You can now watch ${plan?.dailyAdsLimit} ads daily."))
            }.onFailure { err ->
                _eventFlow.emit(UiEvent.ShowMessage(err.message ?: "Could not buy plan", isError = true))
            }
        }
    }

    fun watchAd(adTitle: String, onComplete: () -> Unit = {}) {
        val uid = _currentUserId.value ?: return
        viewModelScope.launch {
            val result = repository.watchAdAndEarn(uid, adTitle)
            result.onSuccess { (reward, updatedUser) ->
                val remaining = updatedUser.planDailyAdLimit - updatedUser.adsWatchedToday
                _eventFlow.emit(UiEvent.AdWatchSuccess(reward, remaining))
                _eventFlow.emit(UiEvent.ShowMessage("Watched ad successfully! +$${String.format(java.util.Locale.US, "%.2f", reward)} USD credited to your wallet!"))
                onComplete()
            }.onFailure { err ->
                _eventFlow.emit(UiEvent.ShowMessage(err.message ?: "Could not claim ad reward", isError = true))
            }
        }
    }

    fun submitDeposit(
        method: String,
        amount: Double,
        currency: String,
        transactionId: String,
        senderDetails: String,
        screenshotUri: String?,
        onSuccess: () -> Unit
    ) {
        val uid = _currentUserId.value ?: return
        viewModelScope.launch {
            _isLoading.value = true
            val result = repository.submitDeposit(
                userId = uid,
                method = method,
                amount = amount,
                currency = currency,
                transactionId = transactionId,
                senderDetails = senderDetails,
                screenshotUri = screenshotUri
            )
            _isLoading.value = false
            result.onSuccess {
                _eventFlow.emit(UiEvent.ShowMessage("Deposit submitted for verification! Admin will review shortly."))
                onSuccess()
            }.onFailure { err ->
                _eventFlow.emit(UiEvent.ShowMessage(err.message ?: "Deposit submission failed", isError = true))
            }
        }
    }

    fun submitWithdrawal(
        method: String,
        amount: Double,
        accountTitle: String,
        accountIdentifier: String,
        onSuccess: () -> Unit
    ) {
        val uid = _currentUserId.value ?: return
        viewModelScope.launch {
            _isLoading.value = true
            val result = repository.submitWithdrawal(
                userId = uid,
                method = method,
                amount = amount,
                accountTitle = accountTitle,
                accountIdentifier = accountIdentifier
            )
            _isLoading.value = false
            result.onSuccess {
                _eventFlow.emit(UiEvent.ShowMessage("Withdrawal request submitted! Payout will be processed after admin review."))
                onSuccess()
            }.onFailure { err ->
                _eventFlow.emit(UiEvent.ShowMessage(err.message ?: "Withdrawal failed", isError = true))
            }
        }
    }

    fun adminApproveDeposit(depositId: Long, notes: String? = null) {
        viewModelScope.launch {
            val result = repository.approveDeposit(depositId, notes)
            result.onSuccess {
                _eventFlow.emit(UiEvent.ShowMessage("Deposit #$depositId approved and funds credited!"))
            }.onFailure { err ->
                _eventFlow.emit(UiEvent.ShowMessage(err.message ?: "Failed to approve deposit", isError = true))
            }
        }
    }

    fun adminRejectDeposit(depositId: Long, reason: String? = null) {
        viewModelScope.launch {
            val result = repository.rejectDeposit(depositId, reason)
            result.onSuccess {
                _eventFlow.emit(UiEvent.ShowMessage("Deposit #$depositId rejected."))
            }.onFailure { err ->
                _eventFlow.emit(UiEvent.ShowMessage(err.message ?: "Failed to reject deposit", isError = true))
            }
        }
    }

    fun adminCompleteWithdrawal(withdrawalId: Long, notes: String? = null) {
        viewModelScope.launch {
            val result = repository.completeWithdrawal(withdrawalId, notes)
            result.onSuccess {
                _eventFlow.emit(UiEvent.ShowMessage("Withdrawal #$withdrawalId marked as completed!"))
            }.onFailure { err ->
                _eventFlow.emit(UiEvent.ShowMessage(err.message ?: "Failed to complete withdrawal", isError = true))
            }
        }
    }

    fun adminRejectWithdrawal(withdrawalId: Long, reason: String? = null) {
        viewModelScope.launch {
            val result = repository.rejectWithdrawal(withdrawalId, reason)
            result.onSuccess {
                _eventFlow.emit(UiEvent.ShowMessage("Withdrawal #$withdrawalId rejected and funds refunded to user."))
            }.onFailure { err ->
                _eventFlow.emit(UiEvent.ShowMessage(err.message ?: "Failed to reject withdrawal", isError = true))
            }
        }
    }
}
