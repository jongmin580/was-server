package http;

import java.util.Map;

public class SimpleHttpRequest implements HttpRequest {
	private String path;
	private Map<String, String> parameters;

	public SimpleHttpRequest(String path, Map<String, String> parameters) {
		this.path = path;
		this.parameters = parameters;
	}

	@Override
	public String getParameter(String name) {
		return parameters.getOrDefault(name, "");
	}

	@Override
	public String getPath() {
		return path;
	}

	@Override
	public String[] getParameterNames() {
		return parameters.keySet().toArray(new String[0]);
	}
}
