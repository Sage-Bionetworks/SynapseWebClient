package org.sagebionetworks.web.client.resources;

import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import com.google.gwt.core.client.Callback;
import com.google.gwt.core.client.ScriptInjector;
import com.google.gwt.user.client.rpc.AsyncCallback;
import com.google.inject.Inject;

public class ResourceLoaderImpl implements ResourceLoader {

	Set<WebResource> loaded;	
	
	@Inject
	public ResourceLoaderImpl() {
		super();		
		loaded = new HashSet<WebResource>();		
	}	
	
	@Override
	public void requires(WebResource resource, AsyncCallback<Void> loadedCallback) {
		requires(Arrays.asList(new WebResource[] {resource}), loadedCallback);
	}

	@Override
	public void requires(List<WebResource> resources, final AsyncCallback<Void> loadedCallback) {				
		final Set<WebResource> downloading = new HashSet<WebResource>(resources);		
		for(final WebResource resource : resources) {
			if(!loaded.contains(resource)) {
				ScriptInjector.fromUrl(resource.getUrl()).setCallback(new Callback<Void, Exception>() {					
					@Override
					public void onSuccess(Void result) {
						loaded.add(resource);
						downloading.remove(resource);
						checkDone(loadedCallback, downloading);
					}					
					@Override
					public void onFailure(Exception reason) {
						// ignore problems
						downloading.remove(resource);
						checkDone(loadedCallback, downloading);
					}
				}).setWindow(ScriptInjector.TOP_WINDOW).inject();
			}
		}
		checkDone(loadedCallback, downloading);
	}

	@Override
	public boolean isLoaded(WebResource resource) {
		return loaded.contains(resource);
	}
	
	
	/*
	 * Private Methods
	 */
	private void checkDone(AsyncCallback<Void> loadedCallback, Set<WebResource> downloading) {
		if(downloading.size() == 0) {
			loadedCallback.onSuccess(null);
		}
	}

}
