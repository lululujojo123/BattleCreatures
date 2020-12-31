/*
 * Copyright (c) 2020 lululujojo123
 *
 * HomeActivity.kt
 *
 * created by: Andreas G.
 * last edit \ by: 2020/12/29 \ Andreas G.
 */

package org.battlecreatures.activities

import android.content.DialogInterface
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.widget.Button
import android.widget.ProgressBar
import android.widget.TextSwitcher
import android.widget.TextView
import androidx.appcompat.app.AlertDialog
import androidx.core.view.iterator
import androidx.room.Room
import org.battlecreatures.R
import org.battlecreatures.animations.ProgressBarAnimation
import org.battlecreatures.logics.database.BCDatabase
import org.battlecreatures.logics.entities.Card
import kotlin.system.exitProcess

/**
 * The home activity providing the main entrance point to the game and other functions
 */
class HomeActivity : AppCompatActivity() {
    companion object{
        /**
         * Static constant with the class name for logging purposes
         */
        private const val TAG: String = "HomeActivity"
    }

    /**
     * Private field buffering the last level the player had when visiting this screen
     */
    private var lastPlayerLevelBuffer: Int = 0

    /**
     * Private field buffering the last exp points the player had when visiting this screen
     */
    private var lastPlayerExpBuffer: Long = 0

    /**
     * Android related onCreate method preparing all the views from xml file
     *
     * @param savedInstanceState Bundle with the saved instance state
     */
    override fun onCreate(savedInstanceState: Bundle?) {
        // Super classes onCreate method
        super.onCreate(savedInstanceState)

        // Loading the layout from xml file
        setContentView(R.layout.activity_home)

        val bcDatabase = BCDatabase.getMainThreadBCDatabase(applicationContext)
        val playerDAO = bcDatabase.playerDao()
        val ownProfile = playerDAO.getOwnProfile()

        val textSwitcher = findViewById<TextSwitcher>(R.id.currentLevelTextSwitcher)
        textSwitcher.setCurrentText(ownProfile.getLevel().toString())

        val progressBar = findViewById<ProgressBar>(R.id.playerLevelProgressBar)
        var progressBarAnimation = ProgressBarAnimation(progressBar, progressBar.progress.toFloat(),
                (100 - ownProfile.getExpForNextLevel().toInt()).toFloat())
        progressBarAnimation.duration = 1000
        progressBar.startAnimation(progressBarAnimation)

        bcDatabase.close()

        findViewById<Button>(R.id.testButton).setOnClickListener {
            it.isClickable = false

            val bcDatabase = BCDatabase.getMainThreadBCDatabase(applicationContext)
            val playerDAO = bcDatabase.playerDao()
            val ownProfile = playerDAO.getOwnProfile()

            ownProfile.exp += 25

            textSwitcher.setText(ownProfile.getLevel().toString())

            progressBarAnimation = ProgressBarAnimation(progressBar, progressBar.progress.toFloat(),
                    (100 - ownProfile.getExpForNextLevel().toInt()).toFloat())
            progressBarAnimation.duration = 1000
            progressBar.startAnimation(progressBarAnimation)

            playerDAO.updatePlayer(ownProfile)
            bcDatabase.close()

            it.isClickable = true
        }
    }

    /**
     * Android related onBackPressed method for overriding the normal back button functionality
     */
    override fun onBackPressed() {
        // Creating the onClickListener for the AlertDialog
        val dialogClickListener: DialogInterface.OnClickListener = DialogInterface.OnClickListener { _: DialogInterface, which: Int ->
            when (which) {
                DialogInterface.BUTTON_POSITIVE -> {
                    // Close the whole application
                    finishAffinity()
                    exitProcess(0)
                }
                DialogInterface.BUTTON_NEGATIVE -> {
                    // Do nothing
                }
            }
        }

        // Creating the alert dialog builder
        val builder: AlertDialog.Builder = AlertDialog.Builder(this)
        builder.setMessage(getString(R.string.dialog_exit_application_text)).setPositiveButton(getString(R.string.yes), dialogClickListener)
            .setNegativeButton(getString(R.string.no), dialogClickListener)

        // Creating the alert dialog object and disable the touch event while touching outside of the dialog
        val alert: AlertDialog = builder.create()
        alert.setCanceledOnTouchOutside(false)
        alert.show()
    }
}