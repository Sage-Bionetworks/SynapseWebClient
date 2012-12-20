package org.sagebionetworks.web.shared;
import org.pegdown.Extensions;
import org.sagebionetworks.repo.model.util.ModelConstants;

/**
 * Constants for query parameter keys, header names, and field names used by the
 * web components.
 * 
 * All query parameter keys should be in this file as opposed to being defined
 * in individual controllers. The reason for this to is help ensure consistency
 * across controllers.
 * 
 * @author bkng, deflaux
 */
public class WebConstants {
	
	/**
	 * Regex defining a valid entity name. Characters are selected to ensure
	 * compatibility across services and clients.
	 * 
	 */
	public static final String VALID_ENTITY_NAME_REGEX = ModelConstants.VALID_ENTITY_NAME_REGEX;
	
	public static final String INVALID_ENTITY_NAME_MESSAGE = "Entity names may only contain letters, numbers, spaces, underscores, hypens, periods, plus signs, and parentheses.";

	public static final String PROVENANCE_API_URL = "https://sagebionetworks.jira.com/wiki/display/PLFM/Analysis+Provenance+in+Synapse";
	
	/**
	 * Regex defining a valid annotation name. Characters are selected to ensure
	 * compatibility across services and clients.
	 * 
	 */
	public static final String VALID_ANNOTATION_NAME_REGEX = "^[a-z,A-Z,0-9,_,.]+";
	public static final String VALID_URL_REGEX = "^(https?)://[-a-zA-Z0-9+&@#/%?=~_|!:,.;]*[-a-zA-Z0-9+&@#/%=~_|]";
	public static final String WIDGET_NAME_REGEX = "[a-z,A-Z,0-9,., ,\\-,\\+,(,)]";
	public static final String VALID_WIDGET_NAME_REGEX = "^"+WIDGET_NAME_REGEX+"+";
	public static final String VALID_ENTITY_ID_REGEX = "^[Ss]{1}[Yy]{1}[Nn]{1}\\d+";
	public static final String VALID_POSITIVE_NUMBER_REGEX = "^[0-9]+";
	
	// OpenID related constants

	public static final String OPEN_ID_URI = "/Portal/openid";

	public static final String OPEN_ID_PROVIDER = "OPEN_ID_PROVIDER";
	// 		e.g. https://www.google.com/accounts/o8/id
	
	// this is the parameter name for the value of the final redirect
	public static final String RETURN_TO_URL_PARAM = "RETURN_TO_URL";

	public static int MARKDOWN_OPTIONS = 
		Extensions.ABBREVIATIONS |		//Abbreviations in the way of PHP Markdown Extra.
		Extensions.AUTOLINKS |			//Plain (undelimited) autolinks the way Github-flavoured-Markdown implements them.
		Extensions.QUOTES |				//Beautifies single quotes, double quotes and double angle quotes
		Extensions.SMARTS |				//Beautifies apostrophes, ellipses ("..." and ". . .") and dashes ("--" and "---")		
		Extensions.TABLES |				//Tables similar to MultiMarkdown (which is in turn like the PHP Markdown Extra tables, but with colspan support).
		Extensions.SUPPRESS_ALL_HTML |	//Suppresses HTML
		Extensions.WIKILINKS;			//Support [[Wiki-style links]] with a customizable URL rendering logic.
	
}
