# Easy Refuel

Minecraft Fabric 1.21.4 모드 - 지정된 영역 내 화로에 간편하게 연료를 공급합니다.

## 기능

### 영역 지정
- 나무 도끼로 영역 지정
  - 좌클릭: 첫 번째 좌표 설정
  - 우클릭: 두 번째 좌표 설정
- 영역 검증 (1~20개 화로만 허용)

### 연료 공급
- P키 (기본값)를 눌러 활성화된 영역의 화로에 연료 공급
- 플레이어로부터 5.5블록 이내의 화로만 처리
- 영역별 개별 연료 타입 설정 가능
- 서버 측 모드 불필요 (멀티플레이 호환)

### 작업 취소
- P키 재입력: 즉시 취소
- 이동: 0.5블록 이상 이동 시 자동 취소
- ESC 키: 화면 닫힘 감지 시 자동 취소

### 설정
- Cloth Config를 통한 GUI 설정 (한국어/영어 지원)
- 틱 딜레이 조정 (1~30틱)
- 연료 슬롯 처리 모드 선택
- 인벤토리 부족 시 동작 설정
- 설정 화면에서 영역 활성화/비활성화 및 삭제 가능

## 명령어

- `/easyrefuel create <name> <fuel_type>` - 선택된 영역으로 새 영역 생성
- `/easyrefuel list` - 모든 영역 목록 표시
- `/easyrefuel delete <name>` - 영역 삭제
- `/easyrefuel toggle <name>` - 영역 활성화/비활성화
- `/easyrefuel setfuel <name> <fuel_type>` - 영역의 연료 타입 변경

모든 명령어는 Tab 자동완성을 지원합니다. 연료 타입과 영역 이름이 자동 제안됩니다.

## 연료 타입 예시

- `minecraft:coal` - 석탄
- `minecraft:charcoal` - 목탄
- `minecraft:coal_block` - 석탄 블록
- `minecraft:bamboo` - 대나무
- `minecraft:lava_bucket` - 용암 양동이
- `minecraft:blaze_rod` - 블레이즈 막대
- `minecraft:dried_kelp_block` - 말린 켈프 블록
- `minecraft:stick` - 막대기

## 설치

1. Fabric Loader 0.18.0 이상 설치
2. Fabric API 설치
3. Cloth Config 설치
4. Mod Menu 설치 (선택, 설정 화면 접근용)
5. 본 모드를 mods 폴더에 배치

## 개발 환경

- Minecraft 1.21.4
- Fabric Loader 0.18.0
- Fabric API 0.119.4+1.21.4
- Cloth Config 17.0.142
- Mod Menu 13.0.3
- Java 21

## 빌드

```bash
./gradlew build
```

빌드된 JAR 파일은 `build/libs/` 폴더에 생성됩니다.

## 라이선스

All Rights Reserved - 자세한 내용은 [LICENSE.md](LICENSE.md)를 참조하세요.
