package org.sagebionetworks.web.client.widget.upload;

import org.sagebionetworks.web.client.DisplayUtils;
import org.sagebionetworks.web.shared.WebConstants;

public class ImageFileValidator extends AbstractFileValidator {
	
	private double maxFileSize = -1;
	private String invalidMessage;
	@Override
	public boolean isValid(FileMetadata file) {
		invalidMessage = WebConstants.INVALID_IMAGE_FILETYPE_MESSAGE;
		String contentType = file.getContentType();
		if (file == null){
			return false;
		} else if (maxFileSize > 0 && file.getFileSize() > maxFileSize) {
			invalidMessage = WebConstants.INVALID_FILE_SIZE + DisplayUtils.getFriendlySize(maxFileSize, false);
			return false;
		} else if (contentType != null) {
			 return DisplayUtils.isRecognizedImageContentType(contentType);
		} else {
			String filename = file.getFileName();
			String extension = filename.substring(filename.lastIndexOf(".")+1);
			return DisplayUtils.isRecognizedImageContentType("image/"+extension);
		}
	}
	
	public void setMaxSize(double maxFileSize) {
		this.maxFileSize = maxFileSize;
	}
	
	public double getMaxFileSize() {
		return maxFileSize;
	}
	
	@Override
	public String getInvalidMessage() {
		return invalidMessage;
	}

}
