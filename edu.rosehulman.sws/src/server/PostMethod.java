/*
 * PostMethod.java
 * Apr 22, 2015
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
import java.io.FileNotFoundException;
import java.io.FileOutputStream;

import protocol.HttpRequest;
import protocol.HttpResponse;
import protocol.HttpResponseFactory;
import protocol.Protocol;

/**
 * 
 * @author Chandan R. Rupakheti (rupakhcr@clarkson.edu)
 */
public class PostMethod implements IRequestMethod {

	/* (non-Javadoc)
	 * @see server.IRequestMethod#handle(protocol.HttpRequest, server.Server)
	 */
	@Override
	public HttpResponse handle(HttpRequest request, Server server) {
		HttpResponse response = null;
//		Map<String, String> header = request.getHeader();
//		String date = header.get("if-modified-since");
//		String hostName = header.get("host");
//		
		// Handling GET request here
		// Get relative URI path from request
		String uri = request.getUri();
		// Get root directory path from server
		String rootDirectory = server.getRootDirectory();
		// Combine them together to form absolute file path
		File file = new File(rootDirectory + uri);
		/*
		// Check if the file exists
		if(file.exists()) {
			if(file.isDirectory()) { // Its a directory - We don't have to override
				File newFile = createFile();
				
				// Look for default index.html file in a directory
				String location = rootDirectory + uri + System.getProperty("file.separator") + Protocol.DEFAULT_FILE;
				file = new File(location);
				if(file.exists()) {
					// Lets create 200 OK response
					response = HttpResponseFactory.create200OK(file, Protocol.OPEN);
				}
				else {
					// File does not exist so lets create 404 file not found code
					response = HttpResponseFactory.create404NotFound(Protocol.CLOSE);
				}
			}
			else { // Its a file - We have to override*/
				
				// Get the text from the request body
				String body = request.getBody().toString();
				
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
				response = HttpResponseFactory.create200OK(file, Protocol.OPEN);
				/*}
		}
		else {
			File newFile = createFile();
			// File does not exist so lets create 404 file not found code
			response = HttpResponseFactory.create404NotFound(Protocol.CLOSE);
		}*/
		
		return response;
	}
	
	public File createFile()
	{
		//Create the file
		return null;
	}

}
