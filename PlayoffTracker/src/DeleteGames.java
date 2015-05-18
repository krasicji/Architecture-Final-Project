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


public class DeleteGames implements Servlet {

	@Override
	public String getURI() {
		return "/DeleteGames";
	}

	@Override
	public String getContextRoot() {
		return "/PlayoffTracker";
	}

	@Override
	public String getMethod() {
		return Protocol.DELETE;
	}

	@Override
	public HttpResponse processRequest(HttpRequest request, Server server) {
		// Handling DELETE request here
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
					String game = new String(request.getBody());
					reader = new BufferedReader(new InputStreamReader(fis));
		          
		            String line = reader.readLine();
		            boolean deleting = false;
		            while(line != null){
		            	if(line.contains(game))
		            	{
		            		deleting = true;
		            	}
		            	else if(deleting && line.contains("</tr>"))
		            	{
		            		deleting = false;
		            	}
		            	else if(!deleting)
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

}
