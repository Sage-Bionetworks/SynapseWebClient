package org.sagebionetworks.web.client.widget.upload;

import org.sagebionetworks.web.client.DisplayUtils;
import org.sagebionetworks.web.shared.WebConstants;

public class TableFileValidator extends AbstractFileValidator {

	String invalidMessage;
	@Override
	public boolean isValid(FileMetadata file) {
		String contentType = file.getContentType();
		invalidMessage = WebConstants.INVALID_TABLE_FILETYPE_MESSAGE;
		if (!isValidFilename(file.getFileName())) {
			invalidMessage = WebConstants.INVALID_ENTITY_NAME_MESSAGE;
			return false;
		} else if (contentType != null) {
			 return DisplayUtils.isRecognizedTableContentType(contentType);
		} else {
			String filename = file.getFileName();
			String extension = filename.substring(filename.lastIndexOf(".")+1);
			return DisplayUtils.isRecognizedTableContentType("text/"+extension);
		}
	}
	
	@Override
	public String getInvalidMessage() {
		return invalidMessage;
	}
	
}
