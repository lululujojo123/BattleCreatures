/*
 * Copyright (c) 2020 lululujojo123
 *
 * SplashActivity.kt
 *
 * created by: Andreas G.
 * last edit \ by: 2020/12/26 \ Andreas G.
 */

package org.battlecreatures

import android.os.Bundle
import android.view.animation.Animation
import android.view.animation.AnimationUtils
import android.widget.ImageView
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity

class SplashActivity : AppCompatActivity() {
    companion object{
        private const val TAG: String = "SplashActivity"
    }

    private var animated: Boolean = false

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        setContentView(R.layout.activity_splash)

        if (!animated) {
            animate()
        }
    }

    private fun animate() {
        // Left sword image view animation
        val swordLeftAnimation: Animation = AnimationUtils.loadAnimation(this, R.anim.sword_left_animation)
        swordLeftAnimation.duration = 1000

        findViewById<ImageView>(R.id.soSwordImageView1).startAnimation(swordLeftAnimation)

        // Right sword image view animation
        val swordRightAnimation: Animation = AnimationUtils.loadAnimation(this, R.anim.sword_right_animation)
        swordRightAnimation.duration = 1000

        findViewById<ImageView>(R.id.soSwordImageView2).startAnimation(swordRightAnimation)

        // Shield image view animation
        val shieldAnimation: Animation = AnimationUtils.loadAnimation(this, R.anim.shield_animation)
        shieldAnimation.duration = 1000

        findViewById<ImageView>(R.id.soShieldImageView).startAnimation(shieldAnimation)

        // Title text view animation
        val titleAnimation: Animation = AnimationUtils.loadAnimation(this, R.anim.title_animation)
        titleAnimation.duration = 500
        titleAnimation.startOffset = 950

        findViewById<TextView>(R.id.appTitleTextView).startAnimation(titleAnimation)

        // Do not animate again
        animated = true
    }
}