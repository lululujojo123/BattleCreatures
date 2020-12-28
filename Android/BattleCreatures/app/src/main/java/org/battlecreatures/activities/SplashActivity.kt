/*
 * Copyright (c) 2020 lululujojo123
 *
 * SplashActivity.kt
 *
 * created by: Andreas G.
 * last edit \ by: 2020/12/28 \ Andreas G.
 */

package org.battlecreatures.activities

import android.content.Context
import android.content.Intent
import android.content.SharedPreferences
import android.content.pm.PackageManager
import android.media.MediaPlayer
import android.os.Bundle
import android.view.animation.Animation
import android.view.animation.AnimationUtils
import android.widget.ImageView
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import org.battlecreatures.R
import android.Manifest

/**
 * The splash activity for startup and initializations.
 *
 * Class is doing a base animation and prepares the application for the launch.
 */
class SplashActivity : AppCompatActivity() {
    companion object{
        /**
         * Static constant with the class name for logging purposes
         */
        private const val TAG: String = "SplashActivity"
    }

    /**
     * Private field storing whether the animation was already played or not
     */
    private var animated: Boolean = false

    /**
     * Private field storing whether the activity was stopped or not
     */
    private var stopped: Boolean = false

    /**
     * Private field storing the media player object for playing the sword clash
     */
    private lateinit var swordClashPlayer: MediaPlayer

    /**
     * Private field storing the runnable object for application preparation
     */
    private val prepareExecRunnable: Runnable = Runnable {
        //ToDo: Preparation tasks

        // Gathering the application wide private shared preferences
        val shrdPref: SharedPreferences = getSharedPreferences(getString(R.string.application_shared_preferences), Context.MODE_PRIVATE)

        // Waiting for animation to finish if preparation was faster
        while (!this.titleAnimation.hasEnded() || swordClashPlayer.isPlaying()) {
            Thread.sleep(100)
            if (this.stopped) {
                // Jump out of the loop if activity goes to sleep
                break
            }
        }

        // Start proper activity if activity is not paused
        if (!this.stopped) {
            if (!shrdPref.getBoolean(getString(R.string.sp_value_tutorial_completed), false)
                || ActivityCompat.checkSelfPermission(this, Manifest.permission.CAMERA) == PackageManager.PERMISSION_DENIED) {
                // Starting the tutorial activity because the user did not finish the tutorial
                runOnUiThread {
                    startActivity(Intent(this, TutorialActivity::class.java))
                    finish()
                    overridePendingTransition(android.R.anim.fade_in, android.R.anim.fade_out)
                }
            } else {
                // Starting the home activity because the user already finished the tutorial
                runOnUiThread {
                    startActivity(Intent(this, HomeActivity::class.java))
                    finish()
                    overridePendingTransition(android.R.anim.fade_in, android.R.anim.fade_out)
                }
            }
        }
    }

    /**
     * Private field storing the title animation object for gathering the animation status
     */
    private lateinit var titleAnimation: Animation

    /**
     * Android related onCreate method initializing all the views and starting the animation and sound
     *
     * @param savedInstanceState The bundle with the saved instance state
     */
    override fun onCreate(savedInstanceState: Bundle?) {
        // Super classes onCreate method
        super.onCreate(savedInstanceState)

        // Initializing the context by using the activity_splash.xml
        setContentView(R.layout.activity_splash)

        // Preparing the sword clash player
        swordClashPlayer = MediaPlayer.create(this, R.raw.swordclash)

        // Execute only if not already started
        if (!this.animated) {
            // Prepare and starting the animations
            animate()

            // Prepare the sword clash sound to play in 750ms
            Thread {
                Thread.sleep(750)
                this.swordClashPlayer.start()
            }.start()

            // Set flag that animations where already played
            this.animated = true
        }
    }

    /**
     * Android related onResume method restarting the preparation runnable which was not properly executed at the first attempt
     */
    override fun onResume() {
        // Super classes onResume method
        super.onResume()

        // Activity is no longer in stopped state
        this.stopped = false

        // Restart the preparation task
        Thread(prepareExecRunnable).start()
    }

    /**
     * Android related onPause method stopping the sword clash sound and the preparation task
     */
    override fun onPause() {
        // Super classes onPause method
        super.onPause()

        // Stopping the sword clash sound
        this.swordClashPlayer.stop()

        // Changing activity state to stopped which will stop further preparation tasks in prepThread
        this.stopped = true
    }

    /**
     * Private animate method preparing and starting all the required animations
     */
    private fun animate() {
        // Left sword image view animation
        val swordLeftAnimation: Animation = AnimationUtils.loadAnimation(this,
            R.anim.sword_left_animation
        )
        swordLeftAnimation.duration = 1000

        findViewById<ImageView>(R.id.soSwordImageView1).startAnimation(swordLeftAnimation)

        // Right sword image view animation
        val swordRightAnimation: Animation = AnimationUtils.loadAnimation(this,
            R.anim.sword_right_animation
        )
        swordRightAnimation.duration = 1000

        findViewById<ImageView>(R.id.soSwordImageView2).startAnimation(swordRightAnimation)

        // Shield image view animation
        val shieldAnimation: Animation = AnimationUtils.loadAnimation(this, R.anim.shield_animation)
        shieldAnimation.duration = 1000

        findViewById<ImageView>(R.id.soShieldImageView).startAnimation(shieldAnimation)

        // Title text view animation
        this.titleAnimation = AnimationUtils.loadAnimation(this, R.anim.title_animation)
        titleAnimation.duration = 500
        titleAnimation.startOffset = 950

        findViewById<TextView>(R.id.appTitleTextView).animation = titleAnimation
        findViewById<TextView>(R.id.appTitleTextView).animation.start()
    }
}