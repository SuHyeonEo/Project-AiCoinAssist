package com.aicoinassist.batch.domain.market.validator;

import com.aicoinassist.batch.domain.market.enumtype.RawDataValidationStatus;

public record RawDataValidationResult(
        RawDataValidationStatus status,
        String details
) {

    public static RawDataValidationResult valid() {
        return new RawDataValidationResult(RawDataValidationStatus.VALID, null);
    }

    public static RawDataValidationResult invalid(String details) {
        return new RawDataValidationResult(RawDataValidationStatus.INVALID, details);
    }

    public boolean isValid() {
        return status == RawDataValidationStatus.VALID;
    }
}
