/*
 * Copyright (c) 2021 lululujojo123
 *
 * QRCodeAPI.kt
 *
 * created by: Andreas G.
 * last edit \ by: 2021/02/03 \ Andreas G.
 */

package org.battlecreatures.logics

import okhttp3.RequestBody
import org.battlecreatures.logics.entities.QRScanResponse
import retrofit2.Call
import retrofit2.http.*

/**
 * Interface for creating a QR-API request
 */
interface QRCodeAPI {
    @Multipart
    @POST("v1/read-qr-code/")
    fun getQRCodeResult(
        @Part("file\"; filename=\"file.jpeg\" ")
        file: RequestBody
        ): Call<List<QRScanResponse>>
}