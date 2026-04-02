# GCP Docker Deploy

## 구성

- Compute Engine VM 1대
- Cloud SQL MySQL 1개
- Docker Compose
- 컨테이너 3개
  - `spring-server`
  - `nextjs`
  - `nginx`

## 사전 준비

1. Cloud SQL MySQL 생성
2. Cloud SQL private IP 활성화
3. Compute Engine VM을 같은 VPC/리전에 생성
4. VM에 Docker, Docker Compose 설치
5. 이 저장소와 `ai-coin-assist-frontend` 저장소를 같은 상위 경로에 배치

예:

```text
/home/deploy/
  batch/
  ai-coin-assist-frontend/
```

## 환경 파일

`batch/.env` 파일을 직접 만들고 실제 값으로 채웁니다.

예:

```env
DB_HOST=<Cloud SQL private IP>
DB_PORT=3306
DB_NAME=coin_analysis
DB_USERNAME=app_user
DB_PASSWORD=<db password>

FRED_API_KEY=<fred api key>

OPENAI_API_KEY=
OPENAI_MODEL=gpt-5-mini
EXTERNAL_OPENAI_ENABLED=false

APP_CORS_ALLOWED_ORIGINS=https://your-domain.example
NEXT_PUBLIC_SERVER_BASE_URL=https://your-domain.example

SERVER_PORT=8082
SPRING_APPLICATION_NAME=ai-coin-assist-server

SPRINGDOC_API_DOCS_ENABLED=false
SPRINGDOC_SWAGGER_UI_ENABLED=false

ADMIN_API_ENABLED=false
ADMIN_API_TOKEN=
```

중요:
- `DB_HOST`는 Cloud SQL private IP
- `APP_CORS_ALLOWED_ORIGINS`는 실제 프론트 도메인
- `NEXT_PUBLIC_SERVER_BASE_URL`는 실제 프론트가 접근할 공개 도메인
- 첫 배포에서는 `EXTERNAL_OPENAI_ENABLED=false`
- 첫 배포에서는 `ADMIN_API_ENABLED=false`
- Nginx 뒤에서 실행되므로 Spring은 `forward-headers-strategy=framework`를 사용합니다.

## 실행

```bash
docker compose build
docker compose up -d
```

## 확인

- `http://<vm-ip>/api/health`
- `http://<vm-ip>/api/assets`
- 프론트 메인 페이지

운영에서 Swagger를 열지 않을 경우:
- `SPRINGDOC_API_DOCS_ENABLED=false`
- `SPRINGDOC_SWAGGER_UI_ENABLED=false`

## 다음 단계

- DNS 연결
- Nginx에 HTTPS 추가
- `80 -> 443` 리다이렉트 적용
