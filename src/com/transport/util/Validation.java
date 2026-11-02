package com.transport.util;

/**
 * Validation.java - Input Validation Utility Class
 *
 * This class contains STATIC methods to validate user inputs.
 * Static means you don't need to create an object to use them.
 * Just call: Validation.isValidMobile("9876543210")
 *
 * KEY CONCEPTS:
 * - Static methods
 * - String methods (isEmpty, matches, length)
 * - Regular expressions (regex) for pattern matching
 */
public class Validation {

    // Private constructor - nobody should create an object of this class
    private Validation() {}

    /**
     * Check if a text field is empty or blank.
     * @param text - the input to check
     * @return true if empty, false if has content
     */
    public static boolean isEmpty(String text) {
        return text == null || text.trim().isEmpty();
    }

    /**
     * Validate a 10-digit Indian mobile number.
     * Must start with 6, 7, 8, or 9.
     * Example: 9876543210 - valid
     * Example: 12345 - invalid
     */
    public static boolean isValidMobile(String mobile) {
        if (isEmpty(mobile)) return false;
        // Regex: starts with [6-9], followed by exactly 9 digits
        return mobile.matches("[6-9][0-9]{9}");
    }

    /**
     * Validate an email address format.
     */
    public static boolean isValidEmail(String email) {
        if (isEmpty(email)) return false;
        return email.matches("^[A-Za-z0-9+_.-]+@[A-Za-z0-9.-]+\\.[A-Za-z]{2,}$");
    }

    /**
     * Validate that a value is a valid positive number.
     * Used for trip amount validation.
     */
    public static boolean isValidAmount(String amount) {
        if (isEmpty(amount)) return false;
        try {
            double val = Double.parseDouble(amount);
            return val > 0; // Amount must be greater than 0
        } catch (NumberFormatException e) {
            return false; // Not a number
        }
    }

    /**
     * Validate vehicle registration number format.
     * Example: MH-48-AB-1234
     * Indian format: 2 letters - 2 digits - 2 letters - 4 digits
     */
    public static boolean isValidVehicleNumber(String regNo) {
        if (isEmpty(regNo)) return false;
        // Allow flexible format - just check it's not too short
        return regNo.trim().length() >= 5;
    }

    /**
     * Validate minimum text length.
     * @param text - input text
     * @param minLength - minimum required characters
     */
    public static boolean hasMinLength(String text, int minLength) {
        if (isEmpty(text)) return false;
        return text.trim().length() >= minLength;
    }

    /**
     * Check if a username is valid.
     * Must be 4-20 characters, letters/numbers only, no spaces.
     */
    public static boolean isValidUsername(String username) {
        if (isEmpty(username)) return false;
        return username.matches("[a-zA-Z0-9]{4,20}");
    }

    /**
     * Check if a password meets minimum requirements.
     * Must be at least 4 characters.
     */
    public static boolean isValidPassword(String password) {
        if (isEmpty(password)) return false;
        return password.length() >= 4;
    }

    /**
     * Check if a GST number has valid format.
     * GST format: 15 characters
     * Example: 27AAAPZ2323J1Z5
     */
    public static boolean isValidGST(String gst) {
        if (isEmpty(gst)) return true; // GST is optional
        return gst.trim().length() == 15;
    }
}
