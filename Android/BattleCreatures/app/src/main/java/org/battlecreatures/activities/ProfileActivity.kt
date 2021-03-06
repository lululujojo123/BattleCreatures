/*
 * Copyright (c) 2020 lululujojo123
 *
 *  ProfileActivity.kt
 *
 *  created by: Lara B.
 *  last edit \ by: 2021/01/28 \ Lara B.
 */

package org.battlecreatures.activities

import android.os.Bundle
import android.view.View
import android.view.animation.Animation
import android.view.animation.AnimationUtils
import android.widget.*
import androidx.appcompat.app.AppCompatActivity
import org.battlecreatures.R
import org.battlecreatures.animations.ProgressBarAnimation
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
     * Private field for the screenOutAnimation
     */
    private var screenOutAnimation: Animation ?= null

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

        val bcDatabase = BCDatabase.getMainThreadBCDatabase(applicationContext)
        val playerDAO = bcDatabase.playerDao()
        val ownProfile = playerDAO.getOwnProfile()

        // Set the views and animate them
        setTextViews()
        animateScreen()
        animateProgressBar()

        // Set the onClickListener for the change name button
        changeNameButton.setOnClickListener {
            changeNameButton.isClickable = false
            playerNameTextView.visibility = View.INVISIBLE
            changeNameButton.visibility = View.INVISIBLE
            playerNameEditText.visibility = View.VISIBLE
            confirmNameButton.visibility = View.VISIBLE
            confirmNameButton.isClickable = true
        }

        // Set the onClickListener for the confirm name button
        confirmNameButton.setOnClickListener {
            confirmNameButton.isClickable = false

            // The name length has to be between 1 and 10
            if (playerNameEditText.length() in 1..10) {
                ownProfile!!.name = playerNameEditText.text.toString()
                // Set new name
                playerDAO.updatePlayer(Player(ownProfile.id, ownProfile.exp, ownProfile.name))
                playerNameTextView.text = ownProfile.name

                playerNameEditText.visibility = View.INVISIBLE
                confirmNameButton.visibility = View.INVISIBLE
                changeNameButton.visibility = View.VISIBLE
                playerNameTextView.visibility = View.VISIBLE
                changeNameButton.isClickable = true
            } else {
                // Warning message if the name is too long or too short
                Toast.makeText(this, getString(R.string.wrong_name_length), Toast.LENGTH_LONG).show()
                confirmNameButton.isClickable = true
            }
        }

        // Set the onClickListener for the back button
        backButton.setOnClickListener {
            // Back to home activity
            onBackPressed()
        }
    }

    /**
     * Android related onBackPressed method
     */
    override fun onBackPressed() {
        // Animate the screen before going back to home sctivity
        animateScreenOut()
        Thread{
            if(this.screenOutAnimation != null) {
                while(!this.screenOutAnimation!!.hasEnded()) {
                    Thread.sleep(1)
                }
            }
            runOnUiThread{
                super.onBackPressed()
            }
        }.start()
    }

    /**
     * Private method preparing and setting the text views
     */
    private fun setTextViews() {
        val playerNameTextView : TextView = findViewById(R.id.playerNameTextView)
        val currentLevelTextView : TextView = findViewById(R.id.currentLevel)
        val nextLevelTextView : TextView = findViewById(R.id.nextLevel)
        val neededExpTextView : TextView = findViewById(R.id.neededExp)

        val bcDatabase = BCDatabase.getMainThreadBCDatabase(applicationContext)
        val playerDAO = bcDatabase.playerDao()
        val ownProfile = playerDAO.getOwnProfile()

        // Set textview content
        playerNameTextView.text = ownProfile!!.name
        currentLevelTextView.text = ownProfile.getLevel().toString()
        nextLevelTextView.text = (ownProfile.getLevel() + 1).toString()
        neededExpTextView.text = ownProfile.getExpForNextLevel().toString()
    }

    /**
     * Private method preparing and starting the animation for the screen initialization
     */
    private fun animateScreen() {
        val backButton : ImageView = findViewById(R.id.backButton)
        val changeNameButton : ImageView = findViewById(R.id.changeNameButton)
        val playerNameTextView : TextView = findViewById(R.id.playerNameTextView)
        val currentLevelTextView : TextView = findViewById(R.id.currentLevel)
        val nextLevelTextView : TextView = findViewById(R.id.nextLevel)
        val neededExpTextView : TextView = findViewById(R.id.neededExp)
        val neededExpTextView1 : TextView = findViewById(R.id.neededExp1)
        val neededExpTextView2 : TextView = findViewById(R.id.neededExp2)
        val progressBar : ProgressBar = findViewById(R.id.expProgressBar)

        // Prepare the screen animation
        val screenAnimation: Animation = AnimationUtils.loadAnimation(this,
                R.anim.shield_animation
        )
        screenAnimation.duration = 1000

        // Start the animation
        backButton.startAnimation(screenAnimation)
        changeNameButton.startAnimation(screenAnimation)
        playerNameTextView.startAnimation(screenAnimation)
        currentLevelTextView.startAnimation(screenAnimation)
        nextLevelTextView.startAnimation(screenAnimation)
        neededExpTextView.startAnimation(screenAnimation)
        neededExpTextView1.startAnimation(screenAnimation)
        neededExpTextView2.startAnimation(screenAnimation)
        progressBar.startAnimation(screenAnimation)
    }

    /**
     * Private method preparing and starting the animation before onBackPressed
     */
    private fun animateScreenOut() {
        val backButton : ImageView = findViewById(R.id.backButton)
        val changeNameButton : ImageView = findViewById(R.id.changeNameButton)
        val playerNameTextView : TextView = findViewById(R.id.playerNameTextView)
        val currentLevelTextView : TextView = findViewById(R.id.currentLevel)
        val nextLevelTextView : TextView = findViewById(R.id.nextLevel)
        val neededExpTextView : TextView = findViewById(R.id.neededExp)
        val neededExpTextView1 : TextView = findViewById(R.id.neededExp1)
        val neededExpTextView2 : TextView = findViewById(R.id.neededExp2)
        val progressBar : ProgressBar = findViewById(R.id.expProgressBar)

        // Prepare the screen animation
        this.screenOutAnimation = AnimationUtils.loadAnimation(this,
                R.anim.shield_out_animation
        )
        this.screenOutAnimation!!.duration = 1000

        // Start the animation
        backButton.startAnimation(this.screenOutAnimation!!)
        changeNameButton.startAnimation(this.screenOutAnimation!!)
        playerNameTextView.startAnimation(this.screenOutAnimation!!)
        currentLevelTextView.startAnimation(this.screenOutAnimation!!)
        nextLevelTextView.startAnimation(this.screenOutAnimation!!)
        neededExpTextView.startAnimation(this.screenOutAnimation!!)
        neededExpTextView1.startAnimation(this.screenOutAnimation!!)
        neededExpTextView2.startAnimation(this.screenOutAnimation!!)
        progressBar.startAnimation(this.screenOutAnimation!!)

        Thread{
            if(this.screenOutAnimation != null) {
                do {
                    Thread.sleep(1)
                } while (!this.screenOutAnimation!!.hasEnded())
            }
            runOnUiThread{
                // Set the opacity of all views to 0
                backButton.alpha = 0f
                changeNameButton.alpha = 0f
                playerNameTextView.alpha = 0f
                currentLevelTextView.alpha = 0f
                nextLevelTextView.alpha = 0f
                neededExpTextView.alpha = 0f
                neededExpTextView1.alpha = 0f
                neededExpTextView2.alpha = 0f
                progressBar.alpha = 0f
            }
        }.start()
    }

    /**
     * Private method preparing and starting the animation for the progress bar
     */
    private fun animateProgressBar() {
        val expProgressBar : ProgressBar = findViewById(R.id.expProgressBar)
        val bcDatabase = BCDatabase.getMainThreadBCDatabase(applicationContext)
        val playerDAO = bcDatabase.playerDao()
        val ownProfile = playerDAO.getOwnProfile()

        // Prepare and start the progress bar animation
        expProgressBar.progress = 0
        expProgressBar.animation = ProgressBarAnimation(expProgressBar, expProgressBar.progress.toFloat(), ownProfile!!.getExpProgress().toFloat())
        expProgressBar.animation.duration = 1000
        expProgressBar.animation.startOffset = 500
        expProgressBar.animation.setInterpolator(this, android.R.interpolator.decelerate_cubic)
        expProgressBar.animate()

        // Wait for progress bar animation to finish
        Thread.sleep(expProgressBar.animation.duration)
    }
}