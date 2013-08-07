package org.sagebionetworks.web.client.resources;

import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.sagebionetworks.web.client.SynapseJSNIUtils;
import org.sagebionetworks.web.client.callback.CompletedCallback;

import com.google.gwt.user.client.rpc.AsyncCallback;
import com.google.inject.Inject;

public class ResourceLoaderImpl implements ResourceLoader {
	SynapseJSNIUtils synapseJSNIUtils;

	Set<WebResource> loaded;	
	
	@Inject
	public ResourceLoaderImpl(SynapseJSNIUtils synapseJSNIUtils) {
		super();
		this.synapseJSNIUtils = synapseJSNIUtils;
		
		loaded = new HashSet<WebResource>();		
	}	
	
	@Override
	public void requires(List<WebResource> resources, final AsyncCallback<Void> loadedCallback) {
		final Set<WebResource> downloading = new HashSet<WebResource>(resources);		
		for(final WebResource resource : resources) {
			synapseJSNIUtils.requireJs(resource.getUrl(), new CompletedCallback() {				
				@Override
				public void complete() {
					loaded.add(resource);
					downloading.remove(resource);
					if(downloading.size() == 0) {
						loadedCallback.onSuccess(null);
					}
				}
			});			
		}
		
		
	}

	@Override
	public boolean isLoaded(WebResource resource) {
		return loaded.contains(resource);
	}
	
}
