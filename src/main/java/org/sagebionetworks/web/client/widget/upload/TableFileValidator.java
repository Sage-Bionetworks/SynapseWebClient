package org.sagebionetworks.web.client.widget.upload;

import org.sagebionetworks.web.client.DisplayUtils;

import com.google.gwt.core.client.GWT;

public class TableFileValidator implements FileValidator {

	@Override
	public boolean isValid(String fileName) {
		if(fileName == null){
			return false;
		} else {
			String extension = fileName.substring(fileName.lastIndexOf(".")+1);
			GWT.debugger();
			if (!DisplayUtils.isRecognizedTableContentType("text/"+extension)) {
				return false;
			}
		}
		return true;
	}

}
