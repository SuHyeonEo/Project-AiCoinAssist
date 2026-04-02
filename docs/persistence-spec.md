# Persistence Specification

## 1. Purpose
This document defines the intended persistence layering and storage principles for the AI Coin Assist batch backend.

The system should not collapse all data into one table.
Persistence must preserve:
- raw traceability
- processed reproducibility
- report-level usability

---

## 2. Core Principles

### 2.1 Layered persistence
Use three distinct persistence layers:
1. raw layer
2. processed layer
3. report layer

### 2.2 Separation of concerns
Do not mix:
- raw source preservation
- server-calculated facts
- final user-facing interpretation

inside one storage model.

### 2.3 Traceability first
Stored data should support:
- re-ingestion
- reprocessing
- auditability
- source tracking
- version-aware comparison

### 2.4 Time semantics first
Storage must preserve the meaning of each timestamp.
Do not flatten all times into one generic stored-at field.

---

## 3. Raw Layer

## 3.1 Purpose
The raw layer preserves external source data with enough metadata for future validation and reprocessing.

### 3.2 Examples
Examples may include:
- market_price_raw
- market_candle_raw
- derivative_snapshot_raw
- onchain_snapshot_raw
- sentiment_snapshot_raw
- macro_snapshot_raw
- fx_snapshot_raw
- news_signal_raw

### 3.3 Raw storage expectations
Raw storage should preserve:
- original source
- asset / symbol
- interval if applicable
- source event time
- collected time
- metadata
- validation status
- raw payload

### 3.4 Raw layer rules
The raw layer should support:
- explicit validation status
- dedupe / idempotency
- auditability
- later reprocessing

Do not over-normalize raw storage so aggressively that original source meaning is lost.

---

## 4. Processed Layer

## 4.1 Purpose
The processed layer stores server-calculated and normalized analytical data derived from raw sources.

### 4.2 Examples
Examples may include:
- technical_indicator_snapshot
- derivative_fact_snapshot
- onchain_fact_snapshot
- sentiment_snapshot
- macro_context_snapshot
- candidate_level_snapshot
- market_context_snapshot
- window_summary_snapshot

### 4.3 Processed storage expectations
Processed data should preserve enough metadata for:
- analysis reconstruction
- comparison reuse
- source linkage
- version-aware recalculation

### 4.4 Snapshot metadata expectations
Where appropriate, processed snapshots should carry metadata such as:
- snapshotTime
- latestCandleOpenTime
- sourceEventTime or domain equivalent
- sourceDataVersion

### 4.5 Processed layer rules
Processed storage must remain separate from raw entities and report entities.
Do not store final prose interpretation in processed snapshots.

---

## 5. Report Layer

## 5.1 Purpose
The report layer stores final user-facing analysis results and structured report payloads.

### 5.2 Example
- analysis_report

### 5.3 Report metadata expectations
Report records should preserve:
- symbol
- reportType
- analysisBasisTime
- rawReferenceTime
- sourceDataVersion
- analysisEngineVersion
- storedTime
- payload

### 5.4 Report layer rules
Report storage should:
- support API serving
- support version-aware regeneration
- keep structured JSON payload
- avoid duplicating full raw history

Do not use report records as the main source of truth for numeric comparison.

---

## 6. Idempotency and Uniqueness

### 6.1 Raw layer
Raw tables should define dedupe / uniqueness rules appropriate for each domain.

Examples:
- market candle uniqueness by source + symbol + interval + open time
- market price uniqueness by source + symbol + source event time

### 6.2 Processed layer
Processed snapshots should define uniqueness by appropriate basis, such as:
- symbol
- interval
- snapshot time
- source data version where needed

### 6.3 Report layer
Reports should define uniqueness by analysis basis and engine version rules, such as:
- symbol
- report type
- analysis basis time
- source data version
- analysis engine version

---

## 7. JSON vs Column Split

### 7.1 Use columns for
- key query fields
- filtering fields
- uniqueness metadata
- time/version tracing fields

### 7.2 Use JSON payload for
- evolving detail structure
- report content
- step-level structured details
- complex comparison outputs

Do not push every query-relevant field into opaque JSON if it should be indexed or filtered frequently.

---

## 8. Reprocessing and Reconstruction
Persistence design should support:
- rerunning calculations from stored raw data
- rebuilding processed facts
- regenerating reports with newer logic versions
- tracing which source basis produced a given output

This requirement is fundamental, not optional.

---

## 9. Anti-Patterns
Do not:
- collapse raw / processed / report into one giant nullable table
- discard original source payload too early
- store only final prose and lose structured intermediate facts
- rely on report tables as the only historical comparison source
- weaken timestamp semantics for implementation convenience