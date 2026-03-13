package com.aicoinassist.batch.domain.market.enumtype;

import java.time.Duration;
import java.time.Instant;

public enum CandleInterval {
    ONE_HOUR("1h", Duration.ofHours(1), 720),
    FOUR_HOUR("4h", Duration.ofHours(4), 420),
    ONE_DAY("1d", Duration.ofDays(1), 540);

    private final String value;
    private final Duration duration;
    private final int defaultBackfillLimit;

    CandleInterval(String value, Duration duration, int defaultBackfillLimit) {
        this.value = value;
        this.duration = duration;
        this.defaultBackfillLimit = defaultBackfillLimit;
    }

    public String value() {
        return value;
    }

    public Duration duration() {
        return duration;
    }

    public int defaultBackfillLimit() {
        return defaultBackfillLimit;
    }

    public Instant latestClosedOpenTime(Instant now) {
        Instant latestClosedCloseTime = latestClosedCloseTime(now);
        return latestClosedCloseTime == null ? null : latestClosedCloseTime.minus(duration);
    }

    public Instant latestClosedCloseTime(Instant now) {
        long intervalSeconds = duration.getSeconds();
        long epochSeconds = now.getEpochSecond();
        long closedBucket = (epochSeconds / intervalSeconds) * intervalSeconds;
        if (closedBucket <= 0) {
            return null;
        }
        if (epochSeconds % intervalSeconds == 0) {
            closedBucket -= intervalSeconds;
        }
        return Instant.ofEpochSecond(closedBucket);
    }
}
