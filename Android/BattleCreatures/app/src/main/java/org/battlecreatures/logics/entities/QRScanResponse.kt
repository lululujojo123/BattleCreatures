/*
 * Copyright (c) 2021 lululujojo123
 *
 * QRScanResponse.kt
 *
 * created by: Andreas G.
 * last edit \ by: 2021/02/03 \ Andreas G.
 */

package org.battlecreatures.logics.entities

/**
 * Data class defining the data structure of a QR-Scan response
 *
 * @param type The QR content type
 * @param symbol The data of the QR-Code
 */
data class QRScanResponse(
    val type: String,
    val symbol: List<Symbol>
)

/**
 * Data class defining the data structure of a QR-Scan related symbol object
 *
 * @param seq The sequence number if more then one QR-Code is scanned
 * @param data The content stored within the QR-Code
 * @param error A error description in case of an error
 */
data class Symbol(
    val seq: Int,
    val data: String?,
    val error: String?
)