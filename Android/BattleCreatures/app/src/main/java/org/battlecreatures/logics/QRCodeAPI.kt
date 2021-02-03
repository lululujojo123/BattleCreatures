/*
 * Copyright (c) 2020 lululujojo123
 *
 * QRCodeAPI.kt
 *
 * created by: Andreas G.
 * last edit \ by: 2021/01/04 \ Andreas G.
 */

package org.battlecreatures.logics

import okhttp3.MultipartBody
import okhttp3.RequestBody
import org.battlecreatures.logics.entities.Card
import org.battlecreatures.logics.entities.QRScanResponse
import retrofit2.Call
import retrofit2.http.*
import kotlin.jvm.internal.Intrinsics

/**
 * Interface for creating a API request
 */
interface QRCodeAPI {
    @Multipart
    @POST("v1/read-qr-code/")
    fun getQRCodeResult(
        @Part("file\"; filename=\"file.jpeg\" ")
        file: RequestBody
        ): Call<List<QRScanResponse>>
}