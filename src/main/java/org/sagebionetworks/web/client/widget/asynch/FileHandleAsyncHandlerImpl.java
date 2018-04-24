package org.sagebionetworks.web.client.widget.asynch;

import org.sagebionetworks.web.client.GWTWrapper;
import org.sagebionetworks.web.client.SynapseJavascriptClient;

import com.google.inject.Inject;

public class FileHandleAsyncHandlerImpl extends BaseFileHandleAsyncHandlerImpl implements FileHandleAsyncHandler {
	
	@Inject
	public FileHandleAsyncHandlerImpl(SynapseJavascriptClient jsClient, GWTWrapper gwt) {
		super(jsClient, gwt);
	}

	@Override
	protected boolean isIncludeFileHandles() {
		return true;
	}
	
	@Override
	protected boolean isIncludePreSignedURLs() {
		return false;
	}
}
