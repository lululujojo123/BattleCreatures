/*
 * Copyright (c) 2020 lululujojo123
 *
 * ApplicationSetup.kt
 *
 * created by: Andreas G.
 * last edit \ by: 2020/12/31 \ Andreas G.
 */

package org.battlecreatures.logics

import android.content.Context
import com.google.gson.Gson
import com.google.gson.GsonBuilder
import org.battlecreatures.logics.database.BCDatabase
import org.battlecreatures.logics.entities.Card
import org.battlecreatures.logics.entities.Player
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory

/**
 * Static class providing the function which prepares the application launch
 */
object ApplicationSetup {

    /**
     * Public method executing all the preparation steps
     *
     * @param context The context to be used for all the database work
     */
    fun prepareApplication(context: Context) {
        preparePlayerTable(context)
        prepareCardsTable(context)
    }

    /**
     * Method checking and preparing the tb_players table
     *
     * @param context The context to be used for the database access
     */
    private fun preparePlayerTable(context: Context) {
        val bcDatabase = BCDatabase.getBCDatabase(context)
        val playerDAO = bcDatabase.playerDao()

        if (playerDAO.getOwnProfile() == null) {
            playerDAO.addPlayer(Player(1000))
        }

        bcDatabase.close()
    }

    /**
     * Method checking and synchronizing the tb_cards table
     *
     * @param context The context to be used for the database access
     */
    private fun prepareCardsTable(context: Context) {
        // Creating the required overall objects for the preparation
        val bcDatabase = BCDatabase.getBCDatabase(context)
        val cardDAO = bcDatabase.cardDao()
        var apiCardsList: List<Card>? = null

        // Creating a gson object for JSON parsing
        val gson: Gson = GsonBuilder()
            .setLenient()
            .create()

        // Creating a retrofit object for the object translation
        val retrofit: Retrofit = Retrofit.Builder().
        baseUrl("https://raw.githubusercontent.com/lululujojo123/BattleCreatures/main/CardsData/").
        addConverterFactory(GsonConverterFactory.create(gson)).build()

        // CardsAPI object providing the detailed request strategy
        val cardsAPI = retrofit.create(CardsAPI::class.java)

        // Call object processing the cardsAPI request
        val call: Call<CardsAPIRequestResponse> = cardsAPI.getRequestResult()

        // Enqueueing the call to the cardsAPI and specifying the event methods
        call.enqueue(object: Callback<CardsAPIRequestResponse> {
            override fun onResponse(call: Call<CardsAPIRequestResponse>, response: Response<CardsAPIRequestResponse>) {
                // Storing the result to the declared object
                apiCardsList = response.body()?.availableCards
            }

            override fun onFailure(call: Call<CardsAPIRequestResponse>, t: Throwable) {
                //ToDo: Something went wrong. Do some error handling
            }
        })

        // Preparing a loop counter
        var loopCounter: Int = 0

        // Waiting for the call to be finished or for 20 second
        while (apiCardsList == null && loopCounter++ < 201) {
            Thread.sleep(100)
        }

        // Check if API result is empty
        if (apiCardsList != null) {
            // Go through every element in the API result
            apiCardsList!!.forEach {
                // Try to find the card with the same ID in local DB
                val card = cardDAO.getCardByID(it.id)

                // Check if there is an entry in the local DB
                if (card != null) {
                    if (card != it) {
                        it.playerOwns = card.playerOwns

                        cardDAO.deleteCard(card)
                        while (cardDAO.getCardByID(it.id) != null) {
                            Thread.sleep(100)
                        }
                        cardDAO.addCard(it)
                    }
                } else {
                    // If there is no entry create one
                    cardDAO.addCard(it)
                }
            }

            val cards = cardDAO.getAllCards()

            cards.forEach {
                val currentCard = it

                if (apiCardsList!!.find { it.id == currentCard.id } == null) {
                    cardDAO.deleteCard(currentCard)
                }
            }
        } else {
            //ToDo: No result from API. Inform the user
        }

        bcDatabase.close()


//        val bcDatabase = BCDatabase.getBCDatabase()
//        val cardDAO = bcDatabase.cardDao()
//
//        val cards = cardDAO.getAllCards()
//
//        cards.forEach {
//            var resultField = it::class.java.declaredFields.find {
//                it.name == context.getString(R.string.property_name_card_name)
//            }
//
//            Textbox.text = resultField.get(it)
//        }
    }
}