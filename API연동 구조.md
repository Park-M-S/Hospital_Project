# API 연동 구조 및 흐름

## 디렉토리 구조

```plaintext
com.example.project
├── controller
│   └── HospitalApiController.java       # 클라이언트 요청 처리
│
├── service
│   └── HospitalApiService.java          # 비즈니스 로직, 호출-파싱 연동
│
├── api
│   └── HospitalApiCaller.java        # 외부 API 요청 (REST 또는 XML 기반)
│
├── parser
│   └── HospitalApiParser.java        # XML/JSON 응답을 DTO로 변환
│
├── dto
│   ├── HospitalApiResponse.java        # 외부 XML 구조를 그대로 매핑
│   └── HospitalApiItem.java              # 클라이언트에 제공할 최종 형태의 DTO
│
└── config
    └── AppConfig.java      # Bean 설정
```

## 흐름 요약
```
[외부 API 응답(XML)]
       ↓
[HospitalApiCaller]         # 외부 API 요청 수행
       ↓
[HospitalApiParser]         #  응답을 Java 객체(HospitalApiResponse)로 파싱
       ↓
[HospitalApiResponse]         # 응답 구조와 매핑된 임시 DTO
       ↓
[HospitalApiItem]             # 개별 객체 구조화 DTO
       ↓
[HospitalApiService]           # 비즈니스 로직 수행 및 변환
       ↓
[HospitalApiEntity]            # DB 저장용 Entity 변환
       ↓
[HospitalApiRepository / DAO]  # DB에 저장 (Insert)
```



