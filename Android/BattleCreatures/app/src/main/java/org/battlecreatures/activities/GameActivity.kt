/*
 * Copyright (c) 2021 lululujojo123
 *
 * GameActivity.kt
 *
 * created by: Lara B.
 * last edit \ by: 2021/02/03 \ Lara B.
 */

package org.battlecreatures.activities

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.view.View.INVISIBLE
import android.view.View.VISIBLE
import android.widget.Button
import android.widget.ImageView
import android.widget.SeekBar
import android.widget.TextView
import org.battlecreatures.R
import org.battlecreatures.logics.database.BCDatabase
import org.battlecreatures.logics.entities.Player

class GameActivity : AppCompatActivity() {

    companion object{
        /**
         * Static constant with the class name for logging purposes
         */
        private const val TAG: String = "GameActivity"
    }

    /**
     * Android related onCreate method
     */
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_game)

        val backButton : ImageView = findViewById(R.id.backButton)
        val swordButton : ImageView = findViewById(R.id.swordsButton)
        val lostButton : Button = findViewById(R.id.lostBtn)
        val wonButton : Button = findViewById(R.id.wonBtn)
        val startTextView : TextView = findViewById(R.id.startNewGame)
        val cardsNumTextView : TextView = findViewById(R.id.howManyCards)
        val cardsNumTextView2 : TextView = findViewById(R.id.cardsNumber)
        val cardsSeekBar : SeekBar = findViewById(R.id.cardsSeekBar)
        val continueButton : Button = findViewById(R.id.continueBtn)
        val noExpTextView : TextView = findViewById(R.id.noEarnedExp)
        val earnedExpTextView : TextView = findViewById(R.id.earnedExp)
        val earnedExp1 : TextView = findViewById(R.id.earnedExp1)
        val earnedExp2 : TextView = findViewById(R.id.earnedExp2)


        // Set the onClickListener for the swordButton
        swordButton.setOnClickListener {
            swordButton.isClickable = false

            // Set the visibility of the different views
            swordButton.visibility = INVISIBLE
            startTextView.visibility = INVISIBLE
            wonButton.visibility = VISIBLE
            lostButton.visibility = VISIBLE
        }

        // Set the onClickListener for the lostButton
        lostButton.setOnClickListener {
            lostButton.isClickable = false
            wonButton.isClickable = false
            // Set the visibility of the different views
            wonButton.visibility = INVISIBLE
            lostButton.visibility = INVISIBLE
            noExpTextView.visibility = VISIBLE
        }

        // Set the onClickListener for the wonButton
        wonButton.setOnClickListener {
            wonButton.isClickable = false
            lostButton.isClickable = false
            // Set the visibility of the different views
            wonButton.visibility = INVISIBLE
            lostButton.visibility = INVISIBLE
            cardsNumTextView.visibility = VISIBLE
            cardsNumTextView2.visibility = VISIBLE
            cardsSeekBar.visibility = VISIBLE
            continueButton.visibility = VISIBLE
        }

        // Set the onClickListener for the continueButton
        continueButton.setOnClickListener {
            continueButton.isClickable = false
            // Calculate the earned Exp
            val earnedExp : Long = 10 + (cardsSeekBar.progress).toLong() * 5
            // Update the new Exp to the player database
            val bcDatabase = BCDatabase.getMainThreadBCDatabase(applicationContext)
            val playerDAO = bcDatabase.playerDao()
            val ownProfile = playerDAO.getOwnProfile()
            ownProfile!!.exp += earnedExp
            playerDAO.updatePlayer(Player(ownProfile!!.id, ownProfile.exp, ownProfile.name))
            // Set the earnedExp Text View
            earnedExpTextView.text = earnedExp.toString()
            // Set the visibility of the different views
            cardsNumTextView.visibility = INVISIBLE
            cardsNumTextView2.visibility = INVISIBLE
            cardsSeekBar.visibility = INVISIBLE
            continueButton.visibility = INVISIBLE
            earnedExpTextView.visibility = VISIBLE
            earnedExp1.visibility = VISIBLE
            earnedExp2.visibility = VISIBLE
        }

        // Set the onClickListener for the backButton
        backButton.setOnClickListener {
            // Back to home activity
            onBackPressed()
        }
    }
}