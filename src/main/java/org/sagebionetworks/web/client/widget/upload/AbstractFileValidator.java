package org.sagebionetworks.web.client.widget.upload;

import static org.sagebionetworks.repo.model.util.ModelConstants.VALID_ENTITY_NAME_REGEX;
import org.sagebionetworks.web.client.utils.Callback;

public abstract class AbstractFileValidator implements FileValidator {

	Callback invalidCallback;

	public static boolean isValidFilename(String filename) {
		return filename.matches(VALID_ENTITY_NAME_REGEX);
	}

	public void setInvalidFileCallback(Callback invalidCallback) {
		this.invalidCallback = invalidCallback;
	}

	public Callback getInvalidFileCallback() {
		return invalidCallback;
	}
}
