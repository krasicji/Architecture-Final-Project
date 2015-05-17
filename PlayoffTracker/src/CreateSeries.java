import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.InputStreamReader;

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
		String html = new String("<!DOCTYPE html>\n<html>\n");
		html+="<head>\n";
		html+="<body>\n";
		html+="<h1>Series Schedule </h1>\n";
		//Get the opponent
		html+="<h2>Opponent: " + params[0].split("=")[1] + "</h2>\n";
		//Create the table for games
		html+="<table class='table'>\n";
		html+="<thead>\n";
		html+="<tr>\n<th>Game</th>\n<th>Date</th>\n<th>Location</th>\n<th>Result</th>\n<th>Score</th>\n</tr>\n</thead>\n<tbody>\n";
		//Game 1
		html+=createGameRow(1, params,1);
		//Game 2
		html+=createGameRow(2, params,3);
		//Game 3
		html+=createGameRow(3, params,5);
		//Game 4
		html+=createGameRow(4, params,7);
		//Game 5
		html+=createGameRow(5, params,9);
		//Game 6
		html+=createGameRow(6, params,11);
		//Game 7
		html+=createGameRow(7, params,13);
		html+="</tbody>\n</table>\n";
		html+="</body>\n";
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

		//Edit the oppenentList file
		String opponent = params[0].split("=")[1];
		String round = uri.substring(1, 3);
		
		String newOpponentList = "";
		File opponentList = new File(rootDirectory + System.getProperty("file.separator") + "opponentList.html");
		
		FileInputStream fis = null;
		BufferedReader reader = null;
		try {
			fis = new FileInputStream(opponentList);
			
			reader = new BufferedReader(new InputStreamReader(fis));
          
            String line = reader.readLine();
            while(line != null){
            	if(line.contains("id=\""+round))
            	{
            		int start = line.indexOf(">");
            		int end = line.indexOf("<",1);
            		String newLine = line.substring(0, start+1) + opponent + line.substring(end, line.length());
            		newOpponentList += newLine + "\n";
            	}
            	else
            	{
            		newOpponentList += line + "\n";
            	}
                line = reader.readLine();
            }
 
            FileOutputStream opponentListOut = new FileOutputStream(opponentList);
            opponentListOut.write(newOpponentList.getBytes());
            opponentListOut.close();
		} catch (Exception e) {
			e.printStackTrace();
		} finally {
			try {
				if (fis != null)
					fis.close();
			} catch (Exception ex) {
				ex.printStackTrace();
			}
		}
		
		// Lets create 200 OK response
		return new Response200OK(file, Protocol.OPEN);
	}
/*
 * This method creates a row for a game to be added to the response html
 */
	private String createGameRow(int gameNumber, String[] params, int i) {
		String row = new String();
		row+="<tr>\n";
		row+="<td>" + gameNumber + "</td>\n";
		row+="<td>" + params[i].split("=")[1] + "</td>\n";
		row+="<td>" + params[i+1].split("=")[1] + "</td>\n";
		row+="<td></td>\n";
		row+="<td></td>\n";
		row+="</tr>\n";
		return row;	
	}
/*
 * This method splits the requests body into an array of parameters
 */
	private String[] getBodyParams(String body) {
		return body.split("\\s+");
	};
	

}
