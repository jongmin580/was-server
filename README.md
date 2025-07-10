# WAS 서버 개발
JAVA 11 Version Use, Maven 3.1.0 Version Use
# 패키지 구조
wasserver
│── src
│   ├── main
│   │   ├── java
│   │   │   ├── config
│   │   │   │   ├── ConfigLoader.java       # JSON 설정 파일을 로드하는 유틸리티
│   │   │   ├── http
│   │   │   │   ├── HttpRequest.java        # HTTP 요청을 다루는 인터페이스
│   │   │   │   ├── HttpResponse.java       # HTTP 응답을 다루는 인터페이스
│   │   │   │   ├── SimpleHttpRequest.java  # HttpRequest 구현체
│   │   │   │   ├── SimpleHttpResponse.java # HttpResponse 구현체
│   │   │   ├── server
│   │   │   │   ├── HttpServer.java         # HTTP 서버 실행 및 요청 리스닝
│   │   │   │   ├── RequestProcessor.java   # HTTP 요청을 분석하고 처리하는 클래스
│   │   │   ├── servlet
│   │   │   │   ├── SimpleServlet.java      # 서블릿 인터페이스
│   │   │   │   ├── CurrentTimeServlet.java # 현재 시각을 출력하는 서블릿
│   │   │   ├── service
│   │   │   │   ├── Hello.java              # Hello 서블릿 (비즈니스 로직 포함)
│   │   │   ├── sample
│   │   │   │   ├── Hello.java              # 샘플 Hello 서블릿
│   │   │   ├── (default package)
│   │   │   │   ├── Hello.java              # 패키지가 없는 Hello 서블릿 (정리 필요)
│   │   ├── resources
│   │   │   ├── config.json     # host별 서버 설정(JSON)
│   │   │   ├── logback.xml     # Logback 설정 파일
│   ├── test
│   │   ├── java
│   │   │   ├── (default package)
│   │   │   │   ├── TestCase.java # JUnit 테스트 케이스
# 구현 기능 목록
1. HTTP/1.1의 Host 헤더를 해석하세요.  ✔️ 구현 완료
	o 예를 들어, a.com과 b.com의 IP가 같을지라도 설정에 따라 서버에서 다른 데이터를 제공할 수 있어야 합니다.
	o 아파치 웹 서버의 VirtualHost 기능을 참고하세요.

2. 다음 사항을 설정 파일로 관리하세요.  ✔️ 구현 완료
	o 파일 포맷은 JSON으로 자유롭게 구성하세요. 
	o 몇 번 포트에서 동작하는지 
	o HTTP/1.1의 Host별로 
		▪ HTTP_ROOT 디렉터리를 다르게 
		▪ 403, 404, 500 오류일 때 출력할 HTML 파일 이름

3. 403, 404, 500 오류를 처리합니다. ✔️ 구현 완료
	o 해당 오류 발생 시 적절한 HTML을 반환합니다. 
	o 설정 파일에 적은 파일 이름을 이용합니다. 

4. 다음과 같은 보안 규칙을 둡니다. ✔️ 구현 완료
	o 다음 규칙에 걸리면 응답 코드 403을 반환합니다. 
		▪ HTTP_ROOT 디렉터리의 상위 디렉터리에 접근할 때, 예) http://localhost:8000/../../../../etc/passwd 
		▪ 확장자가 .exe인 파일을 요청받았을 때 
	o 추후 규칙을 추가할 것을 고려해주세요. 

5. logback 프레임워크 http://logback.qos.ch/를 이용하여 다음의 로깅 작업을 합니다. ✔️ 구현 완료
	o 로그 파일을 하루 단위로 분리합니다. 
	o 로그 내용에 따라 적절한 로그 레벨을 적용합니다. 
	o 오류 발생 시, StackTrace 전체를 로그 파일에 남깁니다.

6. 간단한 WAS를 구현합니다. ✔️ 부분 구현 완료 (설정 파일 이용하지 않음)
	o 다음과 같은 SimpleServlet 구현체가 동작해야 합니다. 
		▪ 다음 코드에서 SimpleServlet, HttpRequet, HttpResponse 인터페이스나 객체는 여러분이 보다 구체적인 인터페이스나 구현체를 제공해야 합니다. 표준 
		Java Servlet 과는 무관합니다. 
	o URL을 SimpleServlet 구현체로 매핑합니다. 규칙은 다음과 같습니다. 
		▪ http://localhost:8000/Hello --> Hello.java 로 매핑 
		▪ http://localhost:8000/service.Hello --> service 패키지의 Hello.java 로 매핑 
	o 과제는 URL을 바로 클래스 파일로 매핑하지만, 추후 설정 파일을 이용해서 매핑하는 것도 고려해서 개발하십시오. 
		▪ 추후 확장을 고려하면 됩니다. 설정 파일을 이용한 매핑을 구현할 필요는 없습니다. 
		▪ 설정 파일을 이용한 매핑에서 사용할 수 있는 설정의 예, {“/Greeting”: “Hello”, “/super.Greeting”: “service.Hello”}

7. 현재 시각을 출력하는 SimpleServlet 구현체를 작성하세요. ✔️ 구현 완료
	o 앞서 구현한 WAS를 이용합니다. 
	o WAS와 SimpleServlet 인터페이스를 포함한 SimpleServlet 구현 객체가 하나의 JAR에 있어도 괜찮습니다. 
		▪ 분리하면 더 좋습니다. 

8. 앞에서 구현한 여러 스펙을 검증하는 테스트 케이스를 JUnit4를 이용해서 작성하세요. ✔️ 구현 완료

# 실행방법 (wasserver.zip)
1. mission_was.zip 파일 압축 해제

2. cmd 창 열어서 해제한 디렉토리 내부에 wasserver 디렉토리 안으로 이동

3. mvn clean package

4. java -jar target\wasserver-0.0.1-SNAPSHOT.jar 명령어로 실행

5. 브라우저에서 localhost:8080 으로 접속되는지 확인
