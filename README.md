# 🏥 병원 정보 시스템 (Hospital Information System)

## 📋 프로젝트 개요
Spring Framework 기반의 병원 정보 조회 시스템으로, 공공데이터 API를 활용하여 병원, 약국, 응급실 정보를 제공합니다.

## 🛠️ 기술 스택
- **Backend**: Spring Framework 5.x
- **Database**: MariaDB 10.11
- **Frontend**: ~~Vue.js 3 + Vite~~ Flutter
- **Deployment**: Docker + Docker Compose
- **Web Server**: nginx (예정)
- **CI/CD**: GitHub Actions

## 🚀 주요 기능
- 🔍 **병원 검색**: 지역별 병원 정보 조회
- 💊 **약국 검색**: 주변 약국 정보 및 운영시간 확인
- 🚨 **응급실 정보**: 실시간 응급실 가용 병상 현황
- 👨‍⚕️ **전문의 정보**: 병원별 진료과목 및 전문의 조회
- 🗺️ **지도 연동**: 카카오맵 API를 통한 위치 정보 제공

## 📡 사용된 공공 API
- 🏥 병원정보서비스 API (보건복지부)
- 💊 약국정보서비스 API (보건복지부)
- 🚨 응급의료기관 조회 API (보건복지부)
- 🗺️ 카카오맵 API

## 🏗️ 아키텍처
```
🌐 Frontend (Flutter) 
    ↕️
🔧 Backend (Spring Framework)
    ↕️
🗄️ Database (MariaDB)
    ↕️
📡 External APIs (공공데이터 포털)
```

## 🐳 배포 환경
- **서버**: AWS EC2
- **컨테이너**: Docker Compose
- **자동 배포**: GitHub Actions

## 🔐 보안 관리
- GitHub Secrets를 통한 API 키 관리
- 환경변수 기반 설정 관리

## 📂 프로젝트 구조
```
hospital-system/
├── 🗂️ hospital_main/          # Spring Backend
├── 🗂️ frontend/               # Vue.js Frontend  
├── 🐳 docker-compose.prod.yml # 배포용 Docker 설정
├── ⚙️ .github/workflows/      # CI/CD 파이프라인
└── 📋 README.md
```

## 🚀 실행 방법
1. **개발 환경**
   ```bash
   # Backend 실행
   cd hospital_main
   ./mvnw spring-boot:run
   
   # Frontend 실행  
   cd frontend
   npm run dev
   ```

2. **프로덕션 배포**
   ```bash
   docker-compose -f docker-compose.prod.yml up -d
   ```

## 🔧 환경 설정
필요한 환경변수들을 `.env` 파일에 설정:
- 🔑 API 키들 (병원, 약국, 응급실)
- 🗄️ 데이터베이스 연결 정보
- 🌐 DuckDNS 도메인 설정

## 📊 API 엔드포인트
- `GET /api/hospitals` - 🏥 병원 목록 조회
- `GET /api/pharmacies` - 💊 약국 목록 조회  
- `GET /api/emergency` - 🚨 응급실 정보 조회
- `GET /api/proDoc` - 👨‍⚕️ 전문의 정보 조회

## 🤝 기여하기
1. Fork the repository
2. Create your feature branch (`git checkout -b feature/amazing-feature`)
3. Commit your changes (`git commit -m 'Add some amazing feature'`)
4. Push to the branch (`git push origin feature/amazing-feature`)
5. Open a Pull Request

## 📄 라이선스
This project is licensed under the MIT License.

---
💡 **Made with ❤️ for better healthcare accessibility**
