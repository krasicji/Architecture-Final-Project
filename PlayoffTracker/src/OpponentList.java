import java.io.File;

import protocol.HttpRequest;
import protocol.HttpResponse;
import protocol.Protocol;
import protocol.Response200OK;
import protocol.Response404NotFound;
import server.Server;
import server.Servlet;

public class OpponentList implements Servlet {

	@Override
	public String getURI() {
		return "/OpponentList";
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
		HttpResponse response = null;

		// Handling GET request here
		// Get relative URI path from request
		String uri = request.getUri();
		// Get root directory path from server
		String rootDirectory = server.getRootDirectory();
		// Combine them together to form absolute file path
		File file = new File(rootDirectory + uri);
		// Check if the file exists
		if (file.exists()) {
			if (file.isDirectory()) {
				// Look for default index.html file in a directory
				String location = rootDirectory + uri
						+ System.getProperty("file.separator")
						+ Protocol.DEFAULT_FILE;
				file = new File(location);
				if (file.exists()) {
					// Lets create 200 OK response
					response = new Response200OK(file, Protocol.OPEN);
				} else {
					// File does not exist so lets create 404 file not found
					// code
					response = new Response404NotFound(Protocol.CLOSE);
				}
			} else { // Its a file
						// Lets create 200 OK response
				response = new Response200OK(file, Protocol.OPEN);
			}
		} else {
			// File does not exist so lets create 404 file not found code
			response = new Response404NotFound(Protocol.CLOSE);
		}

		return response;
	}

}
