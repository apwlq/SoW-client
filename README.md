# Screen over Wired/Wireless

## 소개
이 프로젝트는 사용자가 자신의 화면을 원격 장치로 무선으로 미러링할 수 있는 애플리케이션입니다. Kotlin(jvm)을 사용하여 개발되었으며, Windows, Linux, macOS에서 모두 작동합니다. 클라이언트-서버 아키텍처를 사용하여 화면을 미러링하며, 서버 애플리케이션이 화면을 캡처하여 클라이언트 애플리케이션으로 전송하면 클라이언트는 이를 원격 장치에서 표시합니다.

UDP 멀티캐스트 연결을 통해 서버에서 클라이언트로 화면 데이터 및 마우스 데이터를 전송합니다. 이로써 장치에서 원격 장치로 디스플레이를 미러링할 수 있습니다. 애플리케이션은 설치가 필요 없으며, 사용하기 쉽고 반응성이 뛰어나게 느껴집니다. 낮은 ~~마우스~~ 지연 시간과 높은 ~~마우스~~ 프레임 속도를 자랑하며, 오픈 소스로 무료로 제공됩니다.

현재 개발 중인 상태로, 버그나 문제가 있을 수 있으니 GitHub Issues에 보고해 주시기 바랍니다.

## 사용 방법
1. 서버 애플리케이션을 실행합니다.
2. 클라이언트 애플리케이션을 실행합니다.
3. 서버 애플리케이션의 IP 주소를 클라이언트 애플리케이션에 입력합니다.
4. 연결 버튼을 클릭하여 서버 애플리케이션과 연결합니다.

## [Updater.jar](https://github.com/apwlq/Updater) 에서 사용 방법
```yaml
github_repo: https://github.com/apwlq/SoW-server.git
auto_update: true
start_command: java -jar runner.jar
version_command: java -jar runner.jar --version
download_file: sow_server.jar
save_logs: false
```

## 기여 방법
이 프로젝트는 여러분의 기여를 환영합니다. 이 프로젝트에 기여하려면 다음 단계를 따르십시오.

1. 이 저장소를 `포크`합니다.
2. 새로운 `브랜치`를 만듭니다. (`git checkout -b feature/fooBar`)
3. 변경 사항을 `커밋`합니다. (`git commit -am 'Add some fooBar'`)
4. 브랜치에 `푸시`합니다. (`git push origin feature/fooBar`)
5. `풀 리퀘스트`를 만듭니다.
6. 리뷰를 받습니다.
7. 머지됩니다!
