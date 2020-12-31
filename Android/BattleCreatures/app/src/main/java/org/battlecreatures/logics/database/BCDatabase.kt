/*
 * Copyright (c) 2020 lululujojo123
 *
 * BCDatabase.kt
 *
 * created by: Andreas G.
 * last edit \ by: 2020/12/31 \ Andreas G.
 */

package org.battlecreatures.logics.database

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import org.battlecreatures.R
import org.battlecreatures.logics.entities.*

/**
 * BCDatabase class providing the main entry point to the room based game database
 */
@Database(entities = [Card::class, Player::class], version = 1, exportSchema = false)
abstract class BCDatabase: RoomDatabase() {
    /**
     * Method providing the DAO for the queries to the players table
     */
    abstract fun playerDao(): PlayerDAO

    /**
     * Method providing the DAO for the queries to the cards table
     */
    abstract fun cardDao(): CardDAO

    companion object {
        /**
         * Method providing a usable room db object for the game database
         *
         * @return The database object
         */
        fun getBCDatabase(context: Context): BCDatabase {
            return Room.databaseBuilder(context, BCDatabase::class.java, context.getString(R.string.db_name)).build()
        }

        /**
         * Method providing a usable room db object for the game database with permission to be used on main thread
         *
         * @return The database object
         */
        fun getMainThreadBCDatabase(context: Context): BCDatabase {
            return Room.databaseBuilder(context, BCDatabase::class.java, context.getString(R.string.db_name)).allowMainThreadQueries().build()
        }
    }
}