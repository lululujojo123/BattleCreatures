/*
 * Copyright (c) 2020 lululujojo123
 *
 *  ProfileActivity.kt
 *
 *  created by: Lara B.
 *  last edit \ by: 2020/12/30 \ Lara B.
 */

package org.battlecreatures.activities

import android.content.Intent
import android.os.Bundle
import android.view.View
import android.widget.Button
import android.widget.EditText
import android.widget.ProgressBar
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.isVisible
import org.battlecreatures.R
import org.battlecreatures.logics.database.BCDatabase

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

        val backButton : Button = findViewById(R.id.backButton)
        val changeNameButton : Button = findViewById(R.id.changeNameButton)
        val playerNameTextView : TextView = findViewById(R.id.playerNameTextView)
        val playerNameEditText : EditText = findViewById(R.id.playerNameEditText)
        val currentLevelTextView : TextView = findViewById(R.id.currentLevel)
        val nextLevelTextView : TextView = findViewById(R.id.nextLevel)
        val expProgressBar : ProgressBar = findViewById(R.id.expProgressBar)

        val bcDatabase = BCDatabase.getMainThreadBCDatabase(applicationContext)
        val playerDAO = bcDatabase.playerDao()
        var ownProfile = playerDAO.getOwnProfile()

        playerNameTextView.text = ownProfile.name
        currentLevelTextView.text = ownProfile.getLevel().toString()
        nextLevelTextView.text = (ownProfile.getLevel() + 1).toString()
        expProgressBar.progress = ownProfile.getExpProgress().toInt()

        changeNameButton.setOnClickListener {
            if (playerNameTextView.isVisible) {
                playerNameTextView.visibility = View.INVISIBLE
                playerNameEditText.visibility = View.VISIBLE
            } else {
                if (playerNameEditText.length() > 0) {
                    ownProfile.name = playerNameEditText.text.toString()
                    playerNameEditText.visibility = View.INVISIBLE
                    playerNameTextView.visibility = View.VISIBLE
                }
            }
        }

        backButton.setOnClickListener {
            onBackPressed()
        }

    }
}