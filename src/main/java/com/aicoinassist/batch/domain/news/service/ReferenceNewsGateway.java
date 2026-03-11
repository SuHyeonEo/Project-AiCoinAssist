package com.aicoinassist.batch.domain.news.service;

import com.aicoinassist.batch.domain.news.dto.ReferenceNewsGatewayRequest;
import com.aicoinassist.batch.domain.news.dto.ReferenceNewsGatewayResponse;

public interface ReferenceNewsGateway {

    ReferenceNewsGatewayResponse generate(ReferenceNewsGatewayRequest request);
}
