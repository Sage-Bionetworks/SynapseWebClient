package org.sagebionetworks.web.client.widget.asynch;

import org.sagebionetworks.web.client.GWTWrapper;
import org.sagebionetworks.web.client.SynapseJavascriptClient;

import com.google.inject.Inject;

public class PresignedAndFileHandleURLAsyncHandlerImpl extends BaseFileHandleAsyncHandlerImpl implements PresignedAndFileHandleURLAsyncHandler {
	
	@Inject
	public PresignedAndFileHandleURLAsyncHandlerImpl(SynapseJavascriptClient jsClient, GWTWrapper gwt) {
		super(jsClient, gwt);
	}

	@Override
	protected boolean isIncludeFileHandles() {
		return true;
	}
	
	@Override
	protected boolean isIncludePreSignedURLs() {
		return true;
	}
}
