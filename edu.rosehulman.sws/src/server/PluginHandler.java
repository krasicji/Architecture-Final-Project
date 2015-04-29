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
import java.util.ArrayList;
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
public class PluginHandler {

	
	private final static String PLATFORM_DIRECTORY = "C:/";
	private WatchService watcher;
	private Map<WatchKey, Path> keys;
	private HashMap<String, ServletPlugin> servlets;
	private HashMap<String, String> servletFileNames;
	
	public PluginHandler()
	{
		servlets = new HashMap<String, ServletPlugin>();
		Path dir = Paths.get(PLATFORM_DIRECTORY);
		processFiles();
		try {
			this.watcher = FileSystems.getDefault().newWatchService();
			this.keys = new HashMap<WatchKey, Path>();
			processEvents();
			register(dir);
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
	
	public HttpResponse handleRequest(HttpRequest request, Server server)
	{
		if (servlets.containsKey(request.getUri()))
		{
			return servlets.get(request.getUri()).handleRequest();
		}
		
		//No plugin for this request
		return new Response400BadRequest(Protocol.CLOSE);
	}
	
	public void addPlugin(String path, ServletPlugin plugin)
	{
		servlets.put(plugin.getURI(), plugin);
		servletFileNames.put(path, plugin.getURI());
	}
	
	public void deletePlugin(String path)
	{
		if(servletFileNames.containsKey(path))
		{
			String URI = servletFileNames.get(path);
			if(servlets.containsKey(URI))
			{
				servlets.remove(URI);
			}
		}
	}

	@SuppressWarnings("rawtypes")
	private void processFiles() {
		File folder = new File(PLATFORM_DIRECTORY);
		File[] fileList = folder.listFiles();
		for (File file : fileList) {
			if (file.getName().endsWith(".jar")) {
				try {
					@SuppressWarnings("resource")
					JarInputStream jarFile = new JarInputStream(
							new FileInputStream(file.getAbsolutePath()));
					JarEntry jarEntry;
					String extensionClassName = "";
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
							if (!isNativeClass(myClass))
								extensionClassName = myClass;
						}
					}
					URL url = file.toURI().toURL();
					URLClassLoader cl = URLClassLoader
							.newInstance(new URL[] { url });
					Class loadedClass = cl.loadClass(extensionClassName);
					ServletPlugin plugin = (ServletPlugin) loadedClass.newInstance();
					addPlugin(file.getAbsolutePath(), plugin);
				} catch (Exception e) {
					e.printStackTrace();
				}
			}
		}
	}

	private boolean isNativeClass(String className) {
		ArrayList<String> classes = new ArrayList<String>();
		classes.add("homework5.pluginframework.gui.Display");
		classes.add("homework5.pluginframework.gui.StatusPanel");
		classes.add("homework5.pluginframework.gui.ExecutionPanel");
		classes.add("homework5.pluginframework.gui.AbstractGUIPanel");
		classes.add("homework5.pluginframework.gui.MainPanel");
		classes.add("homework5.pluginframework.gui.ListingPanel");
		classes.add("homework5.pluginframework.gui.Platform");
		
		for (String nativeClass : classes)
		{
			if(className.contains(nativeClass))
				return true;
		}
		return false;
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
						String extensionClassName = "";
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
								if (!isNativeClass(myClass))
									extensionClassName = myClass;
							}
						}

						URL url = child.toUri().toURL();
						URLClassLoader cl = URLClassLoader
								.newInstance(new URL[] { url });
						Class loadedClass = cl.loadClass(extensionClassName);
						ServletPlugin plugin = (ServletPlugin) loadedClass.newInstance();
						addPlugin(child.toFile().getAbsolutePath(), plugin);
					} catch (Exception e) {
						e.printStackTrace();
					}
				} 
				else
				{
					if (kind == ENTRY_DELETE
							&& child.toFile().getName().endsWith(".jar"))
						System.out.println("DO THIS");
				}

				// Print out event
				System.out.format("%s: %s\n", event.kind().name(), child);
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
}
