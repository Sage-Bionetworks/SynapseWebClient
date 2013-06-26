package org.sagebionetworks.web.client;

import com.google.gwt.i18n.client.DateTimeFormat;


public class DisplayConstants {

	public static final DateTimeFormat DATE_FORMAT = DateTimeFormat.getFormat("dd-MMM-yyyy");
	public static final DateTimeFormat DATE_TIME_FORMAT = DateTimeFormat.getFormat(DateTimeFormat.PredefinedFormat.DATE_TIME_MEDIUM);
	public static final DateTimeFormat DATE_FORMAT_SERVICES = DateTimeFormat.getFormat("yyyy-MM-dd");
	/*
	 * Text constants
	 */
	public static final String DEFAULT_PAGE_TITLE = "Sage Synapse: Contribute to the Cure";
	public static final String DEFAULT_PAGE_DESCRIPTION = "Synapse is a collaborative compute space that allows scientists to share and analyze data together.";
	
	public static final String FOLLOW_DATASET_HTML = "Following a dataset allows you to be notified when the dataset has new comments, additional layers or data, is used in a project, or referenced by a new publication.<br/><br/>Click Confirm to Follow this dataset. You can adjust your notifications settings through your Profile.";  
	public static final String FOLLOW_PROJECT_HTML = "Following a project allows you to be notified when the project has new comments, additional layers or data, is used in a project, or referenced by a new publication.<br/><br/>Click Confirm to Follow this project. You can adjust your notifications settings through your Profile.";
	public static final String FOLLOW_ANALYSIS_HTML = "Following an analysis project allows you to be notified when the analysis has new comments or additional steps.<br/><br/>Click Confirm to Follow this project. You can adjust your notifications settings through your Profile.";
	public static final String FOLLOW_STEP_HTML = "REMOVE ME, we don't want to follow steps";
	
	public static final String TEMP_MSKCC_DESCRIPTION = "Genetic and epigenetic alterations have been identified that lead to transcriptional Annotation of prostate cancer genomes provides a foundation for discoveries that can impact disease understanding and treatment. Concordant assessment of DNA copy number, mRNA expression, and focused exon resequencing in the 218 prostate cancer tumors represented in this dataset haveidentified the nuclear receptor coactivator NCOA2 as an oncogene in approximately 11% of tumors. Additionally, the androgen-driven TMPRSS2-ERG fusion was associated with a previously unrecognized, prostate-specific deletion at chromosome 3p14 that implicates FOXP1, RYBP, and SHQ1 as potential cooperative tumor suppressors. DNA copy-number data from primary tumors revealed that copy-number alterations robustly define clusters of low- and high-risk disease beyond that achieved by Gleason score.";
	
	public static final String CREATE_DATASET_TEXT = "Please fill out the initial descriptive fields for the dataset you would like to create. You may add detailed annotations and upload Layers to this dataset by editing it once it is created.";
	public static final String EDIT_DATASET_TEXT = "Edit the dataset's details by altering the fields below and then clicking the Save button. To cancel, click the Close button."; 
	public static final String CREATE_LAYER_TEXT = "Please fill out the initial descriptive fields for the layer you would like to create. You may add detailed annotations and upload an actual data file to this layer by editing it once it is created.";
	public static final String EDIT_LAYER_TEXT = "Edit the layer's details by altering the fields below and then clicking the Save button. To cancel, click the Close button.";
	public static final String CREATE_PROJECT_TEXT = "Please fill out the initial descriptive fields for the project you would like to create.";
	public static final String EDIT_PROJECT_TEXT = "Edit the project's details by altering the fields below and then clicking the Save button. To cancel, click the Close button."; 
	public static final String CREATE_ANALYSIS_TEXT = "Please fill out the initial descriptive fields for the analysis you would like to create.";
	public static final String EDIT_ANALYSIS_TEXT = "Edit the analysis' details by altering the fields below and then clicking the Save button. To cancel, click the Close button.";;
	public static final String CREATE_STEP_TEXT = "Please fill out the initial descriptive fields for the step you would like to create.";
	public static final String EDIT_STEP_TEXT = "Edit the step's details by altering the fields below and then clicking the Save button. To cancel, click the Close button.";;
	public static final String CREATE_CODE_TEXT = "Please fill out the initial descriptive fields for the step you would like to create.";
	public static final String EDIT_CODE_TEXT = "Edit the step's details by altering the fields below and then clicking the Save button. To cancel, click the Close button.";;
	public static final String EDIT_ANNOTATIONS_TEXT = "Edit Annotations for this object. You can add new annotations by using the form fields at the bottom.";
	
	public static final String LOGOUT_TEXT = "You have been logged out of Synapse.";
	public static final String LOGOUT_SSO_TEXT = "To prevent others from accessing your account in this browser, you should log out of your Google account as well.<br/><a href=\"http://www.google.com/accounts/Logout?continue=http://www.google.com/\">Logout of your Google account by clicking here</a>"; // <br/><br/><a href=\"https://www.google.com/accounts/Logout\">Logout of your Google account by clicking here</a>
	public static final String PERMISSIONS_INHERITED_TEXT = "Sharing settings are being inherited from the parent resource and cannot be modified.";
	public static final String PERMISSIONS_CREATE_NEW_ACL_TEXT = "Sharing settings from the parent resource will be overridden.";
	public static final String PERMISSIONS_DELETE_ACL_TEXT = "Sharing settings will be inherited from the parent resource.";
	public static final String PERMISSIONS_APPLY_ACL_TO_CHILDREN_TEXT = "Sharing settings of all child resources will be overwritten.";
	public static final String PERMISSIONS_UNSAVED_CHANGES = "You have unsaved changes";
	
	public static final String PASSWORD_RESET_FAILED_TEXT = "Password reset failed. Please try again.";
	public static final String PASSWORD_SET_FAILED_TEXT = "Password change failed. Please try again.";
	public static final String PASSWORD_RESET_TEXT = "Your password has been reset.";
	public static final String PASSWORD_SET_TEXT = "Your password has been set. Please login to verify.";
	public static final String PASSWORD_AND_EMAIL_SET_TEXT = "Your new email address and password have been set. Please login to verify.";
	public static final String EMAIL_SET_FAILED_TEXT = "Email address change failed. Please try again.";
	
	public static final String PREVIEW_UNAVAILABLE="No Preview Available";	//text shown when FileEntity preview can't be shown
	
	public static final String NEWS_UNAVAILABLE_TEXT = "The Synapse news service is currently unavailable.";
	public static final String SUPPORT_FEED_UNAVAILABLE_TEXT = "The Synapse support feed service is currently unavailable.";
	
	public static final String STORAGE_USAGE_FAILED_TEXT = "Unable to determine the current usage at this time.";
	
	public static final String INVALID_WIDGET_DESCRIPTOR_TYPE = "Widget descriptor is not of the correct type";
	/*
	 * Buttons, titles and labels
	 */
	public static final String TITLE_TERMS_OF_USE = "Terms of Use";
	public static final String TITLE_LAYER_PREVIEW = "Layer Preview";
	public static final String BUTTON_SEE_TERMS_OF_USE = "See Terms of Use";
	public static final String BUTTON_FOLLOW_DATASET = "Follow this Dataset";
	public static final String TITLE_SHARING_PANEL = "Sharing Settings";
	public static final String LABEL_SHARING_PANEL_EXISTING = "Sharing Settings";
	public static final String LABEL_SHARING_PANEL_INHERITED = "Inherited Sharing Settings";
	public static final String BUTTON_PERMISSIONS_CREATE_NEW_ACL = "Create Local Sharing Settings";
	public static final String BUTTON_PERMISSIONS_DELETE_ACL = "Delete Local Sharing Settings";
	public static final String BUTTON_MAKE_PUBLIC_ACL = "Make Public";
	public static final String BUTTON_REVOKE_PUBLIC_ACL = "Make Private";
	public static final String BUTTON_MAKE_PUBLIC_ACL_TOOLTIP = "Click to allow access to anyone on the internet";
	public static final String BUTTON_REVOKE_PUBLIC_ACL_TOOLTIP = "Click to restrict access to only those people you select above";
	public static final String PUBLIC_ACL_TITLE = "Public on the web";
	public static final String PUBLIC_ACL_DESCRIPTION = "Anyone on the internet can access, no login required";
	public static final String PUBLIC_ACL_ENTITY_PAGE = "Public";
	public static final String PRIVATE_ACL_ENTITY_PAGE = "Private";
	public static final String PRIVATE_ACL_DESCRIPTION = "Restricted to those people selected in the Share Settings window";
	
	public static final String AUTHENTICATED_USERS_ACL_TITLE = "Anyone logged in";
	public static final String AUTHENTICATED_USERS_ACL_DESCRIPTION = "Anyone who is logged in can access";
	
	public static final String CONFIRM_DELETE_ACL = "Deleting local sharing settings will cause inheritance from parent. Proceed?";
	public static final String BUTTON_PERMISSIONS_APPLY_ACL_TO_CHILDREN = "Apply Local Sharing Settings to Children";
	public static final String CONFIRM_APPLY_ACL_TO_CHILDREN = PERMISSIONS_APPLY_ACL_TO_CHILDREN_TEXT + " Proceed?";
	public static final String UNSAVED_CHANGES = "Unsaved Changes";
	public static final String CONFIRM_DISCARD_ACL_CHANGES = "Your unsaved changes will be discarded. Are you sure?";
	public static final String MENU_PERMISSION_LEVEL_IS_OWNER = "Is Owner";
	public static final String MENU_PERMISSION_LEVEL_CAN_VIEW = "Can view";
	public static final String MENU_PERMISSION_LEVEL_CAN_EDIT = "Can edit";
	public static final String MENU_PERMISSION_LEVEL_CAN_EDIT_DELETE = "Edit & Delete";
	public static final String MENU_PERMISSION_LEVEL_CAN_ADMINISTER = "Administrator";
	public static final String LABEL_PERMISSION_TEXT_ADD_PEOPLE = "Add People";
	public static final String BUTTON_SHOW_R_CLIENT_INSTALL = "Show R Client Installation Code";
	public static final String LABEL_R_CLIENT_INSTALL = "Install R Client";
	public static final String LABEL_PYTHON_CLIENT_INSTALL = "Install Python Client";
	public static final String INSTALL_JAVA_MAVEN = "Install Java Client";
	public static final String LABEL_CL_CLIENT_INSTALL = "Install Command Line Client";
	public static final String LABEL_CLIENT_GET_ENTITY = "Obtain a pointer and download the data";
	public static final String LABEL_CLIENT_LOAD_ENTITY = "Load the data";
	public static final String GET_PATH_CLIENT_ENTITY = "Get the path to the local copy of the data file";
	public static final String DOWNLOAD_FILE_LOCAL = "Download file";
	public static final String LOGIN_USERNAME_LABEL = "Email Address";
	
	/*
	 * Service Constants (move to another file?)
	 */
	public static final String SINGLE_SIGN_ON_USERID = "SSO";
	public static final String OPEN_ID_SAGE_LOGIN_BUTTON_TEXT = "Sign in with a Google Account";
	public static final String OPEN_ID_PROVIDER_GOOGLE_VALUE = "https://www.google.com/accounts/o8/id";
	public static final String OPEN_ID_PROVIDER_SAGE_VALUE = "https://www.google.com/accounts/o8/site-xrds?hd=sagebase.org";

	public static final String SERVICE_PARENT_ID_KEY = "parentId";
	public static final String SERVICE_ETAG_KEY = "etag";
	public static final String SERVICE_STATUS_KEY = "status";
	public static final String SERVICE_LAYER_TYPE_KEY = "type";
	
	public static final String SERVICE_HEADER_ETAG_KEY = "ETag";

	public static final String SYNAPSE_ID = "Synapse Id";
	public static final String SYNAPSE_ID_PREFIX = SYNAPSE_ID + ": ";		
	public static final String BUTTON_LOGIN_AGAIN = "Go to Synapse Login Page";
	public static final String LABEL_LOGOUT_TEXT = "Logout";
	public static final String BUTTON_CANCEL = "Cancel";
	public static final String BUTTON_ADD_ANNOTATION = "Add Annotation";
	public static final String TEXT_NO_DOWNLOADS = "There are no downloads available.";
	public static final String TEXT_UNAUTH_DOWNLOADS = "Please login or sign the End User License Agreement to see downloads.";
	public static final String BUTTON_SETUP_API_PASSWORD = "Create Synapse Password";
	public static final String BUTTON_SAVING = "Saving";
	public static final String BUTTON_CHANGE_PASSWORD = "Change Password";
	public static final String BUTTON_UPDATE_WITH_LINKED_IN = "Update With LinkedIn";
	public static final String BUTTON_CHANGE_USER_INFO = "Update";
	public static final String LABEL_SINGLE_SIGN_ON_LOGGING_IN = "Logging you in.";
	public static final String LABEL_SEARCHING = "Searching...";
	public static final String LABEL_FOUND_FORWARDING = "Found. Forwarding now.";
	public static final String LABEL_ENTITY_NOT_FOUND = "Sorry, that Synapse Id could not be located.";
	public static final String ERROR_API_TABLE_RENDERER_MISSING_INPUT_COLUMN = "Specified input column is missing from the service response: ";
	public static final String ERROR_GETTING_PERMISSIONS_TEXT = "READ ONLY MODE. Reason: An error occurred in retrieving your level of access.";	
	public static final String ERROR_ACL_RETRIEVAL_FAILED = "Retrieval of sharing settings failed. Please try again.";
	public static final String ERROR_LOCAL_ACL_CREATION_FAILED = "Creation of local sharing settings failed. Please try again.";
	public static final String ERROR_FAILED_PERSIST_AGREEMENT_TEXT = "Your license acceptance was not saved. You will need to sign it again in the future.";	
	public static final String ERROR_USER_ALREADY_EXISTS = "There is an existing account associated with this email.  If you have forgotten your password, please click the \"Forgot Password\" button on the login page.";
	public static final String ERROR_UNABLE_TO_LOAD = "An error occurred trying to load.";
	public static final String ERROR_UNABLE_TO_UPDATE_ATTACHMENTS = "The attachments could not be updated because this entity has conflicting changes. Please reload the page and try again.";
	public static final String ERROR_GENERIC = "An error occurred. Please try again.";
	public static final String ERROR_INCOMPATIBLE_CLIENT_VERSION = "Your client version is incompatible with the repository. Please try reloading the page.";
	public static final String ERROR_GENERIC_RELOAD = "An error occurred. Please try reloading the page.";
	public static final String ERROR_SAVE_MESSAGE = "An error occuring attempting to save. Please try again.";
	public static final String ERROR_BAD_REQUEST_MESSAGE = "An unknown communication error occurred. Please reload the page.";
	public static final String ERROR_DUPLICATE_ENTITY_MESSAGE = "An entity with this name already exists. Please enter a different name.";
	public static final String ERROR_INVALID_ENTITY_NAME = "Name contains contains an invalid character";
	public static final String NO_HEADERS_FOUND = "No Headers were found on this page.";
	public static final String ERROR_TITLE_LOGIN_REQUIRED = "Login Required";
	public static final String ERROR_LOGIN_REQUIRED = "You will need to login for access to that resource.";
	public static final String ERROR_TITLE_VALIDATION_ERROR = "Validation Error";
	public static final String ERROR_ALL_FIELDS_REQUIRED = "All fields are required.";
	public static final String TITLE_ADD_ANNOTATION = "Add Annotation";
	public static final String BUTTON_RETURN_TO_LAYER = "Return to Layer";
	public static final String LABEL_GOTO_SYNAPSE_ID = "Enter a Synapse Id...";
	public static final String LABEL_DELETE_PROJECT = "Delete Project";
	public static final String TITLE_CREATE_DATASET = "Create Dataset";
	public static final String TITLE_CREATE_REFERENCE = "Create Reference";
	public static final String BUTTON_EDIT_PROJECT_DETAILS = "Edit Project Details";
	public static final String BUTTON_ADD_DATASET_TO_PROJECT = "Add Dataset to Project";
	public static final String BUTTON_ADD_DATASET = "Add Dataset";
	public static final String BUTTON_DOWNLOAD_DATASET = "Download Dataset";
	public static final String BUTTON_ADD_A_LAYER_TO_DATASET = "Add a Layer to Dataset";
	public static final String BUTTON_ADD_LAYER = "Add Layer";
	public static final String BUTTON_EDIT_ANALYSIS_DETAILS = "Edit Analysis Details";
	public static final String BUTTON_ADD_STEP = "Add Step";
	public static final String BUTTON_ADD_STEP_TO_ANALYSIS = "Add Step to Analysis";
	public static final String LABEL_DELETE_ANALYSIS = "Delete Analysis";
	public static final String TITLE_CREATE_STEP = "Create Step";
	public static final String BUTTON_EDIT_STEP_DETAILS = "Edit Step Details";
	public static final String LABEL_DELETE_STEP = "Delete Step";
	public static final String BUTTON_ADD_REFERENCE_TO_STEP = "Add Reference to Step";
	public static final String BUTTON_ADD_REFERENCE = "Add Reference";
	public static final String TITLE_ERROR = "Error";
	public static final String BUTTON_TOOLS_MENU = "Tools";
	public static final String BUTTON_ADD = "Add";
	public static final String BUTTON_EDIT = "Edit";
	public static final String BUTTON_EDIT_WIKI = "Edit Wiki";
	public static final String BUTTON_DELETE_WIKI = "Delete&nbspPage";
	public static final String BUTTON_WIKI_ATTACHMENTS = "Attachments";
	public static final String BUTTON_SHARE = "Share";
	public static final String BUTTON_DOWNLOAD = "Download";
	public static final String LABEL_DELETE = "Delete";
	public static final String LABEL_RENAME = "Rename";
	public static final String LABEL_CREATE = "Create";
	public static final String LABEL_MOVE = "Move";
	public static final String SELECT_NEW_PARENT = "Select New Parent for this";
	public static final String LABEL_ARE_YOU_SURE = "Are you sure?";
	public static final String ERROR_ENTITY_DELETE_FAILURE = "Deletion failed. Please try again.";
	public static final String ERROR_ENTITY_MOVE_FAILURE = "Move failed. Please try again.";
	public static final String PROMPT_SURE_DELETE = "Are you sure you want to delete this";
	public static final String PROMPT_SURE_PROMOTE = "Are you sure you want to promote this version to the top?";
	public static final String LABEL_NO_PREVIEW_DATA = "A Preview has not been created.";
	public static final String LABEL_CONTAINS_NO_CHILDREN = "This object contains no children.";
	public static final String BUTTON_REGISTER = "Register";
	public static final String LINK_COMMUNITY_FORUM = "Forum";
	public static final String BUTTON_LOGIN = "Login";
	public static final String BUTTON_DREAM_8 = "DREAM8 Challenges";
	public static final String LABEL_WELCOME = "Welcome";
	public static final String BUTTON_LOGOUT = "Logout";
	public static final String BUTTON_MY_PROFILE = "My Profile";
	public static final String LABEL_NO_SEARCH_RESULTS_PART1 = "Your search for '";
	public static final String LABEL_NO_SEARCH_RESULTS_PART2 = "' did not match any results.";
	public static final String LABEL_SEARCH = "Search";
	public static final String LABEL_PROJECT_NAME = "Project Name";
	public static final String LABEL_PROJECT_CREATED = "Project Created";
	public static final String TEXT_UPLOAD_FILE_OR_LINK = "Upload or Link to File";
	public static final String TEXT_USER_SETTINGS = "Settings";
	public static final String TEXT_USER_VIEW_PROFILE = "Profile";
	public static final String UPLOAD_FILE = "Upload File";
	public static final String LINK_TO_URL = "Link to URL";
	public static final String TEXT_LINK_FILE = "Link File";
	public static final String TEXT_LINK_SUCCESS = "Link successfully updated in Synapse";
	public static final String TEXT_LINK_FAILED = "An error occurred while creating the link. Please check the URL and try again.";
 	public static final String TEXT_UPLOAD_SUCCESS = "File Uploaded to Synapse";
	public static final String TEXT_ATTACHMENT_SUCCESS = "Attachment Uploaded to Synapse";
	public static final String TEXT_PROFILE_PICTURE_SUCCESS = "Profile Photo Uploaded to Synapse";
	public static final String ERROR_UPLOAD = "An error occurred during upload. Please try again.";
	public static final String LABEL_UPLOADING = "Uploading...";
	public static final String LABEL_INITIALIZING = "Initializing...";
	public static final String LABEL_FINISHING = "Finishing...";
	public static final String LABEL_CREATE_LINK = "Save Link To This Page";
	public static final String LABEL_SUBMIT_TO_EVALUATION = "Submit To Challenge";
	public static final String ANONYMOUS_JOIN_EVALUATION = "Please login or register for a free Synapse account to participate in this challenge.";
	
	public static final String SUBMITTED_TITLE = "Submitted";
	public static final String SUBMITTED_TO_EVALUATION = "Submitted to the selected evaluation(s)";
	
	public static final String LABEL_WHERE_SAVE_LINK = "Where would you like to save a Link to this page?";
	public static final String ERROR_NOT_FOUND = "Sorry, the requested object was not found or no longer exists.";
	public static final String ERROR_NOT_AUTHORIZED = "Sorry, you are not authorized to modify the requested entity.";
	public static final String ERROR_ENTITY_CANT_LINKS = "Sorry, the requested entity can not contain links.";
	public static final String ERROR_ENTITY_CREATION = "Sorry, creation failed. Please try again.";
	public static final String ERROR_ENTITY_PROMOTE_FAILURE = "Sorry, promoting the entity failed.";
	public static final String LABEL_SUCCESS = "Success";
	public static final String LABEL_PROMOTED = " was promoted to top.";
	public static final String TEXT_LINK_SAVED = "Link saved.";
	public static final String ERROR_UPDATE_FAILED = "Update failed. Please try again.";
	public static final String TEXT_NO_REFERENCES = "No others point to this";
	public static final String BUTTON_FILTER_ONLY_MY_CREATION = "Show Projects I Created";
	public static final String BUTTON_FILTER_USER_UPDATABLE = "Show Projects I Can Edit";
	public static final String STATUS_CAN_EDIT = "Can Edit";
	public static final String STATUS_CREATED_BY = "Owner";
	public static final String EMPTY = "Empty";
	public static final String NONE_FOUND = "None Found";
	public static final String LABEL_NO_DESCRIPTION = "Please add a description by clicking the \"Edit\" button.";
	public static final String LABEL_NO_MARKDOWN = "Please add more information by clicking the \"Edit Wiki\" button.";
	public static final String LABEL_NO_ACTIVITY = "Activity Feed is empty.";
	public static final String ERROR_GENERIC_NOTIFY = "An error occurred. Please report the problem to synapseInfo@sagebase.org";
	public static final String ERROR_CANT_SAVE_LINK_HERE = "Sorry, you cannot save a Link to the requested spot.";
	public static final String ERROR_CANT_MOVE_HERE = "Sorry, you cannot move this item to the requested spot.";
	public static final String TEXT_RIGHT_CLICK_FOR_CONTEXT_MENU = "Right click on an item in the list to Edit or Delete.";
	public static final String ERROR_USER_PROFILE_SAVE = "An error occurred saving to your user profile.";
	public static final String LABEL_UPDATED = "Upated";
	public static final String TEXT_USER_PROFILE_UPDATED = "Change save to your User Profile.";
	public static final String TEXT_NO_ATTACHMENTS = "No Attachments.";
	public static final String TEXT_NO_COLUMNS = "No Columns.";
	public static final String ERROR_DELETING_ATTACHMENT = "An error occurred deleting the Attachment. Please try again.";
	public static final String LABEL_DELETED = "deleted";
	public static final String ERROR_LOADING_DESCRIPTION_FAILED = "Failed to load the description: ";
	public static final String ERROR_LOADING_MARKDOWN_FAILED = "Failed to load the markdown: ";
	public static final String ERROR_LOADING_WIKI_FAILED = "Failed to load the wiki page: ";
	public static final String ERRROR_ATTACHMENT_FAILED = "Failed to upload attachment: ";
	public static final String ERROR_PROFILE_PICTURE_FAILED = "Failed to upload photo: ";
	public static final String WARNING_PROJECT_NAME_EXISTS = "Sorry, a project with that name already exists. Please try another name.";
	public static final String ERROR_FAILURE_PRIVLEDGES = "Sorry, you do not have sufficient privileges for access.";
	public static final String TITLE_UNAUTHORIZED = "Unauthorized";
	public static final String ATTACHMENT_DIALOG_BUTTON_TEXT = "Attach";
	public static final String ATTACHMENT_DIALOG_WINDOW_TITLE = "Add New File Attachment";
	public static final String ATTACH_PROFILE_PIC_DIALOG_TITLE = "Upload a New Photo";
	public static final String ATTACH_PROFILE_PIC_DIALOG_BUTTON_TEXT = "Upload Photo";
	public static final String ATTACH_IMAGE_DIALOG_BUTTON_TEXT = "Upload Image";
	public static final String LABEL_ADD_TO_SNAPSHOT = "ADD ENTITY<br/>TO SUMMARY";
	public static final String LABEL_CREATE_NEW_GROUP = "Create New Group";
	public static final String BUTTON_ADD_ENTITY_TO_GROUP = "Add To Summary";
	public static final String LABEL_NAME = "Name";
	public static final String NOTE = "Note";
	public static final String LABEL_ADD_GROUP = "Add Group";	
	public static final String GROUP_ADDED = "Group Added";
	public static final String GROUP_REMOVED = "Group Removed";
	public static final String ENTRY_ADDED = "Entry Added";
	public static final String ENTRY_REMOVED = "Entry Removed";
	public static final String SYNAPSE_ENTITY = "Synapse Entity";
	public static final String ERROR_FAILED_PERSIST = "Sorry, the change could not be saved. Please reload the page and try again.";
	public static final String CONTENTS = "Contents";
	public static final String ERROR_NAME_MUST_BE_DEFINED = "You must provide a name";
	public static final String ERROR_NO_EDIT_PERMISSION = "You do not have permission to edit.";
	public static final String UPDATE_SAVED = "Update Saved";
	public static final String ERROR_INVALID_VERSION_FORMAT = "Invalid version format.";
	public static final String DESCRIPTION = "Description";
	public static final String OK = "OK";
	public static final String ERROR_IN_READ_ONLY_MODE = "Sorry, that operation is not allowed in read only mode.";
	public static final String WHY_VERSION_READ_ONLY_MODE = "You are not allowed to modify an Entity when viewing a particular version.";
	public static final String READ_ONLY = "READ ONLY";	
	public static final String SHOW_LIST_EDITOR = "Show List Editor";
	public static final String HIDE_LIST_EDITOR = "Hide List Editor";
	public static final String FLAG = "flag";
	public static final String FLAG_TOOL_TIP = "Click here to learn more about flagging these data for review by the Synapse Access and Compliance Team";
	public static final String DATA_ACCESS_RESTRICTIONS_TEXT = "Data use conditions:";
	public static final String DATA_ACCESS_RESTRICTIONS_TOOLTIP = "Data use conditions are set by human privacy regulations or contractual obligations";
	
	public static final String ERROR_NO_FASTPASS = "Could not retrieve the fastpass url.";
	public static final String ERROR_EXTERNAL_CONTENT_NOT_IN_CACHE = "The requested content is unavailable: ";
	public static final String ERROR_VERSIONS_NOT_FOUND = "No version information was found for this entity.\nTry refreshing the page.";
	public static final String SHOW_VERSIONS = "show";
	public static final String HIDE_VERSIONS = "hide";
	public static final String NOT_FOUND = "Not Found";
	public static final String ERROR_LOADING = "Error Loading";
	public static final String PREVIEW_FAILED_TEXT = "Preview failed: ";
	public static final String ENTITY_DESCRIPTION_PREVIEW_BUTTON_TEXT = "Preview";
	public static final String ENTITY_DESCRIPTION_INSERT_IMAGE_BUTTON_TEXT = "Insert Attachment";
	public static final String ENTITY_DESCRIPTION_TOOLTIP = "Enter a short description that will be displayed prominently on the page and used in search results.";
	public static final String ENTITY_DESCRIPTION_TIPS_TEXT = "Formatting Guide";
	public static final String ENTITY_DESCRIPTION_HIDE_WIDGETS_TEXT = "Hide Widget Manager";
	public static final String ENTITY_DESCRIPTION_SHOW_WIDGETS_TEXT = "Show Widget Manager";
	public static final String ENTITY_DESCRIPTION_ATTACHMENT_PREFIX = "Attachment/entity/";
	public static final String ENTITY_STORAGE_NOT_FOUND_ERROR = "Unable to determine the usage associated with entity id: ";
	public static final String CODE_EXECUTION = "Code Execution";
	public static final String MANUAL = "Manual";
	public static final String UNDEFINED = "Undefined";
	public static final String ERROR_LAYOUT = "An error occured laying out the Provenance view.";
	public static final String ERROR_PROVENANCE = "An error occured creating the Provenance view.";
	public static final String ERROR_PROVENANCE_RELOAD = "Loading Error. Reload.";
	public static final String ENTITY = "Entity";
	public static final String LOADING = "Loading";
	public static final String ACTIVITY = "Activity";
	public static final String ERROR_NAME_PATTERN_MISMATCH = "Names may only contain letters, numbers, spaces, underscores, hypens, periods, plus signs, and parentheses.";
	public static final String ERROR_WIDGET_NAME_PATTERN_MISMATCH = "Names may only contain letters, numbers, spaces, hypens, periods, plus signs, and parentheses.";

	/**
	 * Widget editors
	 */
	public static final String IMAGE_CONFIG_UPLOAD_FIRST_MESSAGE = "A file must be uploaded to continue.";
	public static final String IMAGE_CONFIG_FILE_TYPE_MESSAGE = "The uploaded image file type is not recognized on most browsers. Please convert to PNG or JPEG and try again.";
	public static final String IMAGE_CONFIG_INVALID_URL_MESSAGE = "Please enter a valid URL";
	public static final String IMAGE_CONFIG_INVALID_ALT_TEXT_MESSAGE = "Please enter valid alternate text for the URL";
	public static final String IMAGE_CONFIG_UPLOAD = "Upload";
	public static final String IMAGE_CONFIG_FROM_THE_WEB = "From the Web";
	public static final String IMAGE_CONFIG_FROM_SYNAPSE = "From Synapse File";
	public static final String IMAGE_CONFIG_URL_LABEL = "Image URL:";
	public static final String IMAGE_CONFIG_ALT_TEXT = "Alternate Text:";
	public static final String IMAGE_FAILED_TO_LOAD = "Image failed to load: ";
	public static final String URL_LABEL = "URL:";
	public static final String TABLE_LABEL = "Tab Delimitted Table Contents:";
	public static final String UPLOAD_SUCCESSFUL_STATUS_TEXT = "Uploaded successfully";
	public static final String YOUTUBE_VIDEO_URL_LABEL = "Video URL:";
	public static final String SYNAPSE_API_CALL_URL_LABEL = "Synapse API URI:";
	public static final String SYNAPSE_API_CALL_COLUMNS_LABEL = "Columns To Display:";
	public static final String SYNAPSE_API_CALL_COLUMN_HEADERS_LABEL = "Column Headers:";
	public static final String SYNAPSE_API_CALL_RENDERERS_LABEL = "Renderer Names:";
	public static final String SYNAPSE_API_CALL_SHOW_ROW_NUMBERS_COL = "Show Row Numbers?";
	public static final String SYNAPSE_API_CALL_ROW_NUMBERS_COL_NAME = "Row Numbers Col Name:";
	public static final String SYNAPSE_API_CALL_IS_PAGING = "Support Paging?";
	public static final String SYNAPSE_API_CALL_PAGE_SIZE = "Page Size:";
	public static final String SYNAPSE_API_CALL_WIDTH = "Width (% or pixels)";
	public static final String SYNAPSE_API_CALL_JSON_REUSLTS_KEY_NAME = "JSON \"results\" Key Name:";
	public static final String SYNAPSE_API_CALL_CSS_STYLE_NAME = "CSS Table Style Name:";
	public static final String SHINYSITE_SITE_LABEL = "Shiny Site URL";
	public static final String WIDTH = "Width";
	public static final String HEIGHT = "Height";
	public static final String SYNAPSE_API_CALL_COL_CONFIGS_COL_NAME = "Column Configurations:";
	
	public static final String INVALID_URL_MESSAGE = "Please enter a valid URL";
	public static final String SHOW_EXPAND = "Show Expand";
	public static final String INVALID_NUMBER_MESSAGE = "Please enter a valid number";
	public static final String DEPTH_LABEL = "Depth";
	public static final String INVALID_SYNAPSE_ID_MESSAGE = "Please enter a valid Synapse ID";
	public static final String SYNAPSE_ID_LABEL = "Synapse ID";
	public static final String SAVE_BUTTON_LABEL = "Save";
	public static final String INSERT_BUTTON_LABEL = "Insert";

	/*
	 * Element ids for Selenium
	 */
	public static final String ID_BTN_LOGIN = "id_btn_login";
	public static final String ID_BTN_REGISTER = "id_btn_register";
	public static final String ID_BTN_USER = "id_btn_user";
	public static final String ID_BTN_FORGOT_PWD = "id_btn_forgot_pwd";
	public static final String ID_BTN_REGISTER2 = "id_btn_register2";
	public static final String ID_BTN_LOGIN_AGAIN = "id_btn_login_again";
	public static final String ID_INP_EMAIL_NAME = "id_inp_email_name";
	public static final String ID_INP_EMAIL_PASSWORD = "id_inp_email_password";
	public static final String ID_BTN_LOGIN2 = "id_btn_login2";
	public static final String ID_MNU_USER_PROFILE = "id_mnu_user_profile";
	public static final String ID_MNU_USER_LOGOUT = "id_mnu_user_logout";
	public static final String ID_MNU_USER_SETTINGS = "id_mnu_user_settings";
	public static final String ID_BTN_LOGIN_GOOGLE = "login-via-gapp-google";
	public static final String ID_INP_EMAIL_ADDRESS = "id_inp_email_address";
	public static final String ID_INP_EMAIL_ADDRESS2 = "id_inp_email_address2";
	public static final String ID_INP_FIRSTNAME = "id_inp_firstname";
	public static final String ID_INP_LASTNAME = "id_inp_lastname";
	public static final String ID_INP_NEWPASSWORD = "id_inp_new_password";
	public static final String ID_INP_CONFIRMPASSWORD = "id_inp_confirm_password";
	public static final String ID_BTN_SUBMIT = "id_btn_submit";
	public static final String ID_BTN_SUBMIT_TO_EVALUATION = "id_btn_submit_to_evaluation";
	public static final String ID_BTN_START_PROJECT = "id_btn_start_project";
	public static final String ID_BTN_DOWNLOAD = "id_btn_download";
	public static final String ID_BTN_EDIT = "id_btn_edit";
	public static final String ID_BTN_SHARE = "id_btn_share";
	public static final String ID_BTN_ADD = "id_btn_add";
	public static final String ID_BTN_TOOLSMNU = "id_btn_toolsmnu";

	// constants used in GovernanceDialogHelper	
	public static final String RESTRICTED_USE = "Restricted Use";
	public static final String CONTROLLED_USE = "Controlled Use";
	public static final String MODIFY = "modify";
	public static final String INFO = "info";
	public static final String GAIN_ACCESS = "gain access";
	public static final String FLAG_DIALOG_PREFIX = 
		"If you think this data is posted inappropriately or should have different access conditions, please ";
	public static final String FLAG_DIALOG_ANONYMOUS_SUFFIX = "<span class=\"strong\">log in</span> to alert the Synapse Access and Compliance Team (ACT).";
	public static final String FLAG_DIALOG_LOGGED_IN_SUFFIX = "alert the Synapse Access and Compliance Team (ACT) by clicking below.";
	public static final String FLAG_DIALOG_LOGGED_IN_SUFFIX_WITH_HYPERLINK_1 = "alert the Synapse Access and Compliance Team (ACT) by clicking <a href=\"";
	public static final String FLAG_DIALOG_LOGGED_IN_SUFFIX_WITH_HYPERLINK_2 = "\" target=\"_blank\" class=\"link\">here</a>.";
	public static final String BUTTON_TEXT_FLAG_DATA = "Flag Data";
	public static final String BUTTON_TEXT_RESTRICT_DATA = "Add Restrictions";
	public static final String BUTTON_TEXT_REQUEST_ACCESS_FROM_ACT = "Request Access";
	public static final String BUTTON_TEXT_ACCEPT_TERMS_OF_USE = "Accept Terms of Use";
	public static final String BUTTON_TEXT_LOGIN = "Login";
	public static final String FLAG_DIALOG_TITLE = "Flag Data";
	public static String UNRESTRICTED_DATA_DIALOG_TITLE = "Unrestricted Data";
	public static String RESTRICTED_DATA_DIALOG_TITLE = "Restricted Data";
	public static String RESTRICTED_DATA_DIALOG_PROMPT = "Access to these data is restricted.";
	public static String RESTRICTION_FULFILLED_DATA_DIALOG_TITLE = "Access Requirements Fulfilled";
	
	public static String SYNAPSE_GUIDELINES_LINK = "<a href=\"#!WikiPlace:dataAccessLevelsContent\" class=\"link\" target=\"_blank\">Data Use Restrictions for Human Data in Synapse</a>";
	public static String UNRESTRICTED_DESCRIPTION = "This data is currently accessible to all registered Synapse users.  ";
	public static String GOVERNANCE_REFERENCE = "For more information on Data Use conditions, please read the "+SYNAPSE_GUIDELINES_LINK+".";
	
	public static String ADMIN_GOVERNANCE_REFERENCE = "You are responsible to ensure your data is shared in compliance with the "+
	SYNAPSE_GUIDELINES_LINK+" and all applicable laws. ";
	public static String RESTRICTION_DIALOG_TEXT_3 = "To add data restrictions, contact the Access and Compliance Team (ACT), by clicking below.</p>";
	public static String RESTRICTION_DIALOG_TEXT_4 = "<p class=\"strong\">NOTE:  If you continue, data access to all registered Synapse users will be temporarily restricted pending review by the ACT.</p>";
	
	public static final String TOU_RESTRICTION_HEADER = "In order to access these data you must be a registered Synapse user and agree to the following terms:";
	public static final String TOU_PROMPT = "Access to the data requires that you are a registered Synapse user and agree to the following terms and conditions:";
	public static final String ACT_PROMPT = "Access to the data requires that you are a registered Synapse user and fulfill the following requirements:";
	public static final String RESTRICTION_FULFILLED_STATEMENT = "<p class=\"strong\">Access Requirements Fulfilled:  You have access to these data under the following terms:</p>";
	public static final String RESTRICTION_WARNING_TITLE = "Add Data Restriction";
	public static final String RESTRICTION_WARNING = "By clicking 'OK' below, I request that the Synapse ACT contact me to assign the appropriate access restrictions for this dataset.";
	public static final String RESTRICTED_DATA_LOGIN_WARNING = "Note:  You must log in to gain access to restricted data.</p>";
	public static final String DATA_USE_BANNER = "Data Use Restrictions for Public Data";
	public static final String DATA_USE_BANNER_SUB1 = "If you change Sharing to be ";
	public static final String DATA_USE_BANNER_SUB2 = ", who can use these data?";
	public static final String DATA_USE_NOTE = 
		"You are responsible to ensure your data is used in compliance with the "+SYNAPSE_GUIDELINES_LINK+
		" and all applicable laws. If/when you make this file public, can its data be used by any registered Synapse user "+
		"without restriction? If so, then select \"Any Use\", below.  Otherwise select \"Restricted / Controlled\".";
	public static final String VERSION_INFO_UPDATED = "Updated Version Info";
	
	
	public static final String DEFINE_ACTIVITY = "Please Define Provenance";
	public static final String HOW_TO_DEFINE_ACTIVITY = "Create Provenance using the Synapse R Client.";
	public static final String ERROR_NO_LINK_DEFINED = "Warning: No target reference is defined for this link.";
	public static final String DATA_USE = "Data Use";
	public static final String ACCESS_TO_DATA = "Access to these data is";
	public static final String PAGE_NOT_FOUND = "Page not found";
	public static final String PAGE_NOT_FOUND_DESC = "The page you requested was not found, that's all we know.";
	public static final String ANNOTATIONS = "Annotations";
	public static final String TO_LOAD_VERSION_1 = "To load this version";
	public static final String TO_LOAD_AN_ENTITY = "To load an entity";
	public static final String PROGRAMMATIC_ACCESS = "Programmatic Access";
	public static final String PROVENANCE = "Provenance";
	public static final String CREATED = "Created";
	public static final String MODIFIED = "Modified";
	public static final String NO_VERSION_COMMENT = "No Version Comment";
	public static final String ADD_FOLDER = "Add Folder";
	public static final String ADD_FILE = "Add File";
	public static final String FILES = "Files";
	public static final String ERROR_FOLDER_CREATION_FAILED = "Folder creation failed. Please try again";
	public static final String ERROR_FILE_CREATION_FAILED = "File creation failed. Please try again";
	public static final String PAGES = "Pages";
	public static final String ADD_PAGE = "Add a new Page";
	public static final String CREATE_WIKI = "Insert Wiki";
	public static final String DEFAULT_ROOT_WIKI_NAME = "Root";
	public static final String PROJECT_WIKI_NAME = "Wiki";
	public static final String ERROR_PAGE_CREATION_FAILED = "Page creation failed. Please try again";
	public static final String UNAUTHORIZED = "Unauthorized";
	public static final String UNAUTHORIZED_DESC = "You are not authorized to access the page requested.";
	public static final String LOGIN_TO_DOWNLOAD = "Please login to download data.";
	public static final String INVALID_WIDGET_MARKDOWN_MESSAGE = "Invalid widget markdown: ";
	public static final String SELECT = "Select";
	public static final String NONE = "None";
	public static final String LOOKUP = "Lookup";
	public static final String CURRENT_VERSION = "Current Version";
	public static final String ALWAYS_CURRENT_VERSION = "Always refer to the most current version";
	public static final String BROWSE_MY_ENTITIES = "Browse My Entities";
	public static final String ENTER_SYNAPSE_ID = "Enter Synapse Id";
	public static final String REFER_TO_SPECIFIC_VERSION = "Refer to a Specific Version";
	public static final String CURRENT = "Current";
	public static final String CURRENTLY_SELCTED = "Currently Selected";
	public static final String PLEASE_MAKE_SELECTION = "Please make a selection";
	public static final String CLOSE = "Close";
	public static final String DISPLAY_HEIGHT = "Display Height";
	public static final String DEFAULT = "Default";
	public static final String ENTITY_LIST = "Entity List";
	public static final String IMAGE_FILE_ENTITY = "Image File Entity";
	public static final String FIND_IMAGE_ENTITY = "Find Image File Entity";
	public static final String ERROR_ENTER_AT_LEAST_ONE_ENTITY = "Please enter at least one entity";
	public static final String FIND_ENTITIES = "Find Entities";
	
	public static final String API_TABLE_COLUMN_COUNT_MISMATCH = "SuperTable: The number of columns must be the same as the number of renderers and column header titles.";
	public static final String API_TABLE_MISSING_URI = "SuperTable: Endpoint path not specified.";
	
	public static final String PAGE_BACK = "Go to the previous page";
	public static final String PAGE_NEXT = "Go to the next page";
	public static final String SWITCH_TO_STANDARD_WEBSITE = "Switch back to the release version";
	public static final String SWITCH_TO_TEST_WEBSITE = "Try an alpha test version of the website";
	public static final String TEST_MODE_WARNING = "This mode is for alpha testing features only. Please note that the developer does not guarantee an absence of errors, and that the data created using alpha features may be lost during product upgrade. Are you sure you want to switch into this mode?";
	
	public static final String UNSUPPORTED_FOR_OWNER_TYPE = "This operation is not currently supported for the given owner type: ";
	public static final String MY_PROJECTS = "My Projects";
	public static final String MY_EVALUATIONS = "My Challenges";
	public static final String SUBMITTER_ALIAS = "your alias (or team name)";
	public static final String FAVORITES = "Favorites";
	public static final String ERROR_LOADING_FAVORITES = "Error loading Favorites.";
	public static final String ERROR_SAVE_FAVORITE_MESSAGE = "Saving your Favorite change failed. Please try again.";
	public static final String EXTERNAL_URL = "External URL";
	public static final String ADD_ENTITY = "Add Entity";
	public static final String INVALID_SHINY_SITE = " is not a valid Shiny Site URL. Please " + DisplayUtils.HELP_EMAIL_ADDRESS_LINK + " if you would like your Shiny Server added to our white list.";
	public static final String MARKDOWN_WIDGET_WARNING = "Markdown Widget Warning";
	public static final String ACCESS_WILL_BE = "Access Will Be";
	public static final String ANY_USE = "Any Use";
	public static final String SELECT_DATA_USE = "Please select a Data Use Level";
	public static final String DOI_REQUEST_SENT_TITLE = "The request to create a new DOI has been sent.";	
	public static final String DOI_REQUEST_SENT_MESSAGE = "Note that it may take a few minutes for the service to create the new DOI.";
	public static final String DOI_CREATION_ERROR = "There was a problem creating a DOI";
	public static final String DOI_CREATION_IN_PROGRESS = "Creating a DOI";
	public static final String LARGE_FILE_ON_UNSUPPORTED_BROWSER = "The file exceeds the maximum file size that Synapse supports for this browser. Please update your browser to the latest version and try again.";
	public static final String PASSWORD = "Password";
	public static final String SIGN_IN = "Sign in";
	public static final String REGISTER_BUTTON = "Register for a Synapse Account";
	public static final String CREATE_PROJECT = "Create Project";
	public static final String NEW_PROJECT_NAME = "New Project Name";
	public static final String WHAT_IS_A_PROJECT = "What's a Project?";
	public static final String USER_GUIDE = "User Guide";
	public static final String ADD_ACL_UNSAVED_CHANGES = "You have modified the Add People fields but not clicked the 'Add' button to grant the permission. Would you like to add these changes?";
	
	public static final String EVALUATION_REGISTRATION_ERROR = "Unable to register for the evaluation: ";
	public static final String EVALUATION_USER_STATE_ERROR = "Unable to determine if you are registered for the evaluation: ";
	public static final String EVALUATION_USER_ACCESS_ERROR = "Unable to determine if you have administration access to the evaluation: ";
	public static final String NOT_PARTICIPATING_IN_ANY_EVALUATIONS = "You are not currently participating in an Evaluation/Challenge. Please join one and try again.";
	public static final String JOINED_EVALUATION_HTML = "<span class=\"mainPageHeader inline-block\"><h3>You are registered for this challenge!</h3><ul class=\"list arrow-list\"><li>Download the file(s) by going to the bottom of this page...</li></ul></span>";
	public static final String READ_ONLY_MODE = "Read Only Mode";
	public static final String SYNAPSE_IN_READ_ONLY_MODE = "Synapse is in READ_ONLY mode for maintenance. You can continue to browse, but can not modify during this period.";
	public static final String VIEW_EDIT_PROFILE = "View / Edit Profile";
}

