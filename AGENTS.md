# AI Coin Assist - Project Instructions for Codex

## Project Overview
This repository is the batch backend for an AI-based crypto market analysis assistant built with Java and Spring Boot.

This service is **not** an auto-trading system.
It is a **market interpretation and decision-support system**.

Initial MVP assets:
- BTC
- ETH
- XRP

Current architecture direction:
- Batch server
- API server
- MySQL
- Redis
- GPT for final interpretation only

---

## Core Service Principle
The reliability of this service depends more on the **raw data layer** than on GPT.

Always treat the ingestion layer as a **core production-grade foundation**, not as an MVP-only temporary implementation.

If raw data quality is unstable, then:
- indicator calculations become unreliable
- candidate level extraction becomes unreliable
- GPT report quality becomes unreliable
- Redis/API outputs become unreliable

Prioritize:
- correctness
- precision
- reproducibility
- traceability
- validation

---

## Data Pipeline Principle
The intended pipeline is:

1. Raw data ingestion
2. Server-side normalization / structuring
3. Server-side comparison calculation
4. GPT interpretation and report generation
5. Redis caching
6. API serving

Do **not** send raw datasets directly to GPT.

The server must first compute and structure:
- changes versus previous reference points
- relative position
- market structure context
- comparison metrics by time horizon

GPT is responsible for:
- integrated interpretation
- scenario writing
- support/resistance explanation
- risk explanation
- final report generation

GPT is **not** the primary calculation engine.

---

## Comparison-First Analysis Principle
Before sending data to GPT, the server must calculate comparison-based facts.

### Short-term comparison references
Use:
- PREV_BATCH
- D1
- D3
- D7

Focus on:
- current move strength
- short-term trend continuation vs slowdown
- reactions near key support/resistance
- short-term scenario building

### Mid-term comparison references
Use:
- D7
- D14
- D30
- PREV_MID_REPORT

Focus on:
- structure holding vs breaking
- weekly/daily trend direction
- medium-term key support/resistance
- multi-week interpretation

### Long-term comparison references
Use:
- D30
- D90
- D180
- Y52_HIGH_LOW
- PREV_LONG_REPORT

Focus on:
- long-term trend / cycle position
- long-term support/resistance
- long-term strength and risk

### Server output for GPT must include structured facts
Do not reduce data to vague labels only.

Prefer structured values such as:
- current price
- 24h / 7d / 30d change rate
- RSI current value and delta vs prior points
- MACD histogram current value and delta
- ATR current value vs recent average
- price relative to MA20 / MA60 / MA120
- recent pivot high / pivot low
- candidate support / resistance
- current volume vs average volume
- OI change
- funding vs recent average
- Fear & Greed change
- DXY / Nasdaq / USDKRW change
- recent relevant news/event summary

The server should provide:
- numeric values
- deltas
- relative position
- structure-related facts

The server should **not** jump too early to human-language conclusions.

---

## Persistence Model Principle
Do **not** store everything in one giant snapshot table.

Use layered persistence:

### 1. Raw layer
Store raw external data by domain.

Examples:
- market_price_raw
- market_candle_raw
- derivative_snapshot_raw
- onchain_snapshot_raw
- macro_snapshot_raw
- fx_snapshot_raw

Raw data must preserve:
- original source
- asset / symbol
- interval if applicable
- source event time
- collected time
- metadata
- validation status
- raw payload

### 2. Processed layer
Store server-calculated / structured outputs separately.

Examples:
- technical_indicator_snapshot
- candidate_level_snapshot
- market_context_snapshot

### 3. Report layer
Store final user-facing analysis output separately.

Example:
- analysis_report

Report storage should use:
- important query/filter columns
- JSON payload for detailed or evolving structure
- reference metadata such as:
    - analysis_basis_time
    - raw_reference_time
    - source_data_version
    - analysis_engine_version

Do not duplicate all raw data into report records.

---

## Raw Data Ingestion Rules
External API responses must be handled with production-grade care.

Always prefer:
- preserving raw response values with minimal loss
- explicit metadata management
- clear source tracking
- exact timestamp handling
- defensive validation

Watch for:
- null values
- missing fields
- duplicates
- reverse ordering
- timestamp mismatch
- interval mismatch
- symbol mismatch
- invalid OHLC relationships
- abnormal negative values

Keep raw storage and processed storage separate.

Support:
- re-ingestion
- reprocessing
- validation
- auditability

---

## Precision Rules
Financial and market calculations must be handled carefully.

Rules:
- prefer `BigDecimal` for financial calculations
- avoid unnecessary `double` usage
- do not silently degrade precision during DTO conversion
- be explicit about scale and rounding mode
- review DB precision/scale definitions carefully

Precision issues are high priority.

Indicator implementation rules:
- RSI uses Wilder smoothing
- ATR uses Wilder smoothing
- Bollinger Bands should minimize floating-point precision loss
- calculation definition correctness matters as much as code execution

---

## Time Handling Rules
Time handling must be explicit and reproducible.

Prefer clearly separated concepts such as:
- source event time
- candle open time
- candle close time
- collected time
- processed time
- stored time

Prefer consistent internal handling using `Instant` where appropriate.
Do not mix the business meaning of timestamps.

---

## Current Technical Direction
Current stack and direction:
- Java
- Spring Boot
- Scheduler-based batch processing
- MySQL
- Redis
- Binance as an initial source

Technical indicators currently include:
- MA
- RSI
- MACD
- ATR
- Bollinger Bands

---

## Code Organization Direction
When refactoring or adding code, prefer separation of responsibilities:

- external raw response DTOs
- normalized internal DTOs
- processed/calculated DTOs
- raw entities
- processed entities
- report entities
- validators
- persistence services
- calculation services

Avoid mixing:
- raw response mapping
- calculation logic
- persistence logic
- reporting logic

inside one class.

---

## Development Workflow
Git strategy:
- main / dev / feature/*

Workflow:
- work on feature branch
- PR
- merge into dev

Even for solo development, preserve PR-level history and reasoning.

When summarizing work, prefer this order:
1. PR title / PR body
2. next branch name
3. commit message if needed

---

## Communication / Coding Preferences
The project owner prefers:
- practical backend-oriented decisions
- not over-engineering, but not using MVP as an excuse for unreliable foundations
- careful handling of precision and calculation definitions
- maintainable structure over short-term convenience

When suggesting implementation changes:
- first identify what should be fixed in existing code
- then propose storage / structure changes
- keep refactoring steps explicit and incremental

---

## Anti-Patterns to Avoid
Do not:
- send raw bulk datasets directly to GPT
- store all analysis data in one giant nullable table
- convert precise numeric API values through `double` unnecessarily
- hide time semantics
- skip validation because "it works for MVP"
- couple raw ingestion too tightly with processed snapshot generation
- collapse raw / processed / report into one model

---

## Default Implementation Mindset
For this repository, always think:

"Can this raw data be trusted, rechecked, reprocessed, and traced later?"

If the answer is no, improve the design before adding more features.