package server;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.io.Reader;
import java.io.Writer;
import java.net.Socket;
import java.net.URLConnection;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import config.ConfigLoader;
import http.HttpRequest;
import http.HttpResponse;
import http.SimpleHttpRequest;
import http.SimpleHttpResponse;
import servlet.SimpleServlet;

public class RequestProcessor implements Runnable {
	private final static Logger logger = LoggerFactory.getLogger(RequestProcessor.class);
	private Socket connection;
	private String hostHeader = "default";
	private String indexFileName;
	private File rootDirectory = new File(ConfigLoader.getHttpRoot("default"));

	public RequestProcessor(File rootDirectory, String indexFileName, Socket connection) {
		if (rootDirectory.isFile()) {
			logger.error("rootDirectory must be a directory, not a file");
			throw new IllegalArgumentException("rootDirectory must be a directory, not a file");
		}
		try {
			rootDirectory = rootDirectory.getCanonicalFile();
			this.indexFileName = indexFileName;
		} catch (IOException ex) {
			logger.error("Exception :" + ex.getMessage());
		}
		this.connection = connection;
	}

	@Override
	public void run() {
		OutputStream raw = null;
		Writer out = null;
		Reader in = null;
		
		try {
			raw = new BufferedOutputStream(connection.getOutputStream());
			out = new OutputStreamWriter(raw);
			in = new InputStreamReader(new BufferedInputStream(connection.getInputStream()), "UTF-8");
			StringBuilder requestLine = new StringBuilder();

			while (true) {
				int c = in.read();
				if (c == '\r' || c == '\n') break;
				requestLine.append((char) c);
			}

			hostHeader = getHostHeader(in); 
			if (hostHeader != null) rootDirectory = new File(ConfigLoader.getHttpRoot(hostHeader));

			String[] tokens = requestLine.toString().split("\\s+");
			String method = tokens[0];
			String pathfileName = tokens.length > 1 ? tokens[1] : "/";
			String path = tokens.length > 1 ? tokens[1] : "/";

			hostCheckValidation( pathfileName , out );
			File requestedFile = new File(rootDirectory, indexFileName);

			handleServletRequest( requestedFile, URLConnection.getFileNameMap().getContentTypeFor(indexFileName), method, path, raw, out );
		} catch (IOException ex) {
			sendErrorPage(out, hostHeader, "ERROR_500");
			logger.error("Error Internal Server 500");
			logger.error("Exception :" + ex.getMessage());
		} catch (Exception e) {
			sendErrorPage(out, hostHeader, "ERROR_500");
			logger.error("Error Internal Server 500");
			logger.error("Exception :" + e.getMessage());
		} finally {
			try {
				connection.close();
			} catch (IOException ex) {
				sendErrorPage(out, hostHeader, "ERROR_500");
				logger.error("Error Internal Server 500");
				logger.error("Exception :" + ex.getMessage());
			}
		}
	}

	private void handleServletRequest( File requestedFile, String contentType, String method, String path, OutputStream raw, Writer out ) throws Exception {
		if (method.equals("GET")) {
			if (!requestedFile.exists()) {
				sendErrorPage(out, "HTTP/1.1 404", "ERROR_404");
				logger.error("Error File Not Found 404");
			} else if (!requestedFile.canRead()) {
				sendErrorPage(out, "HTTP/1.1 403", "ERROR_403");
				logger.error("Error Forbidden 403");
			} else {
				byte[] theData = Files.readAllBytes(requestedFile.toPath());
				if (path.equals("/")) {
					sendHeaderResponse(out, "HTTP/1.1 200 OK", contentType, theData.length);
					raw.write(theData);
					raw.flush();
				} else {
					handleServlet(out, path, contentType, theData.length);
				}
			}
		} else {
			sendErrorPage(out, hostHeader, "ERROR_500");
			logger.error("Error not GET Method 500");
		}

	}

	public void handleServlet(Writer out, String path, String contentType, int contentLength) {
		String servletName = extractServletName(path);
		Map<String, String> params = extractQueryParams(path);

		if ("/current-time".equals(path)) servletName = "servlet.CurrentTimeServlet";

		try {
			Class<?> clazz = Class.forName(servletName);
			
			if (!SimpleServlet.class.isAssignableFrom(clazz)) {
				logger.error("Error: " + servletName + " does not implement SimpleServlet interface.");
				sendErrorPage(out, hostHeader, "ERROR_500");
				return;
			}

			SimpleServlet servlet = (SimpleServlet) clazz.getDeclaredConstructor().newInstance();

			HttpRequest req = new SimpleHttpRequest(path, params);
			HttpResponse res = new SimpleHttpResponse(out);

			servlet.service(req, res);
		} catch (ClassNotFoundException e) {
			logger.error("Error: Class not found - " + servletName + " (" + e.getMessage() + ")");
			sendErrorPage(out, hostHeader, "ERROR_404");
		} catch (NoSuchMethodException e) {
			logger.error("Error: No default constructor found for " + servletName + " (" + e.getMessage() + ")");
			sendErrorPage(out, hostHeader, "ERROR_500");
		} catch (InstantiationException | IllegalAccessException e) {
			logger.error("Error: Cannot instantiate " + servletName + " (" + e.getMessage() + ")");
			sendErrorPage(out, hostHeader, "ERROR_500");
		} catch (Exception e) {
			logger.error("Error: Exception in servlet execution - " + e.getMessage());
			sendErrorPage(out, hostHeader, "ERROR_500");
		}
	}

	private String extractServletName(String path) {
		if (path.contains("?")) {
			path = path.substring(0, path.indexOf("?"));
		}
		if (path.startsWith("/")) {
			path = path.substring(1);
		}
		return path.replace("/", ".");
	}

	private Map<String, String> extractQueryParams(String path) {
		Map<String, String> params = new HashMap<>();
		if (path.contains("?")) {
			String queryString = path.substring(path.indexOf("?") + 1);
			for (String param : queryString.split("&")) {
				String[] keyValue = param.split("=");
				if (keyValue.length == 2) {
					params.put(keyValue[0], keyValue[1]);
				}
			}
		}
		return params;
	}

	private String getHostHeader(Reader in) throws IOException {
		BufferedReader bufferedReader = new BufferedReader(in);
		String line;
		while ((line = bufferedReader.readLine()) != null) {
			if (line.startsWith("Host:")) {
				return line.split(":")[1].trim();
			}
		}
		return null;
	}

	public static void sendHeaderResponse(Writer out, String responseCode, String contentType, int length) throws IOException {
		out.write(responseCode + "\r\n");
		Date now = new Date();
		out.write("Date: " + now + "\r\n");
		out.write("Server: WAS HTTP 2.0\r\n");
		out.write("Content-length: " + length + "\r\n");
		out.write("Content-type: " + contentType + "\r\n\r\n");
		out.flush();
	}

	private void sendErrorPage(Writer out, String host, String errorCode) {
		try {
			String errorPage = ConfigLoader.getErrorPage(host, errorCode);  
			File errorFile = new File((System.getProperty("user.dir")) + "\\error", errorPage);
			
			if (errorFile.exists()) {
				byte[] theData = Files.readAllBytes(errorFile.toPath());
				sendHeaderResponse(out, "HTTP/1.1 " + errorCode.replace("ERROR_", "") + " Error", "text/html", theData.length);
				out.write(new String(theData, StandardCharsets.UTF_8));
			} else {
				sendHeaderResponse(out, "HTTP/1.1 " + errorCode.replace("ERROR_", "") + " Error", "text/html", 0);
				out.write("<h1>" + errorCode.replace("ERROR_", "") + " Error</h1>");
			}
			out.flush();
		} catch (Exception e) {
			logger.error("Error sendErrorPage");
			logger.error("Exception :" + e.getStackTrace());
		}
	}

	private void hostCheckValidation( String fileName , Writer out) {
		if (fileName.contains("..")) {
			sendErrorPage(out, hostHeader, "ERROR_403");
			return;
		}

		if (fileName.endsWith(".exe")) {
			sendErrorPage(out, hostHeader, "ERROR_403");
			return;
		}
	}
}