import protocol.HttpRequest;
import protocol.HttpResponse;
import protocol.Protocol;
import server.Server;
import server.Servlet;


public class DeleteGames implements Servlet {

	@Override
	public String getURI() {
		return "/DeleteGames";
	}

	@Override
	public String getContextRoot() {
		return "/PlayoffTracker";
	}

	@Override
	public String getMethod() {
		return Protocol.DELETE;
	}

	@Override
	public HttpResponse processRequest(HttpRequest request, Server server) {
		// TODO Auto-generated method stub
		return null;
	}

}
