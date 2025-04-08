# Nio Netty Mock Framework

이 프로젝트는 Java NIO를 사용한 Netty 스타일의 비동기 서버 프레임워크의 최소 구현체입니다. 채널, 이벤트 루프, 인바운드 및 아웃바운드 이벤트를 처리하는 파이프라인과 같은 핵심 구성 요소를 포함합니다. 로깅은 SLF4J와 Logback을 백엔드로 사용하여 처리됩니다.

## 목차

- [Nio Netty Mock Framework](#nio-netty-mock-framework)
  - [목차](#목차)
  - [프로젝트 구조](#프로젝트-구조)
  - [의존성 및 빌드 구성](#의존성-및-빌드-구성)
    - [로깅 구성](#로깅-구성)
  - [구현된 기능](#구현된-기능)
    - [핵심 구성 요소](#핵심-구성-요소)
    - [작동 방식](#작동-방식)
  - [서버 실행 방법](#서버-실행-방법)
  - [이 프레임워크로 할 수 있는 일](#이-프레임워크로-할-수-있는-일)
  - [향후 개선 사항](#향후-개선-사항)
  - [라이선스](#라이선스)

## 프로젝트 구조

다음은 프로젝트의 디렉터리 트리 예시입니다:

```
NioNettyMock/
├── build.gradle
├── settings.gradle
├── README.md
└── src
    ├── main
    │   ├── java
    │   │   └── com
    │   │       └── example
    │   │           └── nionetty
    │   │               ├── bootstrap
    │   │               │   ├── ServerBootstrap.java
    │   │               │   └── ServerBootstrapRunner.java
    │   │               ├── buffer
    │   │               │   └── CustomBuffer.java
    │   │               ├── channel
    │   │               │   ├── AbstractChannel.java
    │   │               │   ├── Channel.java
    │   │               │   ├── ChannelConfig.java
    │   │               │   ├── ChannelException.java
    │   │               │   ├── ChannelFuture.java
    │   │               │   ├── ChannelHandler.java
    │   │               │   ├── ChannelHandlerContext.java
    │   │               │   ├── ChannelId.java
    │   │               │   ├── ChannelInboundHandler.java
    │   │               │   ├── ChannelInboundHandlerAdapter.java
    │   │               │   ├── ChannelInitializer.java
    │   │               │   ├── ChannelOption.java
    │   │               │   ├── ChannelOutboundHandler.java
    │   │               │   ├── ChannelOutboundHandlerAdapter.java
    │   │               │   ├── ChannelPipeline.java
    │   │               │   ├── ChannelPromise.java
    │   │               │   ├── DefaultChannelConfig.java
    │   │               │   ├── DefaultChannelHandlerContext.java
    │   │               │   ├── DefaultChannelId.java
    │   │               │   ├── DefaultChannelPipeline.java
    │   │               │   └── DefaultChannelPromise.java
    │   │               └── channel
    │   │                   └── nio
    │   │                       ├── NioEventLoop.java
    │   │                       ├── NioEventLoopGroup.java
    │   │                       ├── NioServerSocketChannel.java
    │   │                       └── NioSocketChannel.java
    │   └── resources
    │       └── logback.xml  *(또는, slf4j2.xml (시스템 속성에 따라))*
    └── test
        └── java
            └── (JUnit 테스트 클래스)
```

## 의존성 및 빌드 구성

프로젝트는 Gradle을 빌드 도구로 사용하며, 다음의 주요 의존성을 포함합니다:

- **SLF4J API:** `org.slf4j:slf4j-api:2.0.13`
- **Logback Classic:** `ch.qos.logback:logback-classic:1.5.6`
- **JUnit 5:** (테스트 용도로)

다음은 `build.gradle` 파일의 내용입니다:

```groovy
plugins {
    id 'java'
    id 'application'
}

group = 'com.example.nionetty'
version = '1.0-SNAPSHOT'

repositories {
    mavenCentral()
}

dependencies {
    implementation 'org.slf4j:slf4j-api:2.0.13'
    runtimeOnly 'ch.qos.logback:logback-classic:1.5.6'

    testImplementation platform('org.junit:junit-bom:5.10.2')
    testImplementation 'org.junit.jupiter:junit-jupiter-api'
    testRuntimeOnly 'org.junit.jupiter:junit-jupiter-engine'
}

application {
    mainClass = 'com.example.nionetty.bootstrap.ServerBootstrapRunner'
}

java {
    sourceCompatibility = JavaVersion.VERSION_17
    targetCompatibility = JavaVersion.VERSION_17
}

tasks.named('test') {
    useJUnitPlatform()
}

tasks.withType(JavaCompile) {
    options.encoding = 'UTF-8'
}
```

### 로깅 구성

Logback 구성 파일(`logback.xml` 또는 `slf4j2.xml`)은 `src/main/resources/`에 위치합니다:

```xml
<?xml version="1.0" encoding="UTF-8"?>
<configuration>
    <appender name="STDOUT" class="ch.qos.logback.core.ConsoleAppender">
        <encoder>
            <pattern>%d{yyyy-MM-dd HH:mm:ss.SSS} [%thread] %-5level %logger{36} - %msg%n</pattern>
        </encoder>
    </appender>

    <logger name="com.example.nionetty" level="DEBUG" additivity="false">
        <appender-ref ref="STDOUT" />
    </logger>

    <root level="INFO">
        <appender-ref ref="STDOUT" />
    </root>
</configuration>
```

> **참고:**  
> 비표준 구성 파일명을 사용하는 경우 (예: `slf4j2.xml`), 시스템 속성 `-Dlogback.configurationFile=slf4j2.xml`를 설정하세요.

## 구현된 기능

### 핵심 구성 요소

- **채널 및 관련 클래스:**  
  - `Channel` 인터페이스는 기본 동작(바인딩, 종료, 쓰기 등)을 정의합니다.  
  - `AbstractChannel`은 채널 ID, 설정, 파이프라인 및 채널 종료를 기다리는 Future 등의 공통 기능을 제공합니다.  
  - `NioServerSocketChannel`과 `NioSocketChannel`과 같은 구체적 구현체는 Java NIO 채널을 사용합니다.

- **ChannelFuture 및 Promise:**  
  비동기 작업은 `ChannelFuture`를 반환하여 다음을 수행할 수 있습니다:
  - 작업 완료 여부 확인
  - `sync()`를 사용하여 완료될 때까지 대기
  - 완료 후 리스너 추가 (추후 확장이 가능함)

- **채널 파이프라인 및 핸들러:**  
  프레임워크는 인바운드 및 아웃바운드 이벤트를 순차적으로 처리하는 파이프라인 모델을 지원합니다.  
  - `ChannelPipeline`은 `ChannelHandler` 객체의 목록을 관리합니다.  
  - 인바운드 이벤트는 예를 들어 수신된 메시지를 에코하는 `EchoServerHandler`와 같은 `ChannelInboundHandler` 구현체를 통해 처리됩니다.  
  - 아웃바운드 이벤트는 `ChannelOutboundHandler` 클래스가 관리합니다.

- **이벤트 루프 및 이벤트 루프 그룹:**  
  - `EventLoop` 인터페이스는 NIO `Selector`를 통해 등록된 채널의 이벤트를 모니터링하는 루프를 정의합니다.  
  - `SingleThreadEventLoop`는 단일 스레드에서 I/O 이벤트를 처리하는 `EventLoop` 인터페이스를 구현합니다.  
  - `NioEventLoopGroup`은 여러 `NioEventLoop` 인스턴스를 관리하며, 작업을 분배합니다 (현재 라운드 로빈 방식).

### 작동 방식

1. **서버 부트스트랩:**  
   `ServerBootstrap` 클래스는 이벤트 루프 그룹, 채널 클래스, 채널 초기화 핸들러를 설정합니다.  
   `bind(port)` 메서드 호출 시:
   - 리플렉션을 사용하여 새로운 채널을 생성합니다.
   - 생성된 채널이 `NioServerSocketChannel`인 경우, 워커 그룹(workerGroup)을 설정합니다.
   - 제공된 `ChannelInitializer`가 파이프라인에 추가됩니다.
   - 채널은 지정한 포트에 바인딩됩니다 (InetSocketAddress 사용).

2. **채널 동작:**  
   - **바인딩 및 수락:**  
     `NioServerSocketChannel`은 Java NIO의 `ServerSocketChannel`을 열고, 논블로킹 모드로 설정하며, 로컬 주소에 바인딩합니다. 이후, 별도의 어셉터 스레드를 시작하여 클라이언트 연결을 지속적으로 수락합니다.  
   - **클라이언트 채널 등록:**  
     수락된 클라이언트 연결은 `NioSocketChannel`로 감싸지고, 워커 그룹의 이벤트 루프에 등록됩니다.
   - **데이터 처리:**  
     클라이언트 채널에 데이터가 도착하면, 이벤트 루프는 소켓 채널에서 데이터를 읽고, 이를 문자열로 변환하여 `fireChannelRead()`를 통해 채널 파이프라인으로 전달하며, 이어서 `fireChannelReadComplete()`를 호출합니다.

3. **파이프라인 처리:**  
   `ChannelPipeline` 클래스는 각 등록된 핸들러에 이벤트를 순차적으로 전달합니다. 사용자는 사용자 지정 `ChannelHandler` 구현체를 추가하여 데이터를 처리하거나 변환할 수 있습니다.

## 서버 실행 방법

Gradle을 사용하여 애플리케이션을 빌드하고 실행합니다:

```bash
./gradlew clean build
./gradlew run
```

로그에는 다음과 같은 메시지가 표시되어야 합니다:
- 서버 부트스트랩이 시작되었음을 나타내는 메시지
- 서버 소켓 채널이 포트 8080에 바인딩되었음을 나타내는 메시지
- 서버가 활성 상태를 유지하며 클라이언트 연결을 수락하고 로그를 출력함 (채널 종료 Future에서 대기 중)

## 이 프레임워크로 할 수 있는 일

이 최소 Netty 스타일 프레임워크를 사용하면 다음과 같은 작업을 수행할 수 있습니다:
- **비동기 서버 구축:**  
  블로킹 스레드 없이 I/O 이벤트를 비동기적으로 처리하는 서버를 구축할 수 있습니다.

- **이벤트 처리 커스터마이징:**  
  자체 `ChannelHandler` 구현체를 작성하여 파이프라인에 추가함으로써, 프로토콜 파싱, 데이터 변환, 로깅 또는 사용자 지정 비즈니스 로직 등의 작업을 처리할 수 있습니다.

- **핵심 기능 확장:**  
  다음과 같은 추가 기능을 구현하거나 지원할 수 있습니다:
  - 보다 견고한 오류 처리
  - 쓰기 작업에 대한 백프레셔(Back-pressure) 및 흐름 제어
  - `ChannelConfig`와 `ChannelOption`을 통한 사용자 정의 설정 옵션
  - 연결 풀링 또는 SSL/TLS 지원

- **논블로킹 I/O 패턴 학습:**  
  Java NIO를 사용하여 비동기 네트워크 작업이 어떻게 구현될 수 있는지, 그리고 이벤트 루프와 파이프라인이 어떻게 동작하는지 학습할 수 있습니다.

## 향후 개선 사항

현재의 구현은 기본 기능을 다루고 있으나, 향후 다음과 같은 개선이 필요할 수 있습니다:
- 핸들러 컨텍스트를 적절히 관리하는 보다 완전한 채널 파이프라인 구현
- 다양한 스레딩 모델 지원 및 이벤트 루프 분배 전략 개선
- 타임아웃 및 하트비트 메커니즘 추가
- 포괄적인 단위 및 통합 테스트 도입

## 라이선스

이 프로젝트는 학습 및 개발 목적을 위해 제공됩니다. 코드를 자유롭게 사용 및 수정할 수 있습니다.