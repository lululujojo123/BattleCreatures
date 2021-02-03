/*
 * Copyright (c) 2020 lululujojo123
 *
 * HomeUITest.kt
 *
 * created by: Lara B.
 * last edit \ by: 2021/01/28 \ Lara B.
 */

package org.battlecreatures

import androidx.test.espresso.Espresso.onView
import androidx.test.espresso.assertion.ViewAssertions.matches
import androidx.test.espresso.matcher.ViewMatchers.*
import androidx.test.ext.junit.rules.ActivityScenarioRule
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.filters.LargeTest
import org.battlecreatures.activities.HomeActivity
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith

/**
 * This test class contains simple tests concerning the Home Activity
 */
@RunWith(AndroidJUnit4::class)
@LargeTest
class HomeUITest {

    @get: Rule
    val activityRule = ActivityScenarioRule(HomeActivity::class.java)

    /**
     * This test checks if the Home Activity launches
     */
    @Test
    fun test1_isActivityInView() {
        onView(withId(R.id.home)).check(matches(isDisplayed()))
    }

    /**
     * This test checks if all Constraint Layouts on the Home Activity are visible
     */
    @Test
    fun test2_visibilityConstraintLayouts() {
        onView(withId(R.id.homeBackgroundGroup))
            .check(matches(isDisplayed()))

        onView(withId(R.id.homeBackgroundGroup2))
            .check(matches(isDisplayed()))

        onView(withId(R.id.constraintLayout))
            .check(matches(isDisplayed()))

        onView(withId(R.id.startGameGroup))
            .check(matches(isDisplayed()))

        onView(withId(R.id.cardDeckGroup))
            .check(matches(isDisplayed()))
    }

    /**
     * This test checks if the text on the Home Screen is displayed correctly
     */
    @Test
    fun test3_isTextDisplayed() {
        onView(withId(R.id.startGameCaption))
            .check(matches(withText(R.string.btn_start_game_title)))

        onView(withId(R.id.cardDeckCaption))
            .check(matches(withText(R.string.btn_card_deck)))
    }
}