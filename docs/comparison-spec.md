# Comparison Specification

## 1. Purpose
This document defines how the server should compare market states before generating reports.

The goal is:
- not to compare only the current value
- not to compare only one previous value
- not to send long raw history directly to GPT
- but to generate structured comparison facts from representative values

Comparison must happen **server-side first**.

---

## 2. Core Principles

### 2.1 Primary comparison source
Use processed snapshots / structured facts as the primary source of truth.

Do not use previous report payload as the primary quantitative comparison source.

### 2.2 Previous report usage
Previous reports are allowed only for:
- narrative continuity
- scenario continuation / invalidation checks
- risk emphasis continuity

Previous reports are **not** the main numeric comparison source.

### 2.3 Two representative comparison types
Each horizon should use two kinds of representative values.

#### A. Anchor representative values
A representative snapshot at a specific reference time.

Examples:
- current vs PREV_BATCH
- current vs D1
- current vs D7
- current vs D30
- current vs D90
- current vs D180

Use anchor values for:
- point-in-time delta
- change rate
- prior-state comparison

#### B. Window summary representative values
A structured summary of the whole comparison window.

Examples:
- 7d high / low
- 30d high / low
- 90d high / low
- average volume
- average ATR
- average OI / funding / sentiment
- current position inside the range
- distance from range high
- rebound from range low

Use window summaries for:
- structure interpretation
- range position
- volatility regime
- average-vs-current comparison

### 2.4 Structural relative position
Comparison should also include structural location such as:
- current position inside recent range
- current price relative to MA cluster
- distance from 52w high / low
- support / resistance neighborhood
- volatility expansion / contraction regime

### 2.5 Horizon comparison rule
Do not compare only one previous value and stop there.

Each horizon should combine:
- anchor comparison
- window summary comparison
- structural relative position

Examples:
- short-term: PREV_BATCH + D1 / D3 / D7 + short-window summaries
- mid-term: D7 / D14 / D30 + 7d / 14d / 30d window summaries
- long-term: D30 / D90 / D180 + 30d / 90d / 180d / 52w window summaries + 52w range position

---

## 3. Reference Resolution Rules

### 3.1 Analysis basis time
All references are resolved from the current `analysisBasisTime`.

### 3.2 Reference target time
When resolving D1 / D7 / D30 / D90 / D180:
- define reference target time from the current analysis basis time
- fetch the latest valid snapshot at or before that time

### 3.3 Exact equality is not required
Do not require exact timestamp equality if scheduler execution time shifts slightly.

### 3.4 PREV_BATCH
`PREV_BATCH` means the most recent valid snapshot/report basis before the current analysis basis time within the same comparison scope.

### 3.5 Previous report references
- `PREV_SHORT_REPORT`
- `PREV_MID_REPORT`
- `PREV_LONG_REPORT`

These are used only for continuity of interpretation, not as the primary numeric comparison source.

### 3.6 Consistent time semantics
The comparison service must preserve business meaning of timestamps.
Do not compare unrelated timestamps as if they mean the same thing.

---

## 4. Representative Value Derivation Rules

## 4.1 Why representative values are needed
The system must **not** compare by sending all raw data points from the last 7/30/90/180 days into GPT or directly into report logic.

Instead, the server must derive representative values.

The purpose is:
- preserve raw history for traceability
- keep comparison logic server-side
- reduce noise
- keep report inputs compact and structured

### 4.2 Representative value categories
There are two representative value categories:
1. anchor representative values
2. window summary representative values

### 4.3 Input source rule
Representative values should be derived from:
- validated processed snapshots, or
- validated structured fact snapshots by domain

Do **not** use raw payload blobs directly as the normal comparison input unless no processed form exists yet.

Raw data remains the reprocessing and audit source, not the default comparison source.

### 4.4 Validity rule
Only valid snapshots/facts should be used for representative value derivation.

Exclude:
- invalid snapshots
- corrupted records
- mismatched symbol/interval/domain records
- records that fail semantic validation

### 4.5 Anchor derivation rule
Anchor representative values are derived as:
- define reference target time
- fetch the nearest valid snapshot at or before that time
- use that snapshot as the anchor comparison basis

Examples:
- D30 anchor
- D90 anchor
- D180 anchor

### 4.6 Window derivation rule
Window summary representative values are derived from the full time window between:
- `windowStartTime`
- `windowEndTime = analysisBasisTime`

Examples:
- LAST_7D
- LAST_30D
- LAST_90D
- LAST_180D
- LAST_52W

The server uses valid snapshots/facts in that window and calculates compact summary metrics.

### 4.7 Minimum sample rule
A window summary should only be generated when there are enough valid snapshots/facts to consider the summary meaningful.

If the sample count is too low:
- return partial summary only if safe
- otherwise mark the summary as unavailable

Do not silently fabricate a full summary from insufficient data.

### 4.8 Fallback rule
If a representative value cannot be derived safely:
- prefer explicit unavailability / null / omitted field
- do not substitute arbitrary values
- do not force a human-language conclusion from missing basis data

---

## 5. Window Summary Derivation Rules

## 5.1 Purpose
Window summaries exist to describe the structure of a period, not to dump all historical rows.

A window summary should answer:
- where is the current value inside the window?
- what was the range of the window?
- how does the current state compare with the average condition of the window?
- was the window volatile, compressed, recovering, or weakening?

## 5.2 Required window boundaries
Each window summary should include:
- `windowStartTime`
- `windowEndTime`
- `sampleCount`

## 5.3 Price summary fields
Where applicable, derive:
- `windowHigh`
- `windowLow`
- `windowRange`
- `currentPositionInRange`
- `distanceFromWindowHigh`
- `reboundFromWindowLow`

Suggested formulas:

### Window high
- maximum price inside the valid window

### Window low
- minimum price inside the valid window

### Window range
- `windowHigh - windowLow`

### Current position in range
- `(currentPrice - windowLow) / (windowHigh - windowLow)`

Notes:
- if `windowHigh == windowLow`, do not divide by zero
- return null or a safe neutral handling policy

### Distance from window high
- `(windowHigh - currentPrice) / windowHigh`

### Rebound from window low
- `(currentPrice - windowLow) / windowLow`

If denominator is zero or invalid, use explicit null / unavailable handling.

## 5.4 Average-based summary fields
Where applicable, derive:
- `averageVolume`
- `averageAtr`
- `averageOi`
- `averageFunding`
- `averageFearGreed`
- `averageMetricValue` for domain-specific series

The default average rule should be:
- mean of valid snapshots/facts in the window

If a metric later requires weighted logic, document that explicitly before changing behavior.

## 5.5 Current-vs-average comparison fields
Where applicable, derive:
- `currentVolumeVsAverage`
- `currentAtrVsAverage`
- `currentOiVsAverage`
- `currentFundingVsAverage`
- `currentFearGreedVsAverage`

Suggested formula:
- `(currentValue - averageValue) / averageValue`

If average is zero or unavailable:
- return null / unavailable
- do not silently force a ratio

## 5.6 Optional advanced fields
If useful and safely derivable, window summaries may also include:
- `maxDrawdown`
- `maxRecovery`
- `rangeCompression`
- `rangeExpansion`
- `windowTrendSlope`
- `countOfRangeHighBreaks`
- `countOfRangeLowBreaks`

These are optional and can be added later.

---

## 6. Metric Usage Rules by Domain

## 6.1 Price
Use:
- anchor comparison
- window range summary
- current position in range
- distance from recent highs/lows

Price must support both point-in-time comparison and window summary interpretation.

## 6.2 RSI
Use primarily:
- anchor comparison
- current absolute level
- delta vs prior references

Optional:
- window average only if it adds interpretive value

Do not overcomplicate RSI window summarization in early versions.

## 6.3 MACD Histogram
Use primarily:
- anchor comparison
- expansion / contraction relative to prior anchors

Optional:
- short/mid window behavior summaries if helpful

MACD histogram is more useful as a directional delta series than as a heavy average-based window metric in early versions.

## 6.4 ATR
Use:
- anchor comparison
- average ATR over the window
- current ATR vs window average

ATR is one of the main volatility regime metrics and should support window summarization.

## 6.5 Volume
Use:
- anchor comparison if needed
- window average comparison
- current volume vs window average

Volume should strongly support average-vs-current analysis.

## 6.6 OI
Use:
- current vs anchor
- current vs window average
- trend context if useful

OI is particularly useful when compared with recent average and price behavior together.

## 6.7 Funding
Use:
- current vs anchor
- current vs window average
- sign and magnitude context

Funding interpretation should consider both absolute level and relative deviation from recent norm.

## 6.8 Fear & Greed / sentiment
Use:
- current vs anchor
- current vs window average
- regime labeling if helpful

Sentiment data is usually more meaningful when treated as regime/context rather than only as a point delta.

## 6.9 Macro / external context
Use:
- anchor comparison
- recent average/regime comparison where appropriate
- structural context, not only single-point movement

Examples:
- DXY
- Nasdaq
- USDKRW
- US10Y

---

## 7. Short-Term Comparison

### 7.1 Purpose
Short-term analysis focuses on:
- current move strength
- short-term trend continuation vs slowdown
- reaction near support / resistance
- immediate scenario building

### 7.2 Quantitative anchor references
- PREV_BATCH
- D1
- D3
- D7

### 7.3 Narrative continuity reference
- PREV_SHORT_REPORT

### 7.4 Short-term window summaries
- LAST_1D summary
- LAST_3D summary
- LAST_7D summary

### 7.5 Key metrics
Use at least:
- price
- RSI
- MACD histogram
- ATR
- volume

Use if available:
- OI
- funding
- Fear & Greed
- short-term macro/event changes

### 7.6 Short-term summary facts
Examples:
- price change vs PREV_BATCH / D1 / D3 / D7
- RSI delta vs D1 / D3
- MACD histogram expansion / contraction
- ATR vs 7d average ATR
- current volume vs 7d average volume
- current price position inside recent 7d range

### 7.7 Short-term highlight priority
Recommended priority:
1. PREV_BATCH
2. D1
3. D3
4. D7

PREV_SHORT_REPORT is used only for narrative continuity.

---

## 8. Mid-Term Comparison

### 8.1 Purpose
Mid-term analysis focuses on:
- multi-day / multi-week trend direction
- structure holding vs breaking
- medium-term support / resistance interpretation
- noise reduction relative to short-term movement

### 8.2 Quantitative anchor references
- D7
- D14
- D30

### 8.3 Narrative continuity reference
- PREV_MID_REPORT

### 8.4 Mid-term window summaries
- LAST_7D summary
- LAST_14D summary
- LAST_30D summary

### 8.5 Key metrics
Use at least:
- price
- RSI
- MACD histogram
- ATR
- volume

Use if available:
- OI
- funding
- Fear & Greed
- on-chain factors
- macro factors

### 8.6 Mid-term summary facts
Examples:
- price change vs D7 / D14 / D30
- RSI delta vs D7 / D14 / D30
- MACD histogram improvement / weakening over 30d
- ATR vs 30d average ATR
- current volume vs 30d average volume
- current OI / funding vs 30d average
- current Fear & Greed vs recent average
- current price position inside 30d range

### 8.7 Mid-term highlight priority
Recommended priority:
1. PREV_MID_REPORT
2. D7
3. D30
4. D14

PREV_MID_REPORT must remain a continuity reference, not a quantitative source of truth.

---

## 9. Long-Term Comparison

### 9.1 Purpose
Long-term analysis focuses on:
- long-term trend / cycle position
- major support / resistance structure
- strength / weakness over broader windows
- macro and structural risk

### 9.2 Quantitative anchor references
- D30
- D90
- D180
- Y52_HIGH
- Y52_LOW

### 9.3 Narrative continuity reference
- PREV_LONG_REPORT

### 9.4 Long-term window summaries
- LAST_30D summary
- LAST_90D summary
- LAST_180D summary
- LAST_52W summary

### 9.5 Key metrics
Use at least:
- price
- RSI
- MACD histogram
- ATR
- volume

Use if available:
- OI
- funding
- Fear & Greed
- on-chain factors
- DXY / Nasdaq / USDKRW / US10Y
- ETF flow or other macro context

### 9.6 Long-term summary facts
Examples:
- price change vs D30 / D90 / D180
- current distance from Y52 high / low
- current position inside 90d / 180d / 52w range
- ATR vs 90d average ATR
- volume vs 90d average volume
- OI / funding vs long-window averages
- sentiment vs long-window averages
- macro context relative to broader baseline

### 9.7 Long-term highlight priority
Recommended priority:
1. Y52_HIGH
2. Y52_LOW
3. PREV_LONG_REPORT
4. D180
5. D90
6. D30

PREV_LONG_REPORT is for continuity, not numeric source-of-truth comparison.

---

## 10. Minimum Window Summary Fields

Each window summary should support at least the following fields where applicable:
- windowStartTime
- windowEndTime
- sampleCount
- high
- low
- range
- currentPositionInRange
- averageVolume
- averageAtr

Optional but recommended:
- averageOi
- averageFunding
- averageFearGreed
- averageOnchainMetric
- distanceFromWindowHigh
- reboundFromWindowLow
- maxDrawdown

---

## 11. Comparison Fact Design

Comparison facts should be structured, not vague prose.

Recommended fields:
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

---

## 12. Comparison Highlight Design

Do not use all comparison facts directly as user-facing emphasis.

Keep:
- `comparisonFacts` for structured raw comparison outputs
- `comparisonHighlights` for distilled report emphasis

Highlight extraction may prioritize:
- large meaningful deltas
- threshold crossings
- structure breaks or recoveries
- alignment across multiple metrics
- invalidation or reinforcement of previous scenario narratives

---

## 13. Anti-Patterns
Do not:
- compare only one previous value and stop there
- use previous reports as the main quantitative source
- send raw 30d / 90d / 180d data directly to GPT for reasoning
- skip window summaries for mid-term or long-term interpretation
- collapse anchor comparison and narrative continuity into the same concept
- derive representative values from invalid or semantically broken records
- silently invent full window summaries from insufficient sample counts
- leave formulas undefined when the metric is intended to be used repeatedly across the system