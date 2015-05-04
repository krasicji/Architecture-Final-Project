import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.nio.file.Files;

import protocol.HttpRequest;
import protocol.HttpResponse;
import protocol.Protocol;
import protocol.Response200OK;
import protocol.Response400BadRequest;
import server.Server;
import server.Servlet;

public class PutServlet implements Servlet {

	@Override
	public String getURI() {
		return "/JKPut";
	}

	@Override
	public String getContextRoot() {
		return "/JKPlugin";
	}

	@Override
	public String getMethod() {
		return Protocol.PUT;
	}

	@Override
	public HttpResponse processRequest(HttpRequest request, Server server) {
		// Handling PUT request here
		// Get relative URI path from request
		String uri = request.getUri().substring(getContextRoot().length() + getURI().length());
		// Get root directory path from server
		String rootDirectory = server.getRootDirectory();
		// Combine them together to form absolute file path
		File file = new File(rootDirectory
				+ System.getProperty("file.separator") + uri);

		byte[] contents = new byte[0];
		if (file.exists()) {
			if (!file.isDirectory()) {
				try {
					contents = Files.readAllBytes(file.toPath());
				} catch (IOException e1) {
					// Issue reading the contents of the file
					e1.printStackTrace();
				}
			} else {
				// If the file is a directory, we cannot append to it.
				// Therefore, it is a bad request.
				return new Response400BadRequest(Protocol.CLOSE);
			}
		}

		// Get the text from the request body
		String body = new String(request.getBody());
		
		body += "\nThe plugin has inserted this line into the text.";

		// Append or create the file with the request body
		try {
			FileOutputStream fileOut = new FileOutputStream(file);
			if (contents.length > 0)
				fileOut.write(contents);
			fileOut.write(body.getBytes());
			fileOut.close();
		} catch (Exception e) {
			// This should never happen.
			e.printStackTrace();
		}

		// Lets create 200 OK response
		return new Response200OK(file, Protocol.OPEN);
	}

}
