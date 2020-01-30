package org.sagebionetworks.web.client.widget.upload;

import org.sagebionetworks.web.client.utils.Callback;

public interface FileValidator {

	boolean isValid(FileMetadata file);

	void setInvalidFileCallback(Callback invalidCallback);

	Callback getInvalidFileCallback();

	String getInvalidMessage();

}
