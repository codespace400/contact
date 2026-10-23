package com.example.data.model

import androidx.room.Embedded
import androidx.room.Relation

data class ContactWithCategory(
    @Embedded
    val contact: ContactEntity,
    @Relation(
        parentColumn = "categoryId",
        entityColumn = "id"
    )
    val category: CategoryEntity?
) {
    val displayName: String
        get() = when {
            contact.firstName.isNotBlank() && contact.lastName.isNotBlank() ->
                "${contact.firstName} ${contact.lastName}".trim()
            contact.firstName.isNotBlank() -> contact.firstName.trim()
            contact.lastName.isNotBlank() -> contact.lastName.trim()
            contact.phoneNumber.isNotBlank() -> contact.phoneNumber
            else -> "Unnamed Contact"
        }

    val initials: String
        get() {
            val f = contact.firstName.trim().firstOrNull()?.uppercaseChar()
            val l = contact.lastName.trim().firstOrNull()?.uppercaseChar()
            return when {
                f != null && l != null -> "$f$l"
                f != null -> "$f"
                l != null -> "$l"
                else -> "#"
            }
        }

    fun getSortLetter(byLastName: Boolean): Char {
        val target = if (byLastName && contact.lastName.isNotBlank()) {
            contact.lastName.trim()
        } else if (contact.firstName.isNotBlank()) {
            contact.firstName.trim()
        } else if (contact.lastName.isNotBlank()) {
            contact.lastName.trim()
        } else {
            ""
        }
        val firstChar = target.firstOrNull()?.uppercaseChar() ?: '#'
        return if (firstChar in 'A'..'Z') firstChar else '#'
    }
}
