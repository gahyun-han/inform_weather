# 날씨 코디 (Weather Outfit) 📱👗

> **오늘 날씨에 맞게 옷 입은 캐릭터가 코디를 추천해주는 Android 앱**

---

## 📖 앱 소개

**날씨 코디**는 매일 아침 날씨를 확인하고, 날씨에 딱 맞는 코디를 캐릭터가 직접 입어서 보여주는 스마트한 날씨 앱입니다.

- 🌤️ **날씨 캐릭터**: 오늘 날씨(기온, 강수, 바람)에 맞는 옷을 입은 캐릭터가 잠금화면/홈화면에 표시
- 👚 **MY CLOSET**: 내 옷을 사진으로 찍어 저장하고, 캐릭터가 내 옷으로 코디
- 📝 **피드백 시스템**: "추웠다 / 적당했다 / 더웠다" 피드백으로 맞춤 온도 학습
- 🎯 **개인화 추천**: 피드백 기록을 분석해 나만의 체감온도 프로필 생성

---

## 🏗️ 프로젝트 구조

```
app/
├── src/main/
│   ├── java/com/weather/outfit/
│   │   ├── MainActivity.kt              # 메인 화면 (날씨 + 캐릭터)
│   │   ├── OnboardingActivity.kt        # 온보딩 (이름, 민감도 설정)
│   │   ├── ClosetActivity.kt            # MY CLOSET 화면
│   │   ├── FeedbackActivity.kt          # 코디 피드백 화면
│   │   ├── ClothingDetailActivity.kt    # 옷 상세보기
│   │   ├── WeatherApp.kt               # Application 클래스
│   │   ├── adapter/
│   │   │   └── ClothingAdapter.kt      # 옷장 RecyclerView 어댑터
│   │   ├── api/
│   │   │   ├── WeatherApiService.kt    # OpenWeatherMap Retrofit 서비스
│   │   │   └── WeatherResponse.kt      # API 응답 데이터 클래스
│   │   ├── data/
│   │   │   ├── model/                  # 데이터 모델
│   │   │   │   ├── ClothingItem.kt
│   │   │   │   ├── OutfitFeedback.kt
│   │   │   │   ├── OutfitRecommendation.kt
│   │   │   │   ├── UserPreference.kt
│   │   │   │   └── WeatherData.kt
│   │   │   ├── db/                     # Room 데이터베이스
│   │   │   │   ├── AppDatabase.kt
│   │   │   │   ├── ClothingDao.kt
│   │   │   │   ├── FeedbackDao.kt
│   │   │   │   ├── UserPreferenceDao.kt
│   │   │   │   ├── WeatherCacheDao.kt
│   │   │   │   └── Converters.kt
│   │   │   └── repository/             # 데이터 레포지토리
│   │   │       ├── ClothingRepository.kt
│   │   │       ├── FeedbackRepository.kt
│   │   │       └── WeatherRepository.kt
│   │   ├── ui/                         # ViewModel
│   │   │   ├── MainViewModel.kt
│   │   │   ├── ClosetViewModel.kt
│   │   │   └── FeedbackViewModel.kt
│   │   ├── util/                       # 유틸리티
│   │   │   ├── OutfitRecommender.kt   # 코디 추천 엔진
│   │   │   ├── TemperatureAnalyzer.kt # 체감온도 분석기
│   │   │   ├── ImageUtils.kt          # 이미지 처리
│   │   │   └── DateUtils.kt
│   │   ├── widget/
│   │   │   └── WeatherWidgetProvider.kt # 홈화면 위젯
│   │   ├── worker/
│   │   │   └── WeatherUpdateWorker.kt  # 백그라운드 날씨 업데이트
│   │   └── receiver/
│   │       └── BootReceiver.kt         # 부팅 시 작업 재스케줄
│   └── res/
│       ├── layout/                     # XML 레이아웃
│       ├── values/                     # 색상, 문자열, 테마
│       ├── drawable/                   # 배경, 아이콘, 캐릭터 그래픽
│       └── xml/                        # 위젯 설정, 파일 경로
```

---

## 🚀 시작하기

### 1. 필수 요구사항

- Android Studio Hedgehog (2023.1.1) 이상
- Android SDK 26 (Android 8.0 Oreo) 이상
- Kotlin 1.9.10
- OpenWeatherMap API Key ([무료 발급](https://openweathermap.org/api))

### 2. 설정

```bash
# 1. 저장소 클론
git clone https://github.com/gahyun-han/inform_weather.git
cd inform_weather

# 2. app/build.gradle 에서 API Key 설정
# buildConfigField "String", "WEATHER_API_KEY", '"여기에_API_키_입력"'
```

### 3. Android Studio에서 열기

1. Android Studio → `Open` → `inform_weather` 폴더 선택
2. Gradle Sync 대기
3. 에뮬레이터 또는 실제 기기에서 실행

---

## ✨ 주요 기능

### 🌡️ 날씨 기반 코디 추천

| 기온 | 추천 코디 |
|------|---------|
| 28°C+ | 민소매/반팔 + 반바지 |
| 23-27°C | 반팔 + 얇은 면바지 |
| 20-22°C | 얇은 긴팔 + 면바지 |
| 17-19°C | 긴팔 + 얇은 카디건 |
| 12-16°C | 자켓/청자켓 |
| 9-11°C | 트렌치코트/야상 |
| 5-8°C | 울코트 + 니트 |
| 0-4°C | 두꺼운 패딩 |
| -1°C 이하 | 롱패딩 + 목도리 + 장갑 |

### 👗 MY CLOSET 기능

- 카메라/갤러리로 내 옷 사진 등록
- 카테고리 분류: 상의, 하의, 아우터, 신발, 악세서리
- 보온성 레벨(1~5) 및 적정온도 범위 설정
- 오늘 날씨에 맞는 내 옷으로 자동 코디 매칭

### 📊 맞춤형 학습 시스템

```
사용자 피드백 → 체감온도 보정값 계산 → 다음날 코디 추천에 반영

예시:
- 7°C에 코트 입었는데 "추웠다" 피드백 3번
- → 다음부터 7°C에서는 한 단계 더 따뜻한 코디 추천
```

### 📱 홈화면 위젯

- 날씨 정보 + 캐릭터 미리보기를 홈화면/잠금화면에 표시
- 30분마다 자동 업데이트

### 🔔 아침 알림

- 설정한 시간에 오늘 코디 추천 알림 발송
- 알림 탭 시 앱으로 바로 이동

---

## 🔧 기술 스택

| 분류 | 기술 |
|------|------|
| 언어 | Kotlin |
| 아키텍처 | MVVM + Repository Pattern |
| 데이터베이스 | Room (SQLite) |
| 네트워크 | Retrofit2 + OkHttp3 |
| 이미지 | Glide |
| 비동기 | Kotlin Coroutines + LiveData |
| 백그라운드 | WorkManager |
| 위치 | FusedLocationProviderClient |
| 날씨 API | OpenWeatherMap |
| DI | 수동 의존성 주입 |

---

## 📁 캐릭터 그래픽 가이드

현재 `character_outfit_mild.xml`은 플레이스홀더 벡터 그래픽입니다.
실제 배포 시 다음 파일들을 일러스트레이터 작업으로 교체해야 합니다:

```
res/drawable/
├── character_outfit_hot.png          # 28°C+
├── character_outfit_warm.png         # 23-27°C
├── character_outfit_mild_warm.png    # 20-22°C
├── character_outfit_mild.png         # 17-19°C
├── character_outfit_cool.png         # 12-16°C
├── character_outfit_chilly.png       # 9-11°C
├── character_outfit_cold.png         # 5-8°C
├── character_outfit_very_cold.png    # 0-4°C
├── character_outfit_freezing.png     # -1°C 이하
├── character_outfit_hot_rain.png     # 비 버전들...
└── ...
```

추천 일러스트 도구:
- [Adobe Illustrator](https://adobe.com/illustrator)
- [Procreate](https://procreate.com) (iPad)
- [Avataaars Generator](https://getavataaars.com)

---

## 🤝 기여하기

1. Fork the repository
2. Create feature branch: `git checkout -b feature/my-feature`
3. Commit changes: `git commit -m 'feat: Add my feature'`
4. Push to branch: `git push origin feature/my-feature`
5. Open a Pull Request

---

## 📄 라이선스

MIT License © 2026 gahyun-han
