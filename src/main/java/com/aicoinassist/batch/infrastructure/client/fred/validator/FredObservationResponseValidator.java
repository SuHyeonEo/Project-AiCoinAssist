package com.aicoinassist.batch.infrastructure.client.fred.validator;

import com.aicoinassist.batch.domain.market.validator.RawDataValidationResult;
import com.aicoinassist.batch.infrastructure.client.fred.dto.FredObservationItem;
import com.aicoinassist.batch.infrastructure.client.fred.dto.FredObservationResponse;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.time.LocalDate;

@Component
public class FredObservationResponseValidator {

    public RawDataValidationResult validate(FredObservationResponse response) {
        if (response == null) {
            return RawDataValidationResult.invalid("FRED observation response must not be null.");
        }

        if (response.errorCode() != null || (response.errorMessage() != null && !response.errorMessage().isBlank())) {
            return RawDataValidationResult.invalid("FRED observation response returned an error.");
        }

        if (response.observations() == null || response.observations().isEmpty()) {
            return RawDataValidationResult.invalid("FRED observation response must contain at least one observation.");
        }

        boolean hasValidObservation = false;
        for (FredObservationItem item : response.observations()) {
            if (item == null) {
                continue;
            }
            if (!isValidDate(item.date())) {
                continue;
            }
            if (!isValidNumericValue(item.value())) {
                continue;
            }
            hasValidObservation = true;
            break;
        }

        if (!hasValidObservation) {
            return RawDataValidationResult.invalid("FRED observation response does not contain a usable numeric observation.");
        }

        return RawDataValidationResult.valid();
    }

    private boolean isValidDate(String rawDate) {
        try {
            return rawDate != null && !rawDate.isBlank() && LocalDate.parse(rawDate) != null;
        } catch (RuntimeException exception) {
            return false;
        }
    }

    private boolean isValidNumericValue(String rawValue) {
        try {
            return rawValue != null && !rawValue.isBlank() && !".".equals(rawValue.trim()) && new BigDecimal(rawValue) != null;
        } catch (RuntimeException exception) {
            return false;
        }
    }
}
