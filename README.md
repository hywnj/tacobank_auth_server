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

## **🛠️ 기술 스택**  
- **Java 17**  
- **Spring Boot 3.3**  
- **Spring Security 6.3**  
- **JWT (JSON Web Token)**  
- **Redis 7.2**  
- **Gradle 8.10**  

---

## **🔐 보안 정책**  
- **HttpOnly 쿠키**를 사용해 XSS 공격 방지  
- **Redis 블랙리스트**로 토큰 무효화 관리  
- **Bcrypt 해싱**으로 비밀번호 안전 저장  

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

