package com.aicoinassist.batch.infrastructure.client.fred.validator;

import com.aicoinassist.batch.domain.market.validator.RawDataValidationResult;
import com.aicoinassist.batch.infrastructure.client.fred.dto.FredObservationItem;
import com.aicoinassist.batch.infrastructure.client.fred.dto.FredObservationResponse;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

class FredObservationResponseValidatorTest {

    private final FredObservationResponseValidator validator = new FredObservationResponseValidator();

    @Test
    void validateReturnsValidWhenResponseContainsUsableNumericObservation() {
        FredObservationResponse response = new FredObservationResponse(
                "lin",
                null,
                null,
                List.of(
                        new FredObservationItem("2026-03-10", "2026-03-10", "2026-03-10", "."),
                        new FredObservationItem("2026-03-09", "2026-03-09", "2026-03-09", "119.8421")
                )
        );

        RawDataValidationResult result = validator.validate(response);

        assertThat(result.isValid()).isTrue();
    }

    @Test
    void validateReturnsInvalidWhenResponseContainsNoUsableObservation() {
        FredObservationResponse response = new FredObservationResponse(
                "lin",
                null,
                null,
                List.of(new FredObservationItem("2026-03-10", "2026-03-10", "2026-03-10", "."))
        );

        RawDataValidationResult result = validator.validate(response);

        assertThat(result.isValid()).isFalse();
        assertThat(result.details()).contains("usable numeric observation");
    }

    @Test
    void validateReturnsInvalidWhenResponseContainsApiError() {
        FredObservationResponse response = new FredObservationResponse(
                null,
                400,
                "Bad Request",
                List.of()
        );

        RawDataValidationResult result = validator.validate(response);

        assertThat(result.isValid()).isFalse();
        assertThat(result.details()).contains("returned an error");
    }
}
