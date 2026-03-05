package com.qrcommunication.smsforwarder.util

object PhoneValidator {
    // Format E.164: + suivi de 1-15 chiffres
    private val E164_REGEX = Regex("^\\+[1-9]\\d{5,14}$")
    // Format local francais: 0X XX XX XX XX
    private val FRENCH_LOCAL_REGEX = Regex("^0[1-9](\\s?\\d{2}){4}$")
    // Format avec indicatif 0033
    private val FRENCH_INTL_REGEX = Regex("^0033[1-9]\\d{8}$")

    fun isValid(number: String): Boolean {
        val cleaned = number.replace("\\s".toRegex(), "")
        return E164_REGEX.matches(cleaned) ||
               FRENCH_LOCAL_REGEX.matches(cleaned) ||
               FRENCH_INTL_REGEX.matches(cleaned)
    }

    fun normalize(number: String): String {
        val cleaned = number.replace("\\s".toRegex(), "").replace("-", "")
        return when {
            cleaned.startsWith("+") -> cleaned
            cleaned.startsWith("0033") -> "+" + cleaned.drop(2) // 0033 -> +33
            cleaned.startsWith("0") && cleaned.length == 10 -> "+33" + cleaned.drop(1)
            else -> cleaned
        }
    }

    fun formatDisplay(number: String): String {
        val normalized = normalize(number)
        if (!normalized.startsWith("+33") || normalized.length != 12) return normalized
        // +33612345678 -> +33 6 12 34 56 78
        return "${normalized.substring(0, 3)} ${normalized[3]} ${normalized.substring(4, 6)} ${normalized.substring(6, 8)} ${normalized.substring(8, 10)} ${normalized.substring(10, 12)}"
    }
}
