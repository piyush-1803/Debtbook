package com.example.ui

import android.app.Application
import android.content.Context
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.data.DebtDatabase
import com.example.data.DebtRepository
import com.example.data.DbEntity
import com.example.data.DbTransaction
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import com.google.firebase.FirebaseApp
import com.google.firebase.FirebaseOptions
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser

data class EntityWithBalance(
    val entity: DbEntity,
    val netBalance: Double, // positive if they owe us (Receivable), negative if we owe them (Payable)
    val totalLent: Double,
    val totalBorrowed: Double,
    val transactionCount: Int
)

class MainViewModel(application: Application) : AndroidViewModel(application) {
    private val repository: DebtRepository
    val sharedPrefs = application.getSharedPreferences("debtbook_prefs", Context.MODE_PRIVATE)

    // Firebase Auth States
    private val _firebaseAuth = MutableStateFlow<FirebaseAuth?>(null)
    val firebaseAuth = _firebaseAuth.asStateFlow()

    private val _firebaseUser = MutableStateFlow<FirebaseUser?>(null)
    val firebaseUser = _firebaseUser.asStateFlow()

    private val _firebaseStatus = MutableStateFlow<String>("NOT_INITIALIZED") // "NOT_INITIALIZED", "INITIALIZED", "AUTHENTICATED", "ERROR"
    val firebaseStatus = _firebaseStatus.asStateFlow()

    val firebaseApiKey = MutableStateFlow(sharedPrefs.getString("firebase_api_key", "") ?: "")
    val firebaseProjectId = MutableStateFlow(sharedPrefs.getString("firebase_project_id", "") ?: "")
    val firebaseAppId = MutableStateFlow(sharedPrefs.getString("firebase_app_id", "") ?: "")

    // User profile state
    private val _isSignedUp = MutableStateFlow(sharedPrefs.getBoolean("is_signed_up", false))
    val isSignedUp = _isSignedUp.asStateFlow()

    private val _userName = MutableStateFlow(sharedPrefs.getString("user_name", "") ?: "")
    val userName = _userName.asStateFlow()

    private val _userPhone = MutableStateFlow(sharedPrefs.getString("user_phone", "") ?: "")
    val userPhone = _userPhone.asStateFlow()

    private val _avatarUrl = MutableStateFlow(
        sharedPrefs.getString("avatar_url", "") ?: ""
    )
    val avatarUrl = _avatarUrl.asStateFlow()

    private val _currencySymbol = MutableStateFlow("₹") // Indian Rupee as default
    val currencySymbol = _currencySymbol.asStateFlow()

    // Database flow states
    val allEntities = MutableStateFlow<List<DbEntity>>(emptyList())
    val allTransactions = MutableStateFlow<List<DbTransaction>>(emptyList())

    // Search and filter states
    val searchQuery = MutableStateFlow("")
    val filterType = MutableStateFlow("ALL") // "ALL", "PAYABLE", "RECEIVABLE"
    val filterEntityId = MutableStateFlow<Long?>(null)

    // Computed entity-with-balance flows
    val entitiesWithBalances: StateFlow<List<EntityWithBalance>> = combine(
        allEntities,
        allTransactions
    ) { entities, transactions ->
        entities.map { entity ->
            val entityTx = transactions.filter { it.entityId == entity.id }
            val totalLent = entityTx.filter { it.type == "RECEIVABLE" }.sumOf { it.amount }
            val totalBorrowed = entityTx.filter { it.type == "PAYABLE" }.sumOf { it.amount }
            EntityWithBalance(
                entity = entity,
                netBalance = totalLent - totalBorrowed,
                totalLent = totalLent,
                totalBorrowed = totalBorrowed,
                transactionCount = entityTx.size
            )
        }
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    // Financial totals
    val totalReceivable: StateFlow<Double> = entitiesWithBalances.map { list ->
        list.filter { it.netBalance > 0 }.sumOf { it.netBalance }
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), 0.0)

    val totalPayable: StateFlow<Double> = entitiesWithBalances.map { list ->
        list.filter { it.netBalance < 0 }.sumOf { -it.netBalance }
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), 0.0)

    val netBalance: StateFlow<Double> = entitiesWithBalances.map { list ->
        list.sumOf { it.netBalance }
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), 0.0)

    // Filtered transaction list
    val filteredTransactions: StateFlow<List<DbTransaction>> = combine(
        allTransactions,
        searchQuery,
        filterType,
        filterEntityId
    ) { transactions, query, type, entityId ->
        transactions.filter { tx ->
            val matchesQuery = query.isEmpty() || tx.description.contains(query, ignoreCase = true) || 
                               (getEntityNameSync(tx.entityId).contains(query, ignoreCase = true))
            val matchesType = type == "ALL" || tx.type == type
            val matchesEntity = entityId == null || tx.entityId == entityId
            matchesQuery && matchesType && matchesEntity
        }
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    init {
        tryInitializeFirebase()
        val database = DebtDatabase.getDatabase(application)
        repository = DebtRepository(database.debtDao())

        // Collect flows to update state
        viewModelScope.launch {
            repository.allEntities.collect {
                allEntities.value = it
            }
        }
        viewModelScope.launch {
            repository.allTransactions.collect {
                allTransactions.value = it
            }
        }
    }

    private fun getEntityNameSync(entityId: Long): String {
        return allEntities.value.find { it.id == entityId }?.name ?: "Unknown"
    }

    fun completeSignUp(name: String, phone: String, avatar: String) {
        viewModelScope.launch {
            sharedPrefs.edit()
                .putBoolean("is_signed_up", true)
                .putString("user_name", name)
                .putString("user_phone", phone)
                .putString("avatar_url", avatar)
                .apply()
            _isSignedUp.value = true
            _userName.value = name
            _userPhone.value = phone
            _avatarUrl.value = avatar
        }
    }

    fun addEntity(name: String, phone: String, relationship: String) {
        viewModelScope.launch {
            repository.insertEntity(DbEntity(name = name, phone = phone, relationship = relationship))
        }
    }

    fun deleteEntity(id: Long) {
        viewModelScope.launch {
            repository.deleteEntity(id)
        }
    }

    fun addTransaction(amount: Double, description: String, type: String, category: String, entityId: Long, date: Long = System.currentTimeMillis()) {
        viewModelScope.launch {
            repository.insertTransaction(
                DbTransaction(
                    amount = amount,
                    description = description,
                    type = type,
                    category = category,
                    entityId = entityId,
                    dateStamp = date
                )
            )
        }
    }

    fun deleteTransaction(id: Long) {
        viewModelScope.launch {
            repository.deleteTransaction(id)
        }
    }

    fun copyUriToInternal(uri: android.net.Uri): String? {
        val context = getApplication<Application>()
        return try {
            val resolver = context.contentResolver
            val fileName = "custom_pfp_${System.currentTimeMillis()}.jpg"
            val file = java.io.File(context.filesDir, fileName)
            resolver.openInputStream(uri)?.use { inputStream ->
                java.io.FileOutputStream(file).use { outputStream ->
                    inputStream.copyTo(outputStream)
                }
            }
            file.absolutePath
        } catch (e: Exception) {
            e.printStackTrace()
            null
        }
    }

    fun setCustomPfp(uri: android.net.Uri) {
        viewModelScope.launch {
            val localPath = copyUriToInternal(uri)
            if (localPath != null) {
                sharedPrefs.edit()
                    .putString("avatar_url", localPath)
                    .apply()
                _avatarUrl.value = localPath
            }
        }
    }

    fun tryInitializeFirebase() {
        val app = getApplication<Application>()
        try {
            // First try default Firebase configuration (e.g. from google-services.json)
            FirebaseApp.initializeApp(app)
            val auth = FirebaseAuth.getInstance()
            _firebaseAuth.value = auth
            _firebaseUser.value = auth.currentUser
            _firebaseStatus.value = if (auth.currentUser != null) "AUTHENTICATED" else "INITIALIZED"
        } catch (e: Exception) {
            // If default config isn't available, check SharedPreferences for credentials
            val apiKey = firebaseApiKey.value
            val projId = firebaseProjectId.value
            val appId = firebaseAppId.value
            if (apiKey.isNotEmpty() && projId.isNotEmpty() && appId.isNotEmpty()) {
                val uniqueAppName = "DynamicAuthApp"
                try {
                    val options = FirebaseOptions.Builder()
                        .setApiKey(apiKey)
                        .setProjectId(projId)
                        .setApplicationId(appId)
                        .build()
                    val firebaseApp = try {
                        FirebaseApp.getInstance(uniqueAppName)
                    } catch (ex: Exception) {
                        FirebaseApp.initializeApp(app, options, uniqueAppName)
                    }
                    val auth = FirebaseAuth.getInstance(firebaseApp)
                    _firebaseAuth.value = auth
                    _firebaseUser.value = auth.currentUser
                    _firebaseStatus.value = if (auth.currentUser != null) "AUTHENTICATED" else "INITIALIZED"
                } catch (ex: Exception) {
                    _firebaseStatus.value = "ERROR"
                }
            } else {
                _firebaseStatus.value = "NOT_INITIALIZED"
            }
        }
    }

    fun saveFirebaseConfig(apiKey: String, projectId: String, appId: String) {
        sharedPrefs.edit()
            .putString("firebase_api_key", apiKey.trim())
            .putString("firebase_project_id", projectId.trim())
            .putString("firebase_app_id", appId.trim())
            .apply()
        firebaseApiKey.value = apiKey.trim()
        firebaseProjectId.value = projectId.trim()
        firebaseAppId.value = appId.trim()
        tryInitializeFirebase()
    }

    fun firebaseSignIn(email: String, password: String, onSuccess: () -> Unit, onFailure: (String) -> Unit) {
        val auth = _firebaseAuth.value
        if (auth == null) {
            onFailure("Firebase Authentication is not initialized yet. Please configure credentials.")
            return
        }
        auth.signInWithEmailAndPassword(email, password)
            .addOnCompleteListener { task ->
                if (task.isSuccessful) {
                    _firebaseUser.value = auth.currentUser
                    _firebaseStatus.value = "AUTHENTICATED"
                    val userEmail = auth.currentUser?.email ?: ""
                    completeSignUp(userEmail.substringBefore("@"), "", "")
                    onSuccess()
                } else {
                    onFailure(task.exception?.localizedMessage ?: "Sign in failed")
                }
            }
    }

    fun firebaseSignUp(email: String, password: String, onSuccess: () -> Unit, onFailure: (String) -> Unit) {
        val auth = _firebaseAuth.value
        if (auth == null) {
            onFailure("Firebase Authentication is not initialized yet. Please configure credentials.")
            return
        }
        auth.createUserWithEmailAndPassword(email, password)
            .addOnCompleteListener { task ->
                if (task.isSuccessful) {
                    _firebaseUser.value = auth.currentUser
                    _firebaseStatus.value = "AUTHENTICATED"
                    val userEmail = auth.currentUser?.email ?: ""
                    completeSignUp(userEmail.substringBefore("@"), "", "")
                    onSuccess()
                } else {
                    onFailure(task.exception?.localizedMessage ?: "Sign up failed")
                }
            }
    }

    fun firebaseSignOut() {
        _firebaseAuth.value?.signOut()
        _firebaseUser.value = null
        _firebaseStatus.value = "INITIALIZED"
        resetApp()
    }

    fun resetApp() {
        viewModelScope.launch {
            // Delete all entities and transactions from repository
            allEntities.value.forEach {
                repository.deleteEntity(it.id)
            }
            sharedPrefs.edit().clear().apply()
            _isSignedUp.value = false
            _userName.value = ""
            _userPhone.value = ""
            _avatarUrl.value = ""
        }
    }
}
