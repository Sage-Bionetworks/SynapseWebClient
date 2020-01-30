package org.sagebionetworks.web.shared;

import org.sagebionetworks.repo.model.util.ModelConstants;


/**
 * Constants for query parameter keys, header names, and field names used by the web components.
 * 
 * All query parameter keys should be in this file as opposed to being defined in individual
 * controllers. The reason for this to is help ensure consistency across controllers.
 * 
 * @author bkng, deflaux
 */
public class WebConstants {

	/**
	 * Regex defining a valid entity name. Characters are selected to ensure compatibility across
	 * services and clients.
	 * 
	 */
	public static final String VALID_ENTITY_NAME_REGEX = ModelConstants.VALID_ENTITY_NAME_REGEX;

	public static final String INVALID_ENTITY_NAME_MESSAGE = "Names may only contain letters, numbers, spaces, underscores, hypens, periods, plus signs, and parentheses.";

	public static final String INVALID_EMAIL_MESSAGE = "Invalid email address";

	public static final String DOCS_BASE_URL = "http://docs.synapse.org/";
	public static final String DOCS_URL = DOCS_BASE_URL + "articles/";
	public static final String PROVENANCE_API_URL = DOCS_URL + "provenance.html";

	public static final String PREVIEW_UNAVAILABLE_PATH = "images/blank.png";

	public static final String INVALID_IMAGE_FILETYPE_MESSAGE = "The file selected is not recognized as an image file type. Please convert to PNG, JPEG, GIF, or SVG and try again.";
	public static final String INVALID_FILE_SIZE = "The selected file exceeds the maximum size. Please select a file that is less than ";

	public static final String INVALID_TABLE_FILETYPE_MESSAGE = "The file selected is not recognized as an table file type. Please convert to TXT or CSV and try again.";

	public static final String DEFAULT_FILE_HANDLE_WIDGET_TEXT = "Browse...";

	/**
	 * Regex defining a valid annotation name. Characters are selected to ensure compatibility across
	 * services and clients.
	 * 
	 */
	public static final String VALID_ANNOTATION_NAME_REGEX = "^[a-z,A-Z,0-9,_,.]+";
	// support #!Place:token/with/delimiter, or standard http url
	public static final String VALID_URL_REGEX = "([#]{1}[!]{1}[a-zA-Z_0-9:/]+)|(^((https?)|(ftp)):\\/\\/[-a-zA-Z0-9+&@#/%?=~_|!:,.;]*[-a-zA-Z0-9+&@#/%=~_|])";
	public static final String VALID_SFTP_URL_REGEX = "^sftp:\\/\\/[-a-zA-Z0-9+&@#/%?=~_|!:,.;]*[-a-zA-Z0-9+&@#/%=~_|]";
	// copied from org.sagebionetworks.repo.model.principal.AliasEnum.USER_NAME and USER_EMAIL, added
	// uppercase support
	public static final String VALID_USERNAME_REGEX = "^[A-Za-z0-9._-]{3,}";
	public static final String VALID_EMAIL_REGEX = "^[A-Za-z0-9._%+-]+@[A-Za-z0-9.-]+\\.[A-Za-z]{2,4}";
	public static final String WIDGET_NAME_REGEX = "[a-z,A-Z,0-9,., ,\\-,\\+,(,)]";
	public static final String VALID_WIDGET_NAME_REGEX = "^" + WIDGET_NAME_REGEX + "+";
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
	// e.g. https://www.google.com/accounts/o8/id

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

	/** FileHandleAssociation servlet params **/
	public static final String ASSOCIATED_OBJECT_ID_PARAM_KEY = "associatedObjectId";
	public static final String ASSOCIATED_OBJECT_TYPE_PARAM_KEY = "associatedObjectType";
	public static final String FILE_HANDLE_ID_PARAM_KEY = "fileHandleId";
	/** END FileHandleAssociation servlet params **/

	public static final String SESSION_TOKEN_KEY = "t";
	public static final String EXPIRE_SESSION_TOKEN = "deleted";
	public static final String ENTITY_PARAM_KEY = "entityId";

	public static final String TEAM_PARAM_KEY = "teamId";

	public static final String ENTITY_VERSION_PARAM_KEY = "version";

	public static final String WIKI_OWNER_ID_PARAM_KEY = "ownerId";

	public static final String WIKI_OWNER_TYPE_PARAM_KEY = "ownerType";

	public static final String WIKI_ID_PARAM_KEY = "wikiId";

	public static final String WIKI_FILENAME_PARAM_KEY = "fileName";

	public static final String WIKI_VERSION_PARAM_KEY = "wikiVersion";

	public static final String FILE_HANDLE_PREVIEW_PARAM_KEY = "preview";

	public static final String ADD_TO_ENTITY_ATTACHMENTS_PARAM_KEY = "isAddToAttachments";

	public static final String SUBMISSION_ID = "submissionId";

	public static final String USER_PROFILE_USER_ID = "userId";

	public static final String USER_PROFILE_IMAGE_ID = "imageId";

	public static final String USER_PROFILE_PREVIEW = "preview";

	public static final String USER_PROFILE_APPLIED = "applied";

	public static final String ENTITY_CREATEDBYPRINCIPALID_KEY = "createdByPrincipalId";

	public static final String ETAG_KEY = "etag";

	public static final String ENTITY_VERSION_STRING = "/version/";

	public static final String MATHJAX_PREFIX = "\\[";
	public static final String MATHJAX_SUFFIX = "\\]";

	// Synapse Properties
	public static final String CHALLENGE_TUTORIAL_PROPERTY = "org.sagebionetworks.portal.challenge_synapse_id";
	public static final String CHALLENGE_WRITE_UP_TUTORIAL_PROPERTY = "org.sagebionetworks.portal.challenge_writeup_synapse_id";
	public static final String FORMATTING_GUIDE_ENTITY_ID_PROPERTY = "org.sagebionetworks.portal.formattingguide_synapse_id";
	public static final String FORMATTING_GUIDE_WIKI_ID_PROPERTY = "org.sagebionetworks.portal.formattingguide_wiki_id";
	public static final String CHALLENGE_PARTICIPATION_INFO_ENTITY_ID_PROPERTY = "org.sagebionetworks.portal.challenge_participation_info_synapse_id";
	public static final String CHALLENGE_PARTICIPATION_INFO_WIKI_ID_PROPERTY = "org.sagebionetworks.portal.challenge_participation_info_wiki_id";
	public static final String GOVERNANCE_ENTITY_ID_PROPERTY = "org.sagebionetworks.portal.governance_synapse_id";
	public static final String GOVERNANCE_WIKI_ID_PROPERTY = "org.sagebionetworks.portal.governance_wiki_id";
	public static final String AUTHENTICATED_ACL_PRINCIPAL_ID = "org.sagebionetworks.portal.authenticated_acl_principal_id";
	public static final String PUBLIC_ACL_PRINCIPAL_ID = "org.sagebionetworks.portal.public_acl_principal_id";
	public static final String ANONYMOUS_USER_PRINCIPAL_ID = "org.sagebionetworks.portal.anonymous_user_principal_id";
	public static final String PROVENANCE_ENTITY_ID_PROPERTY = "org.sagebionetworks.portal.provenance_synapse_id";
	public static final String PROVENANCE_WIKI_ID_PROPERTY = "org.sagebionetworks.portal.provenance_wiki_id";
	public static final String NBCONVERT_ENDPOINT_PROPERTY = "org.sagebionetworks.portal.nbconvert_endpoint";
	public static final String FORUM_SYNAPSE_ID_PROPERTY = "org.sagebionetworks.portal.forum_project_id";

	// Workshop
	public static final String COLLABORATORIUM_ENTITY_ID_PROPERTY = "org.sagebionetworks.portal.collaboratorium_synapse_id";
	public static final String STAGE_I_ENTITY_ID_PROPERTY = "org.sagebionetworks.portal.stageI_synapse_id";
	public static final String STAGE_II_ENTITY_ID_PROPERTY = "org.sagebionetworks.portal.stageII_synapse_id";
	public static final String STAGE_III_ENTITY_ID_PROPERTY = "org.sagebionetworks.portal.stageIII_synapse_id";
	public static final String STAGE_IV_ENTITY_ID_PROPERTY = "org.sagebionetworks.portal.stageIV_synapse_id";
	public static final String STAGE_V_ENTITY_ID_PROPERTY = "org.sagebionetworks.portal.stageV_synapse_id";
	public static final String STAGE_VI_ENTITY_ID_PROPERTY = "org.sagebionetworks.portal.stageVI_synapse_id";
	public static final String STAGE_VII_ENTITY_ID_PROPERTY = "org.sagebionetworks.portal.stageVII_synapse_id";
	public static final String STAGE_VIII_ENTITY_ID_PROPERTY = "org.sagebionetworks.portal.stageVIII_synapse_id";
	public static final String STAGE_IX_ENTITY_ID_PROPERTY = "org.sagebionetworks.portal.stageIX_synapse_id";
	public static final String STAGE_X_ENTITY_ID_PROPERTY = "org.sagebionetworks.portal.stageX_synapse_id";

	public static final String FILE_HANDLE_ASSOCIATION_SERVLET = "filehandleassociation";
	public static final String ALIAS_REDIRECTOR_SERVLET = "aliasredirector";
	public static final String ALIAS_PARAM_KEY = "alias";
	public static final String SLACK_SERVLET = "slack";
	public static final String VERSIONS_SERVLET = "versions";
	public static final String FILE_HANDLE_UPLOAD_SERVLET = "filehandle";
	public static final String SESSION_COOKIE_SERVLET = "sessioncookie";
	public static final String FILE_ENTITY_RESOLVER_SERVLET = "fileresolver";
	public static final String SFTP_PROXY_ENDPOINT = "org.sagebionetworks.portal.sftp_proxy_endpoint";

	public static final String TEXT_COMMA_SEPARATED_VALUES = "text/csv";
	public static final String TEXT_TAB_SEPARATED_VALUES = "text/tab-separated-values";

	public static final String FILE_UPLOADER_IS_UPDATE_PARAM = "isUpdate";

	public static final String CONTENT_TYPE_JNLP = "application/x-java-jnlp-file";
	public static final String CONCRETE_TYPE_KEY = "concreteType";
	public static final String NODE_TYPE_KEY = "nodeType";


	public static final String GETTING_STARTED = "GettingStarted";

	public static final String FORMATTING_GUIDE = "FormattingGuide";
	public static final String CHALLENGE_PARTICIPATION_INFO = "ChallengeParticipationInfo";

	public static final String WIKI_PROPERTIES_PACKAGE = "org.sagebionetworks.portal.wikis.";
	public static final String COLLABORATORIUM = "Collaboratorium";
	public static final String STAGE_I = "StageI";
	public static final String STAGE_II = "StageII";
	public static final String STAGE_III = "StageIII";
	public static final String STAGE_IV = "StageIV";
	public static final String STAGE_V = "StageV";
	public static final String STAGE_VI = "StageVI";
	public static final String STAGE_VII = "StageVII";
	public static final String STAGE_VIII = "StageVIII";
	public static final String STAGE_IX = "StageIX";
	public static final String STAGE_X = "StageX";

	public static final String CERTIFICATION = "Certification";
	public static final String VALIDATION = "Validation";
	public static final String FORUM = "Forum";

	// APITableWidget default column names
	public static final String DEFAULT_COL_NAME_PARENT_ID = "parentid";
	public static final String DEFAULT_COL_NAME_ENTITY_ID = "entityid";
	public static final String DEFAULT_COL_NAME_MODIFIED_ON = "modifiedon";
	public static final String DEFAULT_COL_NAME_CREATED_ON = "createdon";
	public static final String DEFAULT_COL_NAME_USER_ID = "userid";
	public static final String DEFAULT_COL_NAME_SUBMITTER_ID = "submitterid";
	public static final String DEFAULT_COL_NAME_MODIFIED_BY_PRINCIPAL_ID = "modifiedbyprincipalid";
	public static final String DEFAULT_COL_NAME_CREATED_BY_PRINCIPAL_ID = "createdbyprincipalid";
	public static final String DEFAULT_COL_NAME_ID = "id";

	public static final String HIGHLIGHT_KEY = "highlight";

	// ClientCache key suffixes (used to avoid collision in the cache)
	public static final String USER_PROFILE_SUFFIX = "_USER_PROFILE";
	public static final String MESSAGE_SUFFIX = "_DISCUSSION_MESSAGE";
	public static final String USERNAME_SUFFIX = "_USERNAME_2_ID";
	public static final String TEMP_IMAGE_ATTACHMENT_SUFFIX = "_TEMP_IMAGE_ATTACHMENT";
	public static final String WIKIPAGE_SUFFIX = "_WIKI_PAGE";
	public static final String FILE_HANDLE_SUFFIX = "_FILE_HANDLE";

	public static final String TABLE_COLUMN_ID = "columnId";
	public static final String TABLE_ROW_ID = "rowId";
	public static final String TABLE_ROW_VERSION_NUMBER = "rowVersionNumber";

	public static final String RAW_FILE_HANDLE_PARAM = "rawFileHandleId";
	public static final String NOCACHE_PARAM = "&nocache=";

	// servlet response header keys/values to instruct caching behavior
	public static final String EXPIRES_KEY = "Expires";
	public static final String NO_CACHE_VALUE = "no-cache";
	public static final String PRAGMA_KEY = "Pragma";
	public static final String CACHE_CONTROL_VALUE_NO_CACHE = "private, no-store, no-cache, must-revalidate";
	public static final String CACHE_CONTROL_KEY = "Cache-Control";

	public static final String HIGHLIGHT_BOX_TITLE = "highlight-box-title";

	public static final String JOIN_WIDGET_IS_CHALLENGE_KEY = "isChallenge";

	// query parameters
	public static final String SELECT_ID_FROM_ENTITY_WHERE_PARENT_ID = "select id from entity where parentId == '";
	public static final String AND_NAME_EQUALS = "' and name == '";
	public static final String LIMIT_ONE = "' limit 1";

	public static final String SFTP_PREFIX = "sftp://";

	public static final String USER_ID_PARAM = "userId";
	public static final String EMAIL_PARAM = "email";
	public static final String LAST_NAME_PARAM = "lastName";
	public static final String FIRST_NAME_PARAM = "firstName";

	public static final String OAUTH2_PROVIDER = "oauth2provider";
	public static final String OAUTH2_CODE = "code";
	public static final String OAUTH2_STATE = "state";

	// discussion message
	public static final String DISCUSSION_MESSAGE_SERVLET = "/discussion/messageUrl";
	public static final String MESSAGE_KEY_PARAM = "messageKey";
	public static final String TYPE_PARAM = "type";
	public static final String THREAD_TYPE = "thread";
	public static final String REPLY_TYPE = "reply";

	public static final String TEXT_PLAIN_CHARSET_UTF8 = "text/plain; charset=utf-8";
	public static final String TEXT_HTML_CHARSET_UTF8 = "text/html; charset=utf-8";

	public static final String CONTENT_TYPE = "Content-Type";

	public static final Long ZERO_OFFSET = 0L;
	public static final String REPO_SERVICE_URL_KEY = "repoServiceUrl";
	public static final String FILE_SERVICE_URL_KEY = "fileServiceUrl";
	public static final String AUTH_PUBLIC_SERVICE_URL_KEY = "authPublicServiceUrl";
	public static final String SYNAPSE_VERSION_KEY = "synapseVersionInfo";

	// View mask constants
	public static final int FILE = 0x01, PROJECT = 0x02, TABLE = 0x04, FOLDER = 0x08, VIEW = 0x10, DOCKER = 0x20;

	/**
	 * Jira Issue Creation constants
	 */
	// These IDs refer to Jira Components created in the Sage Bionetworks Jira Cloud instance in the
	// Governance Project.
	public static final String REVIEW_ABUSIVE_CONTENT_REQUEST_COMPONENT_ID = "14868";
	// public static final String DATA_RESTRICTION_REQUEST_COMPONENT_ID = "14865";
	public static final String REVIEW_DATA_REQUEST_COMPONENT_ID = "14869";
	public static final String GRANT_ACCESS_REQUEST_COMPONENT_ID = "14866";
	// flag issue
	public static final String FLAG_ISSUE_COLLECTOR_URL = "https://sagebionetworks.jira.com/s/d41d8cd98f00b204e9800998ecf8427e-T/g39zuk/b/41/e73395c53c3b10fde2303f4bf74ffbf6/_/download/batch/com.atlassian.jira.collector.plugin.jira-issue-collector-plugin:issuecollector-embededjs/com.atlassian.jira.collector.plugin.jira-issue-collector-plugin:issuecollector-embededjs.js?locale=en-US&collectorId=d0abcfa9";
	public static final String FLAG_ISSUE_PRIORITY = "3";
	public static final String FLAG_ISSUE_DESCRIPTION_PART_1 = "Reporting this page: ";
	public static final String FLAG_ISSUE_DESCRIPTION_PART_2 = " \n\nUser is reporting to the Synapse team that this page is in violation (for example: abusive or harmful content, spam, inappropriate ads), or this data is posted inappropriately or should have different access conditions.";
	// request access
	public static final String REQUEST_ACCESS_ISSUE_COLLECTOR_URL = "https://sagebionetworks.jira.com/s/d41d8cd98f00b204e9800998ecf8427e-T/-2rg9hj/b/25/e73395c53c3b10fde2303f4bf74ffbf6/_/download/batch/com.atlassian.jira.collector.plugin.jira-issue-collector-plugin:issuecollector-embededjs/com.atlassian.jira.collector.plugin.jira-issue-collector-plugin:issuecollector-embededjs.js?locale=en-US&collectorId=bd4dc1e5";
	public static final String REQUEST_ACCESS_ISSUE_SUMMARY = "Request for ACT to grant access to data";
	public static final String REQUEST_ACCESS_ISSUE_DESCRIPTION = "User requests that the Synapse Access and Compliance Team send them information on how to access this data.";
	public static final String ISSUE_PRIORITY_MINOR = "4";
	public static final String ANONYMOUS = "Anonymous";

	// report Synapse error
	public static final String SWC_ISSUE_COLLECTOR_URL = "https://sagebionetworks.jira.com/s/d41d8cd98f00b204e9800998ecf8427e-T/-bhcm7i/b/6/e73395c53c3b10fde2303f4bf74ffbf6/_/download/batch/com.atlassian.jira.collector.plugin.jira-issue-collector-plugin:issuecollector-embededjs/com.atlassian.jira.collector.plugin.jira-issue-collector-plugin:issuecollector-embededjs.js?locale=en-US&collectorId=ddc881b3";
}
