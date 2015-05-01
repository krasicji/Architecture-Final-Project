import protocol.HttpRequest;
import protocol.HttpResponse;
import protocol.Response505NotSupported;
import server.Server;
import server.Servlet;


public class TestServlet implements Servlet {

	@Override
	public String getURI() {
		// TODO Auto-generated method stub
		return "/index.html";
	}

	@Override
	public String getMethod(HttpRequest request) {
		// TODO Auto-generated method stub
		return "GET";
	}

	@Override
	public HttpResponse processRequest(HttpRequest request, Server server) {
		// TODO Auto-generated method stub
		return new Response505NotSupported(this.getMethod(request));
	}

}
