package com.aicoinassist.batch.infrastructure.client.alternativeme.validator;

import com.aicoinassist.batch.domain.market.validator.RawDataValidationResult;
import com.aicoinassist.batch.infrastructure.client.alternativeme.dto.AlternativeMeFearGreedDataItem;
import com.aicoinassist.batch.infrastructure.client.alternativeme.dto.AlternativeMeFearGreedResponse;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;

@Component
public class AlternativeMeFearGreedResponseValidator {

    public RawDataValidationResult validate(AlternativeMeFearGreedResponse response) {
        if (response == null) {
            return RawDataValidationResult.invalid("Fear & Greed response must not be null.");
        }

        if (response.metadata() != null && response.metadata().error() != null && !response.metadata().error().isBlank()) {
            return RawDataValidationResult.invalid("Fear & Greed response metadata error must be blank.");
        }

        if (response.data() == null || response.data().isEmpty()) {
            return RawDataValidationResult.invalid("Fear & Greed response data must contain at least one item.");
        }

        AlternativeMeFearGreedDataItem item = response.data().get(0);
        if (item == null) {
            return RawDataValidationResult.invalid("Fear & Greed response item must not be null.");
        }

        BigDecimal value;
        try {
            value = new BigDecimal(item.value());
        } catch (RuntimeException exception) {
            return RawDataValidationResult.invalid("Fear & Greed value must be numeric.");
        }

        if (value.compareTo(BigDecimal.ZERO) < 0 || value.compareTo(new BigDecimal("100")) > 0) {
            return RawDataValidationResult.invalid("Fear & Greed value must be between 0 and 100.");
        }

        if (item.valueClassification() == null || item.valueClassification().isBlank()) {
            return RawDataValidationResult.invalid("Fear & Greed value classification must not be blank.");
        }

        try {
            long timestamp = Long.parseLong(item.timestamp());
            if (timestamp <= 0L) {
                return RawDataValidationResult.invalid("Fear & Greed timestamp must be greater than zero.");
            }
        } catch (RuntimeException exception) {
            return RawDataValidationResult.invalid("Fear & Greed timestamp must be numeric.");
        }

        if (item.timeUntilUpdate() != null && !item.timeUntilUpdate().isBlank()) {
            try {
                long timeUntilUpdate = Long.parseLong(item.timeUntilUpdate());
                if (timeUntilUpdate < 0L) {
                    return RawDataValidationResult.invalid("Fear & Greed time until update must be greater than or equal to zero.");
                }
            } catch (RuntimeException exception) {
                return RawDataValidationResult.invalid("Fear & Greed time until update must be numeric.");
            }
        }

        return RawDataValidationResult.valid();
    }
}
