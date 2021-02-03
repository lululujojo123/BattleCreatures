/*
 * Copyright (c) 2021 lululujojo123
 *
 * CardCustomView.kt
 *
 * created by: Andreas G.
 * last edit \ by: 2021/02/03 \ Andreas G.
 */

package org.battlecreatures.views

import android.content.Context
import android.content.Intent
import android.graphics.ColorMatrix
import android.graphics.ColorMatrixColorFilter
import android.util.AttributeSet
import android.view.animation.Animation
import android.view.animation.AnimationUtils
import android.widget.ImageView
import androidx.constraintlayout.widget.ConstraintLayout
import com.bumptech.glide.GenericTransitionOptions
import com.bumptech.glide.Glide
import org.battlecreatures.R
import org.battlecreatures.activities.CardDeckActivity
import org.battlecreatures.activities.CardDetailActivity
import org.battlecreatures.logics.entities.Card

/**
 * CardCustomView defining a custom view required for cards and their information in the card deck recycler view
 */
class CardCustomView : ConstraintLayout {
    /**
     * The card object related with this view providing all the information required for the visualization
     */
    var card: Card? = null
        set(card) {
            // Store the provided card object into field
            field = card

            // Store the image view object for further usage
            val cardFrontImage: ImageView = findViewById(R.id.cardFrontImage)

            // Do the glide image load request with transition animation
            Glide.with(context)
                .load(card!!.getCardImageURL(context))
                .transition(GenericTransitionOptions.with(android.R.anim.fade_in))
                .into(cardFrontImage)

            // Create a custom animation for the case that the image is already cached
            val imageLoadedAnimation: Animation = AnimationUtils.loadAnimation(context,
                android.R.anim.fade_in
            )
            imageLoadedAnimation.duration = 750
            if (this.position > -1) {
                imageLoadedAnimation.startOffset = 100L * (this.position % 2)
            }

            // If player doesn't own that card make the image black and white
            if (!field!!.playerOwns) {
                // Create a color matrix and set the saturation to 0
                val colorMatrix = ColorMatrix()
                colorMatrix.setSaturation(0f)

                // Create a color matrix filter handling the defined color matrix
                val colorMatrixColorFilter = ColorMatrixColorFilter(colorMatrix)

                // Apply the color matrix to the image view
                cardFrontImage.colorFilter = colorMatrixColorFilter
            }

            // Start the card image animation
            cardFrontImage.startAnimation(imageLoadedAnimation)
        }

    /**
     * The image view with the card's image.
     */
    val imageView: ImageView

    /**
     * Property storing the position of the current card in the recycler view
     */
    var position: Int = -1

    /**
     * Minimal constructor calling the more specific constructor with a default value
     */
    constructor(context: Context): this(context, null)

    /**
     * Minimal constructor calling the more specific constructor with a default value
     */
    constructor(context: Context, attrs: AttributeSet?): this(context, attrs, 0)

    /**
     * Minimal constructor calling the more specific constructor with a default value
     */
    constructor(context: Context, attrs: AttributeSet?,defStyleAttr: Int): this(context, attrs, defStyleAttr, 0)

    /**
     * Actual constructor calling the super classes constructor and inflating the custom layout file
     */
    constructor(context: Context, attrs: AttributeSet?,defStyleAttr: Int, defStyleRes: Int): super(context, attrs, defStyleAttr, defStyleRes) {
        // Inflate the card custom view layout file
        inflate(context, R.layout.custom_view_card, this)

        // Storing the ImageView object for further use
        this.imageView = findViewById(R.id.cardFrontImage)

        // Set the onClickListener for the imageView
        this.imageView.setOnClickListener {
            // Only run code if still clickable
            if (it.isClickable) {
                // Make all the CardDeck views not clickable
                (context as CardDeckActivity).setClickableAndOnTouchForAllViews(false)

                // Start the CardDetailActivity with card id
                val intent = Intent(context, CardDetailActivity::class.java)
                intent.putExtra(context.getString(R.string.card_id_intent_extra), card!!.id)
                context.startActivity(intent)
            }
        }
    }
}