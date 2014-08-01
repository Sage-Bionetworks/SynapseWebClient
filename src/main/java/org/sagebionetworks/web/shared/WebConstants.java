package org.sagebionetworks.web.shared;

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
	
	public static final String INVALID_EMAIL_MESSAGE = "Invalid email address";

	public static final String PROVENANCE_API_URL = "https://sagebionetworks.jira.com/wiki/display/PLFM/Analysis+Provenance+in+Synapse";
	
	public static final String PREVIEW_UNAVAILABLE_PATH = "images/blank.png";
	
	/**
	 * Regex defining a valid annotation name. Characters are selected to ensure
	 * compatibility across services and clients.
	 * 
	 */
	public static final String VALID_ANNOTATION_NAME_REGEX = "^[a-z,A-Z,0-9,_,.]+";
	//support #!Place:token/with/delimiter, or standard http url
	public static final String VALID_URL_REGEX = "([#]{1}[!]{1}[a-zA-Z_0-9:/]+)|(^(https?)://[-a-zA-Z0-9+&@#/%?=~_|!:,.;]*[-a-zA-Z0-9+&@#/%=~_|])";
	//copied from org.sagebionetworks.repo.model.principal.AliasEnum.USER_NAME and USER_EMAIL, added uppercase support
	public static final String VALID_USERNAME_REGEX = "^[A-Za-z0-9._-]{3,}";
	public static final String VALID_EMAIL_REGEX = "^[A-Za-z0-9._%+-]+@[A-Za-z0-9.-]+\\.[A-Za-z]{2,4}";
	public static final String WIDGET_NAME_REGEX = "[a-z,A-Z,0-9,., ,\\-,\\+,(,)]";
	public static final String VALID_WIDGET_NAME_REGEX = "^"+WIDGET_NAME_REGEX+"+";
	public static final String VALID_ENTITY_ID_REGEX = "^[Ss]{1}[Yy]{1}[Nn]{1}\\d+";
	public static final String VALID_POSITIVE_NUMBER_REGEX = "^[0-9]+";
	public static final String VALID_BOOKMARK_ID_REGEX = "[_A-Za-z0-9-.[^\\s]]+";
	public static final String HTML_ELLIPSIS = "&hellip;";
	public static final String URL_PROTOCOL = "http://";
	
	public static final String TEMPORARY_USERNAME_PREFIX = "TEMPORARY-";
	
	// OpenID related constants
	/**
	 * A token built into the redirect URL by the authentication controller at the end of OpenID
	 * authentication to indicate that an error has occurred.
	 * 
	 */
	public static final String OPEN_ID_ERROR_TOKEN = "OpenIDError";
	public static final String OPEN_ID_UNKNOWN_USER_ERROR_TOKEN = "OpenIDUnknownUser";

	
	public static final String OPEN_ID_URI = "/Portal/openid";

	public static final String OPEN_ID_PROVIDER = "OPEN_ID_PROVIDER";
	// 		e.g. https://www.google.com/accounts/o8/id
	
	public static final String OPEN_ID_PROVIDER_GOOGLE_VALUE = "GOOGLE";
	
	// this is the parameter name for the value of the final redirect
	public static final String RETURN_TO_URL_PARAM = "RETURN_TO_URL";
	
	// a parameter to control how the final redirect is issued
	public static final String OPEN_ID_MODE = "MODE";
	// redirect "GWT" style with the session token appended ot the URL with ":"
	public static final String OPEN_ID_MODE_GWT = "GWT";
	// redirect with the sessio token as a request parameter (this is the default)
	public static final String OPEN_ID_MODE_STANDARD = "STANDARD";

	/*
	 * Dimensions
	 */
	public static final int DEFAULT_GRID_COLUMN_WIDTH_PX = 150;
	public static final int DEFAULT_GRID_LAYER_COLUMN_WIDTH_PX = 100;
	public static final int DEFAULT_GRID_DATE_COLUMN_WIDTH_PX = 85;

	public static final int MAX_COLUMNS_IN_GRID = 100;
	public static final int DESCRIPTION_SUMMARY_LENGTH = 450; // characters for summary

	public static final String PROXY_PARAM_KEY = "proxy";
	
	public static final String REDIRECT_URL_KEY = "redirect";

	public static final String ENTITY_PARENT_ID_KEY = "parentId";

	public static final String ENTITY_EULA_ID_KEY = "eulaId";

	public static final String ENTITY_PARAM_KEY = "entityId";
	
	public static final String TEAM_PARAM_KEY = "teamId";
	
	public static final String ENTITY_VERSION_PARAM_KEY = "version";

	public static final String WIKI_OWNER_ID_PARAM_KEY = "ownerId";

	public static final String WIKI_OWNER_TYPE_PARAM_KEY = "ownerType";

	public static final String WIKI_ID_PARAM_KEY = "wikiId";

	public static final String WIKI_FILENAME_PARAM_KEY = "fileName";
	
	public static final String WIKI_VERSION_PARAM_KEY = "wikiVersion";

	public static final String FILE_HANDLE_PREVIEW_PARAM_KEY = "preview";

	public static final String FILE_HANDLE_CREATE_FILEENTITY_PARAM_KEY = "createFileEntity";

	public static final String FILE_HANDLE_FILEENTITY_PARENT_PARAM_KEY = "fileEntityParentId";

	public static final String IS_RESTRICTED_PARAM_KEY = "isRestricted";

	public static final String ADD_TO_ENTITY_ATTACHMENTS_PARAM_KEY = "isAddToAttachments";

	public static final String USER_PROFILE_PARAM_KEY = "userId";

	public static final String TOKEN_ID_PARAM_KEY = "tokenId";

	public static final String WAIT_FOR_URL = "waitForUrl";

	public static final String ENTITY_CREATEDBYPRINCIPALID_KEY = "createdByPrincipalId";

	public static final String MAKE_ATTACHMENT_PARAM_KEY = "makeAttachment";

	public static final String ETAG_KEY = "etag";

	public static final String ENTITY_VERSION_STRING = "/version/";
	
	public static final String MATHJAX_PREFIX = "\\[";
	public static final String MATHJAX_SUFFIX = "\\]";
	
	//Synapse Properties
	public static final String CHALLENGE_TUTORIAL_PROPERTY ="org.sagebionetworks.portal.challenge_synapse_id";
	public static final String CHALLENGE_WRITE_UP_TUTORIAL_PROPERTY ="org.sagebionetworks.portal.challenge_writeup_synapse_id";
	public static final String GETTING_STARTED_GUIDE_ENTITY_ID_PROPERTY ="org.sagebionetworks.portal.gettingstartedguide_synapse_id";
	public static final String GETTING_STARTED_GUIDE_WIKI_ID_PROPERTY ="org.sagebionetworks.portal.gettingstartedguide_wiki_id";
	public static final String CREATE_PROJECT_ENTITY_ID_PROPERTY ="org.sagebionetworks.portal.createproject_synapse_id";
	public static final String CREATE_PROJECT_WIKI_ID_PROPERTY ="org.sagebionetworks.portal.createproject_wiki_id";
	public static final String R_CLIENT_ENTITY_ID_PROPERTY ="org.sagebionetworks.portal.rclient_synapse_id";
	public static final String R_CLIENT_WIKI_ID_PROPERTY ="org.sagebionetworks.portal.rclient_wiki_id";
	public static final String PYTHON_CLIENT_ENTITY_ID_PROPERTY ="org.sagebionetworks.portal.pythonclient_synapse_id";
	public static final String PYTHON_CLIENT_WIKI_ID_PROPERTY ="org.sagebionetworks.portal.pythonclient_wiki_id";
	public static final String FORMATTING_GUIDE_ENTITY_ID_PROPERTY ="org.sagebionetworks.portal.formattingguide_synapse_id";
	public static final String FORMATTING_GUIDE_WIKI_ID_PROPERTY ="org.sagebionetworks.portal.formattingguide_wiki_id";
	public static final String CHALLENGE_PARTICIPATION_INFO_ENTITY_ID_PROPERTY ="org.sagebionetworks.portal.challenge_participation_info_synapse_id";
	public static final String CHALLENGE_PARTICIPATION_INFO_WIKI_ID_PROPERTY ="org.sagebionetworks.portal.challenge_participation_info_wiki_id";
	public static final String GOVERNANCE_ENTITY_ID_PROPERTY ="org.sagebionetworks.portal.governance_synapse_id";
	public static final String GOVERNANCE_WIKI_ID_PROPERTY ="org.sagebionetworks.portal.governance_wiki_id";


	public static final String JIRA_PROJECT_ID ="org.sagebionetworks.portal.jira_project_id";
	public static final String JIRA_PROJECT_KEY ="org.sagebionetworks.portal.jira_project_key";
	public static final String CONFLUENCE_ENDPOINT = "org.sagebionetworks.portal.confluence_endpoint";
	
	public static final String TEXT_TAB_SEPARATED_VALUES = "text/tab-separated-values";

	public static final String FILE_UPLOADER_IS_UPDATE_PARAM = "isUpdate";

	public static final String CONTENT_TYPE_JNLP = "application/x-java-jnlp-file";
	public static final String CONCRETE_TYPE_KEY = "concreteType";
	public static final String NODE_TYPE_KEY = "nodeType";
	
	
	public static final String GETTING_STARTED = "GettingStarted";
	public static final String CREATE_PROJECT = "CreateProject";
	public static final String R_CLIENT = "RClient";
	public static final String PYTHON_CLIENT = "PythonClient";
	public static final String COMMAND_LINE_CLIENT = "CommandLineClient";
	public static final String FORMATTING_GUIDE = "FormattingGuide";
	public static final String CHALLENGE_PARTICIPATION_INFO = "ChallengeParticipationInfo";
	public static final String GOVERNANCE = "Governance";
	
	public static final String CERTIFICATION = "Certification";
	
	//APITableWidget default column names
	public static final String DEFAULT_COL_NAME_PARENT_ID = "parentid";
	public static final String DEFAULT_COL_NAME_ENTITY_ID = "entityid";
	public static final String DEFAULT_COL_NAME_MODIFIED_ON = "modifiedon";
	public static final String DEFAULT_COL_NAME_CREATED_ON = "createdon";
	public static final String DEFAULT_COL_NAME_USER_ID = "userid";
	public static final String DEFAULT_COL_NAME_MODIFIED_BY_PRINCIPAL_ID = "modifiedbyprincipalid";
	public static final String DEFAULT_COL_NAME_CREATED_BY_PRINCIPAL_ID = "createdbyprincipalid";
	public static final String DEFAULT_COL_NAME_ID = "id";

	public static final String HIGHLIGHT_KEY = "highlight";
	
	//ClientCache key suffixes (used to avoid collision in the cache)
	public static final String USER_PROFILE_SUFFIX = "_USER_PROFILE";
	public static final String TEMP_IMAGE_ATTACHMENT_SUFFIX = "_TEMP_IMAGE_ATTACHMENT";

	public static final String TABLE_COLUMN_ID = "columnId";
	public static final String TABLE_ROW_ID = "rowId";
	public static final String TABLE_ROW_VERSION_NUMBER = "rowVersionNumber";


	public static final String NOCACHE_PARAM = "&nocache=";
	
	//servlet response header keys/values to instruct caching behavior
	public static final String EXPIRES_KEY = "Expires";
	public static final String NO_CACHE_VALUE = "no-cache";
	public static final String PRAGMA_KEY = "Pragma";
	public static final String CACHE_CONTROL_VALUE_NO_CACHE = "private, no-store, no-cache, must-revalidate";
	public static final String CACHE_CONTROL_KEY = "Cache-Control";

	public static final String HIGHLIGHT_BOX_TITLE = "highlight-box-title";
	
	public static final String JOIN_WIDGET_IS_CHALLENGE_KEY = "isChallenge";
}
