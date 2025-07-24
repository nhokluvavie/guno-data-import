package com.guno.etl.util;

import lombok.extern.slf4j.Slf4j;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.HexFormat;

@Slf4j
public class HashUtil {

    private static final String HASH_ALGORITHM = "SHA-256";
    private static final String PHONE_SALT = "GUNO_PHONE_SALT_2025";
    private static final String EMAIL_SALT = "GUNO_EMAIL_SALT_2025";

    /**
     * Hash phone number for privacy-safe storage
     * Removes special characters and normalizes before hashing
     */
    public static String hashPhone(String phoneNumber) {
        if (phoneNumber == null || phoneNumber.trim().isEmpty()) {
            log.warn("Attempting to hash null or empty phone number");
            return null;
        }

        try {
            // Normalize phone number: remove spaces, dashes, parentheses, plus signs
            String normalizedPhone = phoneNumber.replaceAll("[\\s\\-\\(\\)\\+]", "");

            // Remove leading zeros and country codes for Vietnamese numbers
            normalizedPhone = normalizeVietnamesePhone(normalizedPhone);

            // Add salt and hash
            String saltedPhone = PHONE_SALT + normalizedPhone;
            String hash = generateHash(saltedPhone);

            log.debug("Phone hashed successfully, original length: {}, hash: {}",
                    phoneNumber.length(), hash.substring(0, 8) + "...");

            return hash;

        } catch (Exception e) {
            log.error("Error hashing phone number: {}", e.getMessage());
            return null;
        }
    }

    /**
     * Hash email for privacy-safe storage
     * Normalizes email to lowercase before hashing
     */
    public static String hashEmail(String email) {
        if (email == null || email.trim().isEmpty()) {
            log.warn("Attempting to hash null or empty email");
            return null;
        }

        try {
            // Normalize email: trim and lowercase
            String normalizedEmail = email.trim().toLowerCase();

            // Basic email validation
            if (!isValidEmail(normalizedEmail)) {
                log.warn("Invalid email format provided for hashing: {}", email);
                return null;
            }

            // Add salt and hash
            String saltedEmail = EMAIL_SALT + normalizedEmail;
            String hash = generateHash(saltedEmail);

            log.debug("Email hashed successfully, domain: {}, hash: {}",
                    extractDomain(normalizedEmail), hash.substring(0, 8) + "...");

            return hash;

        } catch (Exception e) {
            log.error("Error hashing email: {}", e.getMessage());
            return null;
        }
    }

    /**
     * Generate SHA-256 hash
     */
    private static String generateHash(String input) throws NoSuchAlgorithmException {
        MessageDigest digest = MessageDigest.getInstance(HASH_ALGORITHM);
        byte[] hashBytes = digest.digest(input.getBytes(StandardCharsets.UTF_8));
        return HexFormat.of().formatHex(hashBytes);
    }

    /**
     * Normalize Vietnamese phone numbers
     * Handles various Vietnamese phone number formats
     */
    private static String normalizeVietnamesePhone(String phone) {
        // Remove country code +84 or 84
        if (phone.startsWith("84")) {
            phone = phone.substring(2);
        }

        // Convert old format (01x) to new format (03x, 05x, 07x, 08x, 09x)
        if (phone.startsWith("01")) {
            switch (phone.substring(0, 3)) {
                case "012": phone = "032" + phone.substring(3); break;
                case "016": case "018": phone = "03" + phone.substring(2); break;
                case "019": phone = "059" + phone.substring(3); break;
                // Add more conversions as needed
            }
        }

        // Ensure phone starts with 0 for Vietnamese format
        if (!phone.startsWith("0") && phone.length() >= 9) {
            phone = "0" + phone;
        }

        return phone;
    }

    /**
     * Basic email validation
     */
    private static boolean isValidEmail(String email) {
        return email != null &&
                email.contains("@") &&
                email.contains(".") &&
                email.indexOf("@") < email.lastIndexOf(".") &&
                email.length() >= 5;
    }

    /**
     * Extract domain from email for logging purposes
     */
    private static String extractDomain(String email) {
        int atIndex = email.indexOf("@");
        return atIndex > 0 ? email.substring(atIndex + 1) : "unknown";
    }

    /**
     * Generate customer ID from phone hash
     */
    public static String generateCustomerId(String platform, String phoneHash) {
        if (phoneHash == null || phoneHash.isEmpty()) {
            return null;
        }
        return platform.toUpperCase() + "_" + phoneHash.substring(0, 12);
    }

    /**
     * Verify if a phone number matches a hash (for testing/debugging)
     */
    public static boolean verifyPhone(String phoneNumber, String hash) {
        if (phoneNumber == null || hash == null) {
            return false;
        }
        String computedHash = hashPhone(phoneNumber);
        return hash.equals(computedHash);
    }

    /**
     * Verify if an email matches a hash (for testing/debugging)
     */
    public static boolean verifyEmail(String email, String hash) {
        if (email == null || hash == null) {
            return false;
        }
        String computedHash = hashEmail(email);
        return hash.equals(computedHash);
    }

    /**
     * Generate a short hash for display purposes (first 8 characters)
     */
    public static String generateShortHash(String input) {
        if (input == null || input.isEmpty()) {
            return null;
        }
        try {
            String fullHash = generateHash(input);
            return fullHash.substring(0, 8);
        } catch (Exception e) {
            log.error("Error generating short hash: {}", e.getMessage());
            return null;
        }
    }

    /**
     * Mask phone number for logging (show only first 3 and last 2 digits)
     */
    public static String maskPhone(String phoneNumber) {
        if (phoneNumber == null || phoneNumber.length() < 5) {
            return "***";
        }
        String normalized = phoneNumber.replaceAll("[\\s\\-\\(\\)\\+]", "");
        if (normalized.length() < 5) {
            return "***";
        }
        return normalized.substring(0, 3) + "***" + normalized.substring(normalized.length() - 2);
    }

    /**
     * Mask email for logging (show only first 2 characters and domain)
     */
    public static String maskEmail(String email) {
        if (email == null || !email.contains("@")) {
            return "***@***.***";
        }
        String[] parts = email.split("@");
        if (parts[0].length() < 2) {
            return "**@" + parts[1];
        }
        return parts[0].substring(0, 2) + "***@" + parts[1];
    }
}