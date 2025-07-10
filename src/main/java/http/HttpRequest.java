package http;

public interface HttpRequest {
	String getParameter(String name);
	String getPath();
	String[] getParameterNames();
}
