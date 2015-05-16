import java.io.File;
import java.io.FileOutputStream;

import protocol.HttpRequest;
import protocol.HttpResponse;
import protocol.Protocol;
import protocol.Response200OK;
import protocol.Response400BadRequest;
import server.Server;
import server.Servlet;

public class CreateSeries implements Servlet {

	@Override
	public String getURI() {
		return "/CreateSeries";
	}

	@Override
	public String getContextRoot() {
		return "/PlayoffTracker";
	}

	@Override
	public String getMethod() {
		return Protocol.POST;
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
		//Split the body in order to get the individual request parameters
		String[] params = getBodyParams(body);
		//Build html for reponse
		String html = new String("<!DOCTYPE html><html>");
		html+="<head>";
		//Add Bootstrap styling
		html+="<link rel='stylesheet' href='https://maxcdn.bootstrapcdn.com/bootstrap/3.3.4/css/bootstrap.min.css'>";
		html+="<link rel='stylesheet' href='https://maxcdn.bootstrapcdn.com/bootstrap/3.3.4/css/bootstrap-theme.min.css'>";
		html+="<script src='https://ajax.googleapis.com/ajax/libs/jquery/1.11.2/jquery.min.js'></script>";
		html+="<script src='https://maxcdn.bootstrapcdn.com/bootstrap/3.3.4/js/bootstrap.min.js'></script>";
		html+="</head>";
		html+="<body>";
		html+="<br>";
		html+="<h1>Series Schedule </h1>";
		//Create the table for games
		html+="<table class='table'>";
		html+="<thead>";
		html+="<tr><th>Opponent</th><th>Date</th><th>Location</th></tr></thead><tbody>";
		//Game 1
		html+=createGameRow(params,0);
		//Game 2
		html+=createGameRow(params,3);
		//Game 3
		html+=createGameRow(params,6);
		//Game 4
		html+=createGameRow(params,9);
		//Game 5
		html+=createGameRow(params,12);
		//Game 6
		html+=createGameRow(params,15);
		//Game 7
		html+=createGameRow(params,18);
		html+="</tbody></table>";
		html+="</body>";
		html+="</html>";
		// Override the file with the request body
		try {
			FileOutputStream fileOut = new FileOutputStream(file);
			fileOut.write(html.getBytes());
			fileOut.close();
		} catch (Exception e) {
			// This should never happen.
			e.printStackTrace();
		}

		// Lets create 200 OK response
		return new Response200OK(file, Protocol.OPEN);
	}
/*
 * This method creates a row for a game to be added to the response html
 */
	private String createGameRow(String[] params, int i) {
		String row = new String();
		row+="<tr>";
		row+="<td>" + params[i].split("=")[1] + "</td>";
		row+="<td>" + params[i+1].split("=")[1] + "</td>";
		row+="<td>" + params[i+2].split("=")[1] + "</td>";
		row+="</tr>";
		return row;	
	}
/*
 * This method splits the requests body into an array of parameters
 */
	private String[] getBodyParams(String body) {
		return body.split("\\s+");
	};
	

}
