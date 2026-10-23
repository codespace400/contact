package com.example.ui.screens

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Clear
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.PersonOutline
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.Star
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExtendedFloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.model.CategoryEntity
import com.example.data.model.ContactWithCategory
import com.example.ui.components.AlphabeticalIndexBar
import com.example.ui.components.ContactAvatar
import com.example.ui.components.ContactCard
import com.example.ui.theme.BackgroundLight
import com.example.ui.theme.PrimaryBlue
import com.example.ui.theme.StarGold
import com.example.ui.theme.TextMuted
import com.example.ui.theme.TextPrimary
import com.example.ui.theme.TextSecondary
import com.example.ui.viewmodel.SortOrder
import kotlinx.coroutines.launch

@Composable
fun HomeScreen(
    contacts: List<ContactWithCategory>,
    favorites: List<ContactWithCategory>,
    categories: List<CategoryEntity>,
    searchQuery: String,
    selectedCategoryFilterId: Long?,
    sortOrder: SortOrder,
    onSearchQueryChange: (String) -> Unit,
    onClearCategoryFilter: () -> Unit,
    onContactClick: (Long) -> Unit,
    onToggleFavorite: (Long, Boolean) -> Unit,
    onAddContactClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val listState = rememberLazyListState()
    val coroutineScope = rememberCoroutineScope()

    // Group contacts by alphabet
    val sortByLastName = sortOrder == SortOrder.LAST_NAME
    val groupedContacts = remember(contacts, sortByLastName) {
        contacts.groupBy { it.getSortLetter(sortByLastName) }
            .toSortedMap()
    }

    val availableLetters = remember(groupedContacts) {
        groupedContacts.keys.toSet()
    }

    // Map letters to lazy list index for fast jumping
    val letterToIndexMap = remember(groupedContacts, favorites, searchQuery, selectedCategoryFilterId) {
        val map = mutableMapOf<Char, Int>()
        var currentIndex = 0
        // If favorites row is present at the top, it takes 1 item
        val hasFavoritesRow = favorites.isNotEmpty() && searchQuery.isBlank() && selectedCategoryFilterId == null
        if (hasFavoritesRow) {
            currentIndex++
        }
        for ((letter, itemsInGroup) in groupedContacts) {
            map[letter] = currentIndex
            currentIndex += 1 + itemsInGroup.size // 1 for header + items
        }
        map
    }

    val activeCategory = remember(categories, selectedCategoryFilterId) {
        categories.firstOrNull { it.id == selectedCategoryFilterId }
    }

    Box(
        modifier = modifier
            .fillMaxSize()
            .background(BackgroundLight)
    ) {
        Column(
            modifier = Modifier.fillMaxSize()
        ) {
            // Top App Bar / Title
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 20.dp, vertical = 12.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Column {
                    Text(
                        text = "MyContacts",
                        style = MaterialTheme.typography.headlineMedium.copy(
                            fontWeight = FontWeight.Bold,
                            color = TextPrimary,
                            fontSize = 26.sp
                        )
                    )
                    Text(
                        text = "${contacts.size} contacts",
                        style = MaterialTheme.typography.bodySmall.copy(
                            color = TextSecondary,
                            fontSize = 13.sp
                        )
                    )
                }
            }

            // Search Bar
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 4.dp)
            ) {
                OutlinedTextField(
                    value = searchQuery,
                    onValueChange = onSearchQueryChange,
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("contact_search_input"),
                    placeholder = {
                        Text(
                            text = "Search by name, number, or note...",
                            color = TextMuted,
                            fontSize = 14.sp
                        )
                    },
                    leadingIcon = {
                        Icon(
                            imageVector = Icons.Default.Search,
                            contentDescription = "Search",
                            tint = PrimaryBlue,
                            modifier = Modifier.size(20.dp)
                        )
                    },
                    trailingIcon = {
                        if (searchQuery.isNotEmpty()) {
                            IconButton(
                                onClick = { onSearchQueryChange("") },
                                modifier = Modifier.testTag("clear_search_button")
                            ) {
                                Icon(
                                    imageVector = Icons.Default.Clear,
                                    contentDescription = "Clear search",
                                    tint = TextMuted,
                                    modifier = Modifier.size(18.dp)
                                )
                            }
                        }
                    },
                    singleLine = true,
                    shape = RoundedCornerShape(16.dp),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedContainerColor = Color.White,
                        unfocusedContainerColor = Color.White,
                        focusedBorderColor = PrimaryBlue,
                        unfocusedBorderColor = Color(0xFFE2E8F0)
                    )
                )
            }

            // Category Filter Chip if active
            if (activeCategory != null) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp, vertical = 6.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Card(
                        shape = RoundedCornerShape(20.dp),
                        colors = CardDefaults.cardColors(
                            containerColor = Color(0xFFEFF6FF)
                        )
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp)
                        ) {
                            Text(
                                text = "Filtered: ${activeCategory.name}",
                                fontSize = 12.sp,
                                fontWeight = FontWeight.SemiBold,
                                color = PrimaryBlue
                            )
                            Spacer(modifier = Modifier.width(6.dp))
                            Icon(
                                imageVector = Icons.Default.Close,
                                contentDescription = "Clear filter",
                                tint = PrimaryBlue,
                                modifier = Modifier
                                    .size(16.dp)
                                    .clickable(onClick = onClearCategoryFilter)
                            )
                        }
                    }
                }
            }

            // Main Content Area with Index Bar
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .weight(1f)
            ) {
                if (contacts.isEmpty()) {
                    EmptyContactsView(
                        isSearching = searchQuery.isNotBlank() || selectedCategoryFilterId != null,
                        onAddContactClick = onAddContactClick
                    )
                } else {
                    LazyColumn(
                        state = listState,
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(end = 26.dp),
                        contentPadding = PaddingValues(bottom = 88.dp)
                    ) {
                        // Favorites horizontal scroll section
                        if (favorites.isNotEmpty() && searchQuery.isBlank() && selectedCategoryFilterId == null) {
                            item(key = "favorites_header") {
                                FavoritesHorizontalRow(
                                    favorites = favorites,
                                    onContactClick = onContactClick
                                )
                            }
                        }

                        // Alphabetical sections
                        for ((letter, itemsInGroup) in groupedContacts) {
                            item(key = "header_$letter") {
                                Box(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .padding(horizontal = 16.dp, vertical = 6.dp)
                                ) {
                                    Text(
                                        text = letter.toString(),
                                        style = MaterialTheme.typography.titleMedium.copy(
                                            fontWeight = FontWeight.Bold,
                                            color = PrimaryBlue,
                                            fontSize = 15.sp
                                        )
                                    )
                                }
                            }

                            items(
                                items = itemsInGroup,
                                key = { it.contact.id }
                            ) { contactWithCategory ->
                                ContactCard(
                                    contactWithCategory = contactWithCategory,
                                    onClick = { onContactClick(contactWithCategory.contact.id) },
                                    onToggleFavorite = {
                                        onToggleFavorite(
                                            contactWithCategory.contact.id,
                                            contactWithCategory.contact.isFavorite
                                        )
                                    },
                                    modifier = Modifier.padding(horizontal = 16.dp, vertical = 4.dp)
                                )
                            }
                        }
                    }

                    // Alphabetical index scroll bar on right edge
                    AlphabeticalIndexBar(
                        availableLetters = availableLetters,
                        selectedLetter = null,
                        onLetterSelected = { letter ->
                            letterToIndexMap[letter]?.let { targetIndex ->
                                coroutineScope.launch {
                                    listState.animateScrollToItem(targetIndex)
                                }
                            }
                        },
                        modifier = Modifier
                            .align(Alignment.CenterEnd)
                            .padding(end = 2.dp)
                    )
                }
            }
        }

        // Floating Action Button
        ExtendedFloatingActionButton(
            onClick = onAddContactClick,
            containerColor = PrimaryBlue,
            contentColor = Color.White,
            shape = RoundedCornerShape(28.dp),
            modifier = Modifier
                .align(Alignment.BottomEnd)
                .padding(bottom = 20.dp, end = 20.dp)
                .testTag("add_contact_fab")
        ) {
            Icon(imageVector = Icons.Default.Add, contentDescription = "Add Contact")
            Spacer(modifier = Modifier.width(6.dp))
            Text(
                text = "Add Contact",
                fontWeight = FontWeight.SemiBold,
                fontSize = 14.sp
            )
        }
    }
}

@Composable
private fun FavoritesHorizontalRow(
    favorites: List<ContactWithCategory>,
    onContactClick: (Long) -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 8.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 4.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(
                imageVector = Icons.Default.Star,
                contentDescription = null,
                tint = StarGold,
                modifier = Modifier.size(16.dp)
            )
            Spacer(modifier = Modifier.width(6.dp))
            Text(
                text = "Favorites",
                style = MaterialTheme.typography.titleSmall.copy(
                    fontWeight = FontWeight.Bold,
                    color = TextPrimary,
                    fontSize = 14.sp
                )
            )
            Spacer(modifier = Modifier.width(4.dp))
            Text(
                text = "(${favorites.size})",
                fontSize = 12.sp,
                color = TextMuted
            )
        }

        LazyRow(
            contentPadding = PaddingValues(horizontal = 16.dp, vertical = 6.dp),
            horizontalArrangement = Arrangement.spacedBy(14.dp)
        ) {
            items(favorites, key = { "fav_${it.contact.id}" }) { contactWithCat ->
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally,
                    modifier = Modifier
                        .width(62.dp)
                        .clickable { onContactClick(contactWithCat.contact.id) }
                        .testTag("favorite_pill_${contactWithCat.contact.id}")
                ) {
                    Box(contentAlignment = Alignment.BottomEnd) {
                        ContactAvatar(
                            displayName = contactWithCat.displayName,
                            initials = contactWithCat.initials,
                            photoUri = contactWithCat.contact.photoUri,
                            size = 54.dp,
                            textSize = 17.sp
                        )
                        Box(
                            modifier = Modifier
                                .size(18.dp)
                                .clip(CircleShape)
                                .background(StarGold),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                imageVector = Icons.Default.Star,
                                contentDescription = null,
                                tint = Color.White,
                                modifier = Modifier.size(12.dp)
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = contactWithCat.contact.firstName.ifBlank { contactWithCat.displayName },
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Medium,
                        color = TextPrimary,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis,
                        textAlign = TextAlign.Center
                    )
                }
            }
        }
    }
}

@Composable
private fun EmptyContactsView(
    isSearching: Boolean,
    onAddContactClick: () -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(32.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Box(
            modifier = Modifier
                .size(72.dp)
                .clip(CircleShape)
                .background(Color(0xFFE2E8F0)),
            contentAlignment = Alignment.Center
        ) {
            Icon(
                imageVector = Icons.Default.PersonOutline,
                contentDescription = null,
                tint = TextMuted,
                modifier = Modifier.size(36.dp)
            )
        }

        Spacer(modifier = Modifier.height(16.dp))

        Text(
            text = if (isSearching) "No contacts found" else "No contacts yet",
            style = MaterialTheme.typography.titleMedium.copy(
                fontWeight = FontWeight.Bold,
                color = TextPrimary
            )
        )

        Spacer(modifier = Modifier.height(6.dp))

        Text(
            text = if (isSearching) "Try a different search term or clear the filter." else "Tap \"Add Contact\" below to create your first address book entry.",
            style = MaterialTheme.typography.bodyMedium.copy(
                color = TextSecondary,
                textAlign = TextAlign.Center
            )
        )
    }
}
