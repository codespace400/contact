package com.example.data.repository

import android.content.Context
import android.provider.ContactsContract
import com.example.data.db.AppDatabase
import com.example.data.db.CategoryDao
import com.example.data.db.ContactDao
import com.example.data.model.CategoryEntity
import com.example.data.model.ContactEntity
import com.example.data.model.ContactWithCategory
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.withContext

class ContactRepository(
    private val contactDao: ContactDao,
    private val categoryDao: CategoryDao,
    private val database: AppDatabase
) {

    fun getContacts(sortByLastName: Boolean): Flow<List<ContactWithCategory>> {
        return if (sortByLastName) {
            contactDao.getAllContactsSortedByLastName()
        } else {
            contactDao.getAllContactsSortedByFirstName()
        }
    }

    fun getFavoriteContacts(): Flow<List<ContactWithCategory>> {
        return contactDao.getFavoriteContacts()
    }

    fun getContactsByCategory(categoryId: Long): Flow<List<ContactWithCategory>> {
        return contactDao.getContactsByCategory(categoryId)
    }

    fun getContactById(id: Long): Flow<ContactWithCategory?> {
        return contactDao.getContactById(id)
    }

    fun getAllCategories(): Flow<List<CategoryEntity>> {
        return categoryDao.getAllCategories()
    }

    suspend fun saveContact(contact: ContactEntity): Long = withContext(Dispatchers.IO) {
        if (contact.id == 0L) {
            contactDao.insertContact(contact)
        } else {
            contactDao.updateContact(contact.copy(updatedAt = System.currentTimeMillis()))
            contact.id
        }
    }

    suspend fun toggleFavorite(id: Long, currentIsFavorite: Boolean) = withContext(Dispatchers.IO) {
        contactDao.updateFavorite(id = id, isFavorite = !currentIsFavorite)
    }

    suspend fun deleteContactById(id: Long) = withContext(Dispatchers.IO) {
        contactDao.deleteContactById(id)
    }

    suspend fun addCategory(name: String, colorHex: String, iconName: String): Long = withContext(Dispatchers.IO) {
        val existing = categoryDao.getCategoryByName(name.trim())
        if (existing != null) {
            existing.id
        } else {
            categoryDao.insertCategory(
                CategoryEntity(
                    name = name.trim(),
                    colorHex = colorHex,
                    iconName = iconName,
                    isDefault = false
                )
            )
        }
    }

    suspend fun deleteCategory(category: CategoryEntity) = withContext(Dispatchers.IO) {
        if (!category.isDefault) {
            categoryDao.deleteCategory(category)
        }
    }

    suspend fun resetWithSamples() = withContext(Dispatchers.IO) {
        contactDao.deleteAllContacts()
        AppDatabase.prepopulate(database)
    }

    suspend fun deleteAllContacts() = withContext(Dispatchers.IO) {
        contactDao.deleteAllContacts()
    }

    suspend fun importDeviceContacts(context: Context): Int = withContext(Dispatchers.IO) {
        val importedList = mutableListOf<ContactEntity>()
        val contentResolver = context.contentResolver

        val projection = arrayOf(
            ContactsContract.CommonDataKinds.Phone.DISPLAY_NAME,
            ContactsContract.CommonDataKinds.Phone.NUMBER,
            ContactsContract.CommonDataKinds.Phone.CONTACT_ID
        )

        try {
            contentResolver.query(
                ContactsContract.CommonDataKinds.Phone.CONTENT_URI,
                projection,
                null,
                null,
                ContactsContract.CommonDataKinds.Phone.DISPLAY_NAME + " ASC"
            )?.use { cursor ->
                val nameIndex = cursor.getColumnIndex(ContactsContract.CommonDataKinds.Phone.DISPLAY_NAME)
                val numberIndex = cursor.getColumnIndex(ContactsContract.CommonDataKinds.Phone.NUMBER)

                while (cursor.moveToNext()) {
                    val rawName = if (nameIndex >= 0) cursor.getString(nameIndex) ?: "" else ""
                    val rawNumber = if (numberIndex >= 0) cursor.getString(numberIndex) ?: "" else ""

                    if (rawName.isNotBlank() || rawNumber.isNotBlank()) {
                        val nameParts = rawName.trim().split(" ", limit = 2)
                        val firstName = nameParts.getOrNull(0) ?: "Contact"
                        val lastName = nameParts.getOrNull(1) ?: ""

                        importedList.add(
                            ContactEntity(
                                firstName = firstName,
                                lastName = lastName,
                                phoneNumber = rawNumber.trim(),
                                categoryId = 4L, // Default to "Other"
                                notes = "Imported from device address book",
                                isFavorite = false
                            )
                        )
                    }
                }
            }
        } catch (_: SecurityException) {
            // Permission not granted or query blocked
            return@withContext 0
        }

        if (importedList.isNotEmpty()) {
            contactDao.insertContacts(importedList)
        }
        return@withContext importedList.size
    }

    fun exportToCsv(contacts: List<ContactWithCategory>): String {
        val sb = StringBuilder()
        sb.append("First Name,Last Name,Phone Number,Email,Category,Notes,Favorite\n")
        for (item in contacts) {
            val c = item.contact
            val catName = item.category?.name ?: "Other"
            sb.append(escapeCsv(c.firstName)).append(",")
            sb.append(escapeCsv(c.lastName)).append(",")
            sb.append(escapeCsv(c.phoneNumber)).append(",")
            sb.append(escapeCsv(c.email)).append(",")
            sb.append(escapeCsv(catName)).append(",")
            sb.append(escapeCsv(c.notes)).append(",")
            sb.append(if (c.isFavorite) "Yes" else "No").append("\n")
        }
        return sb.toString()
    }

    private fun escapeCsv(value: String): String {
        return if (value.contains(",") || value.contains("\"") || value.contains("\n")) {
            "\"" + value.replace("\"", "\"\"") + "\""
        } else {
            value
        }
    }
}
