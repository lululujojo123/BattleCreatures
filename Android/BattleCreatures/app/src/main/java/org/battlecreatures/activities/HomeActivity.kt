/*
 * Copyright (c) 2021 lululujojo123
 *
 * HomeActivity.kt
 *
 * created by: Andreas G.
 * last edit \ by: 2021/01/27 \ Andreas G.
 */

package org.battlecreatures.activities

import android.annotation.SuppressLint
import android.content.DialogInterface
import android.content.Intent
import android.content.res.Resources
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.view.MotionEvent
import android.view.View
import android.view.animation.Animation
import android.view.animation.AnimationUtils
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
     * Private field storing the swipe animation object for the first background object
     */
    private lateinit var swipeBackgroundAnimation: Animation

    /**
     * Private field storing the swipe animation object for the second background object
     */
    private var swipeBackgroundAnimation2: Animation? = null

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

        // Make the background images height high enough
        findViewById<ConstraintLayout>(R.id.homeBackgroundGroup).layoutParams.height = Resources.getSystem().displayMetrics.heightPixels + 600
        findViewById<ConstraintLayout>(R.id.homeBackgroundGroup2).layoutParams.height = Resources.getSystem().displayMetrics.heightPixels + 600

        // Set the onClickListener for player button
        findViewById<ConstraintLayout>(R.id.playerLevelGroup).setOnClickListener {
            // Make all buttons not clickable
            this.setClickableAndOnTouchForAllViews(false)

            // Do the transition animation
            this.doTransition(0)
        }

        // Set the onClickListener for game button
        findViewById<ConstraintLayout>(R.id.startGameGroup).setOnClickListener {
            // Make all buttons not clickable
            this.setClickableAndOnTouchForAllViews(false)

            // Do the transition animation
            this.doTransition(1)
        }

        // Set the onClickListener for card deck button
        findViewById<ConstraintLayout>(R.id.cardDeckGroup).setOnClickListener {
            // Make all buttons not clickable
            this.setClickableAndOnTouchForAllViews(false)

            // Do the transition animation
            this.doTransition(2)
        }

        // Make all buttons not clickable while animation is pending
        this.setClickableAndOnTouchForAllViews(false)

        // Init animation
        this.animateScreen()
    }

    /**
     * Android related onResume method refreshing the views according to the pending
     * changes within the data set and the current animation state.
     */
    override fun onResume() {
        // Super classes onResume method
        super.onResume()

        // Reverting all the transitions if required
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
                this.setClickableAndOnTouchForAllViews(true)
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

        // Do cleanup for onClick listeners
        findViewById<ConstraintLayout>(R.id.playerLevelGroup).setOnClickListener(null)
        findViewById<ConstraintLayout>(R.id.startGameGroup).setOnClickListener(null)
        findViewById<ConstraintLayout>(R.id.cardDeckGroup).setOnClickListener(null)

        // Do cleanup for onTouch listeners
        this.setClickableAndOnTouchForAllViews(false)

        // Try to garbage collect
        Runtime.getRuntime().gc()
    }

    /**
     * Android related onBackPressed method for overriding the normal back button functionality
     */
    override fun onBackPressed() {
        // Don't show the application close dialog while animation
        if (this.swipeBackgroundAnimation2 != null && !this.swipeBackgroundAnimation2!!.hasEnded()) {
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
                .setCancelable(false)

        // Creating the alert dialog object and disable the touch event while touching outside of the dialog
        val alert: AlertDialog = builder.create()
        alert.setCanceledOnTouchOutside(false)
        alert.show()
    }

    /**
     * Private method refreshing the progress bar according to the current level progress
     */
    private fun refreshLevelProgress() {
        // Run all the refresh level animation related stuff in a new Thread
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
            this.setClickableAndOnTouchForAllViews(false)

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

            // Make buttons clickable again
            this.setClickableAndOnTouchForAllViews(true)
        }.start()
    }

    /**
     * Set the isClickable property and the onTouchListener for all the buttons in this activity
     *
     * @param newValue The new value for the isClickable property
     */
    @SuppressLint("ClickableViewAccessibility")
    private fun setClickableAndOnTouchForAllViews(newValue: Boolean) {
        // Preparing the onTouchListener for all the views
        var onTouch: View.OnTouchListener? = null

        // Only onTouch if clickable is true
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
     * Private method preparing and starting the animation for the screen initialization
     */
    private fun animateScreen() {
        // Prepare and start the animation for the player button
        val playerLevelAnimation: Animation = AnimationUtils.loadAnimation(this,
                R.anim.shield_animation
        )
        playerLevelAnimation.duration = 800
        this.levelProgressDelay = playerLevelAnimation.duration - 300

        findViewById<ConstraintLayout>(R.id.playerLevelGroup).startAnimation(playerLevelAnimation)

        // Prepare and start the animation for the brown background shape
        this.swipeBackgroundAnimation = AnimationUtils.loadAnimation(this,
                R.anim.background_in_bottom_animation
        )
        this.swipeBackgroundAnimation.duration = 1000
        this.swipeBackgroundAnimation.startOffset = 500

        findViewById<ConstraintLayout>(R.id.homeBackgroundGroup).startAnimation(this.swipeBackgroundAnimation)

        // Prepare and start the animation for the game button
        val playGameAnimation: Animation = AnimationUtils.loadAnimation(this,
                R.anim.shield_animation
        )
        playGameAnimation.duration = 1000
        playGameAnimation.startOffset = 950

        findViewById<ConstraintLayout>(R.id.startGameGroup).startAnimation(playGameAnimation)

        // Prepare and start the animation for the yellow background shape
        this.swipeBackgroundAnimation2 = AnimationUtils.loadAnimation(this,
                R.anim.background2_in_bottom_animation
        )
        this.swipeBackgroundAnimation2!!.duration = 1000
        this.swipeBackgroundAnimation2!!.startOffset = 850

        findViewById<ConstraintLayout>(R.id.homeBackgroundGroup2).startAnimation(this.swipeBackgroundAnimation2!!)

        // Prepare and start the animation for the card deck button
        this.fadeAnimationCards = AnimationUtils.loadAnimation(this,
                R.anim.shield_animation
        )
        this.fadeAnimationCards.duration = 1000
        this.fadeAnimationCards.startOffset = 1350

        findViewById<ConstraintLayout>(R.id.cardDeckGroup).startAnimation(this.fadeAnimationCards)
    }

    /**
     * Private method doing the transition animation and switching the current activity
     *
     * @param indexOfActivity The index of the activity to make a transition to
     */
    private fun doTransition(indexOfActivity: Int) {
        // Preparing and starting the fade animation for the player button
        fadeAnimationPlayer = AnimationUtils.loadAnimation(this,
                android.R.anim.fade_out
        )
        fadeAnimationPlayer.duration = 750

        findViewById<ConstraintLayout>(R.id.playerLevelGroup).startAnimation(fadeAnimationPlayer)

        // Waiting to finish the animation and make button invisible
        Thread {
            do {
                Thread.sleep(1)
            } while(!fadeAnimationPlayer.hasEnded())

            runOnUiThread {
                findViewById<ConstraintLayout>(R.id.playerLevelGroup).alpha = 0f
            }
        }.start()

        // Preparing and starting the fade animation for the game button
        fadeAnimationGame = AnimationUtils.loadAnimation(this,
                android.R.anim.fade_out
        )
        fadeAnimationGame.duration = 750
        fadeAnimationGame.startOffset = 250

        findViewById<ConstraintLayout>(R.id.startGameGroup).startAnimation(fadeAnimationGame)

        // Waiting to finish the animation and make button invisible
        Thread {
            do {
                Thread.sleep(1)
            } while(!fadeAnimationGame.hasEnded())


            runOnUiThread {
                findViewById<ConstraintLayout>(R.id.startGameGroup).alpha = 0f
            }
        }.start()

        // Preparing and starting the fade animation for the card deck button
        fadeAnimationCards = AnimationUtils.loadAnimation(this,
                android.R.anim.fade_out
        )
        fadeAnimationCards.duration = 750
        fadeAnimationCards.startOffset = 500

        findViewById<ConstraintLayout>(R.id.cardDeckGroup).startAnimation(fadeAnimationCards)

        // Waiting to finish the animation and make button invisible
        Thread {
            do {
                Thread.sleep(1)
            } while(!fadeAnimationCards.hasEnded())

            runOnUiThread {
                findViewById<ConstraintLayout>(R.id.cardDeckGroup).alpha = 0f
            }
        }.start()

        // Run the appropriate animation and start the correct activity
        when(indexOfActivity) {
            // PlayerActivity
            0 -> {
                // Preparing and starting the swipe animation for the brown background shape
                this.swipeBackgroundAnimation = AnimationUtils.loadAnimation(this,
                        R.anim.background_out_bottom_animation
                )
                this.swipeBackgroundAnimation.duration = 1000
                this.swipeBackgroundAnimation.startOffset = 1250

                findViewById<ConstraintLayout>(R.id.homeBackgroundGroup).y += findViewById<ConstraintLayout>(R.id.homeBackgroundGroup).height * 0.6f
                findViewById<ConstraintLayout>(R.id.homeBackgroundGroup).startAnimation(this.swipeBackgroundAnimation)

                // Preparing and starting the swipe animation for the yellow background shape
                this.swipeBackgroundAnimation2 = AnimationUtils.loadAnimation(this,
                        R.anim.background2_out_bottom_animation
                )
                this.swipeBackgroundAnimation2!!.duration = 1000
                this.swipeBackgroundAnimation2!!.startOffset = 1750

                findViewById<ConstraintLayout>(R.id.homeBackgroundGroup2).y += findViewById<ConstraintLayout>(R.id.homeBackgroundGroup2).height * 0.6f
                findViewById<ConstraintLayout>(R.id.homeBackgroundGroup2).startAnimation(this.swipeBackgroundAnimation2!!)

                // Wait for finishing the animation and start the appropriate activity
                Thread {
                    do {
                        Thread.sleep(1)
                    } while(!this.swipeBackgroundAnimation2!!.hasEnded())

                    runOnUiThread {
                        // Start the profile activity without transition
                        startActivity(Intent(this, ProfileActivity::class.java))

                        // Set the appropriate field in array to true
                        this.transitionMade[indexOfActivity] = true
                    }
                }.start()
            }
            // GameActivity
            1 -> {
                // Preparing and starting the swipe animation for the brown background shape
                this.swipeBackgroundAnimation = AnimationUtils.loadAnimation(this,
                        R.anim.background_out_top_animation
                )
                this.swipeBackgroundAnimation.duration = 1000
                this.swipeBackgroundAnimation.startOffset = 1250

                findViewById<ConstraintLayout>(R.id.homeBackgroundGroup).y -= findViewById<ConstraintLayout>(R.id.homeBackgroundGroup).height * 0.3f
                findViewById<ConstraintLayout>(R.id.homeBackgroundGroup).startAnimation(this.swipeBackgroundAnimation)

                // Preparing and starting the swipe animation for the yellow background shape
                this.swipeBackgroundAnimation2 = AnimationUtils.loadAnimation(this,
                        R.anim.background2_out_bottom_animation
                )
                this.swipeBackgroundAnimation2!!.duration = 1000
                this.swipeBackgroundAnimation2!!.startOffset = 1250

                findViewById<ConstraintLayout>(R.id.homeBackgroundGroup2).y += findViewById<ConstraintLayout>(R.id.homeBackgroundGroup2).height * 0.6f
                findViewById<ConstraintLayout>(R.id.homeBackgroundGroup2).startAnimation(this.swipeBackgroundAnimation2!!)

                // Wait for animation to finish and start the appropriate activity
                Thread {
                    do {
                        Thread.sleep(1)
                    } while(!this.swipeBackgroundAnimation2!!.hasEnded())

                    runOnUiThread {
                        // Start the game activity without transition
                        startActivity(Intent(this, GameActivity::class.java))

                        // Set the appropriate field in array to true
                        this.transitionMade[indexOfActivity] = true
                    }
                }.start()
            }
            // CardDeckActivity
            2 -> {
                // Preparing and starting the swipe animation for the brown background shape
                this.swipeBackgroundAnimation = AnimationUtils.loadAnimation(this,
                        R.anim.background_out_top_animation
                )
                this.swipeBackgroundAnimation.duration = 750
                this.swipeBackgroundAnimation.startOffset = 1250

                findViewById<ConstraintLayout>(R.id.homeBackgroundGroup).y -= findViewById<ConstraintLayout>(R.id.homeBackgroundGroup).height * 0.3f
                findViewById<ConstraintLayout>(R.id.homeBackgroundGroup).startAnimation(this.swipeBackgroundAnimation)

                // Preparing and starting the swipe animation for the yellow background shape
                this.swipeBackgroundAnimation2 = AnimationUtils.loadAnimation(this,
                        R.anim.background2_out_top_animation
                )
                this.swipeBackgroundAnimation2!!.duration = 750
                this.swipeBackgroundAnimation2!!.startOffset = 1750

                findViewById<ConstraintLayout>(R.id.homeBackgroundGroup2).y -= findViewById<ConstraintLayout>(R.id.homeBackgroundGroup2).height * 0.6f
                findViewById<ConstraintLayout>(R.id.homeBackgroundGroup2).startAnimation(this.swipeBackgroundAnimation2!!)

                // Wait for animation to finish and start the appropriate activity
                Thread {
                    do {
                        Thread.sleep(1)
                    } while(!this.swipeBackgroundAnimation2!!.hasEnded())

                    runOnUiThread {
                        // Start the card deck activity without transition
                        startActivity(Intent(this, ProfileActivity::class.java))

                        // Set the appropriate field in array to true
                        this.transitionMade[indexOfActivity] = true
                    }
                }.start()
            }
        }
    }

    /**
     * Private method reverting the changes made by the last transition animation
     */
    private fun revertTransition() {
        // Variable storing what was the last animation
        var lastTransitionIndex = -1

        // Iterating over the transitionMade array and storing the first positive appearance
        for (i in 0..2) {
            if (this.transitionMade[i]) {
                lastTransitionIndex = i

                break
            }
        }

        // If no animation was made till last onPause finish that method
        if (lastTransitionIndex == -1) {
            return
        }

        // Prepare and execute the appropriate animation according to the last animation made
        when(lastTransitionIndex) {
            // ProfileActivity
            0 -> {
                // Prepare the swipe animation for the brown backround shape
                this.swipeBackgroundAnimation = AnimationUtils.loadAnimation(this,
                        R.anim.background_in_bottom_animation
                )
                this.swipeBackgroundAnimation.duration = 1000

                findViewById<ConstraintLayout>(R.id.homeBackgroundGroup).y -= findViewById<ConstraintLayout>(R.id.homeBackgroundGroup).height * 0.6f
                findViewById<ConstraintLayout>(R.id.homeBackgroundGroup).startAnimation(this.swipeBackgroundAnimation)

                // Prepare the swipe animation for the yellow backround shape
                this.swipeBackgroundAnimation2 = AnimationUtils.loadAnimation(this,
                        R.anim.background2_in_bottom_animation
                )
                this.swipeBackgroundAnimation2!!.duration = 1000
                this.swipeBackgroundAnimation2!!.startOffset = 500

                findViewById<ConstraintLayout>(R.id.homeBackgroundGroup2).y -= findViewById<ConstraintLayout>(R.id.homeBackgroundGroup2).height * 0.6f
                findViewById<ConstraintLayout>(R.id.homeBackgroundGroup2).startAnimation(this.swipeBackgroundAnimation2!!)
            }
            // GameActivity
            1 -> {
                // Prepare the swipe animation for the brown backround shape
                this.swipeBackgroundAnimation = AnimationUtils.loadAnimation(this,
                        R.anim.background_in_top_animation
                )
                this.swipeBackgroundAnimation.duration = 1000

                findViewById<ConstraintLayout>(R.id.homeBackgroundGroup).y += findViewById<ConstraintLayout>(R.id.homeBackgroundGroup).height * 0.3f
                findViewById<ConstraintLayout>(R.id.homeBackgroundGroup).startAnimation(this.swipeBackgroundAnimation)

                // Prepare the swipe animation for the yellow backround shape
                this.swipeBackgroundAnimation2 = AnimationUtils.loadAnimation(this,
                        R.anim.background2_in_bottom_animation
                )
                this.swipeBackgroundAnimation2!!.duration = 1000

                findViewById<ConstraintLayout>(R.id.homeBackgroundGroup2).y -= findViewById<ConstraintLayout>(R.id.homeBackgroundGroup2).height * 0.6f
                findViewById<ConstraintLayout>(R.id.homeBackgroundGroup2).startAnimation(this.swipeBackgroundAnimation2!!)
            }
            // CardDeckActivity
            2 -> {
                // Prepare the swipe animation for the brown backround shape
                this.swipeBackgroundAnimation = AnimationUtils.loadAnimation(this,
                        R.anim.background_in_top_animation
                )
                this.swipeBackgroundAnimation.duration = 750
                this.swipeBackgroundAnimation.startOffset = 500

                findViewById<ConstraintLayout>(R.id.homeBackgroundGroup).y += findViewById<ConstraintLayout>(R.id.homeBackgroundGroup).height * 0.3f
                findViewById<ConstraintLayout>(R.id.homeBackgroundGroup).startAnimation(this.swipeBackgroundAnimation)

                // Prepare the swipe animation for the yellow backround shape
                this.swipeBackgroundAnimation2 = AnimationUtils.loadAnimation(this,
                        R.anim.background2_in_top_animation
                )
                this.swipeBackgroundAnimation2!!.duration = 750

                findViewById<ConstraintLayout>(R.id.homeBackgroundGroup2).y += findViewById<ConstraintLayout>(R.id.homeBackgroundGroup2).height * 0.6f
                findViewById<ConstraintLayout>(R.id.homeBackgroundGroup2).startAnimation(this.swipeBackgroundAnimation2!!)
            }
        }

        // Prepare the fade animation for the PlayerButton
        fadeAnimationPlayer = AnimationUtils.loadAnimation(this,
                android.R.anim.fade_in
        )
        fadeAnimationPlayer.duration = 750
        fadeAnimationPlayer.startOffset = 1250

        // Store the information for how long should the progress refresh wait
        this.levelProgressDelay = fadeAnimationPlayer.duration + fadeAnimationPlayer.startOffset

        findViewById<ConstraintLayout>(R.id.playerLevelGroup).alpha = 1f
        findViewById<ConstraintLayout>(R.id.playerLevelGroup).startAnimation(fadeAnimationPlayer)

        // Prepare the fade animation for the GameButton
        fadeAnimationGame = AnimationUtils.loadAnimation(this,
                android.R.anim.fade_in
        )
        fadeAnimationGame.duration = 750
        fadeAnimationGame.startOffset = 1500

        findViewById<ConstraintLayout>(R.id.startGameGroup).alpha = 1f
        findViewById<ConstraintLayout>(R.id.startGameGroup).startAnimation(fadeAnimationGame)

        // Preapare the fade animation for the CardsButton
        fadeAnimationCards = AnimationUtils.loadAnimation(this,
                android.R.anim.fade_in
        )
        fadeAnimationCards.duration = 750
        fadeAnimationCards.startOffset = 1750

        findViewById<ConstraintLayout>(R.id.cardDeckGroup).alpha = 1f
        findViewById<ConstraintLayout>(R.id.cardDeckGroup).startAnimation(fadeAnimationCards)

        // Reset the information that a transition was made
        this.transitionMade[lastTransitionIndex] = false
    }
}