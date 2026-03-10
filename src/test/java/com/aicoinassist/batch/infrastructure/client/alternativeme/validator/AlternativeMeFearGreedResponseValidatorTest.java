package com.aicoinassist.batch.infrastructure.client.alternativeme.validator;

import com.aicoinassist.batch.domain.market.validator.RawDataValidationResult;
import com.aicoinassist.batch.infrastructure.client.alternativeme.dto.AlternativeMeFearGreedDataItem;
import com.aicoinassist.batch.infrastructure.client.alternativeme.dto.AlternativeMeFearGreedMetadata;
import com.aicoinassist.batch.infrastructure.client.alternativeme.dto.AlternativeMeFearGreedResponse;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

class AlternativeMeFearGreedResponseValidatorTest {

    private final AlternativeMeFearGreedResponseValidator validator = new AlternativeMeFearGreedResponseValidator();

    @Test
    void validateReturnsValidForWellFormedResponse() {
        RawDataValidationResult result = validator.validate(validResponse());

        assertThat(result.isValid()).isTrue();
        assertThat(result.details()).isNull();
    }

    @Test
    void validateReturnsInvalidWhenResponseIsNull() {
        RawDataValidationResult result = validator.validate(null);

        assertThat(result.isValid()).isFalse();
        assertThat(result.details()).contains("must not be null");
    }

    @Test
    void validateReturnsInvalidWhenDataIsEmpty() {
        RawDataValidationResult result = validator.validate(new AlternativeMeFearGreedResponse(
                "Fear and Greed Index",
                List.of(),
                new AlternativeMeFearGreedMetadata(null)
        ));

        assertThat(result.isValid()).isFalse();
        assertThat(result.details()).contains("at least one item");
    }

    @Test
    void validateReturnsInvalidWhenValueIsNotNumeric() {
        RawDataValidationResult result = validator.validate(responseWithItem(
                new AlternativeMeFearGreedDataItem("fear", "Greed", "1741564800", "3600")
        ));

        assertThat(result.isValid()).isFalse();
        assertThat(result.details()).contains("value must be numeric");
    }

    @Test
    void validateReturnsInvalidWhenValueIsOutOfRange() {
        RawDataValidationResult result = validator.validate(responseWithItem(
                new AlternativeMeFearGreedDataItem("101", "Extreme Greed", "1741564800", "3600")
        ));

        assertThat(result.isValid()).isFalse();
        assertThat(result.details()).contains("between 0 and 100");
    }

    @Test
    void validateReturnsInvalidWhenTimestampIsNotNumeric() {
        RawDataValidationResult result = validator.validate(responseWithItem(
                new AlternativeMeFearGreedDataItem("54", "Neutral", "today", "3600")
        ));

        assertThat(result.isValid()).isFalse();
        assertThat(result.details()).contains("timestamp must be numeric");
    }

    @Test
    void validateReturnsInvalidWhenClassificationIsBlank() {
        RawDataValidationResult result = validator.validate(responseWithItem(
                new AlternativeMeFearGreedDataItem("54", " ", "1741564800", "3600")
        ));

        assertThat(result.isValid()).isFalse();
        assertThat(result.details()).contains("classification must not be blank");
    }

    private AlternativeMeFearGreedResponse validResponse() {
        return responseWithItem(new AlternativeMeFearGreedDataItem("54", "Neutral", "1741564800", "3600"));
    }

    private AlternativeMeFearGreedResponse responseWithItem(AlternativeMeFearGreedDataItem item) {
        return new AlternativeMeFearGreedResponse(
                "Fear and Greed Index",
                List.of(item),
                new AlternativeMeFearGreedMetadata(null)
        );
    }
}
