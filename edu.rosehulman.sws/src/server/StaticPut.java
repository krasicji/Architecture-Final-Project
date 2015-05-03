/*
 * StaticPut.java
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
import java.io.IOException;
import java.nio.file.Files;

import protocol.HttpRequest;
import protocol.HttpResponse;
import protocol.Protocol;
import protocol.Response200OK;
import protocol.Response400BadRequest;

/**
 * 
 * @author Chandan R. Rupakheti (rupakhcr@clarkson.edu)
 */
public class StaticPut implements Servlet{

	/* (non-Javadoc)
	 * @see server.Servlet#getURI(protocol.HttpRequest)
	 */
	@Override
	public String getURI() {
		return Protocol.PUT;
	}

	/* (non-Javadoc)
	 * @see server.Servlet#getMethod()
	 */
	@Override
	public String getMethod() {
		return Protocol.PUT;
	}

	/* (non-Javadoc)
	 * @see server.Servlet#processRequest(protocol.HttpRequest, server.Server)
	 */
	@Override
	public HttpResponse processRequest(HttpRequest request, Server server) {
		// Handling PUT request here
				// Get relative URI path from request
				String uri = request.getUri();
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

	/* (non-Javadoc)
	 * @see server.Servlet#getContextRoot()
	 */
	@Override
	public String getContextRoot() {
		return "/";
	}

}
