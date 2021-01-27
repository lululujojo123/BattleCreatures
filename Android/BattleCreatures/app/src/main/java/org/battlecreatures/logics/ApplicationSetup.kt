/*
 * Copyright (c) 2020 lululujojo123
 *
 * ApplicationSetup.kt
 *
 * created by: Andreas G.
 * last edit \ by: 2021/01/04 \ Andreas G.
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
     * Static constant with the class name for logging purposes
     */
    private const val TAG: String = "ApplicationSetup"

    /**
     * Public method executing all the preparation steps
     *
     * @param context The context to be used for all the database work
     * @return Whether preparations were successful or not
     */
    fun prepareApplication(context: Context): Boolean {
        preparePlayerTable(context)
        return prepareCardsTable(context)
    }

    /**
     * Method checking and preparing the tb_players table
     *
     * @param context The context to be used for the database access
     */
    private fun preparePlayerTable(context: Context) {
        // Creating all required objects
        val bcDatabase = BCDatabase.getBCDatabase(context)
        val playerDAO = bcDatabase.playerDao()

        // If the own profile is not existing create a standard one
        if (playerDAO.getOwnProfile() == null) {
            playerDAO.addPlayer(Player(1000))
        }

        // Close the database connection
        bcDatabase.close()
    }

    /**
     * Method checking and synchronizing the tb_cards table
     *
     * @param context The context to be used for the database access
     * @return Whether preparation was successful or not
     */
    private fun prepareCardsTable(context: Context): Boolean {
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
                // ToDo: Do proper error handling
            }
        })

        // Preparing a loop counter
        var loopCounter = 0

        // Waiting for the call to be finished or for 10 seconds
        while (apiCardsList == null && loopCounter++ < 101) {
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
                    // Checking if something has changed for that card in the API
                    if (card != it) {
                        // Storing the playerOwns value in the new card object
                        it.playerOwns = card.playerOwns

                        // Delete the old card object
                        cardDAO.deleteCard(card)

                        // Wait for finishing the DB query
                        while (cardDAO.getCardByID(it.id) != null) {
                            Thread.sleep(100)
                        }

                        // Write the new card object to DB
                        cardDAO.addCard(it)
                    }
                } else {
                    // If there is no entry create one
                    cardDAO.addCard(it)
                }
            }

            // Get all the cards from the local DB
            val cards = cardDAO.getAllCards()

            // Go through all the cards objects
            cards.forEach {
                // Buffering the current card object
                val currentCard = it

                // If card not available in the API any more, delete the card
                if (apiCardsList!!.find { it.id == currentCard.id } == null) {
                    cardDAO.deleteCard(currentCard)
                }
            }
        } else {
            // Close the database connection
            bcDatabase.close()

            // Not successful
            return false
        }

        // Close the database connection
        bcDatabase.close()

        // Successful
        return true
    }
}