/*
 * Copyright (c) 2020 lululujojo123
 *
 * Card.kt
 *
 * created by: Andreas G.
 * last edit \ by: 2020/12/31 \ Andreas G.
 */

package org.battlecreatures.logics.entities

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "tb_cards")
data class Card(
    @PrimaryKey
    val id: String,

    // Name value in all languages
    val name: String,
    val name_de: String,

    // Image url in all languages
    val imageURL: String,
    val imageURL_de: String,

    // Rest of the properties
    val physicalAttack: Short,
    val physicalDefence: Short,
    val magicalAttack: Short,
    val magicalDefence: Short,
    var playerOwns: Boolean = false
) {
    /**
     * Method calculating the hash code for the current object with all important properties
     *
     * @return Returns the unique hash code for the current object
     */
    override fun hashCode(): Int {
        // The result to store the string which will be hashed
        var result: String = ""

        // Append all the values of the properties of that object except playerOwns to result string
        this::class.java.declaredFields.forEach {
            if (it.name != "playerOwns") {
                if (it.type.name == String::class.java.name) {
                    result += it.get(this)
                } else {
                    result += it.get(this)?.toString()
                }
            }
        }

        // Return the hash code of the result string
        return result.hashCode()
    }

    /**
     * Method checking if the provided object is equal to the current object
     *
     * @param other The object to be checked for equality
     * @return Whether the object equals the current one or not
     */
    override fun equals(other: Any?): Boolean {
        // If same reference than surely equal
        if (other === this) {
            return true
        }

        // If not same type surely not equal
        if (other !is Card) {
            return false
        }

        // If hash codes are the same surely equal
        val card: Card = other
        return card.hashCode() == this.hashCode()
    }
}
