package org.sagebionetworks.web.client;

import static org.sagebionetworks.web.client.ServiceEntryPointUtils.fixServiceEntryPoint;

import com.google.gwt.user.client.rpc.AsyncCallback;
import com.google.inject.Inject;

public class UrlCacheImpl implements UrlCache {
	
	String repoUrl;
	
	@Inject
	public UrlCacheImpl(SynapseClientAsync client, final SynapseJSNIUtils jsniUtils){
		fixServiceEntryPoint(client);
		client.getRepositoryServiceUrl(new AsyncCallback<String>() {
			
			@Override
			public void onSuccess(String result) {
				repoUrl = result;
				jsniUtils.consoleLog("Setting the repo url: "+result);
			}
			
			@Override
			public void onFailure(Throwable caught) {
				jsniUtils.consoleError(caught.getMessage());
			}
		});
	}

	@Override
	public String getRepositoryServiceUrl() {
		return repoUrl;
	}
}
