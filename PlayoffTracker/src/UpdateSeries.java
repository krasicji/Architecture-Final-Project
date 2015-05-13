import protocol.HttpRequest;
import protocol.HttpResponse;
import protocol.Protocol;
import server.Server;
import server.Servlet;


public class UpdateSeries implements Servlet {

	@Override
	public String getURI() {
		return "/UpdateSeries";
	}

	@Override
	public String getContextRoot() {
		return "/PlayoffTracker";
	}

	@Override
	public String getMethod() {
		return Protocol.PUT;
	}

	@Override
	public HttpResponse processRequest(HttpRequest request, Server server) {
		// TODO Auto-generated method stub
		return null;
	}

}
