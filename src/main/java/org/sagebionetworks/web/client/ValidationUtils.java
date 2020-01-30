package org.sagebionetworks.web.client;

import org.sagebionetworks.web.shared.WebConstants;
import com.google.gwt.regexp.shared.MatchResult;
import com.google.gwt.regexp.shared.RegExp;

public class ValidationUtils {
	public static boolean isValidUsername(String username) {
		if (username == null)
			return false;
		RegExp regEx = RegExp.compile(WebConstants.VALID_USERNAME_REGEX, "gm");
		MatchResult matchResult = regEx.exec(username);
		// the entire string must match (group 0 is the whole matched string)
		return (matchResult != null && username.equals(matchResult.getGroup(0)));
	}

	public static boolean isValidEmail(String email) {
		if (email == null)
			return false;
		RegExp regEx = RegExp.compile(WebConstants.VALID_EMAIL_REGEX, "gm");
		MatchResult matchResult = regEx.exec(email);
		// the entire string must match (group 0 is the whole matched string)
		return (matchResult != null && email.equals(matchResult.getGroup(0)));
	}

	public static boolean isValidWidgetName(String name) {
		if (name == null || name.trim().length() == 0)
			return false;
		RegExp regEx = RegExp.compile(WebConstants.VALID_WIDGET_NAME_REGEX, "gm");
		MatchResult matchResult = regEx.exec(name);
		// the entire string must match (group 0 is the whole matched string)
		return (matchResult != null && name.equals(matchResult.getGroup(0)));
	}

	public static boolean isValidUrl(String url, boolean isUndefinedUrlValid) {
		if (url == null || url.trim().length() == 0) {
			// url is undefined
			return isUndefinedUrlValid;
		}
		RegExp regEx = RegExp.compile(WebConstants.VALID_URL_REGEX, "gm");
		MatchResult matchResult = regEx.exec(url);
		return (matchResult != null && url.equals(matchResult.getGroup(0)));
	}

	public static boolean isTrue(Boolean b) {
		return b != null && b.booleanValue();
	}
}
