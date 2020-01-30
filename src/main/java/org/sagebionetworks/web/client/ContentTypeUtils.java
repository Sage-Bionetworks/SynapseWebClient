package org.sagebionetworks.web.client;

import static org.sagebionetworks.repo.model.util.ContentTypeUtils.APPLICATION_OCTET_STREAM;
import static org.sagebionetworks.repo.model.util.ContentTypeUtils.PLAIN_TEXT;
import static org.sagebionetworks.repo.model.util.ContentTypeUtils.isRecognizedCodeFileName;
import static org.sagebionetworks.web.client.ClientProperties.CODE_EXTENSIONS_SET;
import static org.sagebionetworks.web.client.ClientProperties.IMAGE_CONTENT_TYPES_SET;
import static org.sagebionetworks.web.client.ClientProperties.TABLE_CONTENT_TYPES_SET;
import org.sagebionetworks.web.shared.WebConstants;

public class ContentTypeUtils {

	public static boolean isRecognizedImageContentType(String contentType) {
		String lowerContentType = contentType.toLowerCase();
		return IMAGE_CONTENT_TYPES_SET.contains(lowerContentType);
	}

	public static boolean isRecognizedTableContentType(String contentType) {
		String lowerContentType = contentType.toLowerCase();
		return TABLE_CONTENT_TYPES_SET.contains(lowerContentType);
	}

	public static String getExtension(String fileName) {
		if (fileName != null) {
			int lastDot = fileName.lastIndexOf(".");
			if (lastDot > -1) {
				return fileName.substring(lastDot + 1).toLowerCase();
			}
		}
		return null;
	}

	public static boolean isWebRecognizedCodeFileName(String fileName) {
		boolean isPlainText = false;
		String extension = getExtension(fileName);
		if (extension != null) {
			isPlainText = CODE_EXTENSIONS_SET.contains("." + extension);
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

	public static boolean isPDF(String contentType) {
		return contentType != null && contentType.toLowerCase().startsWith("application/pdf");
	}

	public static String fixDefaultContentType(String type, String fileName) {
		String contentType = type;
		String lowercaseFilename = fileName.toLowerCase();
		if (type == null || type.trim().length() == 0) {
			if (isRecognizedCodeFileName(fileName) || isWebRecognizedCodeFileName(fileName) || lowercaseFilename.endsWith(".txt")) {
				contentType = PLAIN_TEXT;
			} else if (lowercaseFilename.endsWith(".tsv") || lowercaseFilename.endsWith(".tab")) {
				contentType = WebConstants.TEXT_TAB_SEPARATED_VALUES;
			} else if (lowercaseFilename.endsWith(".csv")) {
				contentType = WebConstants.TEXT_COMMA_SEPARATED_VALUES;
			} else {
				// fall back to the least specific official MIME type...
				contentType = APPLICATION_OCTET_STREAM;
			}
		}
		return contentType;
	}
}
