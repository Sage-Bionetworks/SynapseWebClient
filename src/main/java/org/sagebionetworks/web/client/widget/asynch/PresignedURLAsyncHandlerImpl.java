package org.sagebionetworks.web.client.widget.asynch;

import org.sagebionetworks.schema.adapter.AdapterFactory;
import org.sagebionetworks.web.client.GWTWrapper;
import org.sagebionetworks.web.client.SynapseJavascriptClient;
import org.sagebionetworks.web.client.cache.ClientCache;

import com.google.inject.Inject;

public class PresignedURLAsyncHandlerImpl extends BaseFileHandleAsyncHandlerImpl implements PresignedURLAsyncHandler {
	
	@Inject
	public PresignedURLAsyncHandlerImpl(SynapseJavascriptClient jsClient, GWTWrapper gwt, ClientCache clientCache, AdapterFactory adapterFactory) {
		super(jsClient, gwt, clientCache, adapterFactory);
	}

	@Override
	protected boolean isIncludeFileHandles() {
		return false;
	}
	
	@Override
	protected boolean isIncludePreSignedURLs() {
		return true;
	}
}
