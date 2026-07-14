package com.mockproject.group3.common;

/**
 * Global application constants.
 */
public final class AppConstants {

    /**
     * Private constructor to prevent instantiation of utility class.
     */
    private AppConstants() {
        throw new UnsupportedOperationException("This is a utility class and cannot be instantiated");
    }

    /**
     * The base prefix for all REST API endpoints.
     */
    public static final String API_PREFIX = "/api/v1";

}
