/*
 * Copyright (c) 2020 lululujojo123
 *
 * CardsAPI.kt
 *
 * created by: Andreas G.
 * last edit \ by: 2020/12/31 \ Andreas G.
 */

package org.battlecreatures.logics

import org.battlecreatures.logics.entities.Card
import retrofit2.Call
import retrofit2.http.GET

/**
 * Interface for creating a API request
 */
interface CardsAPI {
    @GET("cardsData.json")
    fun getRequestResult(): Call<CardsAPIRequestResponse>
}

/**
 * Class storing the whole response of the request
 *
 * @param availableCards The list with all the cards available in the game
 */
class CardsAPIRequestResponse(val availableCards: List<Card>)