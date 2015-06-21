package org.sagebionetworks.web.client.widget.upload;

import org.sagebionetworks.web.client.utils.Callback;

public abstract class AbstractFileValidator implements FileValidator {

	Callback invalidCallback;
	
	public void setInvalidFileCallback(Callback invalidCallback) {
		this.invalidCallback = invalidCallback;
	}
	
	public Callback getInvalidFileCallback() {
		return invalidCallback;
	}
}
