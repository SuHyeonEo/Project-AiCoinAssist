package com.aicoinassist.batch.domain.macro.service;

import com.aicoinassist.batch.domain.macro.dto.FredMacroRawSnapshot;
import com.aicoinassist.batch.domain.macro.entity.MacroSnapshotRawEntity;
import com.aicoinassist.batch.domain.macro.enumtype.MacroMetricType;
import com.aicoinassist.batch.domain.macro.repository.MacroSnapshotRawRepository;
import com.aicoinassist.batch.infrastructure.client.fred.FredMacroClient;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;

@Service
@RequiredArgsConstructor
public class MacroRawIngestionService {

    private static final String FRED_SOURCE = "FRED";

    private final FredMacroClient fredMacroClient;
    private final MacroSnapshotRawRepository macroSnapshotRawRepository;

    @Transactional
    public MacroSnapshotRawEntity ingestDxyProxy() {
        return ingest(MacroMetricType.DXY_PROXY);
    }

    @Transactional
    public MacroSnapshotRawEntity ingestUs10yYield() {
        return ingest(MacroMetricType.US10Y_YIELD);
    }

    @Transactional
    public MacroSnapshotRawEntity ingestUsdKrw() {
        return ingest(MacroMetricType.USD_KRW);
    }

    @Transactional
    public void ingestAll() {
        ingestDxyProxy();
        ingestUs10yYield();
        ingestUsdKrw();
    }

    private MacroSnapshotRawEntity ingest(MacroMetricType metricType) {
        Instant collectedTime = Instant.now();
        FredMacroRawSnapshot snapshot = fredMacroClient.fetchLatestObservation(metricType);

        MacroSnapshotRawEntity existingEntity = snapshot.observationDate() == null
                ? null
                : macroSnapshotRawRepository
                        .findTopBySourceAndMetricTypeAndObservationDateOrderByCollectedTimeDescIdDesc(
                                FRED_SOURCE,
                                metricType,
                                snapshot.observationDate()
                        )
                        .orElse(null);

        if (existingEntity == null) {
            MacroSnapshotRawEntity entity = MacroSnapshotRawEntity.builder()
                                                                 .source(FRED_SOURCE)
                                                                 .metricType(metricType)
                                                                 .seriesId(snapshot.seriesId())
                                                                 .units(snapshot.units())
                                                                 .observationDate(snapshot.observationDate())
                                                                 .collectedTime(collectedTime)
                                                                 .validationStatus(snapshot.validation().status())
                                                                 .validationDetails(snapshot.validation().details())
                                                                 .metricValue(snapshot.metricValue())
                                                                 .rawPayload(snapshot.rawPayload())
                                                                 .build();
            return macroSnapshotRawRepository.save(entity);
        }

        existingEntity.refreshFromIngestion(
                snapshot.seriesId(),
                snapshot.units(),
                collectedTime,
                snapshot.validation().status(),
                snapshot.validation().details(),
                snapshot.metricValue(),
                snapshot.rawPayload()
        );
        return existingEntity;
    }
}
