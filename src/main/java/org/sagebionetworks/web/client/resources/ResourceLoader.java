package org.sagebionetworks.web.client.resources;

import java.util.List;
import com.google.gwt.user.client.rpc.AsyncCallback;

public interface ResourceLoader {

	/**
	 * Require certain web resources
	 * 
	 * @param resources
	 * @param loadedCallback
	 */
	void requires(WebResource resource, AsyncCallback<Void> loadedCallback);

	/**
	 * Require certain web resources
	 * 
	 * @param resources
	 * @param loadedCallback
	 */
	void requires(List<WebResource> resources, AsyncCallback<Void> loadedCallback);

	boolean isLoaded(WebResource resource);
}
