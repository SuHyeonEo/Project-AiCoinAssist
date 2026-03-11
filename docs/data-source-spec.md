# Data Source Specification

## 1. Purpose
This document defines the intended data domains, source categories, and ingestion expectations for the AI Coin Assist batch backend.

The goal is to ensure that the service does not rely only on chart candles and technical indicators.

The system should integrate:
- market data
- technical indicators
- derivative data
- on-chain data
- sentiment data
- macro / external context data

---

## 2. Core Principles

### 2.1 Raw-first reliability
External source data must first be ingested and preserved in raw form with enough metadata for:
- re-ingestion
- reprocessing
- validation
- auditability
- traceability

### 2.2 Source semantics matter
Each source may define time differently.
Preserve the original business meaning of:
- source event time
- candle open/close time
- collected time

Do not flatten unrelated time semantics into a fake common meaning.

### 2.3 Minimal-loss storage
Preserve source values with minimal precision loss.
Prefer exact textual / numeric preservation where practical.

---

## 3. Data Domains

## 3.1 Market / Price Data
Purpose:
- core market state
- base for technical calculations
- support/resistance extraction
- anchor/window comparison inputs

Typical fields:
- symbol
- price
- trade time / source event time
- OHLCV
- turnover
- interval
- open time
- close time

Possible initial source:
- Binance spot market endpoints

Expected raw tables:
- market_price_raw
- market_candle_raw

---

## 3.2 Technical Indicator Data
Purpose:
- processed analytical interpretation inputs
- trend / momentum / volatility structure

Typical indicators:
- MA
- RSI
- MACD
- ATR
- Bollinger Bands

These are typically server-calculated from validated market/candle data.

Expected processed tables:
- technical_indicator_snapshot

---

## 3.3 Derivative / Leverage Data
Purpose:
- leverage buildup detection
- risk regime interpretation
- confirmation / divergence relative to price movement

Typical fields:
- Open Interest
- funding rate
- long/short ratio if available
- liquidation-related signals if available

Possible initial source:
- Binance futures / derivatives endpoints

Expected raw tables:
- derivative_snapshot_raw

Expected processed outputs:
- derivative_fact_snapshot

---

## 3.4 On-Chain Data
Purpose:
- structural market state beyond exchange price alone
- long-horizon confirmation / warning signals

Possible metrics:
- realized price
- MVRV
- exchange netflow
- SOPR
- other structured on-chain factors if later added

Possible source categories:
- blockchain analytics APIs
- public on-chain metric providers

Expected raw tables:
- onchain_snapshot_raw

Expected processed outputs:
- onchain_fact_snapshot

Note:
On-chain source expansion may happen after core market / derivative foundation is stable.

---

## 3.5 Sentiment / Market Psychology Data
Purpose:
- crowd-state and risk appetite context
- contrarian or continuation signals
- report explanation support

Possible metrics:
- Fear & Greed Index
- structured sentiment score if later added
- event/news summary inputs

Possible source categories:
- sentiment index providers
- curated news/event inputs

Expected raw tables:
- sentiment_snapshot_raw
- news_signal_raw if added later

Expected processed outputs:
- sentiment_snapshot

---

## 3.6 Macro / External Context Data
Purpose:
- cross-market pressure context
- crypto-external environment interpretation
- long-term and risk-sensitive explanation support

Possible metrics:
- DXY
- Nasdaq
- US10Y yield
- USDKRW
- ETF flow
- oil or other macro series if later added

Possible source categories:
- macro data APIs
- market index APIs
- FX data APIs

Expected raw tables:
- macro_snapshot_raw
- fx_snapshot_raw

Expected processed outputs:
- macro_context_snapshot

---

## 4. Ingestion Expectations by Domain

### 4.1 Market data
Must support:
- validation
- dedupe / idempotency
- interval consistency
- symbol consistency
- source event tracking

### 4.2 Derivative data
Must support:
- source timestamp preservation
- symbol / contract consistency
- interpretation of missing vs zero
- comparison against recent averages

### 4.3 On-chain data
Must support:
- metric definition clarity
- source frequency awareness
- source timestamp semantics
- slower-moving refresh models where needed

### 4.4 Sentiment data
Must support:
- index timestamp preservation
- clear distinction between point score and narrative summary
- comparison against recent average or regime

### 4.5 Macro data
Must support:
- source calendar awareness
- release / close-time semantics
- cross-domain comparison safety

---

## 5. Validation Rules
All raw source ingestion should watch for:
- null values
- missing fields
- duplicate records
- reverse ordering
- timestamp mismatch
- interval mismatch
- symbol mismatch
- abnormal negative values
- unsupported domain semantics
- unexpected source payload changes

The validation layer should remain explicit and traceable.

---

## 6. Precision Rules by Domain
Use `BigDecimal` where precision matters.

Especially important for:
- price
- funding rate
- OI values
- on-chain ratios
- macro percentage changes
- FX values

Do not silently degrade precision through unnecessary `double` conversion.

---

## 7. Initial MVP Prioritization
Recommended priority order:

### Phase 1
- market price / candle ingestion
- server-calculated technical indicators

### Phase 2
- derivative data such as OI / funding

### Phase 3
- sentiment data such as Fear & Greed
- macro context such as DXY / Nasdaq / USDKRW

### Phase 4
- deeper on-chain factors
- richer event/news inputs
- broader external context

This order may change if implementation constraints require it, but market raw quality remains the top priority.

---

## 8. Anti-Patterns
Do not:
- assume technical indicators alone are enough
- skip metadata because the source looks simple
- flatten all sources into one generic ingestion model without semantic care
- use report generation needs as a reason to weaken raw traceability
- mix raw source preservation with user-facing interpretation logic