package com.aicoinassist.batch.domain.market.dto;

import com.aicoinassist.batch.domain.market.entity.MarketIndicatorSnapshotEntity;

import java.util.List;

public record MarketIndicatorSnapshotContext(
        MarketIndicatorSnapshotEntity snapshotEntity,
        List<Candle> candles
) {
}
