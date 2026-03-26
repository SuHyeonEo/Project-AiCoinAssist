package com.aicoinassist.batch.domain.report.service;

import com.aicoinassist.batch.domain.market.enumtype.MarketWindowType;
import com.aicoinassist.batch.domain.report.enumtype.AnalysisPriceLevelSourceType;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

class AnalysisTextLocalizationSupport {

    private static final int DISPLAY_SCALE = 2;
    private static final Pattern DECIMAL_NUMBER_PATTERN = Pattern.compile("(?<![A-Za-z0-9_])([+-]?\\d+\\.\\d{3,})(?![A-Za-z0-9_])");

    private static final Pattern PREVIOUS_REPORT_PATTERN = Pattern.compile(
            "^Previous (short-term|mid-term|long-term) report (highlighted|emphasized) (.+?)\\.?$",
            Pattern.CASE_INSENSITIVE
    );
    private static final Pattern CONFIRMS_IMPULSE_PATTERN = Pattern.compile(
            "^([A-Z0-9_]+) confirms the latest impulse with (.+?)\\.?$"
    );
    private static final Pattern RANGE_POSITION_PATTERN = Pattern.compile(
            "^([A-Z0-9_]+) (?:keeps price at|price is at) (.+?) of the range(?: with (.+?))?\\.?$"
    );
    private static final Pattern RANGE_PATTERN = Pattern.compile(
            "^([A-Z0-9_]+) range ([^ ]+) to ([^.]+)\\.?$"
    );
    private static final Pattern DERIVATIVE_KEEPS_PATTERN = Pattern.compile(
            "^([A-Z0-9_]+) keeps OI ([^,]+), funding (?:Δ|delta) ([^,]+), basis (?:Δ|delta) (.+?)\\.?$"
    );
    private static final Pattern MACRO_KEEPS_PATTERN = Pattern.compile(
            "^([A-Z0-9_]+) keeps DXY(?: proxy)? ([^,]+), US10Y ([^,]+), USD/KRW (.+?)\\.?$"
    );
    private static final Pattern MACRO_SINGLE_PATTERN = Pattern.compile(
            "^([A-Z0-9_]+) keeps DXY(?: proxy)? (.+?)\\.?$"
    );
    private static final Pattern MACRO_CHANGES_PATTERN = Pattern.compile(
            "^([A-Z0-9_]+) changes DXY ([^,]+), US10Y ([^,]+), USD/KRW (.+?)\\.?$"
    );
    private static final Pattern SENTIMENT_KEEPS_PATTERN = Pattern.compile(
            "^([A-Z0-9_]+) keeps Fear & Greed ([^,]+), greed samples ([0-9]+)/([0-9]+)\\.?$"
    );
    private static final Pattern SENTIMENT_CHANGE_PATTERN = Pattern.compile(
            "^([A-Z0-9_]+) changes Fear & Greed by ([^ ]+) \\(([^)]+)\\)(?: with classification switching from (.+?) to (.+?)| while classification stays (.+?))\\.?$",
            Pattern.CASE_INSENSITIVE
    );
    private static final Pattern ONCHAIN_KEEPS_PATTERN = Pattern.compile(
            "^([A-Z0-9_]+) keeps active addresses ([^,]+), transactions ([^,]+), market cap (.+?)\\.?$"
    );
    private static final Pattern ONCHAIN_MARKET_CAP_PATTERN = Pattern.compile(
            "^On-chain market cap sits at ([^,]+), which keeps the asset in a large-cap network regime\\.?$",
            Pattern.CASE_INSENSITIVE
    );
    private static final Pattern EXTERNAL_DOMINANCE_PATTERN = Pattern.compile(
            "^([A-Z0-9_]+) keeps (.+?) dominance for (.+?) of samples with high severity on (.+?) of observations\\.?$",
            Pattern.CASE_INSENSITIVE
    );
    private static final Pattern EXTERNAL_SHIFT_PATTERN = Pattern.compile(
            "^([A-Z0-9_]+) external regime direction shifted to headwind\\.?$",
            Pattern.CASE_INSENSITIVE
    );
    private static final Pattern EXTERNAL_TRANSITION_PATTERN = Pattern.compile(
            "^([A-Z0-9_]+) external regime transitioned to headwind\\.?$",
            Pattern.CASE_INSENSITIVE
    );
    private static final Pattern FEAR_GREED_IS_AT_PATTERN = Pattern.compile(
            "^Fear & Greed is at ([^ ]+) \\(([^)]+)\\), (.+?)\\.?$",
            Pattern.CASE_INSENSITIVE
    );
    private static final Pattern FEAR_GREED_CLASSIFICATION_PATTERN = Pattern.compile(
            "^Fear & Greed classification is (.+?)\\.?$",
            Pattern.CASE_INSENSITIVE
    );
    private static final Pattern FEAR_GREED_CLASSIFIED_AS_PATTERN = Pattern.compile(
            "^Fear & Greed is at ([^ ]+) and classified as (.+?)\\.?$",
            Pattern.CASE_INSENSITIVE
    );
    private static final Pattern CURRENT_SENTIMENT_PATTERN = Pattern.compile(
            "^Current sentiment is (elevated|depressed)\\.?$",
            Pattern.CASE_INSENSITIVE
    );
    private static final Pattern MEAN_REVERT_PATTERN = Pattern.compile(
            "^Fear & Greed mean-reverts toward its recent average\\.?$",
            Pattern.CASE_INSENSITIVE
    );
    private static final Pattern PRIMARY_EXTERNAL_SIGNAL_CHANGED_PATTERN = Pattern.compile(
            "^Primary external signal changed from (.+?) at ([A-Z0-9_]+) to (.+?)\\.?$"
    );
    private static final Pattern EXTERNAL_RISK_DELTA_PATTERN = Pattern.compile(
            "^([A-Z0-9_]+) composite (?:external )?risk (?:score )?(increased|expanded|eased) by ([^ ]+)p\\.?$",
            Pattern.CASE_INSENSITIVE
    );
    private static final Pattern EXTERNAL_STABLE_PATTERN = Pattern.compile(
            "^([A-Z0-9_]+) external regime remains broadly stable\\.?$",
            Pattern.CASE_INSENSITIVE
    );
    private static final Pattern REGIME_STAYS_AVERAGE_PATTERN = Pattern.compile(
            "^([A-Z0-9_]+) (funding|basis|DXY|US10Y) stays ([^ ]+) versus average\\.?$",
            Pattern.CASE_INSENSITIVE
    );
    private static final Pattern DERIVATIVE_WINDOW_REGIME_PATTERN = Pattern.compile(
            "^([A-Z0-9_]+) keeps funding vs average ([^,]+), OI vs average ([^,]+), basis vs average (.+?)\\.?$",
            Pattern.CASE_INSENSITIVE
    );
    private static final Pattern SENTIMENT_VS_AVERAGE_PATTERN = Pattern.compile(
            "^([A-Z0-9_]+) sentiment vs average is (.+?)\\.?$",
            Pattern.CASE_INSENSITIVE
    );
    private static final Pattern ATR_RATIO_PATTERN = Pattern.compile(
            "^ATR14 ratio is (.+?) of price\\.?$",
            Pattern.CASE_INSENSITIVE
    );
    private static final Pattern ATR_VOLATILITY_PATTERN = Pattern.compile(
            "^ATR14 is more than 3% of price, so intraperiod swings can expand\\.?$",
            Pattern.CASE_INSENSITIVE
    );
    private static final Pattern MACRO_BACKDROP_PATTERN = Pattern.compile(
            "^Macro backdrop is firm with DXY ([^,]+), US10Y ([^,]+), USD/KRW ([^,]+), which can pressure crypto risk appetite\\.?$",
            Pattern.CASE_INSENSITIVE
    );
    private static final Pattern DXY_PROXY_VALUE_PATTERN = Pattern.compile("^DXY proxy is (.+?)\\.?$", Pattern.CASE_INSENSITIVE);
    private static final Pattern US10Y_YIELD_PATTERN = Pattern.compile("^US10Y yield is (.+?)\\.?$", Pattern.CASE_INSENSITIVE);
    private static final Pattern USD_KRW_VALUE_PATTERN = Pattern.compile("^USD/KRW is (.+?)\\.?$", Pattern.CASE_INSENSITIVE);
    private static final Pattern DOMINANT_EXTERNAL_DIRECTION_PATTERN = Pattern.compile(
            "^Dominant external direction is (.+?)\\.?$",
            Pattern.CASE_INSENSITIVE
    );
    private static final Pattern COMPOSITE_EXTERNAL_REGIME_PATTERN = Pattern.compile(
            "^Composite external regime keeps (.+?) in focus with risk score (.+?)\\.?$",
            Pattern.CASE_INSENSITIVE
    );
    private static final Pattern EXTERNAL_REVERSAL_RISK_SCORE_PATTERN = Pattern.compile(
            "^External reversal risk score is (.+?)\\.?$",
            Pattern.CASE_INSENSITIVE
    );
    private static final Pattern EXTERNAL_REVERSAL_RISK_ELEVATED_PATTERN = Pattern.compile(
            "^External regime reversal risk remains elevated at (.+?)\\.?$",
            Pattern.CASE_INSENSITIVE
    );
    private static final Pattern ZONE_SINGLE_LEVEL_PATTERN = Pattern.compile(
            "^(SUPPORT|RESISTANCE) zone is a single calculated level at ([^ ]+) with ([0-9]+) candidate levels\\.?$"
    );
    private static final Pattern STRONGEST_LEVEL_PATTERN = Pattern.compile(
            "^Strongest level is ([A-Z0-9_]+) from (.+?) at (.+?)\\.?$",
            Pattern.CASE_INSENSITIVE
    );
    private static final Pattern ZONE_COMBINES_LABELS_PATTERN = Pattern.compile(
            "^Zone combines labels (.+?)\\.?$",
            Pattern.CASE_INSENSITIVE
    );
    private static final Pattern CURRENT_PRICE_ZONE_DISTANCE_PATTERN = Pattern.compile(
            "^Current price is (above|below) zone(?: the zone)? by (.+?)\\.?$",
            Pattern.CASE_INSENSITIVE
    );
    private static final Pattern RECENT_TESTS_PATTERN = Pattern.compile(
            "^Recent tests=([0-9]+), rejections=([0-9]+), breaks=([0-9]+) within 14 days\\.?$",
            Pattern.CASE_INSENSITIVE
    );
    private static final Pattern CURRENT_PRICE_VS_LEVEL_PATTERN = Pattern.compile(
            "^Current price ([^ ]+) vs ([A-Z0-9_]+) (.+?)\\.?$",
            Pattern.CASE_INSENSITIVE
    );
    private static final Pattern ZONE_SPANS_PATTERN = Pattern.compile(
            "^(SUPPORT|RESISTANCE) zone spans ([^ ]+) to ([^ ]+) with ([0-9]+) candidate levels\\.?$",
            Pattern.CASE_INSENSITIVE
    );
    private static final Pattern ZONE_DISTANCE_PATTERN = Pattern.compile(
            "^(SUPPORT|RESISTANCE) distance (.+?)\\.?$",
            Pattern.CASE_INSENSITIVE
    );
    private static final Pattern REACTION_COUNT_PATTERN = Pattern.compile(
            "^Reaction count ([0-9]+) within recent valid candles\\.?$",
            Pattern.CASE_INSENSITIVE
    );
    private static final Pattern CLUSTER_SIZE_PATTERN = Pattern.compile(
            "^Cluster size ([0-9]+) candidate levels near the same price zone\\.?$",
            Pattern.CASE_INSENSITIVE
    );
    private static final Pattern WINDOW_EXTREMUM_SCORE_PATTERN = Pattern.compile(
            "^Window extremum proximity score (.+?)\\.?$",
            Pattern.CASE_INSENSITIVE
    );
    private static final Pattern REFERENCE_RECENCY_SCORE_PATTERN = Pattern.compile(
            "^Reference recency score (.+?)\\.?$",
            Pattern.CASE_INSENSITIVE
    );
    private static final Pattern PIVOT_LOW_SUPPORT_PATTERN = Pattern.compile(
            "^Pivot low marks a recent defense point and possible demand support\\.?$",
            Pattern.CASE_INSENSITIVE
    );
    private static final Pattern PIVOT_HIGH_RESISTANCE_PATTERN = Pattern.compile(
            "^Pivot high marks a recent rejection point and possible overhead supply\\.?$",
            Pattern.CASE_INSENSITIVE
    );
    private static final Pattern MID_TREND_AVERAGE_SUPPORT_PATTERN = Pattern.compile(
            "^Mid-trend average support\\.?$",
            Pattern.CASE_INSENSITIVE
    );
    private static final Pattern MID_TREND_AVERAGE_RESISTANCE_PATTERN = Pattern.compile(
            "^Mid-trend average resistance\\.?$",
            Pattern.CASE_INSENSITIVE
    );
    private static final Pattern RECENT_PIVOT_LOW_SUPPORT_PATTERN = Pattern.compile(
            "^Recent pivot low support\\.?$",
            Pattern.CASE_INSENSITIVE
    );
    private static final Pattern RECENT_PIVOT_HIGH_RESISTANCE_PATTERN = Pattern.compile(
            "^Recent pivot high resistance\\.?$",
            Pattern.CASE_INSENSITIVE
    );
    private static final Pattern MOVING_AVERAGE_TREND_PATTERN = Pattern.compile(
            "^(MA\\d+) captures moving-average trend support/resistance\\.?$",
            Pattern.CASE_INSENSITIVE
    );
    private static final Pattern NEAREST_ZONE_PATTERN = Pattern.compile(
            "^Nearest (support|resistance) (.+?) is currently (.+?) with ([0-9]+) tests and break risk (.+?)\\.?$",
            Pattern.CASE_INSENSITIVE
    );
    private static final Pattern PRICE_HOLDS_ABOVE_PATTERN = Pattern.compile("^Price holds above (.+?)\\.?$", Pattern.CASE_INSENSITIVE);
    private static final Pattern PRICE_HOLDS_ABOVE_AND_EXTENDS_PATTERN = Pattern.compile(
            "^Price holds above (.+?) and extends toward (.+?)\\.?$",
            Pattern.CASE_INSENSITIVE
    );
    private static final Pattern PRICE_LOSES_SUPPORT_PATTERN = Pattern.compile(
            "^Price loses (.+?) support\\.?$",
            Pattern.CASE_INSENSITIVE
    );
    private static final Pattern LOSS_OF_LEVEL_PATTERN = Pattern.compile(
            "^A loss of (.+?) can trigger a pullback toward (.+?)\\.?$",
            Pattern.CASE_INSENSITIVE
    );
    private static final Pattern PRICE_STAYS_BELOW_PATTERN = Pattern.compile("^Price stays below (.+?)\\.?$", Pattern.CASE_INSENSITIVE);
    private static final Pattern PRICE_STAYS_BELOW_AND_PROBE_PATTERN = Pattern.compile(
            "^Price stays below (.+?) and can probe (.+?)\\.?$",
            Pattern.CASE_INSENSITIVE
    );
    private static final Pattern PRICE_RECLAIMS_PATTERN = Pattern.compile("^Price reclaims (.+?)\\.?$", Pattern.CASE_INSENSITIVE);
    private static final Pattern RECOVERY_ABOVE_PATTERN = Pattern.compile(
            "^A recovery above (.+?) can force short-covering toward (.+?)\\.?$",
            Pattern.CASE_INSENSITIVE
    );
    private static final Pattern PRICE_REMAINS_IN_RANGE_PATTERN = Pattern.compile(
            "^Price remains inside the active range\\.?$",
            Pattern.CASE_INSENSITIVE
    );
    private static final Pattern TREND_STRENGTH_MIXED_PATTERN = Pattern.compile(
            "^Trend strength stays mixed\\.?$",
            Pattern.CASE_INSENSITIVE
    );
    private static final Pattern PRICE_OSCILLATES_PATTERN = Pattern.compile(
            "^Price oscillates between support and resistance while waiting for directional confirmation\\.?$",
            Pattern.CASE_INSENSITIVE
    );
    private static final Pattern RANGE_INVALIDATION_PATTERN = Pattern.compile(
            "^A decisive break beyond range extremes invalidates the range case\\.?$",
            Pattern.CASE_INSENSITIVE
    );
    private static final Pattern PRICE_BREAKS_BAND_EXTREMES_PATTERN = Pattern.compile(
            "^Price breaks beyond the current band extremes\\.?$",
            Pattern.CASE_INSENSITIVE
    );
    private static final Pattern DIRECTIONAL_MOVE_PATTERN = Pattern.compile(
            "^A decisive move beyond the current band extremes can set the next short-term direction\\.?$",
            Pattern.CASE_INSENSITIVE
    );
    private static final Pattern BREAKOUT_INVALIDATION_PATTERN = Pattern.compile(
            "^Failure to hold the breakout level invalidates the directional case\\.?$",
            Pattern.CASE_INSENSITIVE
    );
    private static final Pattern MOMENTUM_CONSTRUCTIVE_PATTERN = Pattern.compile(
            "^Momentum remains constructive\\.?$",
            Pattern.CASE_INSENSITIVE
    );
    private static final Pattern MOMENTUM_WEAK_PATTERN = Pattern.compile(
            "^Momentum remains weak\\.?$",
            Pattern.CASE_INSENSITIVE
    );
    private static final Pattern BAND_EXTENSION_PATTERN = Pattern.compile(
            "^Price is trading at an outer Bollinger band, which raises reversion risk\\.?$",
            Pattern.CASE_INSENSITIVE
    );
    private static final Pattern OUTER_BOLLINGER_TOUCH_PATTERN = Pattern.compile(
            "^Current price is touching an outer Bollinger band\\.?$",
            Pattern.CASE_INSENSITIVE
    );
    private static final Pattern CURRENT_FUNDING_RATE_PATTERN = Pattern.compile(
            "^Current funding rate is (.+?)\\.?$",
            Pattern.CASE_INSENSITIVE
    );
    private static final Pattern FAST_RECOVERY_INVALIDATION_PATTERN = Pattern.compile(
            "^A fast recovery above (.+?) invalidates the (.+?) case\\.?$",
            Pattern.CASE_INSENSITIVE
    );
    private static final Pattern LOSS_WEAKENS_PATH_PATTERN = Pattern.compile(
            "^A loss of (.+?) weakens the bullish continuation path\\.?$",
            Pattern.CASE_INSENSITIVE
    );

    String localizePhrase(String value) {
        if (value == null || value.isBlank()) {
            return value;
        }

        return normalizeWhitespace(value)
                .replace("SHORT_TERM view", "단기 관점")
                .replace("MID_TERM view", "중기 관점")
                .replace("LONG_TERM view", "장기 관점")
                .replace("comparison", "비교")
                .replace("derivative shift", "파생 변화")
                .replace("macro shift", "거시 변화")
                .replace("sentiment shift", "심리 변화")
                .replace("onchain shift", "온체인 변화")
                .replace("activity expansion", "활동 확장")
                .replace("activity contraction", "활동 위축")
                .replace("Dollar strength regime", "달러 강세 국면")
                .replace("Funding crowding regime", "펀딩 과밀 국면")
                .replace("Greed regime", "탐욕 국면")
                .replace("Fear regime", "공포 국면")
                .replace("Extreme fear regime", "극단적 공포 국면")
                .replace("Extreme greed regime", "극단적 탐욕 국면")
                .replace("Neutral regime", "중립 국면")
                .replace("Negative basis pressure", "음의 베이시스 압력")
                .replace("Negative funding pressure", "음의 펀딩 압력")
                .replace("Dollar strength", "달러 강세")
                .replace("Yield pressure", "금리 부담")
                .replace("Large-cap on-chain base", "대형 온체인 기반")
                .replace("External regime direction changed", "외부 국면 방향 전환")
                .replace("TRANSITION_TO_HEADWIND", "하방 부담 전환")
                .replace("headwind dominance", "하방 부담 우세");
    }

    String localizeSentence(String value) {
        if (value == null || value.isBlank()) {
            return value;
        }

        String normalized = normalizeWhitespace(value);
        String localizedPattern = localizePatternSentence(normalized);
        String localized = localizedPattern != null ? localizedPattern : normalized;

        localized = localizePhrase(localized)
                .replace("Fear & Greed", "공포·탐욕 지수")
                .replace("(Greed)", "(탐욕)")
                .replace("(Fear)", "(공포)")
                .replace("(Neutral)", "(중립)")
                .replace("(Extreme Fear)", "(극단적 공포)")
                .replace("(Extreme Greed)", "(극단적 탐욕)")
                .replace("Active addresses", "활성 주소")
                .replace("transactions", "트랜잭션")
                .replace("market cap", "시가총액")
                .replace("on-chain", "온체인")
                .replace("Price oscillates between support and resistance while waiting for directional confirmation", "가격은 방향 확인 전까지 지지와 저항 사이에서 등락할 가능성이 큽니다")
                .replace("A decisive move beyond the current band extremes can set the next short-term direction", "현재 밴드 극단값을 넘어서는 결정적 움직임이 다음 단기 방향을 정할 수 있습니다")
                .replace("External regime reversal risk remains elevated at", "외부 국면 반전 위험은 높은 수준을 유지합니다:")
                .replace("Current funding is", "현재 펀딩비는")
                .replace("Macro context keeps DXY", "거시 맥락에서 DXY는")
                .replace("Sentiment remains", "심리는")
                .replace("sentiment stays", "심리 지표는")
                .replace("activity runs", "활동은")
                .replace("on-chain activity runs", "온체인 활동은")
                .replace(" is in a neutral structure with price at ", "는 현재 중립 구조로 평가되며, 현재 가격은 ")
                .replace(" is in a bullish structure with price at ", "는 현재 상승 우위 구조로 평가되며, 현재 가격은 ")
                .replace(" is in a bearish structure with price at ", "는 현재 하락 우위 구조로 평가되며, 현재 가격은 ")
                .replace(", RSI14 at ", ", RSI14는 ")
                .replace(", and MACD histogram at ", ", MACD 히스토그램은 ")
                .replace(". Macro context keeps DXY ", ". 거시 맥락에서 DXY는 ")
                .replace(", external reversal risk is ", ", 외부 국면 반전 위험은 전체 스케일 대비 ")
                .replace(" of full scale", "")
                .replace("composite risk score", "종합 리스크 점수")
                .replace("종합 리스크 score", "종합 리스크 점수")
                .replace(" keeps unknown dominance for ", " 기준 우세 방향은 아직 뚜렷하지 않으며, 해당 방향 비중은 ")
                .replace(" keeps 하방 부담 우세 for ", " 기준 외부 우세 방향은 하방 부담 우세이며, 해당 방향 비중은 ")
                .replace(" of samples with high severity on ", ", 높은 심각도 비중은 ")
                .replace(" of observations", "입니다")
                .replace(" external regime remains broadly stable", " 기준 외부 국면은 전반적으로 안정적인 상태를 유지합니다")
                .replace("versus average", "평균 대비")
                .replace("vs average", "평균 대비")
                .replace("distance from range high", "레인지 고점 대비 거리")
                .replace("distance from high", "고점 대비 거리")
                .replace("range high", "레인지 고점")
                .replace("range low", "레인지 저점")
                .replace("range", "레인지")
                .replace("reversion risk", "되돌림 위험")
                .replace("directional confirmation", "방향 확인")
                .replace("directional case", "방향성 시나리오")
                .replace("range case", "레인지 시나리오")
                .replace("breakout level", "돌파 레벨")
                .replace("short-covering", "숏커버링")
                .replace("pullback", "조정")
                .replace("outer Bollinger band", "볼린저 밴드 외곽")
                .replace("leveraged directional crowding", "레버리지 방향성 과밀")
                .replace("crowded directional leverage", "과밀한 방향성 레버리지")
                .replace("reactive selloffs and whipsaws can expand", "반응성 매도와 변동성 확대 가능성이 커집니다")
                .replace("chase risk can rise near resistance", "저항 부근 추격 위험이 커질 수 있습니다")
                .replace("Current signals are mixed enough that follow-through may stall near nearby levels", "현재 신호가 혼재돼 있어 인접 레벨 부근에서 추세 연장이 둔화될 수 있습니다")
                .replace("DXY and yields remain firm versus representative averages", "DXY와 금리는 대표 평균 대비 높은 수준을 유지합니다")
                .replace("Funding remains elevated", "펀딩은 높은 수준을 유지하고 있습니다")
                .replace("DXY stays above average", "DXY는 평균 대비 높은 수준을 유지합니다")
                .replace("On-chain activity stays above average", "온체인 활동은 평균 대비 높은 수준을 유지합니다")
                .replace("macro backdrop still shows firm dollar/yield pressure", "거시 배경에서는 달러/금리 부담이 여전히 강합니다")
                .replace("macro backdrop keeps DXY", "거시 배경에서 DXY는")
                .replace("sentiment remains", "심리는")
                .replace("on-chain activity sits", "온체인 활동은")
                .replace("activity sits", "활동은")
                .replace("external regime focus stays on", "외부 국면 핵심 신호는")
                .replace("external regime confluence keeps risk skew elevated", "외부 국면 중첩으로 리스크 편향이 높게 유지됩니다")
                .replace("external regime biased toward", "외부 국면은")
                .replace("keeps the external regime biased toward", "외부 국면은")
                .replace("composite external risk stays", "외부 종합 리스크는")
                .replace("external composite risk remains above its representative average", "외부 종합 리스크는 대표 평균을 웃돌고 있습니다")
                .replace("external composite risk remains", "외부 종합 리스크는")
                .replace("remains above its representative average", "대표 평균을 웃돌고 있습니다")
                .replace("versus its representative average", "대표 평균 대비")
                .replace("reversal risk stays at", "반전 위험은")
                .replace("External regime reversal risk stays at", "외부 국면 반전 위험은")
                .replace("Funding and open interest normalize closer to representative averages", "펀딩과 미결제약정이 대표 평균 부근으로 정상화됩니다")
                .replace("Dollar/yield pressure cools back toward window averages", "달러/금리 부담이 구간 평균 방향으로 완화됩니다")
                .replace("On-chain activity stabilizes around its recent average", "온체인 활동이 최근 평균 부근에서 안정됩니다")
                .replace("Composite external regime score cools back toward neutral", "외부 종합 국면 점수가 중립 방향으로 완화됩니다")
                .replace("Composite external risk normalizes back toward its representative window average", "외부 종합 리스크가 대표 구간 평균 방향으로 정상화됩니다")
                .replace("External regime persistence weakens and stops reinforcing the current bias", "외부 국면의 지속성이 약해지며 현재 편향 강화가 멈춥니다")
                .replace("External reversal risk score cools back to a lower regime", "외부 반전 위험 점수가 더 낮은 국면으로 완화됩니다")
                .replace("Firm macro conditions can amplify downside volatility", "거시 부담이 강하면 하방 변동성이 확대될 수 있습니다")
                .replace("elevated greed can unwind quickly", "높아진 탐욕 심리는 빠르게 되돌려질 수 있습니다")
                .replace("stays above average", "평균 대비 높은 수준을 유지합니다")
                .replace("stays below average", "평균 대비 낮은 수준을 유지합니다")
                .replace("remains elevated", "높은 수준을 유지합니다")
                .replace("which can pressure crypto risk appetite", "이는 가상자산 위험선호에 부담을 줄 수 있습니다")
                .replace("which points to risk appetite staying elevated", "이는 위험선호가 높은 상태로 유지되고 있음을 시사합니다")
                .replace("which points to defensive positioning staying elevated", "이는 방어적 포지셔닝이 높은 상태로 유지되고 있음을 시사합니다")
                .replace("risk appetite", "위험선호")
                .replace("defensive positioning", "방어적 포지셔닝")
                .replace("supportive", "우호")
                .replace("cautionary", "주의")
                .replace("headwind", "하방 부담")
                .replace("composite risk", "종합 리스크")
                .replace("reversal risk", "반전 위험")
                .replace("stays capped", "상단이 제한된 상태입니다")
                .replace("support strength stronger", "지지 강도가 더 강한 상태입니다")
                .replace("a momentum continuation setup", "모멘텀 지속 구도")
                .replace("structure holding above weekly support", "주간 지지 위 구조 유지")
                .replace("momentum continuation setup", "모멘텀 지속 구도")
                .replace("momentum continuation", "모멘텀 지속")
                .replace("cycle recovery above the major base", "주요 바닥 위 사이클 회복")
                .replace("could not be parsed", "파싱할 수 없습니다")
                .replace("summary is unavailable", "요약을 확인할 수 없습니다");

        return finalizeSentence(localized);
    }

    String priceLevelSourceLabel(AnalysisPriceLevelSourceType sourceType) {
        if (sourceType == null) {
            return null;
        }
        return switch (sourceType) {
            case MOVING_AVERAGE -> "이동평균";
            case BOLLINGER_BAND -> "볼린저 밴드";
            case PIVOT_LEVEL -> "피벗 레벨";
        };
    }

    String windowLabel(MarketWindowType windowType) {
        if (windowType == null) {
            return "최근 기준";
        }
        return switch (windowType) {
            case LAST_1D -> "최근 1일";
            case LAST_3D -> "최근 3일";
            case LAST_7D -> "최근 7일";
            case LAST_14D -> "최근 14일";
            case LAST_30D -> "최근 30일";
            case LAST_90D -> "최근 90일";
            case LAST_180D -> "최근 180일";
            case LAST_52W -> "최근 52주";
        };
    }

    String classificationLabel(String classification) {
        if (classification == null || classification.isBlank()) {
            return classification;
        }
        return switch (classification.trim().toLowerCase()) {
            case "extreme fear" -> "극단적 공포";
            case "fear" -> "공포";
            case "neutral" -> "중립";
            case "greed" -> "탐욕";
            case "extreme greed" -> "극단적 탐욕";
            default -> classification;
        };
    }

    private String localizePatternSentence(String value) {
        var previousReport = PREVIOUS_REPORT_PATTERN.matcher(value);
        if (previousReport.matches()) {
            return "이전 " + reportTypeLabel(previousReport.group(1))
                    + " 리포트에서는 "
                    + localizePhrase(previousReport.group(3))
                    + "를 강조했습니다.";
        }

        var confirmsImpulse = CONFIRMS_IMPULSE_PATTERN.matcher(value);
        if (confirmsImpulse.matches()) {
            return confirmsImpulse.group(1)
                    + " 기준 최신 탄력은 "
                    + localizeSentenceFragment(confirmsImpulse.group(2))
                    + "로 확인됩니다.";
        }

        var rangePosition = RANGE_POSITION_PATTERN.matcher(value);
        if (rangePosition.matches()) {
            String trailing = rangePosition.group(3) == null
                    ? ""
                    : ", " + localizeSentenceFragment(rangePosition.group(3));
            return rangePosition.group(1)
                    + " 기준 가격은 레인지의 "
                    + rangePosition.group(2)
                    + " 위치를 유지하고 있습니다"
                    + trailing
                    + ".";
        }

        var range = RANGE_PATTERN.matcher(value);
        if (range.matches()) {
            return range.group(1)
                    + " 레인지는 "
                    + range.group(2)
                    + "부터 "
                    + range.group(3)
                    + "까지입니다.";
        }

        var derivativeKeeps = DERIVATIVE_KEEPS_PATTERN.matcher(value);
        if (derivativeKeeps.matches()) {
            return derivativeKeeps.group(1)
                    + " 기준 OI는 "
                    + derivativeKeeps.group(2)
                    + ", 펀딩 변화는 "
                    + derivativeKeeps.group(3)
                    + ", 베이시스 변화는 "
                    + derivativeKeeps.group(4)
                    + "입니다.";
        }

        var macroKeeps = MACRO_KEEPS_PATTERN.matcher(value);
        if (macroKeeps.matches()) {
            return macroKeeps.group(1)
                    + " 기준 DXY는 "
                    + macroKeeps.group(2)
                    + ", US10Y는 "
                    + macroKeeps.group(3)
                    + ", USD/KRW는 "
                    + localizeSentenceFragment(macroKeeps.group(4))
                    + "입니다.";
        }

        var macroChanges = MACRO_CHANGES_PATTERN.matcher(value);
        if (macroChanges.matches()) {
            return macroChanges.group(1)
                    + " 기준 DXY는 "
                    + macroChanges.group(2)
                    + ", US10Y는 "
                    + macroChanges.group(3)
                    + ", USD/KRW는 "
                    + macroChanges.group(4)
                    + "입니다.";
        }

        var macroSingle = MACRO_SINGLE_PATTERN.matcher(value);
        if (macroSingle.matches()) {
            return macroSingle.group(1)
                    + " 기준 DXY는 "
                    + localizeSentenceFragment(macroSingle.group(2))
                    + "입니다.";
        }

        var sentimentKeeps = SENTIMENT_KEEPS_PATTERN.matcher(value);
        if (sentimentKeeps.matches()) {
            return sentimentKeeps.group(1)
                    + " 기준 공포·탐욕 지수는 "
                    + localizeSentenceFragment(sentimentKeeps.group(2))
                    + "이며, 탐욕 표본은 "
                    + sentimentKeeps.group(3)
                    + "/"
                    + sentimentKeeps.group(4)
                    + "입니다.";
        }

        var sentimentChange = SENTIMENT_CHANGE_PATTERN.matcher(value);
        if (sentimentChange.matches()) {
            String classificationTail = sentimentChange.group(6) != null
                    ? "분류는 " + classificationLabel(sentimentChange.group(6)) + " 상태를 유지했습니다."
                    : "분류는 " + classificationLabel(sentimentChange.group(4))
                    + "에서 " + classificationLabel(sentimentChange.group(5)) + "로 바뀌었습니다.";
            return sentimentChange.group(1)
                    + " 기준 공포·탐욕 지수 변화는 "
                    + sentimentChange.group(2)
                    + "이며("
                    + sentimentChange.group(3)
                    + "), "
                    + classificationTail;
        }

        var onchainKeeps = ONCHAIN_KEEPS_PATTERN.matcher(value);
        if (onchainKeeps.matches()) {
            return onchainKeeps.group(1)
                    + " 기준 활성 주소는 "
                    + onchainKeeps.group(2)
                    + ", 트랜잭션은 "
                    + onchainKeeps.group(3)
                    + ", 시가총액은 "
                    + localizeSentenceFragment(onchainKeeps.group(4))
                    + "입니다.";
        }

        var onchainMarketCap = ONCHAIN_MARKET_CAP_PATTERN.matcher(value);
        if (onchainMarketCap.matches()) {
            return "온체인 시가총액은 "
                    + onchainMarketCap.group(1)
                    + "이며, 자산은 대형 네트워크 구간에 속해 있습니다.";
        }

        var externalDominance = EXTERNAL_DOMINANCE_PATTERN.matcher(value);
        if (externalDominance.matches()) {
            String dominanceLabel = "unknown".equalsIgnoreCase(externalDominance.group(2))
                    ? "우세 방향은 아직 뚜렷하지 않으며"
                    : "외부 우세 방향은 " + localizePhrase(externalDominance.group(2) + " dominance").replace(" 우세", "") + "이며";
            return externalDominance.group(1)
                    + " 기준 " + dominanceLabel
                    + ", 해당 방향 비중은 "
                    + externalDominance.group(3)
                    + "에서 나타났고, 높은 심각도는 "
                    + externalDominance.group(4)
                    + "입니다.";
        }

        var externalShift = EXTERNAL_SHIFT_PATTERN.matcher(value);
        if (externalShift.matches()) {
            return externalShift.group(1) + " 기준 외부 국면 방향이 하방 부담으로 전환됐습니다.";
        }

        var externalTransition = EXTERNAL_TRANSITION_PATTERN.matcher(value);
        if (externalTransition.matches()) {
            return externalTransition.group(1) + " 기준 외부 국면이 하방 부담으로 전환됐습니다.";
        }

        var fearGreedIsAt = FEAR_GREED_IS_AT_PATTERN.matcher(value);
        if (fearGreedIsAt.matches()) {
            return "공포·탐욕 지수는 "
                    + fearGreedIsAt.group(1)
                    + " ("
                    + classificationLabel(fearGreedIsAt.group(2))
                    + ")이며, "
                    + localizeSentenceFragment(fearGreedIsAt.group(3))
                    + ".";
        }

        var fearGreedClassifiedAs = FEAR_GREED_CLASSIFIED_AS_PATTERN.matcher(value);
        if (fearGreedClassifiedAs.matches()) {
            return "공포·탐욕 지수는 "
                    + fearGreedClassifiedAs.group(1)
                    + "이며, 분류는 "
                    + classificationLabel(fearGreedClassifiedAs.group(2))
                    + "입니다.";
        }

        var fearGreedClassification = FEAR_GREED_CLASSIFICATION_PATTERN.matcher(value);
        if (fearGreedClassification.matches()) {
            return "공포·탐욕 지수 분류는 " + classificationLabel(fearGreedClassification.group(1)) + "입니다.";
        }

        var currentSentiment = CURRENT_SENTIMENT_PATTERN.matcher(value);
        if (currentSentiment.matches()) {
            return "현재 심리 상태는 "
                    + ("elevated".equalsIgnoreCase(currentSentiment.group(1)) ? "높은 편입니다." : "위축된 상태입니다.");
        }

        var meanRevert = MEAN_REVERT_PATTERN.matcher(value);
        if (meanRevert.matches()) {
            return "공포·탐욕 지수는 최근 평균 방향으로 되돌림을 시도하고 있습니다.";
        }

        var primarySignalChanged = PRIMARY_EXTERNAL_SIGNAL_CHANGED_PATTERN.matcher(value);
        if (primarySignalChanged.matches()) {
            return primarySignalChanged.group(2)
                    + " 기준 핵심 외부 신호는 "
                    + primarySignalChanged.group(1)
                    + "에서 "
                    + primarySignalChanged.group(3)
                    + "로 바뀌었습니다.";
        }

        var externalRiskDelta = EXTERNAL_RISK_DELTA_PATTERN.matcher(value);
        if (externalRiskDelta.matches()) {
            String action = "eased".equalsIgnoreCase(externalRiskDelta.group(2)) ? "완화됐습니다" : "확대됐습니다";
            return externalRiskDelta.group(1)
                    + " 기준 외부 종합 리스크는 "
                    + externalRiskDelta.group(3)
                    + "p "
                    + action
                    + ".";
        }

        var externalStable = EXTERNAL_STABLE_PATTERN.matcher(value);
        if (externalStable.matches()) {
            return externalStable.group(1) + " 기준 외부 국면은 전반적으로 안정적인 상태를 유지하고 있습니다.";
        }

        var regimeAverage = REGIME_STAYS_AVERAGE_PATTERN.matcher(value);
        if (regimeAverage.matches()) {
            String metric = switch (regimeAverage.group(2).toLowerCase()) {
                case "funding" -> "펀딩은";
                case "basis" -> "베이시스는";
                case "dxy" -> "DXY는";
                case "us10y" -> "US10Y는";
                default -> regimeAverage.group(2) + "는";
            };
            return regimeAverage.group(1) + " 기준 " + metric + " 평균 대비 " + regimeAverage.group(3) + "입니다.";
        }

        var derivativeWindowRegime = DERIVATIVE_WINDOW_REGIME_PATTERN.matcher(value);
        if (derivativeWindowRegime.matches()) {
            return derivativeWindowRegime.group(1)
                    + " 기준 펀딩은 평균 대비 "
                    + derivativeWindowRegime.group(2)
                    + ", OI는 평균 대비 "
                    + derivativeWindowRegime.group(3)
                    + ", 베이시스는 평균 대비 "
                    + derivativeWindowRegime.group(4)
                    + "입니다.";
        }

        var sentimentVsAverage = SENTIMENT_VS_AVERAGE_PATTERN.matcher(value);
        if (sentimentVsAverage.matches()) {
            return sentimentVsAverage.group(1) + " 기준 심리는 평균 대비 " + sentimentVsAverage.group(2) + "입니다.";
        }

        var atrRatio = ATR_RATIO_PATTERN.matcher(value);
        if (atrRatio.matches()) {
            return "ATR14 비율은 가격 대비 " + atrRatio.group(1) + "입니다.";
        }

        var atrVolatility = ATR_VOLATILITY_PATTERN.matcher(value);
        if (atrVolatility.matches()) {
            return "ATR14가 가격의 3%를 웃돌아 기간 내 변동폭이 확대될 수 있습니다.";
        }

        var macroBackdrop = MACRO_BACKDROP_PATTERN.matcher(value);
        if (macroBackdrop.matches()) {
            return "거시 배경에서 DXY는 "
                    + macroBackdrop.group(1)
                    + ", US10Y는 "
                    + macroBackdrop.group(2)
                    + ", USD/KRW는 "
                    + macroBackdrop.group(3)
                    + "이며, 이는 가상자산 위험선호에 부담을 줄 수 있습니다.";
        }

        var dxyProxyValue = DXY_PROXY_VALUE_PATTERN.matcher(value);
        if (dxyProxyValue.matches()) {
            return "DXY 프록시는 " + dxyProxyValue.group(1) + "입니다.";
        }

        var us10yYield = US10Y_YIELD_PATTERN.matcher(value);
        if (us10yYield.matches()) {
            return "US10Y 금리는 " + us10yYield.group(1) + "입니다.";
        }

        var usdKrwValue = USD_KRW_VALUE_PATTERN.matcher(value);
        if (usdKrwValue.matches()) {
            return "USD/KRW는 " + usdKrwValue.group(1) + "입니다.";
        }

        var dominantExternalDirection = DOMINANT_EXTERNAL_DIRECTION_PATTERN.matcher(value);
        if (dominantExternalDirection.matches()) {
            return "외부 우세 방향은 " + localizePhrase(dominantExternalDirection.group(1)) + "입니다.";
        }

        var compositeExternalRegime = COMPOSITE_EXTERNAL_REGIME_PATTERN.matcher(value);
        if (compositeExternalRegime.matches()) {
            return "외부 종합 국면은 "
                    + localizePhrase(compositeExternalRegime.group(1))
                    + "에 초점을 두고 있으며, 리스크 점수는 "
                    + compositeExternalRegime.group(2)
                    + "입니다.";
        }

        var externalReversalRiskScore = EXTERNAL_REVERSAL_RISK_SCORE_PATTERN.matcher(value);
        if (externalReversalRiskScore.matches()) {
            return "외부 반전 위험 점수는 " + externalReversalRiskScore.group(1) + "입니다.";
        }

        var externalReversalRiskElevated = EXTERNAL_REVERSAL_RISK_ELEVATED_PATTERN.matcher(value);
        if (externalReversalRiskElevated.matches()) {
            return "외부 국면 반전 위험은 " + externalReversalRiskElevated.group(1) + " 수준으로 높게 유지됩니다.";
        }

        var zoneSingleLevel = ZONE_SINGLE_LEVEL_PATTERN.matcher(value);
        if (zoneSingleLevel.matches()) {
            String zoneLabel = "SUPPORT".equalsIgnoreCase(zoneSingleLevel.group(1)) ? "지지" : "저항";
            return zoneLabel + " 구간은 "
                    + zoneSingleLevel.group(2)
                    + "의 단일 계산 레벨이며, 후보 레벨은 "
                    + zoneSingleLevel.group(3)
                    + "개입니다.";
        }

        var strongestLevel = STRONGEST_LEVEL_PATTERN.matcher(value);
        if (strongestLevel.matches()) {
            return "가장 강한 레벨은 "
                    + strongestLevel.group(1)
                    + "이며, "
                    + localizeSentenceFragment(strongestLevel.group(2))
                    + " 기준 가격은 "
                    + strongestLevel.group(3)
                    + "입니다.";
        }

        var zoneCombinesLabels = ZONE_COMBINES_LABELS_PATTERN.matcher(value);
        if (zoneCombinesLabels.matches()) {
            return "구간을 구성하는 레벨 라벨은 " + zoneCombinesLabels.group(1) + "입니다.";
        }

        var currentPriceZoneDistance = CURRENT_PRICE_ZONE_DISTANCE_PATTERN.matcher(value);
        if (currentPriceZoneDistance.matches()) {
            return "현재 가격은 구간 "
                    + ("above".equalsIgnoreCase(currentPriceZoneDistance.group(1)) ? "상단" : "하단")
                    + " 기준 "
                    + currentPriceZoneDistance.group(2)
                    + " 떨어져 있습니다.";
        }

        var recentTests = RECENT_TESTS_PATTERN.matcher(value);
        if (recentTests.matches()) {
            return "최근 14일 기준 테스트는 "
                    + recentTests.group(1)
                    + "회, 거부는 "
                    + recentTests.group(2)
                    + "회, 이탈은 "
                    + recentTests.group(3)
                    + "회입니다.";
        }

        var currentPriceVsLevel = CURRENT_PRICE_VS_LEVEL_PATTERN.matcher(value);
        if (currentPriceVsLevel.matches()) {
            return "현재 가격 "
                    + currentPriceVsLevel.group(1)
                    + "은 "
                    + currentPriceVsLevel.group(2)
                    + " "
                    + currentPriceVsLevel.group(3)
                    + " 대비 위치에 있습니다.";
        }

        var zoneSpans = ZONE_SPANS_PATTERN.matcher(value);
        if (zoneSpans.matches()) {
            String zoneLabel = "SUPPORT".equalsIgnoreCase(zoneSpans.group(1)) ? "지지" : "저항";
            return zoneLabel + " 구간은 "
                    + zoneSpans.group(2)
                    + "부터 "
                    + zoneSpans.group(3)
                    + "까지이며, 후보 레벨은 "
                    + zoneSpans.group(4)
                    + "개입니다.";
        }

        var zoneDistance = ZONE_DISTANCE_PATTERN.matcher(value);
        if (zoneDistance.matches()) {
            String zoneLabel = "SUPPORT".equalsIgnoreCase(zoneDistance.group(1)) ? "지지" : "저항";
            return zoneLabel + " 구간과의 거리는 " + zoneDistance.group(2) + "입니다.";
        }

        var reactionCount = REACTION_COUNT_PATTERN.matcher(value);
        if (reactionCount.matches()) {
            return "최근 유효 캔들 기준 반응 횟수는 " + reactionCount.group(1) + "회입니다.";
        }

        var clusterSize = CLUSTER_SIZE_PATTERN.matcher(value);
        if (clusterSize.matches()) {
            return "같은 가격대 인근 후보 레벨 군집은 " + clusterSize.group(1) + "개입니다.";
        }

        var windowExtremumScore = WINDOW_EXTREMUM_SCORE_PATTERN.matcher(value);
        if (windowExtremumScore.matches()) {
            return "윈도우 극값 근접 점수는 " + windowExtremumScore.group(1) + "입니다.";
        }

        var referenceRecencyScore = REFERENCE_RECENCY_SCORE_PATTERN.matcher(value);
        if (referenceRecencyScore.matches()) {
            return "기준 시점 최신성 점수는 " + referenceRecencyScore.group(1) + "입니다.";
        }

        var pivotLowSupport = PIVOT_LOW_SUPPORT_PATTERN.matcher(value);
        if (pivotLowSupport.matches()) {
            return "피벗 저점은 최근 방어 구간이자 잠재 수요 지점을 나타냅니다.";
        }

        var pivotHighResistance = PIVOT_HIGH_RESISTANCE_PATTERN.matcher(value);
        if (pivotHighResistance.matches()) {
            return "피벗 고점은 최근 저항 구간이자 잠재 공급 지점을 나타냅니다.";
        }

        var midTrendAverageSupport = MID_TREND_AVERAGE_SUPPORT_PATTERN.matcher(value);
        if (midTrendAverageSupport.matches()) {
            return "중기 추세 이동평균 지지 구간입니다.";
        }

        var midTrendAverageResistance = MID_TREND_AVERAGE_RESISTANCE_PATTERN.matcher(value);
        if (midTrendAverageResistance.matches()) {
            return "중기 추세 이동평균 저항 구간입니다.";
        }

        var recentPivotLowSupport = RECENT_PIVOT_LOW_SUPPORT_PATTERN.matcher(value);
        if (recentPivotLowSupport.matches()) {
            return "최근 피벗 저점 기반 지지 구간입니다.";
        }

        var recentPivotHighResistance = RECENT_PIVOT_HIGH_RESISTANCE_PATTERN.matcher(value);
        if (recentPivotHighResistance.matches()) {
            return "최근 피벗 고점 기반 저항 구간입니다.";
        }

        var movingAverageTrend = MOVING_AVERAGE_TREND_PATTERN.matcher(value);
        if (movingAverageTrend.matches()) {
            return movingAverageTrend.group(1) + "은 이동평균 기반 추세 지지·저항을 반영합니다.";
        }

        var nearestZone = NEAREST_ZONE_PATTERN.matcher(value);
        if (nearestZone.matches()) {
            String zoneLabel = "support".equalsIgnoreCase(nearestZone.group(1)) ? "지지" : "저항";
            return "가까운 "
                    + zoneLabel
                    + " 구간 "
                    + nearestZone.group(2)
                    + "은 현재 "
                    + nearestZone.group(3)
                    + " 상태이며, 테스트는 "
                    + nearestZone.group(4)
                    + "회, "
                    + ("support".equalsIgnoreCase(nearestZone.group(1)) ? "이탈 위험은 " : "돌파 위험은 ")
                    + nearestZone.group(5)
                    + "입니다.";
        }

        var priceHoldsAboveAndExtends = PRICE_HOLDS_ABOVE_AND_EXTENDS_PATTERN.matcher(value);
        if (priceHoldsAboveAndExtends.matches()) {
            return "가격은 " + priceHoldsAboveAndExtends.group(1)
                    + " 위를 유지하며 " + priceHoldsAboveAndExtends.group(2) + " 방향으로 확장할 수 있습니다.";
        }

        var priceHoldsAbove = PRICE_HOLDS_ABOVE_PATTERN.matcher(value);
        if (priceHoldsAbove.matches()) {
            return "가격은 " + priceHoldsAbove.group(1) + " 위를 유지합니다.";
        }

        var priceLosesSupport = PRICE_LOSES_SUPPORT_PATTERN.matcher(value);
        if (priceLosesSupport.matches()) {
            return "가격이 " + priceLosesSupport.group(1) + " 지지를 이탈합니다.";
        }

        var lossOfLevel = LOSS_OF_LEVEL_PATTERN.matcher(value);
        if (lossOfLevel.matches()) {
            return lossOfLevel.group(1) + " 이탈 시 " + lossOfLevel.group(2) + " 방향 조정이 전개될 수 있습니다.";
        }

        var priceStaysBelowAndProbe = PRICE_STAYS_BELOW_AND_PROBE_PATTERN.matcher(value);
        if (priceStaysBelowAndProbe.matches()) {
            return "가격은 " + priceStaysBelowAndProbe.group(1)
                    + " 아래에 머물며 " + priceStaysBelowAndProbe.group(2) + "까지 시험할 수 있습니다.";
        }

        var priceStaysBelow = PRICE_STAYS_BELOW_PATTERN.matcher(value);
        if (priceStaysBelow.matches()) {
            return "가격은 " + priceStaysBelow.group(1) + " 아래에 머뭅니다.";
        }

        var priceReclaims = PRICE_RECLAIMS_PATTERN.matcher(value);
        if (priceReclaims.matches()) {
            return "가격이 " + priceReclaims.group(1) + "를 다시 회복합니다.";
        }

        var recoveryAbove = RECOVERY_ABOVE_PATTERN.matcher(value);
        if (recoveryAbove.matches()) {
            return recoveryAbove.group(1) + " 상향 회복 시 " + recoveryAbove.group(2) + " 방향 숏커버링이 나올 수 있습니다.";
        }

        var priceRemainsInRange = PRICE_REMAINS_IN_RANGE_PATTERN.matcher(value);
        if (priceRemainsInRange.matches()) {
            return "가격은 현재 활성 레인지 내부에 머물고 있습니다.";
        }

        var trendStrengthMixed = TREND_STRENGTH_MIXED_PATTERN.matcher(value);
        if (trendStrengthMixed.matches()) {
            return "추세 강도는 혼재된 상태입니다.";
        }

        var priceOscillates = PRICE_OSCILLATES_PATTERN.matcher(value);
        if (priceOscillates.matches()) {
            return "가격은 방향 확인 전까지 지지와 저항 사이에서 등락할 가능성이 큽니다.";
        }

        var rangeInvalidation = RANGE_INVALIDATION_PATTERN.matcher(value);
        if (rangeInvalidation.matches()) {
            return "레인지 극단값 이탈이 나오면 현재 레인지 시나리오는 무효화됩니다.";
        }

        var priceBreaksBandExtremes = PRICE_BREAKS_BAND_EXTREMES_PATTERN.matcher(value);
        if (priceBreaksBandExtremes.matches()) {
            return "가격이 현재 밴드 극단값을 돌파합니다.";
        }

        var directionalMove = DIRECTIONAL_MOVE_PATTERN.matcher(value);
        if (directionalMove.matches()) {
            return "현재 밴드 극단값을 넘어서는 결정적 움직임이 다음 단기 방향을 정할 수 있습니다.";
        }

        var breakoutInvalidation = BREAKOUT_INVALIDATION_PATTERN.matcher(value);
        if (breakoutInvalidation.matches()) {
            return "돌파 레벨을 지키지 못하면 방향성 시나리오는 무효화됩니다.";
        }

        var momentumConstructive = MOMENTUM_CONSTRUCTIVE_PATTERN.matcher(value);
        if (momentumConstructive.matches()) {
            return "모멘텀은 우호적으로 유지됩니다.";
        }

        var momentumWeak = MOMENTUM_WEAK_PATTERN.matcher(value);
        if (momentumWeak.matches()) {
            return "모멘텀은 약한 상태를 유지합니다.";
        }

        var bandExtension = BAND_EXTENSION_PATTERN.matcher(value);
        if (bandExtension.matches()) {
            return "가격이 볼린저 밴드 외곽에서 움직여 되돌림 위험을 키우고 있습니다.";
        }

        var outerBollingerTouch = OUTER_BOLLINGER_TOUCH_PATTERN.matcher(value);
        if (outerBollingerTouch.matches()) {
            return "현재 가격이 볼린저 밴드 외곽에 닿아 있습니다.";
        }

        var currentFundingRate = CURRENT_FUNDING_RATE_PATTERN.matcher(value);
        if (currentFundingRate.matches()) {
            return "현재 펀딩비는 " + currentFundingRate.group(1) + "입니다.";
        }

        var fastRecoveryInvalidation = FAST_RECOVERY_INVALIDATION_PATTERN.matcher(value);
        if (fastRecoveryInvalidation.matches()) {
            return "가격이 빠르게 " + fastRecoveryInvalidation.group(1)
                    + " 위를 회복하면 " + fastRecoveryInvalidation.group(2) + " 시나리오는 무효화됩니다.";
        }

        var lossWeakensPath = LOSS_WEAKENS_PATH_PATTERN.matcher(value);
        if (lossWeakensPath.matches()) {
            return lossWeakensPath.group(1) + " 이탈 시 상승 지속 경로는 약화됩니다.";
        }

        return null;
    }

    private String localizeSentenceFragment(String value) {
        return normalizeWhitespace(value)
                .replace("versus average", "평균 대비")
                .replace("vs average", "평균 대비")
                .replace("MACD histogram", "MACD 히스토그램")
                .replace("RSI delta", "RSI 변화")
                .replace("MACD hist", "MACD 히스토그램")
                .replace("funding delta", "펀딩 변화")
                .replace("basis delta", "베이시스 변화")
                .replace("delta", "변화")
                .replace("Δ", "변화 ")
                .replace("which points to defensive positioning staying elevated", "이는 방어적 포지셔닝이 높은 상태로 유지되고 있음을 시사합니다")
                .replace("which points to risk appetite staying elevated", "이는 위험선호가 높은 상태로 유지되고 있음을 시사합니다")
                .replace("volume", "거래량")
                .replace("taker-buy", "시장가 매수")
                .replace("taker buy", "시장가 매수")
                .replace("with volume", "거래량은")
                .replace("is at", "현재")
                .replace("pivot level", "피벗 레벨")
                .replace("moving average", "이동평균")
                .replace("which can pressure crypto risk appetite", "이는 가상자산 위험선호에 부담을 줄 수 있습니다");
    }

    private String reportTypeLabel(String value) {
        return switch (value.toLowerCase()) {
            case "short-term" -> "단기";
            case "mid-term" -> "중기";
            case "long-term" -> "장기";
            default -> value;
        };
    }

    private String finalizeSentence(String value) {
        String normalized = truncateDecimalNumbers(normalizeWhitespace(value))
                .replace(" ,", ",")
                .replace(" .", ".")
                .replace("..", ".")
                .replace("입니다.", "입니다.")
                .replace("입니다..", "입니다.");

        if (normalized.endsWith(".")) {
            return normalized;
        }
        if (normalized.endsWith("입니다") || normalized.endsWith("합니다") || normalized.endsWith("됩니다")
                || normalized.endsWith("있습니다") || normalized.endsWith("시사합니다") || normalized.endsWith("전환됐습니다")) {
            return normalized + ".";
        }
        return normalized;
    }

    private String normalizeWhitespace(String value) {
        return value == null ? null : value.trim().replaceAll("\\s+", " ");
    }

    private String truncateDecimalNumbers(String value) {
        if (value == null || value.isBlank()) {
            return value;
        }

        Matcher matcher = DECIMAL_NUMBER_PATTERN.matcher(value);
        StringBuffer buffer = new StringBuffer();
        while (matcher.find()) {
            BigDecimal decimal = new BigDecimal(matcher.group(1));
            String truncated = decimal.setScale(DISPLAY_SCALE, RoundingMode.DOWN)
                                      .stripTrailingZeros()
                                      .toPlainString();
            matcher.appendReplacement(buffer, Matcher.quoteReplacement(truncated));
        }
        matcher.appendTail(buffer);
        return buffer.toString();
    }
}
