package com.example

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.BackHandler
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.navigationBars
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.safeDrawing
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.layout.windowInsetsPadding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Label
import androidx.compose.material.icons.filled.People
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.filled.Star
import androidx.compose.material.icons.outlined.Label
import androidx.compose.material.icons.outlined.PeopleOutline
import androidx.compose.material.icons.outlined.Settings
import androidx.compose.material.icons.outlined.StarBorder
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.NavigationBarItemDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.ui.screens.AddEditContactScreen
import com.example.ui.screens.CategoriesScreen
import com.example.ui.screens.DetailScreen
import com.example.ui.screens.FavoritesScreen
import com.example.ui.screens.HomeScreen
import com.example.ui.screens.SettingsScreen
import com.example.ui.theme.BackgroundLight
import com.example.ui.theme.MyApplicationTheme
import com.example.ui.theme.PrimaryBlue
import com.example.ui.theme.TextMuted
import com.example.ui.theme.TextPrimary
import com.example.ui.viewmodel.AppScreen
import com.example.ui.viewmodel.ContactViewModel

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            MyApplicationTheme {
                MyContactsApp()
            }
        }
    }
}

@Composable
fun MyContactsApp(
    viewModel: ContactViewModel = viewModel()
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    val contacts by viewModel.contactsList.collectAsStateWithLifecycle()
    val favorites by viewModel.favoriteContacts.collectAsStateWithLifecycle()
    val allCategories by viewModel.allCategories.collectAsStateWithLifecycle()
    val rawAllContacts by viewModel.rawAllContacts.collectAsStateWithLifecycle()

    val snackbarHostState = remember { SnackbarHostState() }

    LaunchedEffect(uiState.statusMessage) {
        uiState.statusMessage?.let { msg ->
            snackbarHostState.showSnackbar(msg)
            viewModel.clearStatusMessage()
        }
    }

    // System Back Handler
    BackHandler(enabled = uiState.backStack.size > 1) {
        viewModel.navigateBack()
    }

    val isTopLevelScreen = uiState.activeScreen in listOf(
        AppScreen.HOME,
        AppScreen.FAVORITES,
        AppScreen.CATEGORIES,
        AppScreen.SETTINGS
    )

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(BackgroundLight)
            .windowInsetsPadding(WindowInsets.safeDrawing),
        contentAlignment = Alignment.TopCenter
    ) {
        // Enforce mobile-first 375x812 proportion on wider screens
        Scaffold(
            modifier = Modifier
                .fillMaxSize()
                .widthIn(max = 500.dp),
            snackbarHost = { SnackbarHost(snackbarHostState) },
            containerColor = BackgroundLight,
            bottomBar = {
                if (isTopLevelScreen) {
                    NavigationBar(
                        containerColor = Color.White,
                        tonalElevation = 6.dp,
                        modifier = Modifier.windowInsetsPadding(WindowInsets.navigationBars)
                    ) {
                        NavigationBarItem(
                            selected = uiState.currentTab == AppScreen.HOME,
                            onClick = { viewModel.selectTab(AppScreen.HOME) },
                            icon = {
                                Icon(
                                    imageVector = if (uiState.currentTab == AppScreen.HOME) Icons.Filled.People else Icons.Outlined.PeopleOutline,
                                    contentDescription = "Contacts"
                                )
                            },
                            label = { Text("Contacts", fontSize = 11.sp, fontWeight = FontWeight.Medium) },
                            colors = navigationBarColors(),
                            modifier = Modifier.testTag("nav_tab_contacts")
                        )

                        NavigationBarItem(
                            selected = uiState.currentTab == AppScreen.FAVORITES,
                            onClick = { viewModel.selectTab(AppScreen.FAVORITES) },
                            icon = {
                                Icon(
                                    imageVector = if (uiState.currentTab == AppScreen.FAVORITES) Icons.Filled.Star else Icons.Outlined.StarBorder,
                                    contentDescription = "Favorites"
                                )
                            },
                            label = { Text("Favorites", fontSize = 11.sp, fontWeight = FontWeight.Medium) },
                            colors = navigationBarColors(),
                            modifier = Modifier.testTag("nav_tab_favorites")
                        )

                        NavigationBarItem(
                            selected = uiState.currentTab == AppScreen.CATEGORIES,
                            onClick = { viewModel.selectTab(AppScreen.CATEGORIES) },
                            icon = {
                                Icon(
                                    imageVector = if (uiState.currentTab == AppScreen.CATEGORIES) Icons.Filled.Label else Icons.Outlined.Label,
                                    contentDescription = "Categories"
                                )
                            },
                            label = { Text("Categories", fontSize = 11.sp, fontWeight = FontWeight.Medium) },
                            colors = navigationBarColors(),
                            modifier = Modifier.testTag("nav_tab_categories")
                        )

                        NavigationBarItem(
                            selected = uiState.currentTab == AppScreen.SETTINGS,
                            onClick = { viewModel.selectTab(AppScreen.SETTINGS) },
                            icon = {
                                Icon(
                                    imageVector = if (uiState.currentTab == AppScreen.SETTINGS) Icons.Filled.Settings else Icons.Outlined.Settings,
                                    contentDescription = "Settings"
                                )
                            },
                            label = { Text("Settings", fontSize = 11.sp, fontWeight = FontWeight.Medium) },
                            colors = navigationBarColors(),
                            modifier = Modifier.testTag("nav_tab_settings")
                        )
                    }
                }
            }
        ) { innerPadding ->
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(innerPadding)
            ) {
                AnimatedContent(
                    targetState = uiState.activeScreen,
                    transitionSpec = { fadeIn() togetherWith fadeOut() },
                    label = "ScreenTransition"
                ) { screen ->
                    when (screen) {
                        AppScreen.HOME -> {
                            HomeScreen(
                                contacts = contacts,
                                favorites = favorites,
                                categories = allCategories,
                                searchQuery = uiState.searchQuery,
                                selectedCategoryFilterId = uiState.categoryFilterId,
                                sortOrder = uiState.sortOrder,
                                onSearchQueryChange = { viewModel.onSearchQueryChange(it) },
                                onClearCategoryFilter = { viewModel.setCategoryFilter(null) },
                                onContactClick = { id -> viewModel.openContactDetail(id) },
                                onToggleFavorite = { id, fav -> viewModel.toggleFavorite(id, fav) },
                                onAddContactClick = { viewModel.openAddContact() }
                            )
                        }

                        AppScreen.FAVORITES -> {
                            FavoritesScreen(
                                favorites = favorites,
                                onContactClick = { id -> viewModel.openContactDetail(id) },
                                onToggleFavorite = { id, fav -> viewModel.toggleFavorite(id, fav) }
                            )
                        }

                        AppScreen.CATEGORIES -> {
                            CategoriesScreen(
                                categories = allCategories,
                                allContacts = rawAllContacts,
                                onCategoryClick = { catId -> viewModel.setCategoryFilter(catId) },
                                onAddCategory = { name, color -> viewModel.addCategory(name, color) }
                            )
                        }

                        AppScreen.SETTINGS -> {
                            SettingsScreen(
                                sortOrder = uiState.sortOrder,
                                onSortOrderChange = { viewModel.onSortOrderToggle() },
                                onImportContacts = { ctx -> viewModel.importDeviceContacts(ctx) },
                                onExportCsv = { ctx -> viewModel.exportContactsAsCsv(ctx) },
                                onLoadDemoDirectory = { viewModel.loadSampleDirectory() },
                                onClearAll = { viewModel.clearAllContacts() }
                            )
                        }

                        AppScreen.DETAIL -> {
                            val selected = rawAllContacts.firstOrNull { it.contact.id == uiState.selectedContactId }
                            DetailScreen(
                                contactWithCategory = selected,
                                onBack = { viewModel.navigateBack() },
                                onEdit = { id -> viewModel.openEditContact(id) },
                                onDelete = { id -> viewModel.deleteContact(id) },
                                onToggleFavorite = { id, fav -> viewModel.toggleFavorite(id, fav) }
                            )
                        }

                        AppScreen.ADD_EDIT -> {
                            val editingContact = rawAllContacts.firstOrNull { it.contact.id == uiState.selectedContactId }
                            AddEditContactScreen(
                                contactWithCategory = editingContact,
                                categories = allCategories,
                                preselectedCategoryId = uiState.categoryFilterId,
                                onSave = { id, fName, lName, phone, mail, catId, photo, notes, fav ->
                                    viewModel.saveContact(id, fName, lName, phone, mail, catId, photo, notes, fav)
                                },
                                onCancel = { viewModel.navigateBack() }
                            )
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun navigationBarColors() = NavigationBarItemDefaults.colors(
    selectedIconColor = PrimaryBlue,
    selectedTextColor = PrimaryBlue,
    unselectedIconColor = TextMuted,
    unselectedTextColor = TextMuted,
    indicatorColor = Color(0xFFEFF6FF)
)
