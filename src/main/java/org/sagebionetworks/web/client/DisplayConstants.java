package org.sagebionetworks.web.client;

public class DisplayConstants {

	/*
	 * Text constants
	 */
	public static final String UPLOAD_SUCCESS = "Upload Success";
	public static final String DEFAULT_PAGE_TITLE = "Synapse | Sage Bionetworks";
	public static final String DEFAULT_PAGE_DESCRIPTION = "Synapse is a collaborative compute space that allows scientists to share and analyze data together.";
	public static final String NO_FILES_SELECTED_FOR_UPLOAD_MESSAGE = "No files were selected for upload.";
	public static final String CREDENTIALS_REQUIRED_MESSAGE = "Credentials are required to upload to this external site.";

	public static final String USERNAME_FORMAT_ERROR = "User names can only contain letters, numbers, dots (.), dashes (-) and underscores (_). They must also be at least 3 characters long.";

	public static final String SHARED_ON_SYNAPSE = " has shared an item with you on Synapse";
	public static final String SHARED_ON_SYNAPSE_SUBJECT = " (shared on Synapse)";
	public static final String UPDATED_NOTIFICATION_SETTINGS = "Updated Notification Settings";

	public static final String SINGLE_LINE_COMMAND_MESSAGE = "This command only supports single line operations.\nPlease select text within a single line and try again.";

	public static final String UNSAVED_CHANGES = "Unsaved Changes";
	public static final String CLOSE_PORTAL_CONFIRMATION_MESSAGE = "Any unsaved changes will be lost. Are you sure that you would like to leave Synapse?";
	public static final String NAVIGATE_AWAY_CONFIRMATION_MESSAGE = "Any unsaved changes may be lost. Are you sure that you would like to navigate away from this editor?";

	public static final String LOGOUT_TEXT = "You have signed out of Synapse.";
	public static final String PASSWORD_RESET_TEXT = "Your password has been set.";

	/*
	 * Buttons, titles and labels
	 */
	public static final String BUTTON_PERMISSIONS_CREATE_NEW_ACL = "Create Local Sharing Settings";
	public static final String BUTTON_PERMISSIONS_DELETE_ACL = "Delete Local Sharing Settings";
	public static final String NOTIFY_PEOPLE_TOOLTIP = "Select to notify newly added people that this item has been shared with them";

	public static final String MENU_PERMISSION_LEVEL_CAN_VIEW = "Can view";
	public static final String MENU_PERMISSION_LEVEL_CAN_DOWNLOAD = "Can download";
	public static final String MENU_PERMISSION_LEVEL_CAN_EDIT = "Can edit";
	public static final String MENU_PERMISSION_LEVEL_CAN_EDIT_DELETE = "Can edit & delete";
	public static final String MENU_PERMISSION_LEVEL_CAN_ADMINISTER = "Administrator";
	public static final String MENU_PERMISSION_LEVEL_CAN_SUBMIT = "Can submit";
	public static final String MENU_PERMISSION_LEVEL_CAN_SCORE = "Can score";
	/*
	 * Service Constants (move to another file?)
	 */
	public static final String BUTTON_LOGIN_AGAIN = "Go to the Synapse login page";
	public static final String BUTTON_CANCEL = "Cancel";
	public static final String BUTTON_CANCEL_REQUESTED = "Cancel Requested";
	public static final String BUTTON_DELETE = "Delete";
	public static final String ERROR_API_TABLE_RENDERER_MISSING_INPUT_COLUMN = "Specified input column is missing from the service response: ";
	public static final String ERROR_ACL_RETRIEVAL_FAILED = "Retrieval of sharing settings failed. Please try again.";
	public static final String ERROR_RESTORING_TRASH_PARENT_NOT_FOUND = "Sorry, an error occurred while restoring this item.";
	public static final String FORGOT_PASSWORD_MESSAGE = "If you have forgotten your password, please click the \"Forgot Password\" button on the login page.";
	public static final String ERROR_USERNAME_ALREADY_EXISTS = "Sorry, that username has already been taken.";
	public static final String ERROR_EMAIL_ALREADY_EXISTS = "There is an existing account with this email.  " + FORGOT_PASSWORD_MESSAGE;
	public static final String ERROR_GENERIC = "An error occurred. Please try again.";
	public static final String ERROR_GENERIC_RELOAD = "An error occurred. Please try reloading the page.";
	public static final String NO_HEADERS_FOUND = "No Headers were found on this page.";
	public static final String ERROR_LOGIN_REQUIRED = "You will need to sign in for access to that resource.";
	public static final String ERROR_ALL_FIELDS_REQUIRED = "All fields must first be properly fill in.";
	public static final String ERROR_ALL_QUESTIONS_REQUIRED = "Please answer all of the questions and try again.";
	public static final String BUTTON_DOWNLOAD = "Download";
	public static final String ERROR_ENTITY_DELETE_FAILURE = "Deletion failed. ";
	public static final String PROMPT_SURE_DELETE = "Are you sure you want to delete this";
	public static final String PROMPT_SURE_REMOVE_MEMBER = " will lose access to resources shared with the team.  Are you sure?";
	public static final String LABEL_NO_SEARCH_RESULTS_PART1 = "Your search for '";
	public static final String LABEL_NO_SEARCH_RESULTS_PART2 = "' did not match any results.";
	public static final String LABEL_SEARCH = "Search";
	public static final String LABEL_PROJECT_CREATED = "Project Created: ";
	public static final String LABEL_TEAM_CREATED = "Team Created: ";
	public static final String TEXT_UPLOAD_FILE_OR_LINK = "Upload or Link to File";
	public static final String UPLOAD_FILE = "Upload File";
	public static final String LINK_TO_URL = "Link to URL";
	public static final String TEXT_LINK_FILE = "Link File";
	public static final String TEXT_LINK_SUCCESS = "Link successfully updated in Synapse";
	public static final String TEXT_LINK_FAILED = "An error occurred while creating the link. Please check the URL and try again.";
	public static final String TEXT_UPLOAD_SUCCESS = "File successfully uploaded";
	public static final String ERROR_UPLOAD_TITLE = "Upload Error";
	public static final String LABEL_UPLOADING = "Uploading...";
	public static final String LABEL_INITIALIZING = "Initializing...";
	public static final String ANONYMOUS_JOIN_EVALUATION = "Please sign in or register for a free Synapse account to participate in this challenge.";
	public static final String ANONYMOUS_JOIN = "Please sign in or register for a free Synapse account to join.";
	public static final String ERROR_TOO_MANY_REQUESTS = "Synapse is over capacity.  You've been temporarily blocked from Synapse due to a high level of activity.  Please wait a moment and try again.";
	public static final String ERROR_CONFLICTING_UPDATE = "This item was updated since you last fetched it.  Please retrieve it again and reapply the update.";
	public static final String ERROR_DEPRECATED_SERVICE = "Synapse attempted to call a service that no longer exists.";
	public static final String ERROR_NOT_FOUND = "Sorry, the requested object was not found or no longer exists.";
	public static final String ERROR_NOT_AUTHORIZED = "Sorry, you are not authorized to modify the requested entity.";
	public static final String NO_ENTITY_SELECTED = "Please select an entity and try again.";
	public static final String NO_EVALUATION_SELECTED = "Please select an evaluation and try again.";
	public static final String TEXT_LINK_SAVED = "Link saved.";
	public static final String ERROR_UPDATE_FAILED = "Update failed. Please try again.";
	public static final String EMPTY = "Empty";
	public static final String CONFIRM_DELETE_DIALOG_TITLE = "Confirm Deletion";
	public static final String ERROR_GENERIC_NOTIFY = "An error occurred. Please report the problem to synapseInfo@sagebase.org";
	public static final String ERROR_CANT_MOVE_HERE = "Sorry, you cannot move this item to the requested spot.";
	public static final String ERROR_DELETING_ATTACHMENT = "An error occurred deleting the Attachment. Please try again.";
	public static final String LABEL_DELETED = "deleted";
	public static final String ERROR_SAVING_WIKI = "Could not save your changes.\nIt is recommended that you copy your version of the wiki text so that it is not lost.\n";
	public static final String ERROR_LOADING_WIKI_FAILED = "Failed to load the wiki page: ";
	public static final String ERROR_LOADING_WIKI_HISTORY_WIDGET_FAILED = "Failed to load the history of the wiki page.";
	public static final String WARNING_PROJECT_NAME_EXISTS = "Sorry, a project with that name already exists. Please try another name.";
	public static final String WARNING_TEAM_NAME_EXISTS = "Sorry, a team with that name already exists. Please try another name.";
	public static final String ERROR_FAILURE_PRIVLEDGES = "Sorry, you do not have sufficient privileges for access.";
	public static final String NETWORK_ERROR = "Network error";
	public static final String ERROR_RESPONSE_UNAVAILABLE = "The web page is not available. Please check your Internet connection.";
	public static final String DESCRIPTION = "Description";
	public static final String OK = "OK";
	public static final String LEFT_ARROWS = "<<";
	public static final String RIGHT_ARROWS = ">>";
	public static final String NOT_FOUND = "Not Found";
	public static final String ERROR_LOADING = "Error Loading";
	public static final String UNDEFINED = "Undefined";
	public static final String ERROR_PROVENANCE = "An error occured creating the Provenance view.";
	public static final String ERROR_PROVENANCE_RELOAD = "Loading Error. Reload.";
	public static final String ACTIVITY = "Activity";

	/**
	 * Widget editors
	 */
	public static final String ERROR_SELECT_ATTACHMENT_MESSAGE = "Unable to insert attachment.";
	public static final String IMAGE_CONFIG_INVALID_URL_MESSAGE = "Please enter a valid URL";
	public static final String IMAGE_CONFIG_INVALID_ALT_TEXT_MESSAGE = "Please enter valid alternate text for the URL";
	public static final String IMAGE_FAILED_TO_LOAD = "Image failed to load: ";

	public static final String INVALID_URL_MESSAGE = "Please enter a valid URL";
	public static final String INVALID_SYNAPSE_ID_MESSAGE = "Please enter a valid Synapse ID";
	public static final String SAVE_BUTTON_LABEL = "Save";


	public static final String ID_BTN_LOGIN_AGAIN = "id_btn_login_again";
	public static final String CHANGE = "change";

	public static final String SEND_BUG_REPORT = "Send Error Report";
	public static final String DO_NOT_SEND_BUG_REPORT = "Don't Send";
	public static final String SHARING_PUBLIC_TITLE = "Sharing Settings";
	public static final String SHARING_PUBLIC_DESCRIPTION = "Everyone can view content in this folder.";
	public static final String SHARING_PRIVATE_DESCRIPTION = "You control who can view content in this folder.";

	public static final String VERSION_INFO_UPDATED = "Updated Version Info";

	public static final String PROVENANCE_BASIC_HELP = "Provenance tracks the relationship between data, code and analytical results.";
	public static final String ERROR_NO_LINK_DEFINED = "Warning: No target reference is defined for this link.";
	public static final String DATA_USE = "Conditions For Use";
	public static final String MEMBERS = "Members";
	public static final String PENDING_TEAM_INVITATIONS = "Pending Team Invitations";
	public static final String PENDING_JOIN_REQUESTS_TOOLTIP = "Team has a pending join request";
	public static final String DELETE_TEAM_SUCCESS = "Team successfully deleted";
	public static final String UPDATE_TEAM_SUCCESS = "Team successfully updated";
	public static final String LEAVE_TEAM_SUCCESS = "Successfully left team";
	public static final String ACCEPT = "Accept";
	public static final String FILES = "Files";
	public static final String ERROR_PAGE_CREATION_FAILED = "Page creation failed. Please try again";
	public static final String UNAUTHORIZED = "Unauthorized";
	public static final String INVALID_WIDGET_MARKDOWN_MESSAGE = "Invalid widget markdown: ";
	public static final String BROWSE_MY_ENTITIES = "Browse";
	public static final String ENTER_SYNAPSE_ID = "Enter Synapse Id";
	public static final String ENTER_PAGE_TITLE = "Enter Page Title";
	public static final String CURRENT = "Current";
	public static final String PLEASE_MAKE_SELECTION = "Please make a selection";
	public static final String INVALID_SELECTION = "Please make a valid selection and try again.";
	public static final String CLOSE = "Close";
	public static final String ERROR_ENTER_AT_LEAST_ONE_ENTITY = "Please enter at least one entity";
	public static final String ERROR_ENTER_DEPTH = "Please enter a valid depth";
	public static final String ERROR_SELECT_VIDEO_FILE = "Please select a video file";
	public static final String ERROR_SELECT_CHALLENGE_PROJECT = "Please select the Synapse project linked to a Challenge.";
	public static final String ERROR_SET_EVALUATION_QUEUE_ID = "Please set the Evaluation Queue ID.";
	public static final String ERROR_SELECT_FORM_CONTAINER = "Please select the Synapse project/folder to look for user form folders.";
	public static final String ERROR_SELECT_FORM_SCHEMA = "Please select the Synapse file representing the form json schema.";
	public static final String ERROR_SELECT_FORM_UI_SCHEMA = "Please select the Synapse file representing the form ui schema.";
	public static final String ERROR_SELECT_CYTOSCAPE_FILE = "Please select a Synapse file representing the Cytoscape JS (JSON).";

	public static final String API_TABLE_MISSING_URI = "SuperTable: Endpoint path not specified.";

	public static final String TEST_MODE_WARNING = "<h5>Alpha Test Mode</h5>This mode is for alpha testing features only. Please note that the <a target=\"blank\" href=\"https://github.com/Sage-Bionetworks/SynapseWebClient\"> developers</a> do not guarantee an absence of errors, " + "and that the data created using alpha features may be lost during product upgrade.<br><br><strong>Are you sure you want to switch into this mode?</strong>";

	public static final String ERROR_SAVE_FAVORITE_MESSAGE = "Saving your Favorite change failed. Please try again.";
	public static final String INVALID_SHINY_SITE = " is not a valid Site URL. Please contact us at " + ClientProperties.HELP_EMAIL_ADDRESS + " if you would like your Server added to our white list.";
	public static final String MARKDOWN_WIDGET_WARNING = "Markdown Widget Warning";
	public static final String DOI_REQUEST_SENT_TITLE = "The request to create a new DOI has been sent. ";
	public static final String DOI_REQUEST_SENT_MESSAGE = "Note that it may take a few minutes for the service to create the new DOI.";
	public static final String LARGE_FILE_ON_UNSUPPORTED_BROWSER = "The file exceeds the maximum file size that Synapse supports for this browser. Please update your browser to the latest version and try again.";
	public static final String PASSWORD = "Password";
	public static final String SIGN_IN = "Sign in";
	public static final String REGISTER_BUTTON = "Register for a free Synapse account";
	public static final String INVALID_USERNAME_OR_PASSWORD = "The user name or password is incorrect.  Please try again.";
	public static final String EVALUATION_SUBMISSION_ERROR = "Unable to submit to the evaluation: ";
	public static final String CHALLENGE_EVALUATIONS_ERROR = "Unable to find the evaluation queues associated with the challenge: ";
	public static final String THANK_YOU_FOR_SUBMISSION = "Thank you for your submission!";
	public static final String SUBMISSION_RECEIVED_TEXT = "Your submission will be scored and results posted to the challenge leaderboard.";
	public static final String NOT_PARTICIPATING_IN_ANY_EVALUATIONS = "You are not currently participating in an Evaluation/Challenge. Please join one and try again.";
	public static final String SEND_PASSWORD_CHANGE_REQUEST = "Reset your Password";
	public static final String EMAIL_ADDRESS = "Username or Email Address";
	public static final String SET_PASSWORD = "Set Password";
	public static final String SUCCESS = "Success";
	public static final String REQUEST_SENT = "Request Sent";
	public static final String PLEASE_ENTER_PROJECT_NAME = "Please enter a project name";
	public static final String PLEASE_ENTER_TEAM_NAME = "Please enter a team name";
	public static final String OLD_VERSION = "old version";
	public static final String NEW_VERSION_AVAILABLE = "New Version of Synapse Available. ";
	public static final String NEW_VERSION_INSTRUCTIONS = "A new version of Synapse is now available, please save your work and reload the page to ensure proper function.";
	public static final String UPLOAD_DIALOG_TITLE = "Upload";
	public static final String SHOW_ALL_RESULTS = "Show Results for All Types";
	public static final String FORGOT_PASSWORD = "forgot password?";
	public static final String SIGNING_IN = "Signing in...";
	public static final String TABLES = "Tables";
	public static final String CREATE_ACCOUNT_MESSAGE_SSO = "The email address supplied by Google was not found in our user list. Please check that you are signed in to the proper Google account, or complete registration if you are new to Synapse.";
	public static final String SSO_ERROR_UNKNOWN = "An unknown error occurred while signing you in with Google account login. Please try again or use your Synapse username and password.";

	public static final String CONFIRM_DELETE_EVAL_QUEUE = "Are you sure you want to delete evaluation queue: ";
	public static final String CONFIRM_DELETE_CHALLENGE = "Are you sure you want to delete the challenge associated with this project?";
	public static final String CHALLENGE_CREATED = "A challenge has been successfully associated with this project.";

	public static final String RESTORING_WIKI_VERSION_WARNING_TITLE = "Restoration Warning";
	public static final String RESTORING_WIKI_VERSION_WARNING_MESSAGE = "Are you sure you want to replace the current version of this Wiki with this one?";
	public static final String SET_PASSWORD_EXPIRED = "This set password request has expired. Please make a new request via the login page.";
	public static final String REQUEST_EXPIRED = "Request Expired";
	public static final String TABLE_UNAVAILABLE = "Table Unavailable";
	public static final String TABLE_PROCESSING_DESCRIPTION = "This table is processing an update. To avoid displaying stale data, query is temporarily unavailable until the processing has completed.";
	public static final String TRY_NOW = "Try Now";
	public static final String WAITING = "Waiting";

	public static final String PASSWORD_HAS_BEEN_CHANGED = "Your password has been changed.";
	public static final String PASSWORD_RESET_SENT = "If a matching account was found, then your password reset request has been sent. Please check your email.";
	public static final String PASSWORDS_MISMATCH = "Passwords do not match. Please re-enter your new password.";

	public static final String ACCOUNT_CREATED = "Your Synapse account has been created.";
	public static final String ACCOUNT_CREATION_FAILURE = "Unable to create your Synapse account:";
	public static final String ACCOUNT_EMAIL_SENT = "We have sent you an email with instructions on how to complete the registration process.";
	public static final String EMAIL_ADDED = "We have sent you an email with instructions on how to verify the new email address.";
	public static final String EMAIL_SUCCESS = "Successfully verified the email address";
	public static final String EMAIL_FAILURE = "Unable to verify the email address:";
	public static final String API_KEY_CHANGED = "API Key successfully changed";
	public static final String API_KEY_CONFIRMATION = "Any scripts using the old API key will break. Are you sure you want Synapse to generate a new API Key?";
	public static final String SESSION_TIMEOUT = "Session Timeout";
	public static final String SESSION_HAS_TIMED_OUT = "Your session has timed out. Please sign in again.";

	public static final String UPLOAD_DESTINATION = "All uploaded files will be stored in ";
	public static final String DOWNLOAD_CREDENTIALS_REQUIRED = "Sign in to download from ";
	public static final String SUCCESSFULLY_LINKED_OAUTH2_ACCOUNT = "Successfully linked the external account to your Synapse profile.";
	public static final String INVALID_EMAIL = "Email address is not valid.";
	public static final String MD5_CALCULATION_ERROR = "Unable to upload. Please select a set of accessible Files to upload.  Uploading a Folder is not supported.";

	// Button styles
	public static final String PRIMARY_BUTTON_STYLE = "btn-primary";
	public static final String LINK_BUTTON_STYLE = "btn-link";
	public static final String DANGER_BUTTON_STYLE = "btn-danger";
	public static final String FINISH = "Finish";
	public static final String NEXT = "Next";
	public static final String SUBMIT = "Submit";
}

