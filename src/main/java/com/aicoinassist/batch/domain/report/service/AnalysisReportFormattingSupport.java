package com.aicoinassist.batch.domain.report.service;

import com.aicoinassist.batch.domain.report.enumtype.AnalysisPriceZoneInteractionType;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.Locale;

class AnalysisReportFormattingSupport {

    private static final int DISPLAY_SCALE = 2;

    String percentage(BigDecimal value) {
        if (value == null) {
            return "확인 불가";
        }

        return plain(value.multiply(new BigDecimal("100"))) + "%";
    }

    String signedRatio(BigDecimal value) {
        if (value == null) {
            return "확인 불가";
        }

        BigDecimal asPercent = truncate(value.multiply(new BigDecimal("100")));
        if (asPercent.compareTo(BigDecimal.ZERO) > 0) {
            return "+" + asPercent.toPlainString() + "%";
        }
        return asPercent.toPlainString() + "%";
    }

    String fundingRatePercentage(BigDecimal fundingRate) {
        if (fundingRate == null) {
            return "확인 불가";
        }

        BigDecimal percentage = truncate(fundingRate.multiply(new BigDecimal("100")));
        if (percentage.compareTo(BigDecimal.ZERO) > 0) {
            return "+" + percentage.toPlainString() + "%";
        }
        return percentage.toPlainString() + "%";
    }

    String signedPercent(BigDecimal percentValue) {
        if (percentValue == null) {
            return "확인 불가";
        }

        BigDecimal normalized = truncate(percentValue);
        if (normalized.compareTo(BigDecimal.ZERO) > 0) {
            return "+" + normalized.toPlainString() + "%";
        }
        return normalized.toPlainString() + "%";
    }

    String signed(BigDecimal value) {
        String plainValue = plain(value);
        if (value.compareTo(BigDecimal.ZERO) > 0) {
            return "+" + plainValue;
        }
        return plainValue;
    }

    String plain(BigDecimal value) {
        if (value == null) {
            return "확인 불가";
        }
        return truncate(value).toPlainString();
    }

    String enumLabel(Enum<?> value) {
        return value.name().toLowerCase(Locale.ROOT).replace('_', '-');
    }

    String distanceFromExtremum(BigDecimal priceChangeRate, String relation) {
        BigDecimal absoluteValue = priceChangeRate.abs();
        String relationLabel = switch (relation) {
            case "below" -> "아래";
            case "above" -> "위";
            default -> relation;
        };
        return plain(absoluteValue) + "% " + relationLabel;
    }

    String interactionShift(
            AnalysisPriceZoneInteractionType currentInteractionType,
            AnalysisPriceZoneInteractionType referenceInteractionType
    ) {
        if (currentInteractionType == null && referenceInteractionType == null) {
            return "변화 없음";
        }
        if (currentInteractionType == null) {
            return interactionLabel(referenceInteractionType) + "에서 이탈";
        }
        if (referenceInteractionType == null) {
            return interactionLabel(currentInteractionType) + "로 진입";
        }
        if (currentInteractionType == referenceInteractionType) {
            return interactionLabel(currentInteractionType) + " 유지";
        }
        return interactionLabel(referenceInteractionType)
                + " -> "
                + interactionLabel(currentInteractionType);
    }

    String zoneLabel(BigDecimal zoneLow, BigDecimal zoneHigh) {
        String low = plain(zoneLow);
        String high = plain(zoneHigh);
        if (zoneLow.compareTo(zoneHigh) == 0) {
            return low + " 단일 레벨";
        }
        return low + " ~ " + high;
    }

    boolean singlePriceLevel(BigDecimal zoneLow, BigDecimal zoneHigh) {
        return zoneLow.compareTo(zoneHigh) == 0;
    }

    String interactionLabel(AnalysisPriceZoneInteractionType interactionType) {
        if (interactionType == null) {
            return "확인 불가";
        }
        return switch (interactionType) {
            case ABOVE_ZONE -> "구간 상단";
            case BELOW_ZONE -> "구간 하단";
            case INSIDE_ZONE -> "구간 내부";
        };
    }

    private BigDecimal truncate(BigDecimal value) {
        return value.setScale(DISPLAY_SCALE, RoundingMode.DOWN).stripTrailingZeros();
    }
}
