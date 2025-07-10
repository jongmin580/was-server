package servlet;

import java.io.IOException;
import java.io.Writer;
import java.text.SimpleDateFormat;
import java.util.Date;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import http.HttpRequest;
import http.HttpResponse;

public class CurrentTimeServlet implements SimpleServlet  {
	private static final Logger logger = LoggerFactory.getLogger(CurrentTimeServlet.class);

	@Override
	public void service(HttpRequest req, HttpResponse res) {
		try {
			String currentTime = getCurrentTime();
			Writer writer = res.getWriter();
			writer.write("Current Time: " + currentTime);
			writer.flush();
		} catch (IOException e) {
			logger.error("Exception :" + e.getMessage());
		}
	}

	private String getCurrentTime() {
		SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
		return dateFormat.format(new Date());
	}
}
