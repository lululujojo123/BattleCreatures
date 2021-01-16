/*
 * Copyright (c) 2020 lululujojo123
 *
 * HomeActivity.kt
 *
 * created by: Andreas G.
 * last edit \ by: 2021/01/14 \ Andreas G.
 */

package org.battlecreatures.activities

import android.annotation.SuppressLint
import android.content.DialogInterface
import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.view.MotionEvent
import android.view.View
import android.view.animation.Animation
import android.view.animation.AnimationUtils
import android.widget.ImageView
import android.widget.ProgressBar
import android.widget.TextSwitcher
import androidx.appcompat.app.AlertDialog
import androidx.constraintlayout.widget.ConstraintLayout
import org.battlecreatures.R
import org.battlecreatures.animations.ProgressBarAnimation
import org.battlecreatures.logics.database.BCDatabase
import org.battlecreatures.logics.database.PlayerDAO
import org.battlecreatures.logics.entities.Player
import kotlin.system.exitProcess

/**
 * The home activity providing the main entrance point to the game and other functions
 */
class HomeActivity : AppCompatActivity() {
    companion object {
        /**
         * Static constant with the class name for logging purposes
         */
        private const val TAG: String = "HomeActivity"
    }

    /**
     * Private field buffering the last level the player had when visiting this screen
     */
    private var lastPlayerLevelBuffer: Long = 0L

    /**
     * Private field buffering the last progress of exp points the player needed when visiting this screen
     */
    private var lastPlayerExpProgressBuffer: Long = 0L

    /**
     * Private field storing the information whether the activity was called the first time or not
     */
    private var firstInit = true

    /**
     * Private field storing the offset to start the player level progress animation
     */
    private var levelProgressDelay = 0L

    /**
     * Private array storing to which activity the user has navigated
     *
     * 0: ProfileActivity
     *
     * 1: GameActivity
     *
     * 2: CardDeckActivity
     */
    private val transitionMade = booleanArrayOf(false, false, false)

    /**
     * Private field storing the fade animation object for the player button
     */
    private lateinit var fadeAnimationPlayer: Animation

    /**
     * Private field storing the fade animation object for the game button
     */
    private lateinit var fadeAnimationGame: Animation

    /**
     * Private field storing the fade animation object for the cards button
     */
    private lateinit var fadeAnimationCards: Animation



    /**
     * Android related onCreate method preparing all the views from xml file
     *
     * @param savedInstanceState Bundle with the saved instance state
     */
    override fun onCreate(savedInstanceState: Bundle?) {
        // Super classes onCreate method
        super.onCreate(savedInstanceState)

        // Initializing the context by using the activity_home.xml
        setContentView(R.layout.activity_home)

        // Get all the required view objects
        val playerLevelGroup: ConstraintLayout = findViewById(R.id.playerLevelGroup)
        val startGameGroup: ConstraintLayout = findViewById(R.id.startGameGroup)
        val cardDeckGroup: ConstraintLayout = findViewById(R.id.cardDeckGroup)

        // Set the onClickListeners for all the navigation elements
        playerLevelGroup.setOnClickListener {
            // Make all buttons not clickable
            this.setClickableForAllViews(false)

            // Do the transition animation
            this.doTransition(0)
        }

        startGameGroup.setOnClickListener {
            // Make all buttons not clickable
            this.setClickableForAllViews(false)

            // Do the transition animation
            this.doTransition(1)
        }

        cardDeckGroup.setOnClickListener {
            // Make all buttons not clickable
            this.setClickableForAllViews(false)

            // Do the transition animation
            this.doTransition(2)
        }

        this.setClickableForAllViews(false)

        // Init animation
        this.animateScreen()
    }

    /**
     * Android related onResume method refreshing the views according to the pending
     * changes within the data set.
     */
    override fun onResume() {
        // Super classes onResume method
        super.onResume()

        this.revertTransition()

        // Refreshing the level progress bar
        this.refreshLevelProgress()

        // Make all buttons clickable again and reset opacity
        Thread {
            do {
                Thread.sleep(1)
            } while(!this.fadeAnimationCards.hasStarted())

            do {
                Thread.sleep(1)
            } while (!this.fadeAnimationCards.hasEnded())

            runOnUiThread {
                this.setClickableForAllViews(true)
            }
        }.start()

        // Try to garbage collect the unnecessary closed activities
        Runtime.getRuntime().gc()
    }

    /**
     * Android related onDestroy method cleaning all the objects
     * and starting the garbage collector
     */
    override fun onDestroy() {
        // Super classes onDestroy method
        super.onDestroy()

        // Do cleanup for onclick listeners
        findViewById<ConstraintLayout>(R.id.playerLevelGroup).setOnClickListener(null)

        // Do cleanup for ontouch listeners
        this.setClickableForAllViews(false)

        // Try to garbage collect
        Runtime.getRuntime().gc()
    }

    /**
     * Android related onBackPressed method for overriding the normal back button functionality
     */
    override fun onBackPressed() {
        if (this.swipeBackgroundAnimationTriangle2 != null && !this.swipeBackgroundAnimationTriangle2!!.hasEnded()) {
            return
        }

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

    /**
     * Private method refreshing the progress bar according to the current level progress
     */
    private fun refreshLevelProgress() {
        // Run all the animation related stuff in a new Thread
        Thread {
            // Wait for init animation to finish
            Thread.sleep(this.levelProgressDelay)

            // Create all required database objects
            val bcDatabase: BCDatabase = BCDatabase.getBCDatabase(applicationContext)
            val playerDAO: PlayerDAO = bcDatabase.playerDao()
            val ownProfile: Player = playerDAO.getOwnProfile()!!

            // Query the required data
            val currentLevel: Long = ownProfile.getLevel()
            val currentExpProgress: Long = ownProfile.getExpProgress()

            // Close the database session
            bcDatabase.close()

            // Get all required views
            val progressBar = findViewById<ProgressBar>(R.id.playerLevelProgressBar)
            val textSwitcher = findViewById<TextSwitcher>(R.id.currentLevelTextSwitcher)

            // Set the animation for the textSwitcher
            textSwitcher.outAnimation = AnimationUtils.loadAnimation(this, R.anim.spin_out)
            textSwitcher.inAnimation = AnimationUtils.loadAnimation(this, R.anim.spin_in)

            // Make buttons not clickable
            this.setClickableForAllViews(false)

            // For each level up
            for (x: Long in this.lastPlayerLevelBuffer..currentLevel) {
                if (x == currentLevel || this.firstInit) {
                    // If nothing has changed just jump out of the loop
                    if (this.lastPlayerExpProgressBuffer == currentExpProgress && this.lastPlayerLevelBuffer == currentLevel) {
                        break
                    }

                    // TextSwitcher can only be changed from UI thread
                    runOnUiThread {
                        // Set the current level without animation
                        textSwitcher.setCurrentText(currentLevel.toString())
                    }

                    // Prepare and start the progress bar animation
                    progressBar.animation = ProgressBarAnimation(progressBar, progressBar.progress.toFloat(), currentExpProgress.toFloat())
                    progressBar.animation.duration = 1000
                    progressBar.animation.setInterpolator(this, android.R.interpolator.decelerate_cubic)
                    progressBar.animate()

                    // Wait for progress bar animation to finish
                    Thread.sleep(progressBar.animation.duration)

                    // Clear the animation object for next animation. GC could clean the object now.
                    progressBar.animation = null

                    // Not first init any more
                    if (this.firstInit) {
                        // Set it to false
                        this.firstInit = false

                        // Jump out of the loop
                        break
                    }
                } else {
                    // Prepare progress bar animation
                    progressBar.animation = ProgressBarAnimation(progressBar, progressBar.progress.toFloat(), 100f)
                    progressBar.animation.duration = 1000

                    // Set interpolation regarding to the loop count
                    if ((currentLevel - lastPlayerLevelBuffer) > 1 && x > lastPlayerLevelBuffer) {
                        progressBar.animation.setInterpolator(this, android.R.interpolator.linear)
                    } else {
                        progressBar.animation.setInterpolator(this, android.R.interpolator.accelerate_cubic)
                    }

                    // Start the animation
                    progressBar.animate()

                    // Wait for animation to finish
                    Thread.sleep(progressBar.animation.duration)

                    // Clear the progress bar's animation property. GC could collect that object now.
                    progressBar.animation = null

                    // TextSwitcher changes need to be done from UI thread
                    runOnUiThread {
                        // Set text switcher to the next level
                        textSwitcher.setText((x + 1).toString())
                    }

                    // Reset the progress bar to 0
                    progressBar.progress = 0
                }
            }

            // Update the buffer
            lastPlayerExpProgressBuffer = currentExpProgress
            lastPlayerLevelBuffer = currentLevel
        }.start()
    }

    /**
     * Set the isClickable property for all the buttons in this activity
     *
     * @param newValue The new value for the isClickable property
     */
    @SuppressLint("ClickableViewAccessibility")
    private fun setClickableForAllViews(newValue: Boolean) {
        // Preparing the onTouchListener for all the views
        var onTouch: View.OnTouchListener? = null

        if (newValue) {
            onTouch = View.OnTouchListener { view, event ->
                // Do the proper action for each type of action
                when(event.action) {
                    MotionEvent.ACTION_DOWN -> {
                        // Make view transparent
                        view.alpha = 0.5f
                    }
                    MotionEvent.ACTION_UP -> {
                        // Make view solid again
                        view.alpha = 1f
                    }
                    else -> {
                        // Do nothing
                    }
                }

                // Return value according to the super classes definition
                return@OnTouchListener false
            }
        }

        // Set the onTouchListeners for all the navigation elements
        findViewById<ConstraintLayout>(R.id.playerLevelGroup).setOnTouchListener(onTouch)
        findViewById<ConstraintLayout>(R.id.startGameGroup).setOnTouchListener(onTouch)
        findViewById<ConstraintLayout>(R.id.cardDeckGroup).setOnTouchListener(onTouch)

        // Get all the objects and set isClickable to newValue
        findViewById<ConstraintLayout>(R.id.playerLevelGroup).isClickable = newValue
        findViewById<ConstraintLayout>(R.id.startGameGroup).isClickable = newValue
        findViewById<ConstraintLayout>(R.id.cardDeckGroup).isClickable = newValue
    }

    /**
     * Private method preparing and starting the animation for the screen appearance
     */
    private fun animateScreen() {
        val playerLevelAnimation: Animation = AnimationUtils.loadAnimation(this,
                R.anim.shield_animation
        )
        playerLevelAnimation.duration = 800
        this.levelProgressDelay = playerLevelAnimation.duration - 300

        findViewById<ConstraintLayout>(R.id.playerLevelGroup).startAnimation(playerLevelAnimation)

        val playGameAnimation: Animation = AnimationUtils.loadAnimation(this,
                R.anim.shield_animation
        )
        playGameAnimation.duration = 1000
        playGameAnimation.startOffset = 950

        findViewById<ConstraintLayout>(R.id.startGameGroup).startAnimation(playGameAnimation)

        this.fadeAnimationCards = AnimationUtils.loadAnimation(this,
                R.anim.shield_animation
        )
        this.fadeAnimationCards.duration = 1000
        this.fadeAnimationCards.startOffset = 1950

        findViewById<ConstraintLayout>(R.id.cardDeckGroup).startAnimation(this.fadeAnimationCards)
    }

    /**
     * Private method doing the transition animation for switching activities
     *
     * @param indexOfActivity The index of the activity to make a transition to
     */
    private fun doTransition(indexOfActivity: Int) {
        fadeAnimationPlayer = AnimationUtils.loadAnimation(this,
                android.R.anim.fade_out
        )
        fadeAnimationPlayer.duration = 750

        findViewById<ConstraintLayout>(R.id.playerLevelGroup).startAnimation(fadeAnimationPlayer)

        Thread {
            do {
                Thread.sleep(1)
            } while(!fadeAnimationPlayer.hasEnded())

            runOnUiThread {
                findViewById<ConstraintLayout>(R.id.playerLevelGroup).alpha = 0f
            }
        }.start()

        fadeAnimationGame = AnimationUtils.loadAnimation(this,
                android.R.anim.fade_out
        )
        fadeAnimationGame.duration = 750
        fadeAnimationGame.startOffset = 250

        findViewById<ConstraintLayout>(R.id.startGameGroup).startAnimation(fadeAnimationGame)

        Thread {
            do {
                Thread.sleep(1)
            } while(!fadeAnimationGame.hasEnded())


            runOnUiThread {
                findViewById<ConstraintLayout>(R.id.startGameGroup).alpha = 0f
            }
        }.start()

        fadeAnimationCards = AnimationUtils.loadAnimation(this,
                android.R.anim.fade_out
        )
        fadeAnimationCards.duration = 750
        fadeAnimationCards.startOffset = 500

        findViewById<ConstraintLayout>(R.id.cardDeckGroup).startAnimation(fadeAnimationCards)

        Thread {
            do {
                Thread.sleep(1)
            } while(!fadeAnimationCards.hasEnded())

            runOnUiThread {
                findViewById<ConstraintLayout>(R.id.cardDeckGroup).alpha = 0f
            }
        }.start()

        when(indexOfActivity) {
            0 -> {


                Thread {
                    do {
                        Thread.sleep(1)
                    } while()

                    runOnUiThread {
                        // Start the profile activity with fading transition
                        startActivity(Intent(this, ProfileActivity::class.java))
                        overridePendingTransition(android.R.anim.fade_in, android.R.anim.fade_out)
                    }
                }.start()
            }
            1 -> {

            }
            2 -> {

            }
        }

        this.transitionMade[indexOfActivity] = true
    }

    /**
     * Private method reverting the changes made by the last transition animation
     */
    private fun revertTransition() {
        var lastTransitionIndex = -1

        for (i in 0..2) {
            if (this.transitionMade[i]) {
                lastTransitionIndex = i

                break
            }
        }

        if (lastTransitionIndex == -1) {
            return
        }

        when(lastTransitionIndex) {
            0 -> {

            }
            1 -> {

            }
            2 -> {

            }
        }

        fadeAnimationPlayer = AnimationUtils.loadAnimation(this,
                android.R.anim.fade_in
        )
        fadeAnimationPlayer.duration = 750
        this.levelProgressDelay = fadeAnimationPlayer.duration + fadeAnimationPlayer.startOffset - 300

        findViewById<ConstraintLayout>(R.id.playerLevelGroup).startAnimation(fadeAnimationPlayer)

        findViewById<ConstraintLayout>(R.id.playerLevelGroup).alpha = 1f

        fadeAnimationGame = AnimationUtils.loadAnimation(this,
                android.R.anim.fade_in
        )
        fadeAnimationGame.duration = 750
        fadeAnimationGame.startOffset = 250

        findViewById<ConstraintLayout>(R.id.startGameGroup).startAnimation(fadeAnimationGame)

        findViewById<ConstraintLayout>(R.id.startGameGroup).alpha = 1f

        fadeAnimationCards = AnimationUtils.loadAnimation(this,
                android.R.anim.fade_in
        )
        fadeAnimationCards.duration = 750
        fadeAnimationCards.startOffset = 500

        findViewById<ConstraintLayout>(R.id.cardDeckGroup).startAnimation(fadeAnimationCards)

        findViewById<ConstraintLayout>(R.id.cardDeckGroup).alpha = 1f

        this.transitionMade[lastTransitionIndex] = false
    }
}