package sample;

import java.io.IOException;
import java.io.Writer;
import java.nio.charset.StandardCharsets;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import http.HttpRequest;
import http.HttpResponse;
import server.RequestProcessor;
import servlet.SimpleServlet;

public class Hello implements SimpleServlet{
	private static final Logger logger = LoggerFactory.getLogger(Hello.class);

	@Override
	public void service(HttpRequest req, HttpResponse res) {
		try {
			Writer writer = res.getWriter();
			if (req.getParameterNames().length == 0) {
				writer.write("default domain No parameters provided\n");
				writer.flush();
				logger.error("default domain  No parameters provided");
			} else {
				StringBuilder response = new StringBuilder();
				response.append("Param ");
				for (String paramName : req.getParameterNames()) {
					String paramValue = req.getParameter(paramName);
					response.append("sample Hello, ").append(paramName).append(": ").append(paramValue).append("\n");
				}

				 byte[] responseBytes = response.toString().getBytes(StandardCharsets.UTF_8);

				 RequestProcessor.sendHeaderResponse(writer, "HTTP/1.1 200 OK", "text/plain", responseBytes.length);
				writer.write(response.toString());
				writer.flush();
			}
		} catch (IOException e) {
			logger.error("Exception :" + e.getMessage());
		}
	}
}
