package org.sagebionetworks.web.shared;

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
	public static final String VALID_ENTITY_NAME_REGEX = "^[a-z,A-Z,0-9,_,.]+";
	
	/**
	 * Regex defining a valid annotation name. Characters are selected to ensure
	 * compatibility across services and clients.
	 * 
	 */
	public static final String VALID_ANNOTATION_NAME_REGEX = "^[a-z,A-Z,0-9,_,.]+";

}
