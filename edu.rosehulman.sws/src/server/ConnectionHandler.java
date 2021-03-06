/*
 * ConnectionHandler.java
 * Oct 7, 2012
 *
 * Simple Web Server (SWS) for CSSE 477
 * 
 * Copyright (C) 2012 Chandan Raj Rupakheti
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
 */

package server;

import java.io.InputStream;
import java.io.OutputStream;
import java.net.Socket;
import java.util.Comparator;
import java.util.PriorityQueue;
import java.util.Queue;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

import protocol.HttpRequest;
import protocol.HttpResponse;
import protocol.Protocol;
import protocol.ProtocolException;
import protocol.Response400BadRequest;
import protocol.Response408ResponseTimeout;
import protocol.Response505NotSupported;

/**
 * This class is responsible for handling a incoming request by creating a
 * {@link HttpRequest} object and sending the appropriate response be creating a
 * {@link HttpResponse} object. It implements {@link Runnable} to be used in
 * multi-threaded environment.
 * 
 * @author Chandan R. Rupakheti (rupakhet@rose-hulman.edu)
 */
public class ConnectionHandler implements Runnable {
	private Server server;
	private Socket socket;
	private Comparator<HttpRequest> comparator;
	private Queue<HttpRequest> queue;

	public ConnectionHandler(Server server, Socket socket) {
		this.server = server;
		this.socket = socket;
		this.comparator = new ContentLengthComparator();
		// The 10 in the Queue constructor is not definitive, it will grow if it
		// needs to
		this.queue = new PriorityQueue<HttpRequest>(10, comparator);
	}

	/**
	 * @return the socket
	 */
	public Socket getSocket() {
		return socket;
	}

	// Creates the comparator that our queue will use to compare requests by
	// content-length
	private class ContentLengthComparator implements Comparator<HttpRequest> {

		/*
		 * (non-Javadoc)
		 * 
		 * @see java.util.Comparator#compare(java.lang.Object, java.lang.Object)
		 */
		@Override
		public int compare(HttpRequest x, HttpRequest y) {

			int r1Size = Integer.parseInt(x.getHeader().get("content-length"));
			int r2Size = Integer.parseInt(y.getHeader().get("content-length"));

			if (r1Size < r2Size) {
				return -1;
			}
			if (r1Size > r2Size) {
				return 1;
			}
			return 0;
		}
	}

	/**
	 * The entry point for connection handler. It first parses incoming request
	 * and creates a {@link HttpRequest} object, then it creates an appropriate
	 * {@link HttpResponse} object and sends the response back to the client
	 * (web browser).
	 */
	public void run() {
		// Get the start time
		long start = System.currentTimeMillis();

		inStream = null;
		OutputStream outStream = null;

		try {
			inStream = this.socket.getInputStream();
			outStream = this.socket.getOutputStream();
		} catch (Exception e) {
			// Cannot do anything if we have exception reading input or output
			// stream
			// May be have text to log this for further analysis?
			e.printStackTrace();

			// Increment number of connections by 1
			server.incrementConnections(1);
			// Get the end time
			long end = System.currentTimeMillis();
			this.server.incrementServiceTime(end - start);
			return;
		}

		// At this point we have the input and output stream of the socket
		// Now lets create a HttpRequest object
		request = null;
		response = null;

		ExecutorService reader = Executors.newSingleThreadExecutor();
		Future<HttpRequest> futureReader = reader.submit(new RequestReader());

		try {
			request = futureReader.get(1, TimeUnit.MINUTES);
		} catch (TimeoutException e) {
			// There was a hang up reading the request, or a stale connection
			response = new Response408ResponseTimeout(Protocol.CLOSE);
			System.out.println("There was either a stale connection or a "
					+ "timeout reading the request.");
		} catch (Exception e) {
			e.printStackTrace();
			// For any other error, we will create bad request response as well
			response = new Response400BadRequest(Protocol.CLOSE);
		}

		reader.shutdownNow();

		if (response != null) {
			// Means there was an error, now write the response object to the
			// socket
			try {
				response.write(outStream);
				// System.out.println(response);
			} catch (Exception e) {
				// We will ignore this exception
				e.printStackTrace();
			}

			// Increment number of connections by 1
			server.incrementConnections(1);
			// Get the end time
			long end = System.currentTimeMillis();
			this.server.incrementServiceTime(end - start);
			return;
		}

		// We reached here means no error so far, so lets process further
		try {
			// Fill in the code to create a response for version mismatch.
			// You may want to use constants such as Protocol.VERSION,
			// Protocol.NOT_SUPPORTED_CODE, and more.
			// You can check if the version matches as follows
			if (!request.getVersion().equalsIgnoreCase(Protocol.VERSION)) {
				// Here you checked that the "Protocol.VERSION" string is not
				// equal to the
				// "request.version" string ignoring the case of the letters in
				// both strings
				response = new Response505NotSupported(Protocol.CLOSE);
			} else {
				ExecutorService handler = Executors.newSingleThreadExecutor();
				Future<HttpResponse> futureHandler = handler
						.submit(new ResponseHandler());

				try {
					// The request has a minute to be responded to
					response = futureHandler.get(1, TimeUnit.MINUTES);
				} catch (TimeoutException e) {
					// There was a hang up handling the request
					response = new Response408ResponseTimeout(Protocol.CLOSE);
					System.out.println("There was a timeout processing the request.");
				} catch (Exception e) {
					e.printStackTrace();
					// For any other error, we will create bad request response
					// as well
					response = new Response400BadRequest(Protocol.CLOSE);
				}

				handler.shutdownNow();
			}
		} catch (Exception e) {
			e.printStackTrace();
		}

		if (response == null) {
			response = new Response400BadRequest(Protocol.CLOSE);
		}

		try {
			// Write response and we are all done so close the socket
			response.write(outStream);
			socket.close();
		} catch (Exception e) {
			// We will ignore this exception
			e.printStackTrace();
		}

		// Increment number of connections by 1
		server.incrementConnections(1);
		// Get the end time
		long end = System.currentTimeMillis();
		this.server.incrementServiceTime(end - start);
	}

	private class RequestReader implements Callable<HttpRequest> {
		// Using a seperate thread to read in a request to catch stale
		// connections or requests that cause the system to hang.
		@Override
		public HttpRequest call() {
			HttpRequest request = null;

			try {
				request = HttpRequest.read(inStream);

				// Add the request to the queue
				queue.add(request);

				System.out.println(request);
			} catch (ProtocolException pe) {
				// We have some sort of protocol exception. Get its status code
				// and create response
				// We know only two kind of exception is possible inside
				// fromInputStream
				// Protocol.BAD_REQUEST_CODE and Protocol.NOT_SUPPORTED_CODE
				int status = pe.getStatus();
				if (status == Protocol.BAD_REQUEST_CODE) {
					response = new Response400BadRequest(Protocol.CLOSE);
				}
			} catch (Exception e) {
				e.printStackTrace();
				// For any other error, we will create bad request response as
				// well
				response = new Response400BadRequest(Protocol.CLOSE);
			}

			return request;
		}
	}

	private class ResponseHandler implements Callable<HttpResponse> {
		// Using a seperate thread to respond to the request so that
		// any hang-ups are caught
		@Override
		public HttpResponse call() {
			return server.getPluginHandler().handleRequest(queue.remove(), server);
		}
	}

	private InputStream inStream;
	private HttpRequest request;
	private HttpResponse response;
}