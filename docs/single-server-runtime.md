# Single Server Runtime

## 개요

현재 운영 기준 서버는 이 저장소의 단일 Spring Boot 애플리케이션입니다.

하나의 프로세스 안에 아래가 함께 포함됩니다.
- batch scheduler
- raw ingestion
- snapshot generation
- report generation
- read API

## 기본 설정

- 기본 포트: `8082`
- 기본 애플리케이션 이름: `ai-coin-assist-server`
- 포트와 애플리케이션 이름은 환경 변수로 덮어쓸 수 있습니다.
  - `SERVER_PORT`
  - `SPRING_APPLICATION_NAME`

## API 기준

프론트는 별도 API 서버가 아니라 이 통합 서버를 직접 호출합니다.

주요 경로:
- `GET /api/health`
- `GET /api/assets`
- `GET /api/assets/summaries`
- `GET /api/assets/{symbol}/summary`
- `GET /api/reports/latest/summary?symbol=BTCUSDT&reportType=SHORT_TERM`
- `GET /api/reports/latest/detail?symbol=BTCUSDT&reportType=SHORT_TERM`
- `GET /api/reports/{reportId}`
- `GET /api/reports/history?symbol=BTCUSDT&reportType=SHORT_TERM&limit=20`

운영/문서 경로:
- `GET /actuator/health`
- `GET /openapi/v3/api-docs`
- `GET /docs`

Swagger 제어:
- `SPRINGDOC_API_DOCS_ENABLED=false`
- `SPRINGDOC_SWAGGER_UI_ENABLED=false`

## 시간 기준

서버 내부 시간 의미는 모두 명시적으로 분리합니다.

- `analysisBasisTime`: 리포트 분석 기준 시각
- `rawReferenceTime`: 리포트 raw 기준 시각
- `priceSourceEventTime`: 현재가가 실제 시장에서 발생한 시각
- `openTime`: 봉 시작 시각
- `closeTime`: 봉 종료 경계 시각

원칙:
- 저장과 API 응답은 `UTC`
- 프론트 표시는 `KST`

## 프론트 연결 기준

프론트는 아래 환경 변수를 통합 서버 기준으로 맞춥니다.

- `AICA_API_BASE_URL`
- `NEXT_PUBLIC_API_BASE_URL`

로컬 기본값이 필요하면 `http://localhost:8082`를 사용합니다.

## 내부 엔드포인트

현재 내부 admin 경로도 같은 서버에 포함됩니다.

- `/internal/admin/report-batch-runs`
- `/internal/admin/report-narratives`

운영 배포 전에는 아래 중 하나를 결정해야 합니다.
- 외부 노출 차단
- 프록시 레벨 제한
- 토큰 검증 강화
- Swagger 비노출

참고:
- 내부 admin 경로는 현재 Swagger 문서에서 숨김 처리되어 있습니다.

## 외부 api 저장소

이 서버는 더 이상 별도 `api` 저장소 소스를 빌드에 참조하지 않습니다.
기준 서버 코드는 이 저장소 하나입니다.
