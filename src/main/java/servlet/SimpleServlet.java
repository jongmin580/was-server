package servlet;

import http.HttpRequest;
import http.HttpResponse;

public interface SimpleServlet {
	void service(HttpRequest req, HttpResponse res);
}
