package org.sagebionetworks.web.client.widget.upload;


public class NotEmptyFileValidator implements FileValidator {

	//More validation than non-null needed?
	@Override
	public boolean isValid(String fileName) {
		return fileName != null; 
	}

}
