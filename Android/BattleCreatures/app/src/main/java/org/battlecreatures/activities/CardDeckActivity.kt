/*
 * Copyright (c) 2021 lululujojo123
 *
 * CardDeckActivity.kt
 *
 * created by: Andreas G.
 * last edit \ by: 2021/02/03 \ Andreas G.
 */

package org.battlecreatures.activities

import android.annotation.SuppressLint
import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.view.MotionEvent
import android.view.View
import android.view.animation.Animation
import android.view.animation.AnimationUtils
import android.widget.ImageView
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.android.material.floatingactionbutton.FloatingActionButton
import org.battlecreatures.R
import org.battlecreatures.adapters.CardDeckRecyclerAdapter
import org.battlecreatures.decorations.CardDeckItemDecoration
import org.battlecreatures.effectFactories.CardDeckRecyclerEdgeEffectFactory
import org.battlecreatures.extensions.forEachVisibleHolder
import org.battlecreatures.logics.database.BCDatabase
import org.battlecreatures.logics.database.CardDAO

/**
 * CardDeckActivity providing the list of available cards and shows whether
 * the player owns the card or not.
 */
class CardDeckActivity : AppCompatActivity() {
    /**
     * The scan card button related animation object
     */
    private var scanCardButtonAnimation: Animation? = null

    /**
     * The bottom decoration bar related animation object
     */
    private var bottomDecorationBarAnimation: Animation? = null

    /**
     * The recycler view for all the cards
     */
    private lateinit var cardDeckRecyclerView: RecyclerView

    /**
     * Information whether the back animation is already triggered
     */
    private var backTriggered = false

    /**
     * The android related onCreate method
     *
     * @param savedInstanceState The saved instance state
     */
    override fun onCreate(savedInstanceState: Bundle?) {
        // Super classes onCreate method
        super.onCreate(savedInstanceState)

        // Set the proper xml resource for this design
        setContentView(R.layout.activity_card_deck)

        // Store the recycler view object for further use
        this.cardDeckRecyclerView = findViewById(R.id.cardDeckRecyclerView)

        // Let the view appear animation begin
        this.appearAnimation()

        // Set the onClickListener for the back button
        findViewById<ImageView>(R.id.btnBack).setOnClickListener{
            // Only execute code if still clickable
            if (it.isClickable) {
                // Set all views not clickable
                this.setClickableAndOnTouchForAllViews(false)

                // Trigger the back process
                this.onBackPressed()
            }
        }

        // Set the onClickListener for the scan card button
        findViewById<FloatingActionButton>(R.id.scanCardButton).setOnClickListener {
            // Only execute code if still clickable
            if (it.isClickable) {
                // Set all views not clickable
                this.setClickableAndOnTouchForAllViews(false)

                // Start the card scan activity
                startActivity(Intent(this, CardScanActivity::class.java))
            }
        }
    }

    /**
     * Android related onResume method
     */
    override fun onResume() {
        // Super classes onResume method
        super.onResume()

        // Starting a thread triggering the recycler view refresh after all animation where finished
        Thread {
            // Wait for animation to be initialized
            while(this.scanCardButtonAnimation == null) {
                Thread.sleep(1)
            }

            // Wait for animation to finish
            while(!this.scanCardButtonAnimation!!.hasEnded()) {
                Thread.sleep(1)
            }

            // Refresh the recycler view and add the bouncy edge effect
            runOnUiThread {
                this.refreshRecyclerView()
                cardDeckRecyclerView.edgeEffectFactory = CardDeckRecyclerEdgeEffectFactory()
                cardDeckRecyclerView.setFadingEdgeLength(0)
            }
        }.start()

        // Make all views clickable again
        this.setClickableAndOnTouchForAllViews(true)
    }

    /**
     * Android related onBackPressed method
     */
    override fun onBackPressed() {
        // Only run this code if back routine wasn't already triggered
        if (!this.backTriggered) {
            // Set the back trigger information to true
            this.backTriggered = true

            // Prepare and execute the disappear animation
            this.disappearAnimation()

            // Starting thread triggering the back routine after finishing the animation
            Thread {
                // Waiting for animation to finish
                while (!this.bottomDecorationBarAnimation!!.hasEnded()) {
                    Thread.sleep(1)
                }

                // Call the super classes onBack routine
                runOnUiThread {
                    super.onBackPressed()
                }
            }.start()
        }
    }

    /**
     * Private method preparing and executing the disappear animation
     */
    private fun disappearAnimation() {
        // Prepare and start the animation for the top navigation bar
        val topNavigationBarAnimation: Animation = AnimationUtils.loadAnimation(this,
            R.anim.top_nav_bar_out_top_animation
        )
        topNavigationBarAnimation.duration = 800
        topNavigationBarAnimation.startOffset = 300

        findViewById<ConstraintLayout>(R.id.topNavigationBar).startAnimation(topNavigationBarAnimation)

        // Starting thread waiting for animation to finish and update the position
        Thread {
            // Wait for animation to finish
            while (!topNavigationBarAnimation.hasEnded()) {
                Thread.sleep(1)
            }

            // Update the position
            findViewById<ConstraintLayout>(R.id.topNavigationBar).y -= findViewById<ConstraintLayout>(R.id.topNavigationBar).height * 1f
        }.start()

        // Prepare and start the animation for the bottom decoration bar
        this.bottomDecorationBarAnimation = AnimationUtils.loadAnimation(this,
            R.anim.bottom_decoration_bar_out_bottom_animation
        )
        this.bottomDecorationBarAnimation!!.duration = 800
        this.bottomDecorationBarAnimation!!.startOffset = 300

        findViewById<ImageView>(R.id.bottomDecorationBar).startAnimation(this.bottomDecorationBarAnimation!!)

        // Starting thread waiting for animation to finish and update the position
        Thread {
            // Wait for animation to finish
            while (!this.bottomDecorationBarAnimation!!.hasEnded()) {
                Thread.sleep(1)
            }

            // Update the position
            findViewById<ImageView>(R.id.bottomDecorationBar).y += findViewById<ImageView>(R.id.bottomDecorationBar).height * 1f
        }.start()

        // Prepare and start the animation for the scanCard button
        this.scanCardButtonAnimation = AnimationUtils.loadAnimation(this,
            android.R.anim.fade_out
        )
        this.scanCardButtonAnimation!!.duration = 500

        findViewById<FloatingActionButton>(R.id.scanCardButton).startAnimation(this.scanCardButtonAnimation!!)

        // Thread waiting for animation to finish and update the alpha value
        Thread {
            // Wait for animation to finish
            while (!this.scanCardButtonAnimation!!.hasEnded()) {
                Thread.sleep(1)
            }

            // Update the alpha value
            findViewById<FloatingActionButton>(R.id.scanCardButton).alpha = 0f
        }.start()

        // Preparing and starting the animation of the recycler view
        val recyclerViewAnimation: Animation = AnimationUtils.loadAnimation(this,
            android.R.anim.fade_out
        )
        recyclerViewAnimation.duration = 750
        recyclerViewAnimation.startOffset = 150

        findViewById<RecyclerView>(R.id.cardDeckRecyclerView).startAnimation(recyclerViewAnimation)

        // Starting thread waiting for animation to finish and update the alpha value
        Thread {
            // Wait for animation to finish
            while (!recyclerViewAnimation.hasEnded()) {
                Thread.sleep(1)
            }

            // Update the alpha value
            findViewById<RecyclerView>(R.id.cardDeckRecyclerView).alpha = 0f
        }.start()
    }

    /**
     * Private method preparing and executing the appear animation
     */
    private fun appearAnimation() {
        // Prepare and start the animation for the top navigation bar
        val topNavigationBarAnimation: Animation = AnimationUtils.loadAnimation(this,
            R.anim.top_nav_bar_in_top_animation
        )
        topNavigationBarAnimation.duration = 800

        findViewById<ConstraintLayout>(R.id.topNavigationBar).startAnimation(topNavigationBarAnimation)

        // Prepare and start the animation for the bottom decoration bar
        this.bottomDecorationBarAnimation = AnimationUtils.loadAnimation(this,
            R.anim.bottom_decoration_bar_in_bottom_animation
        )
        this.bottomDecorationBarAnimation!!.duration = 800

        findViewById<ImageView>(R.id.bottomDecorationBar).startAnimation(this.bottomDecorationBarAnimation!!)

        // Prepare and start the animation for the scan card button
        this.scanCardButtonAnimation = AnimationUtils.loadAnimation(this,
            R.anim.shield_animation
        )
        this.scanCardButtonAnimation!!.duration = 750
        this.scanCardButtonAnimation!!.startOffset = 300

        findViewById<FloatingActionButton>(R.id.scanCardButton).startAnimation(this.scanCardButtonAnimation!!)
    }

    /**
     * Private method refreshing the recycler view with the current data from database
     */
    private fun refreshRecyclerView() {
        // Create database related objects
        val database: BCDatabase = BCDatabase.getMainThreadBCDatabase(applicationContext)
        val cardDAO: CardDAO = database.cardDao()

        // Undo all possible changes of the recycler view
        val recyclerView: RecyclerView = findViewById(R.id.cardDeckRecyclerView)
        recyclerView.adapter = null
        recyclerView.layoutManager = null

        // Remove all added item decorations
        for (i: Int in 0 until recyclerView.itemDecorationCount) {
            recyclerView.removeItemDecorationAt(i)
        }

        // Provide adapter and layout manager to the recycler view
        recyclerView.adapter = CardDeckRecyclerAdapter(cardDAO.getAllCards())
        recyclerView.layoutManager = GridLayoutManager(this, 3)

        // Analyze display density and calculate the pixel value for 20dp
        val scale = baseContext.resources.displayMetrics.density
        val dpAsPixels = (20 * scale + 0.5f).toInt()

        // Add the custom item decorator with space of 20dp to the recycler view
        recyclerView.addItemDecoration(CardDeckItemDecoration(dpAsPixels, 3, true))
    }

    /**
     * Set the isClickable property and the onTouchListener for all the buttons in this activity
     *
     * @param newValue The new value for the isClickable property
     */
    @SuppressLint("ClickableViewAccessibility")
    fun setClickableAndOnTouchForAllViews(newValue: Boolean) {
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
        cardDeckRecyclerView.forEachVisibleHolder { holder: CardDeckRecyclerAdapter.ViewHolder ->
            holder.customCardView.imageView.setOnTouchListener(onTouch)
        }

        // Get all the objects and set isClickable to newValue
        findViewById<ImageView>(R.id.btnBack).isClickable = newValue
        findViewById<FloatingActionButton>(R.id.scanCardButton).isClickable = newValue
        cardDeckRecyclerView.forEachVisibleHolder { holder: CardDeckRecyclerAdapter.ViewHolder ->
            holder.customCardView.imageView.isClickable = newValue
        }
    }
}