/*
 * Copyright (c) 2020 lululujojo123
 *
 * PlayerDAO.kt
 *
 * created by: Andreas G.
 * last edit \ by: 2020/12/31 \ Andreas G.
 */

package org.battlecreatures.logics.database

import androidx.room.*
import org.battlecreatures.logics.entities.Player

/**
 * Interface for creating a class or object for the access to the db table tb_players
 */
@Dao
interface PlayerDAO {
    @Insert(onConflict = OnConflictStrategy.ABORT)
    fun addPlayer(player: Player)

    @Delete
    fun deletePlayer(player: Player)

    @Update
    fun updatePlayer(player: Player)

    @Query("SELECT * FROM tb_players ORDER BY id ASC")
    fun getAllPlayers(): List<Player>

    @Query("SELECT * FROM tb_players WHERE id=1000")
    fun getOwnProfile(): Player

    @Query("SELECT * FROM tb_players WHERE id=:id")
    fun getProfileByID(id: Int): Player
}