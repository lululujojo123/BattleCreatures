/*
 * Copyright (c) 2020 lululujojo123
 *
 * ProfileUITest.kt
 *
 * created by: Lara B.
 * last edit \ by: 2021/01/28 \ Lara B.
 */

package org.battlecreatures

import androidx.test.espresso.Espresso.onView
import androidx.test.espresso.action.ViewActions.click
import androidx.test.espresso.assertion.ViewAssertions.matches
import androidx.test.espresso.matcher.ViewMatchers.isDisplayed
import androidx.test.espresso.matcher.ViewMatchers.withId
import androidx.test.ext.junit.rules.ActivityScenarioRule
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.filters.LargeTest
import org.battlecreatures.activities.HomeActivity
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
@LargeTest
class ProfileUITest {

    @get: Rule
    val activityRule = ActivityScenarioRule(HomeActivity::class.java)

    /**
     * This test navigates from the Home Activity to the Profile Activity
     */
    @Test
    fun test1_navigateToProfile() {
        onView(withId(R.id.playerLevelGroup))
            .perform(click())

        onView(withId(R.id.profile)).check(matches(isDisplayed()))
    }

    /**
     * This test changes the player's name and checks if it changed correctly
     */
    @Test
    fun test2_changePlayerName() {
        //ToDo: Test changing the player's name
    }
}