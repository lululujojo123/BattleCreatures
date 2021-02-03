/*
 * Copyright (c) 2021 lululujojo123
 *
 * CardDetailActivity.kt
 *
 * created by: Andreas G.
 * last edit \ by: 2021/02/03 \ Andreas G.
 */

package org.battlecreatures.activities

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.view.animation.Animation
import android.view.animation.AnimationUtils
import android.widget.ImageView
import com.bumptech.glide.GenericTransitionOptions
import com.bumptech.glide.Glide
import org.battlecreatures.R
import org.battlecreatures.logics.database.BCDatabase
import org.battlecreatures.logics.database.CardDAO
import org.battlecreatures.logics.entities.Card

/**
 * CardDetailActivity showing the card image in a greater view
 */
class CardDetailActivity : AppCompatActivity() {
    /**
     * Android related onCreate method
     *
     * @param savedInstanceState The saved instance state
     */
    override fun onCreate(savedInstanceState: Bundle?) {
        // Super classes onCreate method
        super.onCreate(savedInstanceState)

        // Loading the proper xml layout file
        setContentView(R.layout.activity_card_detail)

        // Gathering the cardId from current intent
        var cardId = intent.getStringExtra(getString(R.string.card_id_intent_extra))

        // Set variable to empty string if no intent extra provided
        if (cardId == null) {
            cardId = ""
        }

        // Prepare the database related objects
        val bcDatabase: BCDatabase = BCDatabase.getMainThreadBCDatabase(this)
        val cardDAO: CardDAO = bcDatabase.cardDao()
        val card: Card? = cardDAO.getCardByID(cardId)

        // Check whether the no proper card with that id was found
        if (card != null) {
            // Store the image view object for further usage
            val cardFrontImage: ImageView = findViewById(R.id.cardDetailImageView)

            // Do the glide image load request with transition animation
            Glide.with(this)
                    .load(card.getCardImageURL(this))
                    .transition(GenericTransitionOptions.with(android.R.anim.fade_in))
                    .into(cardFrontImage)

            // Create a custom animation for the case that the image is already cached
            val imageLoadedAnimation: Animation = AnimationUtils.loadAnimation(this,
                    android.R.anim.fade_in
            )
            imageLoadedAnimation.duration = 750

            // Start the card image animation
            cardFrontImage.startAnimation(imageLoadedAnimation)
        } else {
            // Close activity because no proper id was provided
            this.onBackPressed()
        }
    }

    /**
     * Android related onUserInteraction method
     */
    override fun onUserInteraction() {
        // Super classes onUserInteraction method
        super.onUserInteraction()

        // Close activity because user touched somewhere on the screen
        this.onBackPressed()
    }
}