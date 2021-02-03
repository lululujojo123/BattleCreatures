/*
 * Copyright (c) 2020 lululujojo123
 *
 * PlayerUnitTest.kt
 *
 * created by: Lara B.
 * last edit \ by: 2021/02/03 \ Lara B.
 */

package org.battlecreatures

import org.battlecreatures.logics.entities.Player
import org.junit.Assert
import org.junit.Test

/**
 * Unit tests which will test the methods in the Player.kt class
 */
class PlayerUnitTest {

    val player : Player = Player()

    /**
     * Tests the getExpForNextLevel() method
     */
    @Test
    fun testExpForNextLevel() {
        Assert.assertEquals(100, player.getExpForNextLevel())
        player.exp += 75
        Assert.assertEquals(25, player.getExpForNextLevel())
    }

    /**
     * Tests the getExpProgress() method
     */
    @Test
    fun testExpProgress() {
        Assert.assertEquals(0, player.getExpProgress())
        player.exp += 75
        Assert.assertEquals(75, player.getExpProgress())
    }

    /**
     * Tests the getLevel() method
     */
    @Test
    fun testLevel() {
        Assert.assertEquals(0, player.getLevel())
        player.exp += 100
        Assert.assertEquals(1, player.getLevel())
    }
}
