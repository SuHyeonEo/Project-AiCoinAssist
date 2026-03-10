package com.aicoinassist.batch.domain.macro.service;

import com.aicoinassist.batch.domain.macro.dto.MacroContextSnapshot;
import com.aicoinassist.batch.domain.macro.entity.MacroSnapshotRawEntity;
import com.aicoinassist.batch.domain.macro.enumtype.MacroMetricType;
import com.aicoinassist.batch.domain.macro.repository.MacroSnapshotRawRepository;
import com.aicoinassist.batch.domain.market.enumtype.RawDataValidationStatus;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.Instant;
import java.time.LocalDate;
import java.time.ZoneOffset;

@Service
@RequiredArgsConstructor
public class MacroContextSnapshotService {

    private final MacroSnapshotRawRepository macroSnapshotRawRepository;

    public MacroContextSnapshot create() {
        MacroSnapshotRawEntity dxyRaw = latestValidRaw(MacroMetricType.DXY_PROXY);
        MacroSnapshotRawEntity us10yRaw = latestValidRaw(MacroMetricType.US10Y_YIELD);
        MacroSnapshotRawEntity usdKrwRaw = latestValidRaw(MacroMetricType.USD_KRW);

        Instant snapshotTime = latestObservationDate(dxyRaw, us10yRaw, usdKrwRaw)
                .atStartOfDay()
                .toInstant(ZoneOffset.UTC);

        return new MacroContextSnapshot(
                snapshotTime,
                dxyRaw.getObservationDate(),
                us10yRaw.getObservationDate(),
                usdKrwRaw.getObservationDate(),
                buildSourceDataVersion(dxyRaw, us10yRaw, usdKrwRaw),
                dxyRaw.getMetricValue(),
                us10yRaw.getMetricValue(),
                usdKrwRaw.getMetricValue()
        );
    }

    private MacroSnapshotRawEntity latestValidRaw(MacroMetricType metricType) {
        MacroSnapshotRawEntity rawEntity = macroSnapshotRawRepository
                .findTopByMetricTypeOrderByObservationDateDescCollectedTimeDescIdDesc(metricType)
                .orElseThrow(() -> new IllegalStateException("No macro raw snapshot found for " + metricType + "."));

        if (rawEntity.getValidationStatus() != RawDataValidationStatus.VALID) {
            throw new IllegalStateException("Macro raw snapshot is invalid for " + metricType + ": " + rawEntity.getValidationDetails());
        }

        return rawEntity;
    }

    private LocalDate latestObservationDate(
            MacroSnapshotRawEntity dxyRaw,
            MacroSnapshotRawEntity us10yRaw,
            MacroSnapshotRawEntity usdKrwRaw
    ) {
        LocalDate latest = dxyRaw.getObservationDate();
        if (us10yRaw.getObservationDate().isAfter(latest)) {
            latest = us10yRaw.getObservationDate();
        }
        if (usdKrwRaw.getObservationDate().isAfter(latest)) {
            latest = usdKrwRaw.getObservationDate();
        }
        return latest;
    }

    private String buildSourceDataVersion(
            MacroSnapshotRawEntity dxyRaw,
            MacroSnapshotRawEntity us10yRaw,
            MacroSnapshotRawEntity usdKrwRaw
    ) {
        return "dxyProxyDate=" + dxyRaw.getObservationDate()
                + ";us10yYieldDate=" + us10yRaw.getObservationDate()
                + ";usdKrwDate=" + usdKrwRaw.getObservationDate();
    }
}
