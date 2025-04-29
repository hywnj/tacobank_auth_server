# TacoBank - Auth Server (인증 서버)  
타코뱅크는 영수증 OCR 인식을 통해 품목별 정산 기능을 제공하는 오픈뱅킹 서비스 프로젝트입니다.<br>
[↗️TacoBank 프로젝트 바로가기 ](https://github.com/TacoBankOrg/TacoBank)

타코뱅크는 다음과 같이 두 개의 주요 서버로 구성됩니다 :
1. **인증 서버 (`auth_server`)**: 사용자 인증 및 토큰 기반 보안을 담당
2. **코어 서버 (`core_server`)**: 송금, 문자인증, 멤버 관리, 영수증 더치페이 기능과 같은 비즈니스 로직을 담당
<br>

## **📌 개요**  
`auth_server`는 사용자 인증 및 보안을 담당하는 서버입니다. <br>
JWT 토큰과 Redis를 활용하여 안전한 인증 시스템을 제공합니다.  

---

## **✨ 주요 기능**  
- **회원가입**: 비밀번호 유효성 검사를 통해 안전한 사용자 정보 저장  
- **로그인**: JWT 토큰을 HttpOnly 쿠키에 저장하여 보안 강화  
- **로그아웃**: Redis를 활용한 토큰 블랙리스트 관리로 무효화된 토큰 차단  
- **로그인 실패 잠금 (Account Lock)**: 5회 이상 로그인 실패 시 계정을 10분간 잠금  
- **토큰 관리**: Redis를 사용해 토큰 무효화 및 인증 상태 유지  

---

## **🛠️ 기술 스택 요약**  
- **Java 17**  
- **Spring Boot 3.3**  
- **Spring Security 6.3**  
- **JWT (JSON Web Token)**  
- **Redis 7.2**  
- **Gradle 8.10**  

---

## **⚒️ 백엔드 설계 전략**
> 백엔드는 확장성과 금융 서비스, 협업, 유지보수 등을 고려하여 설계했습니다.

### **1. 기술 스택**
- **Java 17**
   - **LTS(Long-Term Support)**: 2029년까지 장기 지원 예정으로 안정적이고 미래 지향적
   - **최신 기능**: Java 8 대비 성능 최적화 및 마이그레이션 비용 절감
   - **Spring Boot 3.x 호환성**: Java 17 이상을 요구하는 Spring Boot 3.x를 지원
   - **금융 서비스 적합성**: 높은 안정성과 성능을 바탕으로 금융 시스템 운영에 적합

- **Spring Boot 3.x + JPA (ORM)**
   - **빠른 개발 속도**: 내장 도구로 신속한 개발을 지원합니다
   - **보안성**: SQL Injection 방지 및 배포 전 오류 최소화

### **2. 아키텍처 설계**
- **서버 분리 설계**:
  - **인증 서버**와 **코어 서버**로 분리하여 설계
  - **장점**:
    - 트래픽 분산
    - 독립적 관리로 유지보수 용이성 증가
    - 장애 발생 시 서비스 전체 영향을 최소화

- **DDD (도메인 중심 설계) + 레이어드 아키텍처**:
  - 도메인 중심 접근법으로 송금, 정산과 같은 핵심 비즈니스 로직을 설계
  - 코드의 역할 분리가 명확해지고, 테스트 및 유지보수가 쉬워지고, 각 계층의 책임이 독립적이라 확장과 변경에 용이
  - **Layer**:
    - **Presentation Layer**: API 엔드포인트
    - **Application Layer**: 비즈니스 로직 
    - **Domain Layer**: 핵심 비즈니스 로직과 규칙이 포함된 도메인 엔티티 및 값 객체
    - **Infrastructure Layer**: 시스템 외부와의 통신, 보안 설정, 유틸리티 제공 등 지원 역할

---

## **🔐 보안 정책**  
- **HttpOnly 쿠키**: 클라이언트 측 스크립트로 JWT 접근 차단
- **토큰 블랙리스트**: Redis를 활용해 만료 토큰을 관리
- **Bcrypt 해싱**: 비밀번호를 안전하게 해싱 처리
- **RBAC(Role-Based Access Control)**
  - **사용자 권한(ROLE_USER)**: 기본 기능 접근 가능
  - **관리자 권한(ROLE_ADMIN)**: 관리자 페이지는 현재 미구현 상태이나, 역할 기반 제한은 구현 완료

---

## **📂 프로젝트 구조**  
```plaintext
src/
├── main/
│   ├── java/
│   │   └── com.almagest_dev.tacobank_auth_server/
│   │       ├── TacobankAuthServerApplication.java   # 메인 애플리케이션 실행 클래스
│   │       ├── auth/
│   │       │   ├── application/    # 비즈니스 로직 서비스 계층
│   │       │   │   └── service/
│   │       │   ├── domain/         # 도메인 모델 및 리포지토리
│   │       │   │   ├── model/
│   │       │   │   └── repository/
│   │       │   ├── infrastructure/ # 인프라 및 보안 설정
│   │       │   │   ├── config/
│   │       │   │   ├── persistence/
│   │       │   │   ├── s3/
│   │       │   │   └── security/
│   │       │   │       ├── authentiactaion/
│   │       │   │       └── handler/
│   │       │   └── presentation/   # API 컨트롤러 및 DTO
│   │       │       ├── controller/
│   │       │       └── dto/
│   │       └── common/             # 공통 모듈 및 유틸리티
│   │           ├── constants/      # 공통 상수 (Redis Key Prefix)
│   │           ├── controller/     # 공통 컨트롤러 (Health Check)
│   │           ├── dto/            # 공통 DTO
│   │           ├── exception/      # 공통 예외 처리
│   │           └── util/           # 공통 유틸리티 클래스
│   └── resources/
│       ├── application.yml         # 메인 환경 설정
│       ├── application-{서버명}.yml  # 서버별 환경 설정
│       └── logback-spring.xml      # 로깅 설정
│
└── test/
    └── java/
        └── com.almagest_dev.tacobank_auth_server/
            └── AuthServiceTest.java         # AuthService 회원가입 테스트
```
---

## **💬 문의**  
- **담당자**: Hyewon Ju ([GitHub](https://github.com/hywnj))  
- **이메일**: jhjsjym@naver.com

