package com.aicoinassist.batch.domain.market.service;

import com.aicoinassist.batch.domain.market.dto.MarketRawIngestionResult;
import com.aicoinassist.batch.domain.market.entity.MarketCandleRawEntity;
import com.aicoinassist.batch.domain.market.entity.MarketPriceRawEntity;
import com.aicoinassist.batch.domain.market.enumtype.CandleInterval;
import com.aicoinassist.batch.domain.market.enumtype.RawDataValidationStatus;
import com.aicoinassist.batch.domain.market.repository.MarketCandleRawRepository;
import com.aicoinassist.batch.domain.market.repository.MarketPriceRawRepository;
import com.aicoinassist.batch.domain.market.validator.RawDataValidationResult;
import com.aicoinassist.batch.infrastructure.client.binance.BinanceApiClient;
import com.aicoinassist.batch.infrastructure.client.binance.dto.BinanceTickerPriceResponse;
import com.aicoinassist.batch.infrastructure.client.binance.validator.BinanceKlineResponseValidator;
import com.aicoinassist.batch.infrastructure.client.binance.validator.BinanceTickerPriceResponseValidator;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class MarketRawIngestionServiceTest {

    @Mock
    private BinanceApiClient binanceApiClient;

    @Mock
    private BinanceTickerPriceResponseValidator tickerPriceResponseValidator;

    @Mock
    private BinanceKlineResponseValidator klineResponseValidator;

    @Mock
    private MarketPriceRawRepository marketPriceRawRepository;

    @Mock
    private MarketCandleRawRepository marketCandleRawRepository;

    @Test
    void ingestStoresPlaceholderRecordWhenKlineResponseIsEmpty() {
        MarketRawIngestionService service = new MarketRawIngestionService(
                binanceApiClient,
                tickerPriceResponseValidator,
                klineResponseValidator,
                marketPriceRawRepository,
                marketCandleRawRepository,
                new ObjectMapper()
        );

        BinanceTickerPriceResponse tickerResponse = new BinanceTickerPriceResponse("BTCUSDT", "87500.12");

        when(binanceApiClient.getTickerPrice("BTCUSDT")).thenReturn(tickerResponse);
        when(tickerPriceResponseValidator.validate("BTCUSDT", tickerResponse)).thenReturn(RawDataValidationResult.valid());
        when(marketPriceRawRepository.save(any(MarketPriceRawEntity.class))).thenAnswer(invocation -> invocation.getArgument(0));
        when(binanceApiClient.getKlines("BTCUSDT", "1h", 120)).thenReturn(List.of());
        when(klineResponseValidator.validateSequence(List.of()))
                .thenReturn(RawDataValidationResult.invalid("Kline response list is empty."));
        when(marketCandleRawRepository.saveAll(any())).thenAnswer(invocation -> invocation.getArgument(0));

        MarketRawIngestionResult result = service.ingest("BTCUSDT", CandleInterval.ONE_HOUR);

        ArgumentCaptor<List<MarketCandleRawEntity>> captor = ArgumentCaptor.forClass(List.class);
        verify(marketCandleRawRepository).saveAll(captor.capture());

        List<MarketCandleRawEntity> savedEntities = captor.getValue();

        assertThat(result.candleCount()).isZero();
        assertThat(result.invalidCandleCount()).isEqualTo(1);
        assertThat(savedEntities).hasSize(1);
        assertThat(savedEntities.get(0).getValidationStatus()).isEqualTo(RawDataValidationStatus.INVALID);
        assertThat(savedEntities.get(0).getRawPayload()).isEqualTo("[]");
        assertThat(savedEntities.get(0).getIntervalValue()).isEqualTo(CandleInterval.ONE_HOUR.value());
    }
}
