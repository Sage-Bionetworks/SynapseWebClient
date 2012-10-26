package org.sagebionetworks.web.client;

import java.util.Date;

import com.google.gwt.core.client.GWT;
import com.google.gwt.i18n.client.DateTimeFormat;
import com.google.gwt.safehtml.client.SafeHtmlTemplates;
import com.google.gwt.safehtml.shared.SafeHtml;
import com.google.gwt.safehtml.shared.SafeHtmlBuilder;

public class DisplayUtilsGWT {
	
	public static final String BASE_PROFILE_ATTACHMENT_URL = GWT.getModuleBaseURL() + "profileAttachment";
	public static final Templates TEMPLATES = GWT.create(Templates.class);

	public interface Templates extends SafeHtmlTemplates {		
		@Template("<div style=\"vertical-align: middle;display:inline-block\">" +
				"<span style=\"font-weight: bold;\">{0}</span>" +
				"<br><span style=\"color: darkGray;\">{1}</span>"+
				"</div>")
		SafeHtml nameAndEmail(String name, String email);
		
		@Template("<span class=\"thumbnail-image-container\" " +
				"style = \"background: url({0}) no-repeat center center; background-size: 125%\"/>" +
		    	"</span></span>")
		SafeHtml profilePicture(String url);
		
	}

	private static DateTimeFormat smallDateFormat = DateTimeFormat.getFormat("MM/dd/yyyy hh:mm:ssaa");
	public static String convertDateToSmallString(Date toFormat) {
		return smallDateFormat.format(toFormat);
	}

}
