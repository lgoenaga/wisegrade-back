package com.wisegrade.common.api;

import java.util.Map;

public record ApiError(
        String message,
        Map<String, String> fieldErrors) {
    public static ApiError ofMessage(String message) {
        return new ApiError(message, Map.of());
    }
}
