/*
 * Copyright (c) 2021 lululujojo123
 *
 * CardDeckActivity.kt
 *
 * created by: Andreas G.
 * last edit \ by: 2021/01/28 \ Andreas G.
 */

package org.battlecreatures.activities

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.provider.ContactsContract
import android.view.animation.Animation
import android.view.animation.AnimationUtils
import android.widget.EdgeEffect
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
import kotlin.concurrent.thread

class CardDeckActivity : AppCompatActivity() {
    private var scanCardButtonAnimation: Animation? = null

    private var bottomDecorationBarAnimation: Animation? = null

    private lateinit var cardDeckRecyclerView: RecyclerView

    private var backTriggered = false

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_card_deck)

        this.cardDeckRecyclerView = findViewById(R.id.cardDeckRecyclerView)

        this.appearAnimation()

        findViewById<ImageView>(R.id.btnBack).setOnClickListener{
            if (it.isClickable) {
                this.onBackPressed()
            }
        }

        findViewById<FloatingActionButton>(R.id.scanCardButton).setOnClickListener {
            if (it.isClickable) {
                startActivity(Intent(this, CardScanActivity::class.java))
            }
        }
    }

    override fun onResume() {
        super.onResume()

        Thread {
            while(this.scanCardButtonAnimation == null) {
                Thread.sleep(1)
            }

            while(!this.scanCardButtonAnimation!!.hasEnded()) {
                Thread.sleep(1)
            }

            runOnUiThread {
                this.refreshRecyclerView()
                cardDeckRecyclerView.edgeEffectFactory = CardDeckRecyclerEdgeEffectFactory()
                cardDeckRecyclerView.setFadingEdgeLength(0)
            }
        }.start()
    }

    override fun onBackPressed() {
        if (!this.backTriggered) {
            this.backTriggered = true

            this.disappearAnimation()

            Thread {
                while (!this.bottomDecorationBarAnimation!!.hasEnded()) {
                    Thread.sleep(1)
                }

                runOnUiThread {
                    super.onBackPressed()
                }
            }.start()
        }
    }

    private fun disappearAnimation() {
        // Prepare and start the animation for the top navigation bar
        val topNavigationBarAnimation: Animation = AnimationUtils.loadAnimation(this,
            R.anim.top_nav_bar_out_top_animation
        )
        topNavigationBarAnimation.duration = 800
        topNavigationBarAnimation.startOffset = 300

        findViewById<ConstraintLayout>(R.id.topNavigationBar).startAnimation(topNavigationBarAnimation)

        Thread {
            while (!topNavigationBarAnimation.hasEnded()) {
                Thread.sleep(1)
            }

            findViewById<ConstraintLayout>(R.id.topNavigationBar).y -= findViewById<ConstraintLayout>(R.id.topNavigationBar).height * 1f
        }.start()

        // Prepare and start the animation for the bottom decoration bar
        this.bottomDecorationBarAnimation = AnimationUtils.loadAnimation(this,
            R.anim.bottom_decoration_bar_out_bottom_animation
        )
        this.bottomDecorationBarAnimation!!.duration = 800
        this.bottomDecorationBarAnimation!!.startOffset = 300

        findViewById<ImageView>(R.id.bottomDecorationBar).startAnimation(this.bottomDecorationBarAnimation!!)

        Thread {
            while (!this.bottomDecorationBarAnimation!!.hasEnded()) {
                Thread.sleep(1)
            }

            findViewById<ImageView>(R.id.bottomDecorationBar).y += findViewById<ImageView>(R.id.bottomDecorationBar).height * 1f
        }.start()

        this.scanCardButtonAnimation = AnimationUtils.loadAnimation(this,
            android.R.anim.fade_out
        )
        this.scanCardButtonAnimation!!.duration = 500

        findViewById<FloatingActionButton>(R.id.scanCardButton).startAnimation(this.scanCardButtonAnimation!!)

        Thread {
            while (!this.scanCardButtonAnimation!!.hasEnded()) {
                Thread.sleep(1)
            }

            findViewById<FloatingActionButton>(R.id.scanCardButton).alpha = 0f
        }.start()

        val recyclerViewAnimation: Animation = AnimationUtils.loadAnimation(this,
            android.R.anim.fade_out
        )
        recyclerViewAnimation.duration = 750
        recyclerViewAnimation.startOffset = 150

        findViewById<RecyclerView>(R.id.cardDeckRecyclerView).startAnimation(recyclerViewAnimation)

        Thread {
            while (!recyclerViewAnimation.hasEnded()) {
                Thread.sleep(1)
            }

            findViewById<RecyclerView>(R.id.cardDeckRecyclerView).alpha = 0f
        }.start()
    }

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
}