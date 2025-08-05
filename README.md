
## 1. 프로젝트 적용시 수정 사항
현재는 테스트용으로 `localhost`에 있는 MySQL과 Redis를 사용했습니다.  
배포 시 다음 부분을 수정하면 됩니다.

**application.properties**
```properties
spring.datasource.url=jdbc:mysql://localhost:3306/myDB
spring.redis.host=localhost  
spring.redis.port=6379
```

---

## 2. 현재 테스트 환경

- **MySQL**: 로컬 또는 Docker 컨테이너 사용 가능
- **Redis**: 로컬 또는 Docker 컨테이너 사용 가능
- **Kafka**: 협업자(현제님) 서버와 연결된 환경에서 동작

```bash
docker run --name mysql   -e MYSQL_ROOT_PASSWORD='master123'   -e MYSQL_DATABASE=myDB   -p 3306:3306   -v mysql_data:/var/lib/mysql   -d mysql:8.0

docker run --name my-redis   -p 6379:6379   -d redis:7.2-alpine
```

---

## 3. 데이터 전달

- **Consumer**: 수신 토픽 → `health-events`
- **Producer**: 발신 토픽 → `alert-events`

**alert-events 데이터 구조**
```java
private String groupId;             
private Long cctvId;
private List<String> users;         
private String eventCode;           
private FaultEventEntity.Severity severity;  
private String reason;
```

- `groupId`와 `users`는 현재 MOCK 값
- 심각도는 `MEDIUM`, `HIGH`만 전송, 정상 상태는 `LOW`로 설정

---

## 4. 프로젝트 처리 흐름

```
[Kafka health-events Consumer] 
    → [Drools Rule Engine] 
    → [Redis 장애 카운터] 
    → [장애 분류] 
    → [MySQL 상태 저장/갱신] 
    → [Kafka alert-events Produce]
```

---

### 4-1. Drools Rule Engine
수신된 CCTV 상태 이벤트를 규칙 기반으로 판별합니다.

- **DroolsConfig.java**  
  Drools 실행 환경(`KieContainer`, `KieSession`) 설정  
  `kmodule.xml`에서 정의된 Rule 설정 로드

- **kmodule.xml**  
  Drools 규칙 파일(.drl) 위치와 KIE 베이스/세션 구성 정의

- **fault-rules.drl**  
  장애 판별 규칙 선언 파일  
  예: `HLS_TIMEOUT` 3회 이상 발생 → `severity=HIGH`

- **FaultRuleService.java**  
  Drools 세션 실행, 변환된 객체를 Redis/DB로 전달

- **build.gradle (Drools 관련 의존성)**
```gradle
implementation 'org.kie:kie-api:7.74.0.Final'
implementation 'org.drools:drools-core:7.74.0.Final'
implementation 'org.drools:drools-compiler:7.74.0.Final'
implementation 'org.drools:drools-decisiontables:7.74.0.Final'
implementation 'javax.inject:javax.inject:1'
```

---

### 4-2. Redis 장애 카운터
장애 이벤트가 일시적/지속적인지 판별

- **RedisService.java**: Redis 연동 로직
- **application.properties**
```properties
spring.redis.host=localhost
spring.redis.port=6379
spring.redis.timeout=60000
```
- **build.gradle**
```gradle
implementation 'org.springframework.boot:spring-boot-starter-data-redis'
```

---

### 4-3. 장애 분류
- 상태감지 서비스에서 제공하는 키워드를 기반으로  
  **FaultEventService**에서 `switch-case`로 분류

---

### 4-4. MySQL 상태 저장/갱신
Drools + Redis를 거친 최종 장애 상태를 저장하고 최신 상태만 유지

- **FaultEventEntity.java**: JPA 엔티티
- **FaultEventRepository.java**: Spring Data JPA CRUD
- **FaultEventService.java**: 저장/갱신 로직

정상 이벤트(`*_OK`) → 해당 장애 기록 삭제

---

### 4-5. Kafka `alert-events` Produce
MySQL 저장/갱신 후 알림 메시지 전송

- **FaultAlertDTO.java**: 전송 데이터 구조
- **KafkaFaultProducer.java**: Spring Kafka 기반 Producer
- **KafkaProducerConfig.java**: Kafka Producer 설정
```java
kafkaTemplate.send("alert-events", faultAlertDTO);
```

---
