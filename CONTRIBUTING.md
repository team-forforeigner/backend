# 🛠️ CONTRIBUTING GUIDE

ForForeigner 프로젝트에 기여해주셔서 감사합니다!  
아래 규칙에 따라 작업해 주세요.

---

## ⚠️ 리포지토리 사용 시 주의사항

1. **절대 `main` 브랜치에 직접 Push하지 마세요.**
   - 모든 작업은 `develop` 브랜치 또는 기능별 브랜치(`feature/`)에서 진행합니다.
   - 최종 Merge는 Pull Request를 통해 `develop` → `main`으로 병합합니다 (ex. 배포 시).

2. **기능 작업 시에는 반드시 새 브랜치를 만들어 작업하세요.**
   - 브랜치명 규칙:
     - 기능 추가: `feature/기능명` (예: `feature/login-page`)
     - 버그 수정: `fix/버그명` (예: `fix/navbar-overlap`)
     - 문서 수정: `docs/수정내용` (예: `docs/update-readme`)

3. **커밋 메시지 컨벤션을 지켜주세요.**
   - 예시:
     - `feat: 로그인 페이지 UI 추가`
     - `fix: 비밀번호 오류 수정`
     - `docs: README 업데이트`

4. **PR(Pull Request)은 항상 리뷰를 받은 후 Merge합니다.**
   - PR 제목은 작업 내용을 명확히 작성하고, 관련 이슈가 있다면 `Closes #이슈번호`를 포함해주세요.

5. **불필요한 파일은 .gitignore에 추가하고, 커밋하지 마세요.**
   - 예: `node_modules/`, `.env`, `build/`, `*.log` 등

6. **작업 완료 후 해당 작업 이슈와 Projects 보드도 함께 업데이트해주세요.**

7. **코드를 변경할 때는 항상 `git pull`로 최신 상태를 먼저 반영한 후 작업을 시작하세요.**
   - 충돌 방지를 위해 중요합니다.


## 📌 브랜치 전략

- `main`: 배포용 브랜치 (절대 직접 Push 금지)
- `develop`: 개발 브랜치 (여러 기능 통합용)
- 기능 브랜치: `feature/기능명`
- 버그 수정: `fix/버그명`
- 문서 작업: `docs/설명`

##### 예시
git commit -m "feat: 로그인 폼 UI 구현"

## 🤝 협업 방법 및 작업 순서

1. 항상 `develop` 브랜치로 이동해 최신 코드를 받아옵니다.

```bash
git checkout develop
git pull origin develop
```

2. 새 기능 개발이나 버그 수정을 위해 기능 브랜치를 만듭니다.

```bash
git checkout -b feature/기능명
```

3. 작업 완료 후 변경사항을 스테이징하고 커밋합니다.

```bash
git add .
git commit -m "feat: 작업 내용 요약"
```

4. 원격 저장소에 기능 브랜치를 푸시합니다.

```bash
git push origin feature/기능명
```

5. GitHub에서 develop 브랜치로 Pull Request를 생성하고 팀원 리뷰를 받습니다.

6. 리뷰 요청 사항을 반영해 수정한 뒤 다시 푸시합니다.

7. PR이 승인되면 develop 브랜치에 병합합니다.

8. 작업 종료 후 다시 develop 브랜치로 돌아가 최신 상태를 받아 작업을 시작합니다.

```bash
git checkout develop
git pull origin develop
``` 
