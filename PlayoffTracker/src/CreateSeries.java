import protocol.HttpRequest;
import protocol.HttpResponse;
import protocol.Protocol;
import server.Server;
import server.Servlet;

public class CreateSeries implements Servlet {

	@Override
	public String getURI() {
		return "/CreateSeries";
	}

	@Override
	public String getContextRoot() {
		return "/PlayoffTracker";
	}

	@Override
	public String getMethod() {
		return Protocol.GET;
	}

	@Override
	public HttpResponse processRequest(HttpRequest request, Server server) {
		// TODO Auto-generated method stub
		return null;
	}

}
