package com.wisegrade.exam.api.dto;

public record ExamenEnsureResponse(
        long examenId,
        boolean created,
        long totalBanco) {
}
