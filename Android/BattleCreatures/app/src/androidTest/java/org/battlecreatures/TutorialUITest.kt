/*
 * Copyright (c) 2020 lululujojo123
 *
 * TutorialUITest.kt
 *
 * created by: Lara B.
 * last edit \ by: 2021/01/28 \ Lara B.
 */

package org.battlecreatures

import androidx.test.core.app.ActivityScenario
import androidx.test.espresso.Espresso.onView
import androidx.test.espresso.action.ViewActions.click
import androidx.test.espresso.assertion.ViewAssertions.matches
import androidx.test.espresso.matcher.ViewMatchers.isDisplayed
import androidx.test.espresso.matcher.ViewMatchers.withId
import androidx.test.ext.junit.rules.ActivityScenarioRule
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.filters.LargeTest
import org.battlecreatures.activities.SplashActivity
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
@LargeTest
class TutorialUITest {

    @get: Rule
    val activityRule = ActivityScenarioRule(SplashActivity::class.java)

    /**
     * This tests checks if the Home Activity is visible directly after the Splash Activity
     * after completing the Tutorial once
     */
    @Test
    fun test1_tutorialCompleted() {
        Thread.sleep(5000)

        onView(withId(R.id.home)).check(matches(isDisplayed()))
    }

    /**
     * This test checks if the Tutorial will be opened again after canceling
     * and not opened again after completing it once
     * !!! This test only succeeds if the Tutorial has not been completed yet !!!
     */
    @Test
    fun test2_tutorialFirstOpened() {
        Thread.sleep(5000)

        onView(withId(R.id.btnSwordNext))
            .perform(click())

        activityRule.scenario.close()
        ActivityScenario.launch(SplashActivity::class.java)
        Thread.sleep(5000)

        onView(withId(R.id.btnSwordNext))
            .perform(click())

        onView(withId(R.id.btnSwordNext2))
            .perform(click())

        onView(withId(R.id.btnSwordNext3))
            .perform(click())

        onView(withId(R.id.home)).check(matches(isDisplayed()))
    }


}