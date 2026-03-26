# AI Coin Assist Server

이 저장소는 이제 AI Coin Assist의 단일 통합 Spring Boot 서버입니다.

포함 역할:
- 배치 스케줄링
- raw 데이터 수집
- snapshot 계산
- 리포트 생성
- 프론트 조회용 HTTP API 제공

기본 포트:
- `SERVER_PORT` 미지정 시 `8082`

주요 공개 경로:
- `/api/health`
- `/api/assets`
- `/api/assets/summaries`
- `/api/reports/latest/summary`
- `/api/reports/latest/detail`
- `/api/reports/{reportId}`
- `/openapi/v3/api-docs`
- `/docs`

Swagger 제어:
- `SPRINGDOC_API_DOCS_ENABLED=false` 로 OpenAPI JSON 비활성화
- `SPRINGDOC_SWAGGER_UI_ENABLED=false` 로 Swagger UI 비활성화

운영 원칙:
- 내부 저장과 API 시간은 `UTC` 기준입니다.
- 프론트는 UTC 값을 받아 `KST`로 변환해 표시합니다.
- 분석/리포트 기준 데이터는 raw-first 원칙을 따릅니다.
- 별도 `api` 저장소는 더 이상 이 서버의 빌드 의존 대상이 아닙니다.

핵심 환경 변수:
- `SERVER_PORT`
- `SPRING_APPLICATION_NAME`
- `DB_HOST`
- `DB_PORT`
- `DB_NAME`
- `DB_USERNAME`
- `DB_PASSWORD`
- `FRED_API_KEY`
- `OPENAI_API_KEY`
- `OPENAI_MODEL`

참고 문서:
- [single-server-runtime.md](C:/Users/tngus/batch/docs/single-server-runtime.md)
- [deployment-checklist.md](C:/Users/tngus/batch/docs/deployment-checklist.md)
