package http;

import java.io.Writer;

public class SimpleHttpResponse implements HttpResponse {
	private Writer writer;

	public void setWriter(Writer writer) {
		this.writer = writer;
	}

	public SimpleHttpResponse(Writer writer) {
		this.writer = writer;
	}

	@Override
	public Writer getWriter() {
		return writer;
	}
}