# Report Payload Specification

## 1. Purpose
This document defines the intended structure of the final analysis report payload.

The report layer should store:
- user-facing structured interpretation data
- enough metadata to trace the analysis basis
- structured comparison outputs
- scenario / risk / support-resistance explanation inputs

The report layer should **not** duplicate full raw history.

---

## 2. Core Principles

### 2.1 Structured first
The report payload should start from structured JSON-style fields, not only free-form prose.

### 2.2 Query/filter columns + payload split
Important search and filtering fields should exist as separate columns outside the payload.
Detailed evolving content can live inside the JSON payload.

### 2.3 Traceability
A report must be traceable to:
- analysis basis time
- raw reference time
- source data version
- analysis engine version

### 2.4 Comparison-first
The report should reflect server-calculated comparison facts.
It should not rely on GPT inventing numeric comparisons from raw history.

---

## 3. Suggested Top-Level Report Entity Fields

Suggested top-level metadata fields:
- symbol
- reportType
- analysisBasisTime
- rawReferenceTime
- sourceDataVersion
- analysisEngineVersion
- storedTime
- reportPayload

Possible uniqueness basis:
- symbol
- reportType
- analysisBasisTime
- sourceDataVersion
- analysisEngineVersion

---

## 4. Suggested Report Payload Structure

## 4.1 Top-level payload example
Suggested sections:
- summary
- marketContext
- supportLevels
- resistanceLevels
- riskFactors
- scenarios
- comparisonFacts
- comparisonHighlights

Optional later sections:
- derivativeContext
- onchainContext
- sentimentContext
- macroContext
- newsContext

---

## 4.2 Summary
Purpose:
- concise integrated interpretation

Suggested fields:
- headline
- outlook
- confidence
- keyMessage

---

## 4.3 Market Context
Purpose:
- summarize structured state before scenario detail

Suggested fields:
- currentPrice
- currentTrendLabel
- volatilityLabel
- rangePositionLabel
- maPositionSummary
- momentumSummary

Optional:
- derivativeContextSummary
- sentimentContextSummary
- macroContextSummary

---

## 4.4 Support Levels
Purpose:
- communicate candidate downside structure

Suggested fields per item:
- level
- strength
- sourceType
- description

Possible source types:
- pivot low
- moving average
- range low
- prior reaction zone
- server-extracted candidate

---

## 4.5 Resistance Levels
Purpose:
- communicate candidate upside structure

Suggested fields per item:
- level
- strength
- sourceType
- description

Possible source types:
- pivot high
- moving average
- range high
- prior reaction zone
- server-extracted candidate

---

## 4.6 Risk Factors
Purpose:
- list structured risk elements

Suggested fields per item:
- category
- title
- description
- severity
- relatedMetric

Possible categories:
- momentum
- volatility
- leverage
- on-chain
- sentiment
- macro

---

## 4.7 Scenarios
Purpose:
- provide actionable interpretation branches

Suggested fields per item:
- scenarioType
- title
- description
- triggerCondition
- invalidationCondition
- implication

Possible types:
- bullish continuation
- range continuation
- bearish breakdown
- recovery attempt
- volatility expansion

---

## 4.8 Comparison Facts
Purpose:
- preserve structured comparison outputs for API/GPT/reporting reuse

Suggested fields per item:
- horizon
- metric
- referenceType
- referenceTime
- currentValue
- referenceValue
- absoluteDelta
- changeRate
- signal
- summary

Optional:
- positionLabel
- confidence
- priority

This field should remain machine-friendly.

---

## 4.9 Comparison Highlights
Purpose:
- surface the most important comparison facts for report emphasis

Suggested fields per item:
- horizon
- title
- summary
- importance
- relatedReferenceType
- relatedMetric

This field should remain more human-facing than `comparisonFacts`.

---

## 4.10 Optional Domain Context Sections

### Derivative Context
Suggested fields:
- currentOi
- oiVsAverage
- fundingRate
- fundingVsAverage
- leverageInterpretation

### On-chain Context
Suggested fields:
- keyMetricName
- currentValue
- vsReferenceSummary
- interpretationHint

### Sentiment Context
Suggested fields:
- currentFearGreed
- vsAverage
- sentimentRegime
- interpretationHint

### Macro Context
Suggested fields:
- dxyChange
- nasdaqChange
- us10yChange
- usdkrwChange
- macroPressureSummary

### News Context
Suggested fields:
- headlineSummary
- eventType
- relevance
- interpretationHint

---

## 5. GPT Usage Rule
GPT should receive:
- structured facts
- comparison facts
- candidate levels
- scenario inputs
- risk inputs

GPT should not be asked to infer everything from raw bulk data.

---

## 6. Evolution Rule
The report payload can evolve over time, but:
- do not break traceability metadata
- do not collapse everything into one prose field
- preserve machine-readable comparison sections
- preserve compatibility for API serving where practical

---

## 7. Anti-Patterns
Do not:
- store only a free-form paragraph and lose structured facts
- duplicate all raw history in the report payload
- let report structure depend entirely on GPT output shape
- remove comparisonFacts in favor of only narrative text