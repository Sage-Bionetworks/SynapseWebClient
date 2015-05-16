package org.sagebionetworks.web.client.widget.upload;

import org.sagebionetworks.web.client.DisplayUtils;

public class ImageFileValidator implements FileValidator {

	@Override
	public boolean isValid(String fileName) {
		if(fileName == null){
			return false;
		} else {
			String extension = fileName.substring(fileName.lastIndexOf(".")+1);
			 if (!DisplayUtils.isRecognizedImageContentType("image/"+extension)) {
				 return false;
			 }
		}
		return true;
	}

}
