package com.aicoinassist.batch.domain.news.service;

import com.aicoinassist.batch.domain.news.dto.ReferenceNewsSnapshotPayload;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.json.JsonMapper;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class ReferenceNewsOutputPostProcessorTest {

    private final ObjectMapper objectMapper = JsonMapper.builder().findAndAddModules().build();

    @Test
    void processAcceptsValidFiveItemPayload() {
        ReferenceNewsOutputPostProcessor processor = new ReferenceNewsOutputPostProcessor(objectMapper);

        ReferenceNewsSnapshotPayload payload = processor.process("""
                {
                  "summary":"오늘 공통 뉴스는 BTC 수급과 거시 이벤트, 시장 리스크 선호를 함께 설명합니다.",
                  "items":[
                    {"category":"DIRECT_ASSET","title":"Bitcoin ETF flow rises","source":"A","published_at":"2026-03-12T00:00:00Z","url":"https://example.com/1","selection_reason":"BTC 직접 수급 변화와 연결됩니다."},
                    {"category":"MACRO_ECONOMY","title":"US CPI preview","source":"B","published_at":"2026-03-12T01:00:00Z","url":"https://example.com/2","selection_reason":"거시 변수로 위험 선호에 영향을 줍니다."},
                    {"category":"OTHER_REFERENCE","title":"Exchange reserve trend","source":"C","published_at":"2026-03-12T02:00:00Z","url":"https://example.com/3","selection_reason":"현물 유동성 흐름 참고에 유용합니다."},
                    {"category":"OTHER_REFERENCE","title":"Stablecoin issuance update","source":"D","published_at":"2026-03-12T03:00:00Z","url":"https://example.com/4","selection_reason":"시장 유동성 체감과 연결됩니다."},
                    {"category":"OTHER_REFERENCE","title":"Fed speaker comments","source":"E","published_at":"2026-03-12T04:00:00Z","url":"https://example.com/5","selection_reason":"달러와 위험자산 민감도에 영향을 줍니다."}
                  ]
                }
                """);

        assertThat(payload.items()).hasSize(5);
    }

    @Test
    void processRejectsWhenFiveItemsAreNotReturned() {
        ReferenceNewsOutputPostProcessor processor = new ReferenceNewsOutputPostProcessor(objectMapper);

        assertThatThrownBy(() -> processor.process("""
                {
                  "summary":"짧은 요약",
                  "items":[
                    {"category":"DIRECT_ASSET","title":"Bitcoin ETF flow rises","source":"A","published_at":"2026-03-12T00:00:00Z","url":"https://example.com/1","selection_reason":"BTC 직접 수급 변화와 연결됩니다."}
                  ]
                }
                """))
                .isInstanceOf(ReferenceNewsGatewayException.class)
                .hasMessage("Reference news item count must be exactly 5.");
    }
}
