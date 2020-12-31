/*
 * Copyright (c) 2020 lululujojo123
 *
 * StringLanguage.kt
 *
 * created by: Andreas G.
 * last edit \ by: 2020/12/30 \ Andreas G.
 */

package org.battlecreatures.logics.enums

/**
 * Enum specifying the string appended after the property name to get the value for a specific language
 *
 * @param languageExtension The string to append the property with
 */
enum class StringLanguage(val languageExtension: String) {
    STANDARD(""),
    GERMAN("_de")
}