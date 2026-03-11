package com.aicoinassist.batch.domain.report.service;

import com.aicoinassist.batch.domain.report.enumtype.AnalysisPriceZoneInteractionType;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.Locale;

class AnalysisReportFormattingSupport {

    String percentage(BigDecimal value) {
        if (value == null) {
            return "unavailable";
        }

        return value.multiply(new BigDecimal("100"))
                    .setScale(2, RoundingMode.HALF_UP)
                    .stripTrailingZeros()
                    .toPlainString() + "%";
    }

    String signedRatio(BigDecimal value) {
        if (value == null) {
            return "unavailable";
        }

        BigDecimal asPercent = value.multiply(new BigDecimal("100"))
                                    .setScale(2, RoundingMode.HALF_UP)
                                    .stripTrailingZeros();
        if (asPercent.compareTo(BigDecimal.ZERO) > 0) {
            return "+" + asPercent.toPlainString() + "%";
        }
        return asPercent.toPlainString() + "%";
    }

    String fundingRatePercentage(BigDecimal fundingRate) {
        if (fundingRate == null) {
            return "unavailable";
        }

        BigDecimal percentage = fundingRate.multiply(new BigDecimal("100"))
                                           .setScale(4, RoundingMode.HALF_UP)
                                           .stripTrailingZeros();
        if (percentage.compareTo(BigDecimal.ZERO) > 0) {
            return "+" + percentage.toPlainString() + "%";
        }
        return percentage.toPlainString() + "%";
    }

    String signedPercent(BigDecimal percentValue) {
        if (percentValue == null) {
            return "unavailable";
        }

        BigDecimal normalized = percentValue.setScale(4, RoundingMode.HALF_UP).stripTrailingZeros();
        if (normalized.compareTo(BigDecimal.ZERO) > 0) {
            return "+" + normalized.toPlainString() + "%";
        }
        return normalized.toPlainString() + "%";
    }

    String signed(BigDecimal value) {
        String plainValue = value.stripTrailingZeros().toPlainString();
        if (value.compareTo(BigDecimal.ZERO) > 0) {
            return "+" + plainValue;
        }
        return plainValue;
    }

    String enumLabel(Enum<?> value) {
        return value.name().toLowerCase(Locale.ROOT).replace('_', '-');
    }

    String distanceFromExtremum(BigDecimal priceChangeRate, String relation) {
        BigDecimal absoluteValue = priceChangeRate.abs();
        return absoluteValue.stripTrailingZeros().toPlainString() + "% " + relation;
    }

    String interactionShift(
            AnalysisPriceZoneInteractionType currentInteractionType,
            AnalysisPriceZoneInteractionType referenceInteractionType
    ) {
        if (currentInteractionType == null && referenceInteractionType == null) {
            return "unchanged";
        }
        if (currentInteractionType == null) {
            return "from " + referenceInteractionType.name().toLowerCase().replace('_', ' ');
        }
        if (referenceInteractionType == null) {
            return "to " + currentInteractionType.name().toLowerCase().replace('_', ' ');
        }
        if (currentInteractionType == referenceInteractionType) {
            return "unchanged at " + currentInteractionType.name().toLowerCase().replace('_', ' ');
        }
        return referenceInteractionType.name().toLowerCase().replace('_', ' ')
                + " -> "
                + currentInteractionType.name().toLowerCase().replace('_', ' ');
    }

    String zoneLabel(BigDecimal zoneLow, BigDecimal zoneHigh) {
        String low = zoneLow.stripTrailingZeros().toPlainString();
        String high = zoneHigh.stripTrailingZeros().toPlainString();
        if (zoneLow.compareTo(zoneHigh) == 0) {
            return "single level at " + low;
        }
        return low + " to " + high;
    }

    boolean singlePriceLevel(BigDecimal zoneLow, BigDecimal zoneHigh) {
        return zoneLow.compareTo(zoneHigh) == 0;
    }
}
