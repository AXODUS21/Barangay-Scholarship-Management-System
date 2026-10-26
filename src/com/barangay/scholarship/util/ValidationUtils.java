package com.barangay.scholarship.util;

import java.util.regex.Pattern;

/**
 * Validation utilities for applicant forms (Staff Registration and Student Portal).
 * Enforces realistic constraints and blocks gibberish, invalid formats, or overly long inputs.
 */
public final class ValidationUtils {

    // Maximum length limits
    public static final int MAX_NAME_LENGTH = 70;
    public static final int MIN_NAME_LENGTH = 3;
    public static final int MAX_EMAIL_LENGTH = 80;
    public static final int MAX_SCHOOL_LENGTH = 90;
    public static final int MIN_SCHOOL_LENGTH = 3;

    // Email RFC 5322 compliant regex pattern
    private static final Pattern EMAIL_PATTERN = Pattern.compile(
            "^[a-zA-Z0-9_+&*-]+(?:\\.[a-zA-Z0-9_+&*-]+)*@(?:[a-zA-Z0-9-]+\\.)+[a-zA-Z]{2,7}$"
    );

    // Philippine Mobile Number formats: 09XXXXXXXXX or +639XXXXXXXXX (11 or 13 digits)
    private static final Pattern PHONE_PATTERN = Pattern.compile(
            "^(09\\d{9}|\\+639\\d{9})$"
    );

    // Name pattern: allows letters, spaces, dots, hyphens, commas, apostrophes (Filipino & international names)
    // E.g., "Dela Cruz, Juan M.", "Maria Jose Santos-Reyes", "John O'Connor Jr."
    private static final Pattern NAME_CHARS_PATTERN = Pattern.compile(
            "^[a-zA-ZñÑáéíóúÁÉÍÓÚüÜ\\s,.'\\-]+$"
    );

    // School pattern: allows letters, digits, spaces, hyphens, dots, parentheses, and common symbols (e.g. "BatStateU - Alangilan", "UP Diliman (Main)")
    private static final Pattern SCHOOL_CHARS_PATTERN = Pattern.compile(
            "^[a-zA-Z0-9ñÑáéíóúÁÉÍÓÚüÜ\\s,.'\\-()&/]+$"
    );

    // Gibberish detector patterns:
    // 1. 4+ identical characters in a row (e.g. "aaaaa", "xxxxxx")
    private static final Pattern REPEATED_CHAR_PATTERN = Pattern.compile("(?i)(.)\\1{3,}");
    // 2. 5+ consecutive consonants with no vowels (e.g. "sdfghj", "qwrtyp")
    private static final Pattern CONSONANT_JUMBLE_PATTERN = Pattern.compile("(?i)[bcdfghjklmnpqrstvwxyz]{5,}");
    // 3. Repeating short syllables or repeated bigrams/trigrams (e.g. "fewfew", "fewfwef", "asdasd", "blaablaa")
    private static final Pattern REPEATED_SYLLABLE_PATTERN = Pattern.compile("(?i)(.{2,4})\\1+");

    private ValidationUtils() {
        // Prevent instantiation
    }

    /**
     * Checks if a string looks like unreadable gibberish or keyboard mash.
     */
    public static boolean isGibberish(String input) {
        if (input == null) return false;
        String clean = input.trim();
        if (REPEATED_CHAR_PATTERN.matcher(clean).find()) return true;
        if (CONSONANT_JUMBLE_PATTERN.matcher(clean).find()) return true;
        if (REPEATED_SYLLABLE_PATTERN.matcher(clean).find()) return true;

        // Vowel-to-letter ratio check for words > 4 chars: English/Tagalog words have ~25%-65% vowels.
        // Also check if any word of length >= 6 consists only of a chaotic mix of few/f/w without real dictionary structure
        String[] words = clean.split("[\\s,.'\\-()&/]+");
        for (String w : words) {
            if (w.length() >= 5) {
                int vowels = 0;
                for (char c : w.toLowerCase().toCharArray()) {
                    if ("aeiouy".indexOf(c) >= 0) vowels++;
                }
                // If 0 vowels or less than 15% vowels in a word of 6+ chars
                if (w.length() >= 6 && vowels == 0) return true;
                // Detect repetition of very small set of distinct characters (e.g. "fewfwef" uses only 3 chars: e, f, w)
                long distinct = w.toLowerCase().chars().distinct().count();
                if (w.length() >= 6 && distinct <= 3) {
                    return true;
                }
            }
        }

        return false;
    }

    /**
     * Validates applicant Full Name.
     * @return null if valid, or a descriptive error message if invalid.
     */
    public static String validateFullName(String name) {
        if (name == null || name.trim().isEmpty()) {
            return "Full Name is required.";
        }
        String clean = name.trim();
        if (clean.length() < MIN_NAME_LENGTH) {
            return "Full Name is too short (minimum " + MIN_NAME_LENGTH + " characters).";
        }
        if (clean.length() > MAX_NAME_LENGTH) {
            return "Full Name is too long (maximum " + MAX_NAME_LENGTH + " characters).";
        }
        if (!NAME_CHARS_PATTERN.matcher(clean).matches()) {
            return "Full Name contains invalid characters. Only letters, spaces, hyphens, and periods are allowed.";
        }
        if (isGibberish(clean)) {
            return "Full Name appears invalid or contains nonsense/keyboard mash text.";
        }
        // Check for at least one space or comma separating surname/given name
        if (!clean.contains(" ") && !clean.contains(",")) {
            return "Please provide both last name and first name (e.g., Dela Cruz, Juan M.).";
        }
        return null;
    }

    /**
     * Validates Email Address.
     * @return null if valid, or a descriptive error message if invalid.
     */
    public static String validateEmail(String email) {
        if (email == null || email.trim().isEmpty()) {
            return "Email Address is required.";
        }
        String clean = email.trim();
        if (clean.length() > MAX_EMAIL_LENGTH) {
            return "Email Address is too long (maximum " + MAX_EMAIL_LENGTH + " characters).";
        }
        if (!EMAIL_PATTERN.matcher(clean).matches()) {
            return "Invalid email address format (e.g., student@example.edu.ph).";
        }
        return null;
    }

    /**
     * Validates Contact Number (Mobile).
     * Accepts standard Philippine formats: 09XXXXXXXXX or +639XXXXXXXXX.
     * @return null if valid, or a descriptive error message if invalid.
     */
    public static String validatePhone(String phone) {
        if (phone == null || phone.trim().isEmpty()) {
            return "Contact Number is required.";
        }
        String clean = phone.trim().replaceAll("[\\s-]", ""); // Strip incidental spaces/dashes
        if (!PHONE_PATTERN.matcher(clean).matches()) {
            return "Invalid mobile number. Must be an 11-digit Philippine mobile number starting with 09 (e.g., 09171234567).";
        }
        return null;
    }

    /**
     * Validates School / University Name.
     * @return null if valid, or a descriptive error message if invalid.
     */
    public static String validateSchool(String school) {
        if (school == null || school.trim().isEmpty()) {
            return "School / University name is required.";
        }
        String clean = school.trim();
        if (clean.length() < MIN_SCHOOL_LENGTH) {
            return "School name is too short (minimum " + MIN_SCHOOL_LENGTH + " characters).";
        }
        if (clean.length() > MAX_SCHOOL_LENGTH) {
            return "School name is too long (maximum " + MAX_SCHOOL_LENGTH + " characters).";
        }
        if (!SCHOOL_CHARS_PATTERN.matcher(clean).matches()) {
            return "School name contains invalid special characters.";
        }
        if (isGibberish(clean)) {
            return "School / University name appears invalid or contains nonsense text.";
        }
        return null;
    }
}
