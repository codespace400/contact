package com.example.data.db

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.sqlite.db.SupportSQLiteDatabase
import com.example.data.model.CategoryEntity
import com.example.data.model.ContactEntity
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

@Database(
    entities = [CategoryEntity::class, ContactEntity::class],
    version = 1,
    exportSchema = false
)
abstract class AppDatabase : RoomDatabase() {

    abstract fun categoryDao(): CategoryDao
    abstract fun contactDao(): ContactDao

    companion object {
        @Volatile
        private var INSTANCE: AppDatabase? = null

        fun getInstance(context: Context): AppDatabase {
            return INSTANCE ?: synchronized(this) {
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    AppDatabase::class.java,
                    "my_contacts_database"
                )
                    .addCallback(DatabasePrepopulateCallback())
                    .fallbackToDestructiveMigration()
                    .build()
                INSTANCE = instance
                instance
            }
        }

        private class DatabasePrepopulateCallback : Callback() {
            override fun onCreate(db: SupportSQLiteDatabase) {
                super.onCreate(db)
                CoroutineScope(Dispatchers.IO).launch {
                    INSTANCE?.let { database ->
                        prepopulate(database)
                    }
                }
            }
        }

        suspend fun prepopulate(database: AppDatabase) {
            val categoryDao = database.categoryDao()
            val contactDao = database.contactDao()

            if (categoryDao.getCount() == 0) {
                categoryDao.insertCategory(CategoryEntity(id = 1L, name = "Work", colorHex = "#2563EB", iconName = "work", isDefault = true))
                categoryDao.insertCategory(CategoryEntity(id = 2L, name = "Family", colorHex = "#E11D48", iconName = "favorite", isDefault = true))
                categoryDao.insertCategory(CategoryEntity(id = 3L, name = "Friends", colorHex = "#059669", iconName = "group", isDefault = true))
                categoryDao.insertCategory(CategoryEntity(id = 4L, name = "Other", colorHex = "#7C3AED", iconName = "label", isDefault = true))
            }

            if (contactDao.getContactCount() == 0) {
                val samples = listOf(
                    ContactEntity(
                        firstName = "Alex",
                        lastName = "Morgan",
                        phoneNumber = "+1 (555) 234-5678",
                        email = "alex.morgan@workcorp.com",
                        categoryId = 1L, // Work
                        notes = "Lead software architect. Product sync every Monday morning.",
                        isFavorite = true
                    ),
                    ContactEntity(
                        firstName = "Aria",
                        lastName = "Chen",
                        phoneNumber = "+1 (555) 345-6789",
                        email = "aria.chen@designstudio.io",
                        categoryId = 3L, // Friends
                        notes = "Met at UX conference in Seattle. Great coffee buddy.",
                        isFavorite = true
                    ),
                    ContactEntity(
                        firstName = "Benjamin",
                        lastName = "Hayes",
                        phoneNumber = "+1 (555) 456-7890",
                        email = "ben.hayes@familycloud.org",
                        categoryId = 2L, // Family
                        notes = "Brother-in-law. Birthday: Nov 14th.",
                        isFavorite = true
                    ),
                    ContactEntity(
                        firstName = "Chloe",
                        lastName = "Bennett",
                        phoneNumber = "+1 (555) 567-8901",
                        email = "chloe.b@creativeagency.com",
                        categoryId = 1L, // Work
                        notes = "Creative Director on the branding refresh.",
                        isFavorite = false
                    ),
                    ContactEntity(
                        firstName = "David",
                        lastName = "Kim",
                        phoneNumber = "+1 (555) 678-9012",
                        email = "david.kim@techventure.co",
                        categoryId = 1L, // Work
                        notes = "Partner manager for the enterprise team.",
                        isFavorite = false
                    ),
                    ContactEntity(
                        firstName = "Elena",
                        lastName = "Rostova",
                        phoneNumber = "+1 (555) 789-0123",
                        email = "elena.rostova@travelers.me",
                        categoryId = 3L, // Friends
                        notes = "Hiking enthusiast and weekend photographer.",
                        isFavorite = true
                    ),
                    ContactEntity(
                        firstName = "Marcus",
                        lastName = "Vance",
                        phoneNumber = "+1 (555) 890-1234",
                        email = "marcus.vance@lawpartners.com",
                        categoryId = 4L, // Other
                        notes = "Legal advisor for intellectual property.",
                        isFavorite = false
                    ),
                    ContactEntity(
                        firstName = "Olivia",
                        lastName = "Parker",
                        phoneNumber = "+1 (555) 901-2345",
                        email = "olivia.p@homebaking.net",
                        categoryId = 2L, // Family
                        notes = "Aunt Olivia. Sends delicious festive cookies every year.",
                        isFavorite = true
                    ),
                    ContactEntity(
                        firstName = "Sam",
                        lastName = "Taylor",
                        phoneNumber = "+1 (555) 012-3456",
                        email = "sam.taylor@freelancecode.io",
                        categoryId = 3L, // Friends
                        notes = "Book club member. Recommends sci-fi thrillers.",
                        isFavorite = false
                    ),
                    ContactEntity(
                        firstName = "Zoe",
                        lastName = "Zimmerman",
                        phoneNumber = "+1 (555) 123-4567",
                        email = "zoe.z@zenithfitness.com",
                        categoryId = 4L, // Other
                        notes = "Personal trainer at Zenith gym. Session on Thursdays.",
                        isFavorite = false
                    )
                )
                contactDao.insertContacts(samples)
            }
        }
    }
}
