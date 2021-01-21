/*
 * Copyright (c) 2020 lululujojo123
 *
 *  ProfileActivity.kt
 *
 *  created by: Lara B.
 *  last edit \ by: 2020/12/30 \ Lara B.
 */

package org.battlecreatures.activities

import android.os.Bundle
import android.view.View
import android.widget.*
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.isVisible
import org.battlecreatures.R
import org.battlecreatures.logics.database.BCDatabase
import org.battlecreatures.logics.entities.Player

/**
 * The profile activity showing the user's profile
 */
class ProfileActivity : AppCompatActivity() {
    companion object{
        /**
         * Static constant with the class name for logging purposes
         */
        private const val TAG: String = "ProfileActivity"
    }

    /**
     * Android related onCreate method
     */
    override fun onCreate(savedInstanceState: Bundle?) {
        // Super classes onCreate method
        super.onCreate(savedInstanceState)

        // Initializing the context by using the activity_profile.xml
        setContentView(R.layout.activity_profile)

        val backButton : ImageView = findViewById(R.id.backButton)
        val changeNameButton : ImageView = findViewById(R.id.changeNameButton)
        val confirmNameButton : ImageView = findViewById(R.id.confirmNameButton)
        val playerNameTextView : TextView = findViewById(R.id.playerNameTextView)
        val playerNameEditText : EditText = findViewById(R.id.playerNameEditText)
        val currentLevelTextView : TextView = findViewById(R.id.currentLevel)
        val nextLevelTextView : TextView = findViewById(R.id.nextLevel)
        val expProgressBar : ProgressBar = findViewById(R.id.expProgressBar)
        val neededExpTextView : TextView = findViewById(R.id.neededExp)

        val bcDatabase = BCDatabase.getMainThreadBCDatabase(applicationContext)
        val playerDAO = bcDatabase.playerDao()
        var ownProfile = playerDAO.getOwnProfile()

        playerNameTextView.text = ownProfile.name
        currentLevelTextView.text = ownProfile.getLevel().toString()
        nextLevelTextView.text = (ownProfile.getLevel() + 1).toString()
        expProgressBar.progress = ownProfile.getExpProgress().toInt()
        neededExpTextView.text = ownProfile.getExpForNextLevel().toString()

        changeNameButton.setOnClickListener {
            changeNameButton.isClickable = false
            playerNameTextView.visibility = View.INVISIBLE
            changeNameButton.visibility = View.INVISIBLE
            playerNameEditText.visibility = View.VISIBLE
            confirmNameButton.visibility = View.VISIBLE
            confirmNameButton.isClickable = true
        }

        confirmNameButton.setOnClickListener {
            confirmNameButton.isClickable = false
            if (playerNameEditText.length() > 0) {
                ownProfile.name = playerNameEditText.text.toString()
                playerDAO.updatePlayer(Player(ownProfile.id, ownProfile.exp, ownProfile.name))
                playerNameTextView.text = ownProfile.name
            }
            playerNameEditText.visibility = View.INVISIBLE
            confirmNameButton.visibility = View.INVISIBLE
            changeNameButton.visibility = View.VISIBLE
            playerNameTextView.visibility = View.VISIBLE
            changeNameButton.isClickable = true
        }


        backButton.setOnClickListener {
            onBackPressed()
        }

    }
}