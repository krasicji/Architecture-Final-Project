import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.InputStreamReader;

import protocol.HttpRequest;
import protocol.HttpResponse;
import protocol.Protocol;
import protocol.Response200OK;
import protocol.Response304NotModified;
import protocol.Response404NotFound;
import server.Server;
import server.Servlet;


public class UpdateSeries implements Servlet {

	@Override
	public String getURI() {
		return "/UpdateSeries";
	}

	@Override
	public String getContextRoot() {
		return "/PlayoffTracker";
	}

	@Override
	public String getMethod() {
		return Protocol.PUT;
	}

	@Override
	public HttpResponse processRequest(HttpRequest request, Server server) {
		// Get relative URI path from request
		String uri = request.getUri().substring(getContextRoot().length() + getURI().length());
		// Get root directory path from server
		String rootDirectory = server.getRootDirectory();
		// Combine them together to form absolute file path
		File file = new File(rootDirectory
				+ System.getProperty("file.separator") + uri);

		if (file.exists()) {
			// Attempts to delete the game
			if (!file.isDirectory()) {
				String newFileHTML = "";
				FileInputStream fis = null;
				BufferedReader reader = null;
				try {
					fis = new FileInputStream(file);
					String[][] games = getBodyParams(new String(request.getBody()));
					reader = new BufferedReader(new InputStreamReader(fis));
		          
		            String line = reader.readLine();
		            int i = 0;
		            while(line != null){
		            	if(line.contains("name=\"result\""))
		            	{
		            		// Only overwrite when info is entered
		            		if(games[i][0].equalsIgnoreCase(""))
		            		{
		            			newFileHTML += line + "\n";
		            		}
		            		else
		            		{
			            		int endIdx = line.indexOf(">");
			            		newFileHTML += line.substring(0, endIdx+1) + games[i][0] + "\n";
		            		}
		            	}
		            	else if(line.contains("name=\"score\""))
		            	{
		            		// Only overwrite when info is entered
		            		if(games[i][1].equalsIgnoreCase(""))
		            		{
		            			newFileHTML += line + "\n";
		            		}
		            		else
		            		{
			            		int endIdx = line.indexOf(">");
			            		newFileHTML += line.substring(0, endIdx+1) + games[i][1] + "\n";
		            		}
		            		i++;
		            	}
		            	else
		            	{
		            		newFileHTML += line + "\n";
		            	}
		            	
		                line = reader.readLine();
		            }
		 
		            FileOutputStream newFile = new FileOutputStream(file);
		            newFile.write(newFileHTML.getBytes());
		            newFile.close();
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
				
				// File successfully deleted.
				HttpResponse response = new Response200OK(file, Protocol.OPEN);
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
	
	

	/*
	 * This method splits the requests body into an array of parameters
	 */
	private String[][] getBodyParams(String body) {
		String[] params = body.split("\\n+");
		String[][] games = new String[params.length / 2][2];
		
		for(int i = 0; i < params.length; i+=2) {
			games[i/2][0] = params[i].split("=").length < 2 ? "" : params[i].split("=")[1];
			games[i/2][1] = params[i+1].split("=").length < 2 ? "" : params[i+1].split("=")[1];
		}
		
		return games;
	};
}
