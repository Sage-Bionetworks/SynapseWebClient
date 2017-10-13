package org.sagebionetworks.web.client.widget.asynch;

import org.sagebionetworks.web.client.GWTWrapper;
import org.sagebionetworks.web.client.SynapseJavascriptClient;

import com.google.inject.Inject;

public class PresignedURLAsyncHandlerImpl extends BaseFileHandleAsyncHandlerImpl implements PresignedURLAsyncHandler {
	
	@Inject
	public PresignedURLAsyncHandlerImpl(SynapseJavascriptClient jsClient, GWTWrapper gwt) {
		super(jsClient, gwt);
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
