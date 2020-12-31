/*
 * Copyright (c) 2020 lululujojo123
 *
 * ProgressBarAnimation.kt
 *
 * created by: Andreas G.
 * last edit \ by: 2020/12/29 \ Andreas G.
 */

package org.battlecreatures.animations

import android.view.animation.Animation
import android.view.animation.Transformation
import android.widget.ProgressBar

/**
 * A progress bar animation class helping you to animate your personalized progress bar
 *
 * @param progressBar The progress bar object which should be animated
 * @param from The value the animation should begin at
 * @param to The value the animation should stop at
 */
class ProgressBarAnimation(private val progressBar: ProgressBar,
                           private val from: Float,
                           private val to: Float) : Animation() {
    companion object{
        /**
         * Static constant with the class name for logging purposes
         */
        private const val TAG: String = "ProgressBarAnimation"
    }

    /**
     * The function applying the current state of the progress bar during animation
     *
     * @param interpolatedTime Provides the information how much time passed
     * @param t The transformation object
     */
    override fun applyTransformation(interpolatedTime: Float, t: Transformation?) {
        // The super classes applyTransformation method
        super.applyTransformation(interpolatedTime, t)

        // Calculating the current progress value during animation and applying it
        val value: Float = from + (to - from) * interpolatedTime
        progressBar.progress = value.toInt()
    }
}