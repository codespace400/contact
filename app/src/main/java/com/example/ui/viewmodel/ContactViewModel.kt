package com.example.ui.viewmodel

import android.app.Application
import android.content.Context
import android.content.Intent
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.data.db.AppDatabase
import com.example.data.model.CategoryEntity
import com.example.data.model.ContactEntity
import com.example.data.model.ContactWithCategory
import com.example.data.repository.ContactRepository
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

enum class AppScreen {
    HOME,
    FAVORITES,
    CATEGORIES,
    SETTINGS,
    DETAIL,
    ADD_EDIT
}

enum class SortOrder {
    FIRST_NAME,
    LAST_NAME
}

data class ContactUiState(
    val currentTab: AppScreen = AppScreen.HOME,
    val activeScreen: AppScreen = AppScreen.HOME,
    val selectedContactId: Long? = null,
    val searchQuery: String = "",
    val categoryFilterId: Long? = null,
    val sortOrder: SortOrder = SortOrder.FIRST_NAME,
    val statusMessage: String? = null,
    val backStack: List<AppScreen> = listOf(AppScreen.HOME)
)

@OptIn(ExperimentalCoroutinesApi::class)
class ContactViewModel(application: Application) : AndroidViewModel(application) {

    private val database = AppDatabase.getInstance(application)
    private val repository = ContactRepository(
        contactDao = database.contactDao(),
        categoryDao = database.categoryDao(),
        database = database
    )

    private val _uiState = MutableStateFlow(ContactUiState())
    val uiState: StateFlow<ContactUiState> = _uiState.asStateFlow()

    private val _sortOrderFlow = MutableStateFlow(SortOrder.FIRST_NAME)

    val allCategories: StateFlow<List<CategoryEntity>> = repository.getAllCategories()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val favoriteContacts: StateFlow<List<ContactWithCategory>> = repository.getFavoriteContacts()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    private val allContactsSorted = _sortOrderFlow.flatMapLatest { sort ->
        repository.getContacts(sortByLastName = (sort == SortOrder.LAST_NAME))
    }

    val contactsList: StateFlow<List<ContactWithCategory>> = combine(
        allContactsSorted,
        _uiState
    ) { contacts, state ->
        contacts.filter { item ->
            val matchesFilter = state.categoryFilterId == null || item.contact.categoryId == state.categoryFilterId
            val matchesSearch = if (state.searchQuery.isBlank()) {
                true
            } else {
                val q = state.searchQuery.trim().lowercase()
                item.displayName.lowercase().contains(q) ||
                        item.contact.phoneNumber.contains(q) ||
                        item.contact.email.lowercase().contains(q) ||
                        item.contact.notes.lowercase().contains(q) ||
                        (item.category?.name?.lowercase()?.contains(q) == true)
            }
            matchesFilter && matchesSearch
        }
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val rawAllContacts: StateFlow<List<ContactWithCategory>> = allContactsSorted
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    fun onSearchQueryChange(query: String) {
        _uiState.value = _uiState.value.copy(searchQuery = query)
    }

    fun onSortOrderToggle() {
        val next = if (_uiState.value.sortOrder == SortOrder.FIRST_NAME) {
            SortOrder.LAST_NAME
        } else {
            SortOrder.FIRST_NAME
        }
        _sortOrderFlow.value = next
        _uiState.value = _uiState.value.copy(sortOrder = next)
    }

    fun setCategoryFilter(categoryId: Long?) {
        _uiState.value = _uiState.value.copy(
            categoryFilterId = categoryId,
            currentTab = AppScreen.HOME,
            activeScreen = AppScreen.HOME
        )
    }

    fun selectTab(tab: AppScreen) {
        val current = _uiState.value
        _uiState.value = current.copy(
            currentTab = tab,
            activeScreen = tab,
            selectedContactId = null,
            backStack = listOf(tab)
        )
    }

    fun openContactDetail(contactId: Long) {
        val current = _uiState.value
        _uiState.value = current.copy(
            selectedContactId = contactId,
            activeScreen = AppScreen.DETAIL,
            backStack = current.backStack + AppScreen.DETAIL
        )
    }

    fun openAddContact(preselectedCategoryId: Long? = null) {
        val current = _uiState.value
        _uiState.value = current.copy(
            selectedContactId = null,
            categoryFilterId = preselectedCategoryId ?: current.categoryFilterId,
            activeScreen = AppScreen.ADD_EDIT,
            backStack = current.backStack + AppScreen.ADD_EDIT
        )
    }

    fun openEditContact(contactId: Long) {
        val current = _uiState.value
        _uiState.value = current.copy(
            selectedContactId = contactId,
            activeScreen = AppScreen.ADD_EDIT,
            backStack = current.backStack + AppScreen.ADD_EDIT
        )
    }

    fun navigateBack(): Boolean {
        val stack = _uiState.value.backStack
        return if (stack.size > 1) {
            val newStack = stack.dropLast(1)
            val prevScreen = newStack.last()
            _uiState.value = _uiState.value.copy(
                activeScreen = prevScreen,
                backStack = newStack,
                selectedContactId = if (prevScreen == AppScreen.DETAIL) _uiState.value.selectedContactId else null
            )
            true
        } else {
            false
        }
    }

    fun toggleFavorite(contactId: Long, currentIsFavorite: Boolean) {
        viewModelScope.launch {
            repository.toggleFavorite(contactId, currentIsFavorite)
        }
    }

    fun saveContact(
        contactId: Long,
        firstName: String,
        lastName: String,
        phoneNumber: String,
        email: String,
        categoryId: Long,
        photoUri: String?,
        notes: String,
        isFavorite: Boolean
    ) {
        viewModelScope.launch {
            val entity = ContactEntity(
                id = contactId,
                firstName = firstName.trim(),
                lastName = lastName.trim(),
                phoneNumber = phoneNumber.trim(),
                email = email.trim(),
                categoryId = categoryId,
                photoUri = photoUri,
                notes = notes.trim(),
                isFavorite = isFavorite
            )
            val savedId = repository.saveContact(entity)
            _uiState.value = _uiState.value.copy(
                statusMessage = if (contactId == 0L) "Contact created successfully" else "Contact updated",
                selectedContactId = savedId,
                activeScreen = AppScreen.DETAIL
            )
        }
    }

    fun deleteContact(contactId: Long) {
        viewModelScope.launch {
            repository.deleteContactById(contactId)
            _uiState.value = _uiState.value.copy(
                statusMessage = "Contact deleted",
                selectedContactId = null,
                activeScreen = _uiState.value.currentTab,
                backStack = listOf(_uiState.value.currentTab)
            )
        }
    }

    fun addCategory(name: String, colorHex: String, iconName: String = "label") {
        viewModelScope.launch {
            if (name.isNotBlank()) {
                repository.addCategory(name.trim(), colorHex, iconName)
                _uiState.value = _uiState.value.copy(statusMessage = "Category \"$name\" added")
            }
        }
    }

    fun importDeviceContacts(context: Context) {
        viewModelScope.launch {
            val count = repository.importDeviceContacts(context)
            _uiState.value = _uiState.value.copy(
                statusMessage = if (count > 0) "Imported $count contacts from device" else "No new contacts imported (or permission not granted)"
            )
        }
    }

    fun exportContactsAsCsv(context: Context) {
        val contacts = rawAllContacts.value
        if (contacts.isEmpty()) {
            _uiState.value = _uiState.value.copy(statusMessage = "No contacts to export")
            return
        }
        val csvText = repository.exportToCsv(contacts)
        val shareIntent = Intent(Intent.ACTION_SEND).apply {
            type = "text/comma-separated-values"
            putExtra(Intent.EXTRA_SUBJECT, "MyContacts Export.csv")
            putExtra(Intent.EXTRA_TEXT, csvText)
            addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
        }
        val chooser = Intent.createChooser(shareIntent, "Export Contacts CSV").apply {
            addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
        }
        context.startActivity(chooser)
        _uiState.value = _uiState.value.copy(statusMessage = "Exported ${contacts.size} contacts to CSV")
    }

    fun loadSampleDirectory() {
        viewModelScope.launch {
            repository.resetWithSamples()
            _uiState.value = _uiState.value.copy(statusMessage = "Sample directory loaded")
        }
    }

    fun clearAllContacts() {
        viewModelScope.launch {
            repository.deleteAllContacts()
            _uiState.value = _uiState.value.copy(statusMessage = "All contacts cleared")
        }
    }

    fun clearStatusMessage() {
        _uiState.value = _uiState.value.copy(statusMessage = null)
    }
}
