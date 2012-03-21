package org.sagebionetworks.web.client;

import com.google.gwt.user.client.rpc.AsyncCallback;
import com.google.inject.Inject;

public class UrlCacheImpl implements UrlCache {
	
	String repoUrl;
	
	@Inject
	public UrlCacheImpl(SynapseClientAsync client, final ClientLogger log){
		client.getRepositoryServiceUrl(new AsyncCallback<String>() {
			
			@Override
			public void onSuccess(String result) {
				repoUrl = result;
				log.info("Setting the repo url: "+result);
			}
			
			@Override
			public void onFailure(Throwable caught) {
				log.error(caught.getMessage(), Throwable.class.getName(), UrlCacheImpl.class.getName(), "<init>", 21);
			}
		});
	}

	@Override
	public String getRepositoryServiceUrl() {
		return repoUrl;
	}

}
