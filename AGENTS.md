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
- comparison facts become unreliable
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

## Data Scope Principle
This project must analyze more than chart candles and technical indicators.

The intended market interpretation requires multiple fact domains, including:
- market / price data
- technical indicator data
- derivative / leverage data
- on-chain data
- sentiment / market psychology data
- macro / external context data

Technical indicators alone are **not** sufficient for the intended market analysis quality.

Detailed data-source rules are documented in `docs/data-source-spec.md`.

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
- derivative / on-chain / sentiment / macro supporting facts

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

Use **processed snapshots / structured facts** as the primary comparison source.

Use previous reports only as **narrative continuity references**, not as the main numeric source of truth.

Do **not** send full raw 7d / 30d / 90d / 180d history directly to GPT.

The system must compare:
- anchor reference values
- window summary representative values
- structural relative position

Detailed comparison rules are documented in `docs/comparison-spec.md`.

---

## Persistence Model Principle
Do **not** store everything in one giant snapshot table.

Use layered persistence:
- raw layer
- processed layer
- report layer

Keep raw / processed / report clearly separated.

Detailed persistence rules are documented in `docs/persistence-spec.md`.

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
- unsupported or inconsistent source semantics across domains

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

The same precision mindset also applies to:
- funding calculations
- OI delta / change-rate calculations
- on-chain ratios
- macro percentage change calculations
- window summary metric calculations

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
- analysis basis time
- reference target time

Prefer consistent internal handling using `Instant` where appropriate.
Do not mix the business meaning of timestamps.

When comparing data from different domains, preserve the time semantics of each source instead of forcing unrelated timestamps into a fake common meaning.

---

## Current Technical Direction
Current stack and direction:
- Java
- Spring Boot
- Scheduler-based batch processing
- MySQL
- Redis
- Binance as an initial source

Current implemented technical indicators:
- MA
- RSI
- MACD
- ATR
- Bollinger Bands

Expected next fact domains beyond technical indicators:
- derivative data such as OI / funding
- on-chain supporting facts
- sentiment data such as Fear & Greed
- macro/external context such as DXY / Nasdaq / USDKRW
- news/event explanation inputs if added

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
- comparison policy / reference resolution services
- window summary calculation services

Avoid mixing:
- raw response mapping
- calculation logic
- persistence logic
- comparison policy logic
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

Prefer:
- incremental extension on top of existing foundation
- traceable storage changes
- explicit comparison rules
- explicit timestamp semantics
- server-first fact calculation before GPT usage

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
- use previous reports as the primary quantitative comparison source
- rely on a single previous value as the only comparison basis
- treat technical indicators as the only meaningful data domain
- send the full raw 30d / 90d / 180d dataset directly into the report layer or GPT prompt when representative summary facts are sufficient

---

## Default Implementation Mindset
For this repository, always think:

"Can this raw data be trusted, rechecked, reprocessed, and traced later?"
"Are comparison facts being generated from structured data rather than from previous prose?"
"Am I comparing representative facts instead of dumping full raw history into the interpretation layer?"
"Does this change preserve precision, time semantics, and reprocessing ability?"

If the answer is no, improve the design before adding more features.