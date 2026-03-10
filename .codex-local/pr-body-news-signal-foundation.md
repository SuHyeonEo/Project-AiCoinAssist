## 변경 내용
- GDELT DOC 2.0 `artlist` 응답을 가져오는 client, DTO, validator 추가
- `news_signal_raw` raw 테이블과 `news_signal_snapshot` processed 테이블 추가
- `btc`, `eth`, `xrp` 자산별 query keyword 지원과 기본 asset code 매핑 추가
- `NewsRawIngestionService` 추가
- GDELT 기사 후보를 자산별 raw row로 idempotent 하게 저장하도록 구현
- `NewsSignalSnapshotService`, `NewsSignalSnapshotPersistenceService` 추가
- valid raw 기사 후보를 기준으로 title keyword hit count와 priority score를 계산한 processed snapshot을 저장하도록 구현
- Flyway migration `V19__add_news_signal_tables.sql` 추가
- validator, raw ingestion, snapshot, repository 제약 테스트 추가

## 기대 효과
- 외부 뉴스/이벤트 후보를 GPT에 직접 찾게 하지 않고 raw-first 구조로 수집할 수 있습니다.
- 기사 URL 기준으로 raw 후보를 idempotent 하게 관리해 재수집과 재검증이 쉬워집니다.
- asset별 keyword query와 title match score를 기반으로, 이후 report 연결 전에 사용할 최소 뉴스 signal foundation이 생깁니다.
- sentiment / macro / on-chain과 같은 방식으로 news domain도 raw / processed 분리 원칙을 따르게 됩니다.

## 검증 내용
- `gradlew.bat test --tests com.aicoinassist.batch.infrastructure.client.gdelt.validator.GdeltArticleResponseValidatorTest --tests com.aicoinassist.batch.domain.news.service.NewsRawIngestionServiceTest --tests com.aicoinassist.batch.domain.news.service.NewsSignalSnapshotServiceTest --tests com.aicoinassist.batch.domain.news.service.NewsSignalSnapshotPersistenceServiceTest --tests com.aicoinassist.batch.domain.news.repository.NewsTableConstraintTest`
- `gradlew.bat test`

## 설계 메모
- GDELT 기사 후보의 시간 의미는 generic `source_event_time`으로 뭉개지 않고 `seen_time`으로 별도 보존했습니다.
- response 전체가 비어 있을 때는 invalid placeholder raw signal을 만들고, 기사 단위 validation도 별도로 보존하도록 구성했습니다.
- 이번 단계는 foundation에 집중했고, 아직 report payload나 GPT 입력에는 news signal을 연결하지 않았습니다.
