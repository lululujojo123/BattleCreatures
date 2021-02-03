/*
 * Copyright (c) 2020 lululujojo123
 *
 * CardDAO.kt
 *
 * created by: Andreas G.
 * last edit \ by: 2021/01/01 \ Andreas G.
 */

package org.battlecreatures.logics.database

import androidx.room.*
import org.battlecreatures.logics.entities.Card


/**
 * Interface for creating a class or object providing the main functionality for access to the cards db table
 */
@Dao
interface CardDAO {
    @Insert(onConflict = OnConflictStrategy.ABORT)
    fun addCard(card: Card)

    @Delete
    fun deleteCard(card: Card)

    @Update
    fun updateCard(card: Card)

    @Query("SELECT * FROM tb_cards ORDER BY id ASC")
    fun getAllCards(): List<Card>

    @Query("SELECT * FROM tb_cards WHERE id=:id")
    fun getCardByID(id: String): Card?
}