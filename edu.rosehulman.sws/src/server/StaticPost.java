/*
 * StaticPost.java
 * May 1, 2015
 *
 * Simple Web Server (SWS) for EE407/507 and CS455/555
 * 
 * Copyright (C) 2011 Chandan Raj Rupakheti, Clarkson University
 * 
 * This program is free software: you can redistribute it and/or
 * modify it under the terms of the GNU Lesser General Public License 
 * as published by the Free Software Foundation, either 
 * version 3 of the License, or any later version.
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Lesser General Public License for more details.
 * You should have received a copy of the GNU Lesser General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/lgpl.html>.
 * 
 * Contact Us:
 * Chandan Raj Rupakheti (rupakhcr@clarkson.edu)
 * Department of Electrical and Computer Engineering
 * Clarkson University
 * Potsdam
 * NY 13699-5722
 * http://clarkson.edu/~rupakhcr
 */
 
package server;

import java.io.File;
import java.io.FileOutputStream;

import protocol.HttpRequest;
import protocol.HttpResponse;
import protocol.Protocol;
import protocol.Response200OK;
import protocol.Response400BadRequest;

/**
 * 
 * @author Chandan R. Rupakheti (rupakhcr@clarkson.edu)
 */
public class StaticPost implements Servlet{

	/* (non-Javadoc)
	 * @see server.Servlet#getURI(protocol.HttpRequest)
	 */
	@Override
	public String getURI() {
		return "/index.html";
	}

	/* (non-Javadoc)
	 * @see server.Servlet#getMethod(protocol.HttpRequest)
	 */
	@Override
	public String getMethod(HttpRequest request) {
		return request.getMethod();
	}

	/* (non-Javadoc)
	 * @see server.Servlet#processRequest(protocol.HttpRequest, server.Server)
	 */
	@Override
	public HttpResponse processRequest(HttpRequest request, Server server) {
		// Handling POST request here
				// Get relative URI path from request
				String uri = request.getUri();
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

}
