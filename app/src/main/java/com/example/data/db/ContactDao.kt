package com.example.data.db

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Transaction
import androidx.room.Update
import com.example.data.model.ContactEntity
import com.example.data.model.ContactWithCategory
import kotlinx.coroutines.flow.Flow

@Dao
interface ContactDao {

    @Transaction
    @Query("SELECT * FROM contacts ORDER BY firstName COLLATE NOCASE ASC, lastName COLLATE NOCASE ASC")
    fun getAllContactsSortedByFirstName(): Flow<List<ContactWithCategory>>

    @Transaction
    @Query("SELECT * FROM contacts ORDER BY lastName COLLATE NOCASE ASC, firstName COLLATE NOCASE ASC")
    fun getAllContactsSortedByLastName(): Flow<List<ContactWithCategory>>

    @Transaction
    @Query("SELECT * FROM contacts WHERE isFavorite = 1 ORDER BY firstName COLLATE NOCASE ASC")
    fun getFavoriteContacts(): Flow<List<ContactWithCategory>>

    @Transaction
    @Query("SELECT * FROM contacts WHERE categoryId = :categoryId ORDER BY firstName COLLATE NOCASE ASC")
    fun getContactsByCategory(categoryId: Long): Flow<List<ContactWithCategory>>

    @Transaction
    @Query("SELECT * FROM contacts WHERE id = :id LIMIT 1")
    fun getContactById(id: Long): Flow<ContactWithCategory?>

    @Transaction
    @Query("SELECT * FROM contacts WHERE id = :id LIMIT 1")
    suspend fun getContactDirect(id: Long): ContactWithCategory?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertContact(contact: ContactEntity): Long

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertContacts(contacts: List<ContactEntity>): List<Long>

    @Update
    suspend fun updateContact(contact: ContactEntity)

    @Query("UPDATE contacts SET isFavorite = :isFavorite, updatedAt = :updatedAt WHERE id = :id")
    suspend fun updateFavorite(id: Long, isFavorite: Boolean, updatedAt: Long = System.currentTimeMillis())

    @Delete
    suspend fun deleteContact(contact: ContactEntity)

    @Query("DELETE FROM contacts WHERE id = :id")
    suspend fun deleteContactById(id: Long)

    @Query("DELETE FROM contacts")
    suspend fun deleteAllContacts()

    @Query("SELECT COUNT(*) FROM contacts")
    suspend fun getContactCount(): Int

    @Query("SELECT COUNT(*) FROM contacts WHERE categoryId = :categoryId")
    fun getContactCountForCategory(categoryId: Long): Flow<Int>
}
