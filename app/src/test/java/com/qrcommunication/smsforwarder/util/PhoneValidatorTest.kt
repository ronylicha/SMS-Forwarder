package com.qrcommunication.smsforwarder.util

import org.junit.Assert.*
import org.junit.Test

class PhoneValidatorTest {

    // --- isValid ---

    @Test
    fun isValid_e164Format() {
        assertTrue(PhoneValidator.isValid("+33612345678"))
    }

    @Test
    fun isValid_frenchLocalFormat() {
        assertTrue(PhoneValidator.isValid("0612345678"))
    }

    @Test
    fun isValid_frenchLocalWithSpaces() {
        assertTrue(PhoneValidator.isValid("06 12 34 56 78"))
    }

    @Test
    fun isValid_internationalPrefix() {
        assertTrue(PhoneValidator.isValid("0033612345678"))
    }

    @Test
    fun isValid_internationalE164WithDifferentCountry() {
        assertTrue(PhoneValidator.isValid("+1234567890"))
    }

    @Test
    fun isValid_invalidEmpty() {
        assertFalse(PhoneValidator.isValid(""))
    }

    @Test
    fun isValid_invalidTooShort() {
        assertFalse(PhoneValidator.isValid("+33"))
    }

    @Test
    fun isValid_invalidLetters() {
        assertFalse(PhoneValidator.isValid("abcdef"))
    }

    @Test
    fun isValid_invalidFormat() {
        assertFalse(PhoneValidator.isValid("123"))
    }

    @Test
    fun isValid_invalidStartsWithZeroButTooShort() {
        assertFalse(PhoneValidator.isValid("061234"))
    }

    // --- normalize ---

    @Test
    fun normalize_e164Unchanged() {
        assertEquals("+33612345678", PhoneValidator.normalize("+33612345678"))
    }

    @Test
    fun normalize_frenchLocal() {
        assertEquals("+33612345678", PhoneValidator.normalize("0612345678"))
    }

    @Test
    fun normalize_doubleZeroPrefix() {
        assertEquals("+33612345678", PhoneValidator.normalize("0033612345678"))
    }

    @Test
    fun normalize_withSpaces() {
        assertEquals("+33612345678", PhoneValidator.normalize("06 12 34 56 78"))
    }

    @Test
    fun normalize_withDashes() {
        assertEquals("+33612345678", PhoneValidator.normalize("06-12-34-56-78"))
    }

    @Test
    fun normalize_nonFrenchE164() {
        assertEquals("+1234567890", PhoneValidator.normalize("+1234567890"))
    }

    // --- formatDisplay ---

    @Test
    fun formatDisplay_frenchNumber() {
        assertEquals("+33 6 12 34 56 78", PhoneValidator.formatDisplay("+33612345678"))
    }

    @Test
    fun formatDisplay_frenchLocalConverted() {
        assertEquals("+33 6 12 34 56 78", PhoneValidator.formatDisplay("0612345678"))
    }

    @Test
    fun formatDisplay_nonFrenchUnchanged() {
        // +1234567890 is not +33 with 12 chars, so returned as-is after normalization
        assertEquals("+1234567890", PhoneValidator.formatDisplay("+1234567890"))
    }

    @Test
    fun formatDisplay_shortNumberUnchanged() {
        assertEquals("+335", PhoneValidator.formatDisplay("+335"))
    }
}
