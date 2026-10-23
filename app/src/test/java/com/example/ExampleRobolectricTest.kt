package com.example

import android.content.Context
import androidx.test.core.app.ApplicationProvider
import com.example.data.model.CategoryEntity
import com.example.data.model.ContactEntity
import com.example.data.model.ContactWithCategory
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config

@RunWith(RobolectricTestRunner::class)
@Config(sdk = [34])
class ExampleRobolectricTest {

  @Test
  fun `read string from context`() {
    val context = ApplicationProvider.getApplicationContext<Context>()
    val appName = context.getString(R.string.app_name)
    assertEquals("MyContacts", appName)
  }

  @Test
  fun `contact initials and display name generation`() {
    val contact = ContactEntity(
      firstName = "John",
      lastName = "Doe",
      phoneNumber = "+15551234",
      categoryId = 1L
    )
    val category = CategoryEntity(
      id = 1L,
      name = "Work",
      colorHex = "#2563EB"
    )
    val contactWithCategory = ContactWithCategory(contact, category)

    assertEquals("John Doe", contactWithCategory.displayName)
    assertEquals("JD", contactWithCategory.initials)
    assertEquals('J', contactWithCategory.getSortLetter(byLastName = false))
    assertEquals('D', contactWithCategory.getSortLetter(byLastName = true))
  }
}
