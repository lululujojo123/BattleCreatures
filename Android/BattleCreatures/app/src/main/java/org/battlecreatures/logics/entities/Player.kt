/*
 * Copyright (c) 2020 lululujojo123
 *
 * Player.kt
 *
 * created by: Andreas G.
 * last edit \ by: 2021/01/04 \ Andreas G.
 */

package org.battlecreatures.logics.entities

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "tb_players")
data class Player(
    @PrimaryKey(autoGenerate = true)
    val id: Int = 0,
    var exp: Long = 0,
    var name: String = "Player")  {
    companion object{
        /**
         * Static constant with the class name for logging purposes
         */
        private const val TAG: String = "Player"
    }

    /**
     * Method calculating how much exp are required to reach the next level
     *
     * @return The required exp to reach the next level
     */
    fun getExpForNextLevel(): Long {
        return (100 * (getLevel() + 1)) - this.exp
    }

    /**
     * Method calculating the progress made in this level
     *
     * @return The progress made in this level
     */
    fun getExpProgress(): Long {
        return this.exp - 100 * getLevel()
    }

    /**
     * Method calculating the level by player's exp
     *
     * @return The current level of the player
     */
    fun getLevel(): Long {
        return this.exp / 100
    }
}