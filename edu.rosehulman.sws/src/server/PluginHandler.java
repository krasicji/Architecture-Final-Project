/*
 * PluginHandler.java
 * Apr 29, 2015
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

import static java.nio.file.StandardWatchEventKinds.ENTRY_CREATE;
import static java.nio.file.StandardWatchEventKinds.ENTRY_DELETE;
import static java.nio.file.StandardWatchEventKinds.ENTRY_MODIFY;
import static java.nio.file.StandardWatchEventKinds.OVERFLOW;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.net.URL;
import java.net.URLClassLoader;
import java.nio.file.FileSystems;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.WatchEvent;
import java.nio.file.WatchKey;
import java.nio.file.WatchService;
import java.util.HashMap;
import java.util.Map;
import java.util.jar.JarEntry;
import java.util.jar.JarInputStream;

import protocol.HttpRequest;
import protocol.HttpResponse;
import protocol.Protocol;
import protocol.Response400BadRequest;

/**
 * 
 * @author Chandan R. Rupakheti (rupakhcr@clarkson.edu)
 */
public class PluginHandler implements Runnable {

	
	private final static String PLUGIN_DIRECTORY = "Plugins";
	private WatchService watcher;
	private Map<WatchKey, Path> keys;
	private HashMap<String, HashMap<String, Servlet>> plugins;
	private HashMap<String, String> servletFileNames;
	
	public PluginHandler()
	{
		// Keep a map of plugins and its servlets and another map joining file paths to plugins
		plugins = new HashMap<String, HashMap<String, Servlet>>();
		servletFileNames = new HashMap<String, String>();
		
		// Add static handlers - using their request method as URI to process the request correctly
		HashMap<String, Servlet> staticHandlers = new HashMap<String, Servlet>();
		staticHandlers.put(Protocol.GET, new StaticGet());
		staticHandlers.put(Protocol.POST, new StaticPost());
		staticHandlers.put(Protocol.PUT, new StaticPut());
		staticHandlers.put(Protocol.DELETE, new StaticDelete());
		// Add the handlers as a "plugin"
		plugins.put("/", staticHandlers);
		
		// Register the plugin directory to watch for the addition / deletion of jar files
		Path dir = Paths.get(PLUGIN_DIRECTORY);
		processFiles();
		try {
			this.watcher = FileSystems.getDefault().newWatchService();
			this.keys = new HashMap<WatchKey, Path>();
			register(dir);
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
	
	public HttpResponse handleRequest(HttpRequest request, Server server)
	{
		String uri = request.getUri();
		//Extract the context root of the request by finding the / separating it from the relative uri 
		int crEnd = uri.indexOf("/", 1);
		String contextRoot = uri.substring(0, crEnd == -1 ? uri.length() : crEnd);
		
		
		if (plugins.containsKey(contextRoot))
		{
			// Extract the relative uri of the request
			String relativeURI = "";
			if (crEnd == -1 || crEnd == contextRoot.length()) {
				int uriEnd = uri.indexOf("/", crEnd + 1);
				relativeURI = uri.substring(crEnd == -1 ? 0 : crEnd, uriEnd == -1 ? uri.length() : uriEnd);
			}
			
			if (plugins.get(contextRoot).containsKey(relativeURI) 
				&& plugins.get(contextRoot).get(relativeURI).getMethod().equalsIgnoreCase(request.getMethod())) {
				
				// Get the correct plugin, then have the correct servlet process the request
				return plugins.get(contextRoot).get(relativeURI).processRequest(request, server);
			}
			
			// No servlet for the URI, or the request method did not match the servlet request method
			return new Response400BadRequest(Protocol.CLOSE);
		}
		
		// No plugin for this request, so attempt to handle it with the static handlers
		if (plugins.get("/").containsKey(request.getMethod())) {
			return plugins.get("/").get(request.getMethod()).processRequest(request, server);
		}
		
		return new Response400BadRequest(Protocol.CLOSE);
	}
	
	public void addPlugin(String path, Servlet servlet)
	{
		// Add a map of servlets if the context root is new
		if(!plugins.containsKey(servlet.getContextRoot())) {
			plugins.put(servlet.getContextRoot(), new HashMap<String, Servlet>());			
		}
		
		if (plugins.get(servlet.getContextRoot()).containsKey(servlet.getURI())) {
			// The servlet already exists. Notify user of error.
			System.out.println("A servlet with the context route " + servlet.getContextRoot() + " and URI " + servlet.getURI() + " already exists.");
		}
		else {
			// Add the servlet to the existing map of servlets
			plugins.get(servlet.getContextRoot()).put(servlet.getURI(), servlet);
		}
		
		// We put the filepath of the jar file into a map with the context root so that
		// The appropriate servlets get removed if the jar file is deleted
		servletFileNames.put(path, servlet.getContextRoot());
	}
	
	public void deletePlugin(String path)
	{
		// The deleted jar's path should have an associated plugin
		if(servletFileNames.containsKey(path))
		{
			// Delete servlets with the given context root
			String contextRoot = servletFileNames.get(path);
			if(plugins.containsKey(contextRoot))
			{
				plugins.remove(contextRoot);
			}
		}
	}

	// The code below was taken from example documentation on folder watching in a link
	// provided by Chandan during the plugin lab	
	@SuppressWarnings("rawtypes")
	private void processFiles() {
		File folder = new File(PLUGIN_DIRECTORY);
		File[] fileList = folder.listFiles();

		if(fileList == null) {
			System.out.println("There are currently no plugins to load");
		}
		
		for (File file : fileList) {
			if (file.getName().endsWith(".jar")) {
				try {
					@SuppressWarnings("resource")
					JarInputStream jarFile = new JarInputStream(
							new FileInputStream(file.getAbsolutePath()));
					JarEntry jarEntry;
					URL url = file.toURI().toURL();
					URLClassLoader cl = URLClassLoader.newInstance(new URL[] { url });
					while (true) {
						jarEntry = jarFile.getNextJarEntry();
						if (jarEntry == null) {
							break;
						}
						if ((jarEntry.getName().endsWith(".class"))) {
							String className = jarEntry.getName().replaceAll(
									"/", "\\.");
							String myClass = className.substring(0,
									className.lastIndexOf('.'));
							if (!isNativeClass(myClass)) {
								Class loadedClass = cl.loadClass(myClass);
								if (loadedClass.newInstance() instanceof Servlet)
								{
									Servlet servlet = (Servlet) loadedClass.newInstance();
									addPlugin(file.getAbsolutePath(), servlet);
									System.out.println( "Loaded " + servlet.getMethod() + " servlet " + servlet.getContextRoot() + servlet.getURI());
								}
								else
								{
									System.out.println( "Failed to load " + loadedClass.getName() + ". Either the class is not a servlet or the servlet is out of date.");
								}
							}
						}
					}
				} catch (Exception e) {
					e.printStackTrace();
				}
			}
		}
	}

	private boolean isNativeClass(String className) {
		return className.startsWith("gui.") || className.startsWith("protocol.") || className.startsWith("server.");
	}

	@SuppressWarnings("unchecked")
	static <T> WatchEvent<T> cast(WatchEvent<?> event) {
		return (WatchEvent<T>) event;
	}

	private void register(Path dir) throws IOException {
		WatchKey key = dir.register(watcher, ENTRY_CREATE, ENTRY_DELETE,
				ENTRY_MODIFY);
		keys.put(key, dir);
	}

	@SuppressWarnings({ "rawtypes", "resource" })
	void processEvents() {
		for (;;) {

			WatchKey key;
			try {
				key = watcher.take();
			} catch (InterruptedException x) {
				return;
			}

			Path dir = keys.get(key);
			if (dir == null) {
				System.err.println("WatchKey not recognized!!");
				continue;
			}

			for (WatchEvent<?> event : key.pollEvents()) {
				WatchEvent.Kind kind = event.kind();

				if (kind == OVERFLOW) {
					continue;
				}

				// Context for directory entry event is the file name of entry
				WatchEvent<Path> ev = cast(event);
				Path name = ev.context();
				Path child = dir.resolve(name);

				if (kind == ENTRY_CREATE
						&& child.toFile().getName().endsWith(".jar")) {
					try {
						JarInputStream jarFile = new JarInputStream(
								new FileInputStream(child.toFile()
										.getAbsolutePath()));
						JarEntry jarEntry;
						URL url = child.toUri().toURL();
						URLClassLoader cl = URLClassLoader
								.newInstance(new URL[] { url });
						
						while (true) {
							jarEntry = jarFile.getNextJarEntry();
							if (jarEntry == null) {
								break;
							}
							if ((jarEntry.getName().endsWith(".class"))) {
								String className = jarEntry.getName()
										.replaceAll("/", "\\.");
								String myClass = className.substring(0,
										className.lastIndexOf('.'));
								if (!isNativeClass(myClass)) {
									Class loadedClass = cl.loadClass(myClass);
									if (loadedClass.newInstance() instanceof Servlet)
									{
										Servlet servlet = (Servlet) loadedClass.newInstance();
										addPlugin(child.toFile().getAbsolutePath(), servlet);
										System.out.println( "Loaded " + servlet.getMethod() + " servlet " + servlet.getContextRoot() + servlet.getURI());
									}
									else
									{
										System.out.println( "Failed to load " + loadedClass.getName() + " - the class is not a servlet.");
									}
								}
								
							}
						}
					} catch (Exception e) {
						e.printStackTrace();
					}
				} 
				else
				{
					// Deleted jar
					if (kind == ENTRY_DELETE
							&& child.toFile().getName().endsWith(".jar"))
						deletePlugin(child.toFile().getAbsolutePath());
				}
			}

			// Reset key and remove from set if directory no longer accessible
			boolean valid = key.reset();
			if (!valid) {
				keys.remove(key);

				// all directories are inaccessible
				if (keys.isEmpty()) {
					break;
				}
			}
		}
	}

	/* (non-Javadoc)
	 * @see java.lang.Runnable#run()
	 */
	@Override
	public void run() {
		processEvents();
	}
}
