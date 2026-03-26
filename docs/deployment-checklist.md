# Deployment Checklist

## 1. 서버 설정

- `SERVER_PORT` 확인
- `SPRING_APPLICATION_NAME` 확인
- `DB_HOST`, `DB_PORT`, `DB_NAME`, `DB_USERNAME`, `DB_PASSWORD` 확인
- `DB_CONNECTION_TIMEZONE=UTC` 유지 여부 확인

## 2. 스케줄 설정

- `batch.scheduler.external-raw-ingestion.enabled` 확인
- `batch.scheduler.analysis-report-generation.enabled` 확인
- 운영 직전 리포트 생성 initial delay/fixed delay 확인

## 3. 외부 연동

- Binance base URL 확인
- `FRED_API_KEY` 확인
- OpenAI를 사용할 경우만 아래 확인
  - `external.openai.enabled=true`
  - `OPENAI_API_KEY`
  - `OPENAI_MODEL`

## 4. API 노출

- `/api/health` 응답 확인
- `/api/assets` 응답 확인
- `/api/assets/summaries` 응답 확인
- `/api/reports/latest/summary` 응답 확인
- `/api/reports/latest/detail` 응답 확인

## 5. 프론트 연결

- 프론트 API base URL이 통합 서버를 바라보는지 확인
- 프론트가 UTC 시간을 KST로 변환하는지 확인
- 참여도 카드가 아래 의미로 노출되는지 확인
  - `quoteVolumeChangeRate` = 거래대금 변화
  - `tradeCountChangeRate` = 체결 수 변화

## 6. 데이터 검증

- `market_candle_raw` 최근 데이터 적재 확인
- `market_price_raw` 최근 데이터 적재 확인
- `market_indicator_snapshot` 최신 row 확인
- `analysis_report` 최신 row 확인
- narrative 사용 시 `analysis_report_narrative`, `analysis_report_shared_context` 확인

## 7. 보안/운영 노출

- `/internal/admin/*` 외부 노출 여부 확인
- `/docs`, `/openapi/v3/api-docs` 운영 노출 여부 확인
- CORS 허용 origin 운영값 확인

## 8. 최종 점검

- 단기/중기/장기 최신 리포트 조회 확인
- 프론트 메인 화면 정상 표시 확인
- 배치 로그에 raw ingestion 실패가 없는지 확인
- Flyway validation/migration 오류가 없는지 확인
