import java.io.File;
import java.io.FileOutputStream;

import protocol.HttpRequest;
import protocol.HttpResponse;
import protocol.Protocol;
import protocol.Response200OK;
import protocol.Response400BadRequest;
import server.Server;
import server.Servlet;


public class TestPost implements Servlet {

	@Override
	public String getURI() {
		return "/TestPost";
	}

	@Override
	public String getMethod() {
		return "POST";
	}

	@Override
	public HttpResponse processRequest(HttpRequest request, Server server) {
		// Handling POST request here
		// Get relative URI path from request
		String uri = request.getUri().substring(getContextRoot().length() + getURI().length());
		// Get root directory path from server
		String rootDirectory = server.getRootDirectory();
		// Combine them together to form absolute file path
		File file = new File(rootDirectory
				+ System.getProperty("file.separator") + uri);

		if (file.exists() && file.isDirectory()) {
			// We cannot write to a directory, only a file. Bad Request
			return new Response400BadRequest(Protocol.CLOSE);
		}

		// Get the text from the request body
		String body = new String(request.getBody());
		
		body += "\nTHIS IS PROOF THAT THE SERVLET WORKS. WOOOOOOT";

		// Override the file with the request body
		try {
			FileOutputStream fileOut = new FileOutputStream(file);
			fileOut.write(body.getBytes());
			fileOut.close();
		} catch (Exception e) {
			// This should never happen.
			e.printStackTrace();
		}

		// Lets create 200 OK response
		return new Response200OK(file, Protocol.OPEN);
	}

	@Override
	public String getContextRoot() {
		return "/TestPlugin";
	}

}
