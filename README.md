# 🔄 리턴즈 (Returns)
> 휴대폰 속 간편한 분실물 센터 앱

<img src="img/Returns_logo_inapp.png" width="20%" style="margin-top: -20px; margin-bottom: -20px;">

## 📌 프로젝트 개요

- **목표:** 누구나 실시간으로 쉽게 분실물을 등록하고, 습득물을 찾아갈 수 있는 직관적인 모바일 환경 제공
- **핵심 가치:** 불필요한 거래가 배제된 신뢰 기반의 반환 엔진,투명한 추적 시스템을 통해 회수 성공률을 극대화
- **주요 특징:** 닉네임 기반 간편 인증, 직관적인 UI/UX, 이미지 중심의 갤러리, 이벤트 기반 실시간 알림 시스템

## 🧑‍💻 팀원 소개
김서연 (이대 컴공 23)

문의혁 (카이스트 컴공 21)
    

## 🚀 주요 기능

### 1. 간편 접근 및 본인 인증 (Tab 0)
<img src="img/Returns_Show_view0.gif" width = "20%">


- **닉네임 로그인:** 별도의 회원가입 없이 닉네임(최대 8자, 중복 확인)만으로 서비스 이용 시작
- **작성자 식별:** 로그인과 동시에 디비에 사용자의 활동이 저장되어 닉네임을 기반으로 본인이 작성한 글에 대한 수정/관리 권한 부여 

### 2. 통합 리스트 홈 (Tab 1)
- 분실물(LOST)과 습득물(FOUND)을 리스트형식으로 볼 수 있는 메인 화면
  
<img src="img/Returns_Show_view1.gif" width = "20%">

- **손쉬운 탐색:**
    - **검색:** Pattern Matching 기반으로 SQL의 LIKE 연산자와 %query% 와일드카드를 결합하여, 제목뿐만 아니라 발견 장소, 특징 정보 등 다중 컬럼에 대한 부분 일치 검색을 지원
    - **필터:** 카테고리(지갑, 우산 등) 및 상태(보관중, 찾아감 등) 필터링 칩을 제공하여 사용자가 칩을 선택할 때마다 실시간으로 SQL WHERE 절을 재구성하여 복합적인 필터링 결과를 즉각적으로 도출
    - **정렬:** 모든 데이터는 생성 시점의 Timestamp를 기준으로 내림차순(DESC) 정렬되도록 Indexing
- **상태 시각화:** 각 아이템의 상태를 직관적인 색 및 태그등으로 표시.

<img src="img/Returns_Show_view1_comment.gif" width = "20%">

- **상세정보:** 해당 게시글 클릭시 상세정보 확인 가능
- **댓글:** 게시글에 댓글을 달아 습득자/분실자에게 연락 가능. 게시글 작성자는 게시물에 댓글이 달리면 상단에 즉시 실시간 팝업이 노출 (조건부 알림 트리거 로직 설정)
- **이벤트 기반 실시간 알림:** 실시간 이벤트로 변환하는 엔진을 자체 구현. DB I/O 작업으로 인해 UI 스레드가 블로킹되는 것을 방지하기 위해 Worker Thread에서 이벤트를 처리하고, runOnUiThread와 Handler를 활용하여 UI 스레드와의 동기화를 맞춰 비동기 멀티스레딩를 구축. 

### 3. 이미지 갤러리 (Tab 2)

<img src="img/Returns_Show_view2.gif" width = "20%">

- **이미지 중심 탐색:** 그리드 형태로 분실/습득물 사진을 모아보는 갤러리 뷰 제공.
- **요약 정보:** 썸네일 하단에 제목과 태그 표시.

### 4. 맞춤형 등록 프로세스 (Tab 3 & 4)

<img src="img/Returns_Show_view3.gif" width = "20%">

- **습득물 등록 (Tab 3):** 이미지, 습득 장소, 날짜, 현재 보관 장소, 상세특징 등 습득에 특화된 폼 제공.
- **분실물 등록 (Tab 4):** 이미지 , 잃어버린 장소, 날짜 특징등 분실에 특화된 폼 제공.
- **실시간 반영:** 등록 즉시 메인 리스트에 데이터 반영.

## 🛠️ Tech Stack

### Frontend & OS
- Framework: Android SDK (Java)
- UI/UX: Material Design Components, Fragment State Persistence
- System Integration: Edge-to-Edge Design (WindowInsets API)

### Data & Persistence
- Database: Room Persistence Library (SQLite 기반 Local-First 전략)
- Session Management: SharedPreferences (UserToken 및 세션 유지)
- Data Processing: Dynamic SQL Querying (다중 조건 필터링 엔진)

### Real-time & Logic
- Notification: Event-driven Notification Engine (자체 구현)
- Asynchronous: Worker Thread & Handler (UI 스레드 동기화 최적화)
- Image Handling: Glide (고성능 이미지 캐싱 및 렌더링)

### Architecture & Tools
- Architecture: Layered Architecture (SoC - View, Controller, Data 레이어 분리)
- Version Control: Git, GitHub
- Asset Management: Android Resources (Drawable, Layouts, Values)

## 🗺️ UX Flow
```java
A[앱 실행] --> B[닉네임 로그인] --> C[메인 화면 - Bottom Nav]


%% 메인 탭 구성
D --> T1[탭 1: 홈 리스트]
D --> T2[탭 2: 이미지 갤러리]
D --> T3[탭 3: 습득물 등록]
D --> T4[탭 4: 분실물 등록]

%% 상세 화면
T1 -- 아이템 클릭 --> DETAIL[아이템 상세 화면]
T2 -- 이미지 클릭 --> DETAIL[아이템 상세 화면]

%% 댓글 및 알림 로직
DETAIL -- 댓글 작성 --> LOGIC{본인 글인가?}
LOGIC -- No --> NOTI_DB[(알림 DB 저장)]
LOGIC -- Yes --> END((종료))

NOTI_DB --> POPUP[본인글에 댓글시 실시간 상단 팝업 노출]

%% 알림 모달 관리
D -- 사람 아이콘 클릭 --> MODAL[알림 목록 모달]
MODAL -- [확인] 클릭 --> DELETE[DB 삭제 및 모달 새로고침]
DELETE --> MODAL
MODAL -- 알림 내용 클릭 --> DETAIL_FROM_NOTI[모달 닫고 상세 화면 이동]
DETAIL_FROM_NOTI --> DETAIL

```

## APK 다운로드
[다운로드](https://github.com/cse2348/madcamp-2026-winter-Returns/raw/refs/heads/master/Returns.apk)
