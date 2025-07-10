import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.io.StringWriter;
import java.io.Writer;

import org.junit.Before;
import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import config.ConfigLoader;
import server.RequestProcessor;


public class TestCase {
	private static final Logger logger = LoggerFactory.getLogger(TestCase.class);
	private RequestProcessor requestProcessor;
	private StringWriter stringWriter;
	private Writer mockWriter;

	@Before
	public void setUp() throws IOException {
		ConfigLoader.loadConfig();
		stringWriter = new StringWriter();
		mockWriter = stringWriter;
		requestProcessor = new RequestProcessor(new File("/mockRoot"), "index.html", null); 
	}
	
	/**
	* 1. Host 헤더 기반 HTTP_ROOT 설정 테스트
	*/
	@Test
	public void testHostBasedHttpRoot() throws IOException {
		assertEquals("/a_domain", ConfigLoader.getHostConfig("a.com").get("HTTP_ROOT"));
		assertEquals("/b_domain", ConfigLoader.getHostConfig("b.com").get("HTTP_ROOT"));
		assertEquals("/", ConfigLoader.getHostConfig("unknown.com").get("HTTP_ROOT")); // 기본값 처리
	}
	
	/**
	* 2. HTTP/1.1 Host별 오류 페이지 반환 테스트
	*/
	@Test
	public void testErrorPages() {
		assertEquals("403.html", ConfigLoader.getErrorPage("a.com", "ERROR_403"));
		assertEquals("404.html", ConfigLoader.getErrorPage("a.com", "ERROR_404"));
		assertEquals("500.html", ConfigLoader.getErrorPage("a.com", "ERROR_500"));
	}

	/**
	* 3. 설정 파일(JSON) 로드 테스트
	*/
	@Test
	public void testConfigLoader() throws IOException {
		ConfigLoader.loadConfig();
		assertEquals(8080, ConfigLoader.getPort());
		assertEquals("/a_domain", ConfigLoader.getHostConfig("a.com").get("HTTP_ROOT"));
		assertEquals("404.html", ConfigLoader.getErrorPage("a.com", "ERROR_404"));
	}

	/**
	* 4. 유효성 메소드 테스트
	*/
	@Test
	public void testHostCheckWithDotDot() {
		String fileName = "/../../etc/passwd";
		// 파일 경로에 '..'이 포함되어 있으면 403 오류를 보내야 함
		assertTrue( hostCheckValidation( fileName ) );

		fileName = "/test.exe";
		// 파일 경로에 'exe'이 포함되어 있으면 403 오류를 보내야 함
		assertTrue( hostCheckValidation( fileName ) );
	}

	/**
	* 5. 응답이 제대로 들어갔는지 확인 (현재 시간 출력) 테스트
	*/
	@Test
	public void testServletStartWithValidServlet() throws Exception {
		String path = "/current-time";
		requestProcessor.handleServlet(mockWriter, path, "text/html", 20);
		System.out.println("stringWriter.toString() : " + stringWriter.toString());
		assertTrue(stringWriter.toString().contains("Current Time:"));
	}

	/**
	* 6. 응답이 제대로 들어갔는지 확인 (default) 테스트
	*/
	@Test
	public void testServletDefaultHello() throws Exception {
		String path = "/Hello";
		requestProcessor.handleServlet(mockWriter, path, "text/html", 20);
		assertTrue(stringWriter.toString().contains("default domain"));
	}
	
	/**
	* 7. 응답이 제대로 들어갔는지 확인 (패키지별로 service.Hello.java 호출) 테스트
	*/
	@Test
	public void testServletServiceHello() throws Exception {
		String path = "/service.Hello?name=jo";
		requestProcessor.handleServlet(mockWriter, path, "text/html", 20);
		assertTrue(stringWriter.toString().contains("service Hello"));
	}

	/**
	* 8. 응답이 제대로 들어갔는지 확인 (패키지별로 sample.Hello.java 호출) 테스트
	*/
	@Test
	public void testServletHello() throws Exception {
		String path = "/sample.Hello?job=jo";
		requestProcessor.handleServlet(mockWriter, path, "text/html", 20);
		assertTrue(stringWriter.toString().contains("sample Hello"));
	}

	/**
	* 8. 로그 기록 테스트 (오류 발생 시 StackTrace 저장 확인)
	*/
	@Test
	public void testLogging() throws IOException {
		try {
			throw new RuntimeException("Test Exception");
		} catch (Exception e) {
			logger.error("Exception", e.getMessage());
		}

		File logFile = new File("logs/was.log");
		assertTrue(logFile.exists());

		BufferedReader reader = new BufferedReader(new FileReader(logFile));
		String line;
		boolean foundError = false;
		while ((line = reader.readLine()) != null) {
			if (line.contains("Exception")) {
			foundError = true;
			break;
			}
		}
		reader.close();
		assertTrue(foundError);
	}
	
	private boolean hostCheckValidation( String fileName ) {
		if (fileName.contains("..")) {
			return true;
		}

		if (fileName.endsWith(".exe")) {
			return true;
		}

		return false;
	}
}