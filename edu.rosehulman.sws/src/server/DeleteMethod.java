/*
 * DeleteMethod.java
 * Apr 24, 2015
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

import protocol.HttpRequest;
import protocol.HttpResponse;
import protocol.Protocol;
import protocol.Response200OK;
import protocol.Response304NotModified;
import protocol.Response404NotFound;

/**
 * 
 * @author Chandan R. Rupakheti (rupakhcr@clarkson.edu)
 */
public class DeleteMethod implements IRequestMethod {

	/* (non-Javadoc)
	 * @see server.IRequestMethod#handle(protocol.HttpRequest, server.Server)
	 */
	@Override
	public HttpResponse handle(HttpRequest request, Server server) {

		// Handling DELETE request here
		// Get relative URI path from request
		String uri = request.getUri();
		// Get root directory path from server
		String rootDirectory = server.getRootDirectory();
		// Combine them together to form absolute file path
		File file = new File(rootDirectory + System.getProperty("file.separator") + uri);
		
		if(file.exists()) {
			// Attempts to delete the file
			if(!file.isDirectory() && file.delete()) {
				//File successfully deleted.
				return new Response200OK(null, Protocol.OPEN);
			}
			else {
				// File couldn't be deleted, or file was a folder.
				// Returning not modified because nothing changed.
				return new Response304NotModified(Protocol.CLOSE);
			}
		}
		else {
			// File does not exist so lets create 404 file not found code
			return new Response404NotFound(Protocol.CLOSE);
		}
	}

}
