import java.io.File;

import protocol.HttpRequest;
import protocol.HttpResponse;
import protocol.Protocol;
import protocol.Response200OK;
import protocol.Response304NotModified;
import protocol.Response404NotFound;
import server.Server;
import server.Servlet;

public class DeleteServlet implements Servlet {

	@Override
	public String getURI() {
		return "/JKDelete";
	}

	@Override
	public String getContextRoot() {
		return "/JKPlugin";
	}

	@Override
	public String getMethod() {
		return Protocol.DELETE;
	}

	@Override
	public HttpResponse processRequest(HttpRequest request, Server server) {
		// Handling DELETE request here
		// Get relative URI path from request
		String uri = request.getUri().substring(getContextRoot().length() + getURI().length());
		// Get root directory path from server
		String rootDirectory = server.getRootDirectory();
		// Combine them together to form absolute file path
		File file = new File(rootDirectory
				+ System.getProperty("file.separator") + uri);

		if (file.exists()) {
			// Attempts to delete the file
			if (!file.isDirectory() && file.delete()) {
				// File successfully deleted.
				HttpResponse response = new Response200OK(null, Protocol.OPEN);
				response.put("jk-delete-test", "Success");
				return response;
			} else {
				// File couldn't be deleted, or file was a folder.
				// Returning not modified because nothing changed.
				return new Response304NotModified(Protocol.CLOSE);
			}
		} else {
			// File does not exist so lets create 404 file not found code
			return new Response404NotFound(Protocol.CLOSE);
		}
	}

}
