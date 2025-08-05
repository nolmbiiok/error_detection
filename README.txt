
1. 프로젝트 적용시 수정 사항
	지금은 테스트용으로 localhost에 있는 MySQL과 Redis를 사용했습니다. 
	이 후 적용시 이부분을 수정하면 될것같습니다.
	application.properties
		MySQL DB 주소를 바꿀 필요가 있습니다.
			spring.datasource.url=jdbc:mysql://localhost:3306/myDB
		Redis DB 주소를 바꿀 필요가 있습니다
			spring.redis.host=localhost  
			spring.redis.port=6379       
		
2. 현재 테스트 환경

	MySQL: 로컬 또는 Docker 컨테이너 사용 가능
	Redis: 로컬 또는 Docker 컨테이너 사용 가능
	Kafka: 협업자(현제님) 서버와 연결된 환경에서 동작
	```
	docker run --name mysql \
	  -e MYSQL_ROOT_PASSWORD='master123' \
	  -e MYSQL_DATABASE=myDB \
	  -p 3306:3306 \
	  -v mysql_data:/var/lib/mysql \
	  -d mysql:8.0

	docker run --name my-redis \
	    -p 6379:6379 \
	    -d redis:7.2-alpine
	```

3. 데이터 전달
	Consumer입장에서 전달받을 Topic이름은 health-events 로 받고 있습니다.
	Producer입장에서 전달해줄 Topic 이름은 alert-events 입니다.
	alert-events에는 
	    private String groupId;             
	    private Long cctvId;
	    private List<String> users;         
	    private String eventCode;           
	    private FaultEventEntity.Severity severity;  
	    private String reason;    
	   	groupId,그리고 users(사용자 이메일 목록)은 MOCK으로 일단 넣어놨습니다. 
	   	(다른 서비스와 연결이 가능하다면 이후 수정하겠습니다. (그룹화 서비스, 구독 서비스))
	alert-event를 보낼때 심각도 MEDIUM, HIGH를 보내고 있고 정상상태는 LOW로 설정했습니다.

4. 프로젝트 파일 구성
	[Kafka health-events Consumer] 
    	→ [Drools Rule Engine] 
	→ [Redis 장애 카운터] 
    	→ [장애 분류] 
        → [MySQL 상태 저장/갱신] 
        → [Kafka alert-events Produce]
        
4-1. Drools Rule Engine
수신된 CCTV 상태 이벤트를 규칙 기반으로 판별하는 엔진입니다.
	DroolsConfig.java
		Drools 실행 환경(KieContainer, KieSession)을 설정하는 구성 클래스입니다.
		kmodule.xml에서 정의된 Rule 설정을 로드하여 서비스 계층에서 Drools를 실행할 수 있게 합니다.
	kmodule.xml
		Drools 규칙 파일(.drl) 위치와 KIE 베이스/세션 구성을 정의하는 설정 파일입니다.
		Drools가 규칙 파일을 자동 스캔하지 않으므로, 명시적으로 규칙 경로와 세션을 선언해야 합니다.
	fault-rules.drl
		장애 판별 규칙이 선언된 Drools 규칙 파일입니다.
		HLS_TIMEOUT이 3회 이상 발생 → severity=HIGH, reason="HLS 연속 장애"
	FaultRuleService.java
		Drools KieSession을 생성하여 이벤트 객체를 주입하고 모든 규칙을 실행합니다.
		실행 후 변환된 객체를 Redis 장애 카운터 및 DB 저장 로직으로 전달합니다.
	build.gradle (Drools 및 관련 의존성 추가)
		implementation 'org.kie:kie-api:7.74.0.Final'
		implementation 'org.drools:drools-core:7.74.0.Final'
		implementation 'org.drools:drools-compiler:7.74.0.Final'
		implementation 'org.drools:drools-decisiontables:7.74.0.Final'
		implementation 'javax.inject:javax.inject:1'
			kie-api: Drools 핵심 API
			drools-core: 규칙 실행 엔진
			drools-compiler: .drl 규칙 파일 컴파일
			drools-decisiontables: 엑셀 기반 규칙(Decision Table) 지원
			javax.inject: Drools 스프링 연동 시 필요한 DI(의존성 주입) 지원

4-2. Redis 장애 카운터
장애 이벤트가 일시적인지, 지속적인지를 판별하기 위해 Redis를 카운터 + TTL 기반 임시 저장소로 사용합니다
	RedisService.java
		Redis 연동 로직을 담당하는 서비스 클래스
	application.properties
		spring.redis.host=localhost
		spring.redis.port=6379
		spring.redis.timeout=60000
	build.gradle
		implementation 'org.springframework.boot:spring-boot-starter-data-redis'

4-3 장애 분류
키워드로 상태감지 서비스에서 제공해주고 있기에 FaultEventService에서 switch-case로 분류하였습니다.

4-4 MySQL 상태 저장/갱신
Drools Rule Engine과 Redis 장애 카운터를 거친 최종 장애 상태를 MySQL에 저장하고, 해당 CCTV의 최신 상태만 유지합니다.
정상 이벤트(*_OK)가 들어오면 해당 CCTV의 장애 기록을 삭제하여 복구 상태로 변경합니다.
	FaultEventEntity.java
		JPA 엔티티 클래스
		DB 테이블 컬럼과 매핑
	FaultEventRepository.java
		Spring Data JPA 기반의 CRUD 인터페이스
	FaultEventService.java
		장애 상태 저장 및 갱신 로직
		Drools와 Redis에서 처리한 이벤트를 받아 DB에 반영
		
4-5  Kafka alert-events Produce
MySQL에 장애 상태를 저장/갱신한 후, 장애 발생시 alert-events 토픽으로 알림 메시지를 전송합니다.
	FaultAlertDTO.java
		Kafka로 전송할 알림 데이터 구조
	KafkaFaultProducer.java
		Spring Kafka 기반 Producer 클래스
	KafkaProducerConfig.java
		KafkaTemplate, ProducerFactory 등 Kafka Producer 설정 정의

