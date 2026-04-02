package com.aicoinassist.batch.domain.news.service;

import com.aicoinassist.batch.domain.news.dto.ReferenceNewsGatewayRequest;
import com.aicoinassist.batch.domain.news.dto.ReferenceNewsGatewayResponse;
import com.aicoinassist.batch.domain.news.enumtype.ReferenceNewsGenerationFailureType;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Component;

@Component
@ConditionalOnProperty(prefix = "external.openai", name = "enabled", havingValue = "false", matchIfMissing = true)
public class UnsupportedReferenceNewsGateway implements ReferenceNewsGateway {

    @Override
    public ReferenceNewsGatewayResponse generate(ReferenceNewsGatewayRequest request) {
        throw new ReferenceNewsGatewayException(
                ReferenceNewsGenerationFailureType.UNSUPPORTED,
                false,
                "No reference news gateway provider is configured."
        );
    }
}
