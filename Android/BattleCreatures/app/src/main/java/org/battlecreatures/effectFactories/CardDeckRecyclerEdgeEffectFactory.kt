/*
 * Copyright (c) 2021 lululujojo123
 *
 * CardDeckRecyclerEdgeEffectFactory.kt
 *
 * created by: Andreas G.
 * last edit \ by: 2021/02/01 \ Andreas G.
 */

package org.battlecreatures.effectFactories

import android.widget.EdgeEffect
import androidx.recyclerview.widget.RecyclerView
import org.battlecreatures.adapters.CardDeckRecyclerAdapter
import org.battlecreatures.extensions.forEachVisibleHolder

/**
 * EdgeEffectFactory class providing a overscroll bounce effect on card recycler views
 */
class CardDeckRecyclerEdgeEffectFactory: RecyclerView.EdgeEffectFactory() {
    companion object {
        /**
         * The magnitude of the translation distance while the list is over scrolled
         */
        private const val OVERSCROLL_TRANSLATION_MAGNITUDE = 0.2f

        /**
         * The magnitude of translation distance when the list reaches the edge on fling
         */
        private const val FLING_TRANSLATION_MAGNITUDE = 0.5f
    }

    /**
     * The RecyclerView.EdgeEffectFactory related createEdgeEffect method
     */
    override fun createEdgeEffect(view: RecyclerView, direction: Int): EdgeEffect {
        // Returning a new edge effect object
        return object: EdgeEffect(view.context) {
            /**
             * EdgeEffect related onPull method executed when overscrolled and pulled
             *
             * @param deltaDistance The delta distance of the pull gesture
             */
            override fun onPull(deltaDistance: Float) {
                // Execute method which handles the pull gesture
                handlePull(deltaDistance)
            }

            /**
             * EdgeEffect related onPull method executed when overscrolled and pulled
             *
             * @param deltaDistance The delta distance of the pull gesture
             * @param displacement The displacement of the pull gesture
             */
            override fun onPull(deltaDistance: Float, displacement: Float) {
                // Execute method which handles the pull gesture
                handlePull(deltaDistance)
            }

            /**
             * Private helper method handling the pull gesture
             *
             * @param deltaDistance The delta distance of the pull gesture
             */
            private fun handlePull(deltaDistance: Float) {
                // This is called on every touch event while the list is scrolled with a finger
                // We simply update the values instead of animating
                val sign = if (direction == DIRECTION_BOTTOM) -1 else 1
                val translationYDelta =
                        sign * view.width * deltaDistance * OVERSCROLL_TRANSLATION_MAGNITUDE
                view.forEachVisibleHolder { holder: CardDeckRecyclerAdapter.ViewHolder ->
                    holder.translationY.cancel()
                    holder.customCardView.translationY += translationYDelta
                }
            }

            /**
             * EdgeEffect related onRelease method executed when finger is lifted
             */
            override fun onRelease() {
                // The finger is lifted. This is the point where the animation should take over the control
                // and bring the views back to their resting states
                view.forEachVisibleHolder { holder: CardDeckRecyclerAdapter.ViewHolder ->
                    holder.translationY.start()
                }
            }

            /**
             * EdgeEffect related onAbsorb method executed when reached the edge on fling
             *
             * @param velocity The list's velocity
             */
            override fun onAbsorb(velocity: Int) {
                val sign = if (direction == DIRECTION_BOTTOM) -1 else 1
                // The list has reached the edge on fling
                val translationVelocity = sign * velocity * FLING_TRANSLATION_MAGNITUDE
                view.forEachVisibleHolder { holder: CardDeckRecyclerAdapter.ViewHolder ->
                    holder.translationY
                            .setStartVelocity(translationVelocity)
                            .start()
                }
            }
        }
    }
}