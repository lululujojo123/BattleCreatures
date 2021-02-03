/*
 * Copyright (c) 2021 lululujojo123
 *
 * CardDeckRecyclerAdapter.kt
 *
 * created by: Andreas G.
 * last edit \ by: 2021/02/01 \ Andreas G.
 */

package org.battlecreatures.adapters

import android.view.View
import android.view.ViewGroup
import androidx.dynamicanimation.animation.SpringAnimation
import androidx.dynamicanimation.animation.SpringForce
import androidx.recyclerview.widget.RecyclerView
import org.battlecreatures.logics.entities.Card
import org.battlecreatures.views.CardCustomView

/**
 * CardDeckRecyclerAdapter handling the recycler data set and the view creation
 */
class CardDeckRecyclerAdapter: RecyclerView.Adapter<CardDeckRecyclerAdapter.ViewHolder>{
    /**
     * The cardData used for that recycler adapter
     */
    private var cardData: List<Card>

    /**
     * The classes constructor making a copy of the provided list for preventing changes on source reference
     */
    constructor(cardData: List<Card>) {
        // Creating a fresh copy of the list to prevent changes on the original ones
        this.cardData = ArrayList<Card>(cardData)
    }

    /**
     * Method analyzing the item count and providing it
     *
     * @return The count of items to be displayed in the recycler view
     */
    override fun getItemCount(): Int {
        return cardData.size
    }

    /**
     * Method creating the view holder for every item and providing it
     *
     * @param parent The view group in which the view holders will be placed later on
     * @param viewType The type of the view
     * @return The final prepared view holder for the custom view
     */
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        // Creating and returning the view holder for the currently created custom view
        return ViewHolder(CardCustomView(parent.context))
    }

    /**
     * Method binding the data to the created custom view
     *
     * @param holder The view holder of the currently processed custom view
     * @param position The list position of the current element
     */
    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        // Provide reference to the currently processed card object to the custom view
        holder.customCardView.position = position
        holder.customCardView.card = cardData[position]
    }

    /**
     * Nested class ViewHolder holding the card custom view
     *
     * @param v The custom view to be hold by the view holder
     */
    inner class ViewHolder(v: View) : RecyclerView.ViewHolder(v) {
        /**
         * Public property with private setter storing the reference to the custom view object
         */
        var customCardView: CardCustomView = v as CardCustomView
            private set

        /**
         * A SpringAnimation for this RecyclerView item. This animation is used to bring the item back
         * to it's normal position after the over scroll effect.
         */
        val translationY: SpringAnimation = SpringAnimation(customCardView, SpringAnimation.TRANSLATION_Y)
            .setSpring(
                SpringForce()
                    .setFinalPosition(0f)
                    .setDampingRatio(SpringForce.DAMPING_RATIO_MEDIUM_BOUNCY)
                    .setStiffness(SpringForce.STIFFNESS_LOW)
            )
    }
}