package org.sagebionetworks.web.client;

import static org.sagebionetworks.web.client.ClientProperties.*;
import static org.sagebionetworks.repo.model.util.ContentTypeUtils.*;
import org.sagebionetworks.web.shared.WebConstants;

import com.google.gwt.core.shared.GWT;

public class ContentTypeUtils {

	public static boolean isRecognizedImageContentType(String contentType) {
		String lowerContentType = contentType.toLowerCase();
		return IMAGE_CONTENT_TYPES_SET.contains(lowerContentType);
	}

	public static boolean isRecognizedTableContentType(String contentType) {
		String lowerContentType = contentType.toLowerCase();
		return TABLE_CONTENT_TYPES_SET.contains(lowerContentType);
	}

	public static boolean isRecognizedPlainTextFileName(String fileName) {
		boolean isPlainText = false;
		if (fileName != null) {
			int lastDot = fileName.lastIndexOf(".");
			if (lastDot > -1) {
				String extension = fileName.substring(lastDot).toLowerCase();
				isPlainText = PLAIN_TEXT_EXTENSIONS_SET.contains(extension);
			}
		}
		return isPlainText;
	}

	public static boolean isTextType(String contentType) {
		return contentType.toLowerCase().startsWith("text/");
	}

	public static boolean isCSV(String contentType) {
		return contentType != null && contentType.toLowerCase().startsWith("text/csv");
	}

	public static boolean isTAB(String contentType) {
		return contentType != null && contentType.toLowerCase().startsWith(WebConstants.TEXT_TAB_SEPARATED_VALUES);
	}

	public static boolean isHTML(String contentType) {
		return contentType != null && contentType.toLowerCase().startsWith("text/html");
	}

	public static String fixDefaultContentType(String type, String fileName) {
		String contentType = type;
		String lowercaseFilename = fileName.toLowerCase();
		if (type == null || type.trim().length() == 0) {
			
			if (isRecognizedCodeFileName(fileName)) {
				contentType = PLAIN_TEXT;
			}
			else if (lowercaseFilename.endsWith(".tsv") || lowercaseFilename.endsWith(".tab")) {
				contentType = WebConstants.TEXT_TAB_SEPARATED_VALUES;
			}
			else if (lowercaseFilename.endsWith(".csv")) {
				contentType = WebConstants.TEXT_COMMA_SEPARATED_VALUES;
			}
			else if (isRecognizedPlainTextFileName(fileName)) {
				contentType = PLAIN_TEXT;
			} else {
				// fall back to the least specific official MIME type...
				contentType = APPLICATION_OCTET_STREAM;
			}
		}
		return contentType;
	}

}
