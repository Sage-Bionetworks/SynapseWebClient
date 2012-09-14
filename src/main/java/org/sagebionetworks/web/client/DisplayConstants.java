package org.sagebionetworks.web.client;

import com.google.gwt.i18n.client.DateTimeFormat;
import com.google.gwt.safehtml.shared.SafeHtml;

public class DisplayConstants {
	
	/*
	 * DEMO FLAG
	 * Set this flag if you want demo content shown
	 */
	public static boolean showDemoHtml = false;
	public static final String MSKCC_DATASET_DEMO_ID = "4494";
	public static final String TURN_DEMO_ON_TOKEN = "d";
	public static final String TURN_DEMO_OFF_TOKEN = "nd";
	/*
	 * Demo strings
	 */
	public static final String DEMO_COMMENTS = "<div id=\"scrollable_item\">							<span class=\"scrollable_header\">								<img src=\"static/images/down_arrow.png\" class=\"right\" />								<span class=\"scroll_text\">New analysis using this dataset in the Federation Warburg Project</span>								<span class=\"author\"><a href=\"\">Xudong D.</a></span>								<span class=\"date\">21-Nov-2010</span>							</span>							<span class=\"scrollable_comments\">								<span class=\"scrollable_comment\">									<span class=\"comment_text\">The paper references 149 matched normal samples.  Do you have this data?</span>									<span class=\"comment_author\">Sam S.</span>									<span class=\"comment_date\">22-Dec-2010</span>								</span>								<span class=\"scrollable_comment\">									<span class=\"comment_text\">We have only able to obtain 29 matched normal samples.</span>									<span class=\"comment_author\">Matt F.</span>									<span class=\"comment_date\">22-Nov-2010</span>								</span>							</span>						</div>						<div id=\"scrollable_item\">							<span class=\"scrollable_header\">								<img src=\"static/images/down_arrow.png\" class=\"right\" />								<span class=\"scroll_text\">New analysis using this dataset in the MetaGEO Project</span>								<span class=\"author\"><a href=\"\">Brig M</a></span>								<span class=\"date\">01-Apr-2011</span>							</span>							<span class=\"scrollable_comments\">								<span class=\"scrollable_comment\">									<span class=\"comment_text\">Look out, there is a large batch effect in this data.  Have been working on a way to remove it.</span>									<span class=\"comment_author\">Brig M.</span>									<span class=\"comment_date\">02-Apr-2011</span>								</span>								<span class=\"scrollable_comment\">									<span class=\"comment_text\">Appreciate the help.  We can post new version of the normalized data.</span>									<span class=\"comment_author\">Matt F.</span>									<span class=\"comment_date\">02-Apr-2011</span>								</span>							</span>						</div>";
	public static final String DEMO_ANALYSIS = "<div class=\"scrollable\"><table width=\"100%\" class=\"detail\"><tr><th class=\"analysis\">Analysis</th><th class=\"project\">Project</th><th class=\"last_modified\">Last Modified</th></tr>							<tr>								<td class=\"analysis\"><a href=\"network_overview.html\">Network Generation</a></td>								<td class=\"project\"><a href=\"project_details.html\">Federation Warburg</a></td>								<td class=\"last_modified\">12-Apr-2011</td>							</tr>							<tr class=\"gray\">								<td class=\"analysis\"><a href=\"network_overview.html\">Network Survey</a></td>								<td class=\"project\"><a href=\"project_details.html\">Federation Warburg</a></td>								<td class=\"last_modified\">23-Mar-2011</td>							</tr>							<tr>								<td class=\"analysis\"><a href=\"network_overview.html\">Interactome</a></td>								<td class=\"project\"><a href=\"project_details.html\">Federation Warburg</a></td>								<td class=\"last_modified\">04-Mar-2011</td>							</tr>							<tr class=\"gray\">								<td class=\"analysis\"><a href=\"network_overview_metageo.html\">GSE 21034</a></td>								<td class=\"project\"><a href=\"project_details_metageo.html\">MetaGEO</a></td>								<td class=\"last_modified\">2-Apr-2011</td>							</tr> <tr> <td class=\"analysis\"><a href=\"analysis_predictor.html\">Biomarker Prediction</a></td> <td class=\"project\"><a href=\"project_details_predictor.html\">Predictive Modeling</a></td> <td class=\"last_modified\">07-Jul-2011</td> </tr>						</table></div>";
	public static final String DEMO_OVERVIEW = "Genetic and epigenetic alterations have been identified that lead to transcriptional Annotation of prostate cancer genomes provides a foundation for discoveries that can impact disease understanding and treatment. Concordant assessment of DNA copy number, mRNA expression, and focused exon resequencing in the 218 prostate cancer tumors represented in this dataset haveidentified the nuclear receptor coactivator NCOA2 as an oncogene in approximately 11% of tumors. Additionally, the androgen-driven TMPRSS2-ERG fusion was associated with a previously unrecognized, prostate-specific deletion at chromosome 3p14 that implicates FOXP1, RYBP, and SHQ1 as potential cooperative tumor suppressors. DNA copy-number data from primary tumors revealed that copy-number alterations robustly define clusters of low- and high-risk disease beyond that achieved by Gleason score.";



	
	
	
	/*
	 * Dimensions
	 */
	public static final int DEFAULT_GRID_COLUMN_WIDTH_PX = 150;
	public static final int DEFULAT_GRID_LAYER_COLUMN_WIDTH_PX = 100;	
	public static final int DEFULAT_GRID_DATE_COLUMN_WIDTH_PX = 85;
	
	public static final int MAX_COLUMNS_IN_GRID = 100;
	public static final int DESCRIPTION_SUMMARY_LENGTH = 450; // characters for summary
	
	public static final DateTimeFormat DATE_FORMAT = DateTimeFormat.getFormat("dd-MMM-yyyy");
	public static final DateTimeFormat DATE_TIME_FORMAT = DateTimeFormat.getFormat(DateTimeFormat.PredefinedFormat.DATE_TIME_MEDIUM);
	public static final DateTimeFormat DATE_FORMAT_SERVICES = DateTimeFormat.getFormat("yyyy-MM-dd");	
	
	
	/*
	 * Text constatns
	 */
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
	public static final String PERMISSIONS_INHERITED_TEXT = "Pemissions not editable as they are being inherited by this resource's parent. (ex: A layer inheriting a dataset's permissions)";
	public static final String PERMISSIONS_CREATE_NEW_ACL_TEXT = "By creating local sharing settings you ignore the permissions that are inherited from its parent.";
	public static final String PERMISSIONS_DELETE_ACL_TEXT = "By deleting local sharing settings you inherit sharing settings from the parent.";
	
	public static final String PASSWORD_RESET_FAILED_TEXT = "Password reset failed. Please try again.";
	public static final String PASSWORD_SET_FAILED_TEXT = "Password change failed. Please try again.";
	public static final String PASSWORD_RESET_TEXT = "Your password has been reset.";
	public static final String PASSWORD_SET_TEXT = "Your password has been set. Please login to verify.";
	
	public static final String NEWS_UNAVAILABLE_TEXT = "The Synapse news service is currently unavailable.";
	
	/*
	 * Buttons, titles and labels
	 */
	public static final String TITLE_TERMS_OF_USE = "Terms of Use";
	public static final String TITLE_LAYER_PREVIEW = "Layer Preview";
	public static final String BUTTON_SEE_TERMS_OF_USE = "See Terms of Use";
	public static final String BUTTON_FOLLOW_DATASET = "Follow this Dataset";
	public static final String TITLE_SHARING_PANEL = "Sharing Settings";
	public static final String LABEL_SHARING_PANEL_EXISTING = "Permissions";
	public static final String LABEL_SHARING_PANEL_INHERITED = "Inherited Permissions";
	public static final String BUTTON_PERMISSIONS_CREATE_NEW_ACL = "Create Local Sharing Settings";
	public static final String BUTTON_PERMISSIONS_DELETE_ACL = "Delete Local Sharing Settings";
	public static final String MENU_PERMISSION_LEVEL_IS_OWNER = "Is Owner";
	public static final String MENU_PERMISSION_LEVEL_CAN_VIEW = "Can view";
	public static final String MENU_PERMISSION_LEVEL_CAN_EDIT = "Can edit";
	public static final String MENU_PERMISSION_LEVEL_CAN_ADMINISTER = "Administrator";
	public static final String LABEL_PERMISSION_TEXT_ADD_PEOPLE = "Add People";
	public static final String BUTTON_SHOW_R_CLIENT_INSTALL = "Show R Client Installation Code";
	public static final String LABEL_R_CLIENT_INSTALL = "Install Synapse R Client";
	public static final String LABEL_R_CLIENT_GET_ENTITY = "Obtain a pointer";
	public static final String LABEL_R_CLIENT_LOAD_ENTITY = "Load the data";
	public static final String LOGIN_USERNAME_LABEL = "Email Address";
	
	/*
	 * Service Constants (move to another file?)
	 */
	public static final String SINGLE_SIGN_ON_USERID = "SSO";
	public static final String OPEN_ID_SAGE_LOGIN_BUTTON_TEXT = "Login with a Google Account";
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
	public static final String ERROR_GETTING_PERMISSIONS_TEXT = "READ ONLY MODE. Reason: An error occured in retrieving your level of access.";
	public static final String ERROR_FAILED_PERSIST_AGREEMENT_TEXT = "Your license acceptance was not saved. You will need to sign it again in the future.";	
	public static final String ERROR_USER_ALREADY_EXISTS = "There was a problem creating your account: The email address provided is already in use. If you have forgotten your password, please use the \"Forgot Password\" button from the login page.";
	public static final String ERROR_UNABLE_TO_LOAD = "An error occured trying to load.";
	public static final String ERROR_GENERIC = "An error occured. Please try again.";
	public static final String ERROR_INCOMPATIBLE_CLIENT_VERSION = "Your client version is incompatible with the repository. Please try reloading the page.";
	public static final String ERROR_GENERIC_RELOAD = "An error occured. Please try reloading the page.";
	public static final String ERROR_SAVE_MESSAGE = "An error occuring attempting to save. Please try again.";
	public static final String ERROR_BAD_REQUEST_MESSAGE = "An unknown communication error occured. Please reload the page.";
	public static final String ERROR_DUPLICATE_ENTITY_MESSAGE = "An entity with this name already exists. Please enter a different name.";
	public static final String ERROR_INVALID_ENTITY_NAME = "Name contains contains an invalid character";
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
	public static final String BUTTON_SHARE = "Share";
	public static final String BUTTON_DOWNLOAD = "Download";
	public static final String LABEL_DELETE = "Delete";
	public static final String LABEL_CREATE = "Create";
	public static final String LABEL_MOVE = "Move";
	public static final String ERROR_ENTITY_DELETE_FAILURE = "Deletion failed. Please try again.";
	public static final String ERROR_ENTITY_MOVE_FAILURE = "Move failed. Please try again.";
	public static final String PROMPT_SURE_DELETE = "Are you sure you want to delete this";
	public static final String LABEL_NO_PREVIEW_DATA = "A Preview has not been created.";
	public static final String LABEL_CONTAINS_NO_CHILDREN = "This object contains no children.";
	public static final String BUTTON_REGISTER = "Register";
	public static final String BUTTON_LOGIN = "Login";
	public static final String LABEL_WELCOME = "Welcome";
	public static final String BUTTON_LOGOUT = "Logout";
	public static final String BUTTON_MY_PROFILE = "My Profile";
	public static final String LABEL_NO_SEARCH_RESULTS_PART1 = "Your search for '";
	public static final String LABEL_NO_SEARCH_RESULTS_PART2 = "' did not match any results.";
	public static final String LABEL_SEARCH = "Search";
	public static final String LABEL_PROJECT_NAME = "Project Name";
	public static final String LABEL_PROJECT_CREATED = "Project Created";
	public static final String TEXT_UPLOAD_FILE = "Upload File";
	public static final String TEXT_USER_SETTINGS = "Settings";
	public static final String TEXT_USER_VIEW_PROFILE = "Profile";
	public static final String LABEL_UPLOAD_TO_SYNAPSE = "To Synapse";
	public static final String LABEL_TO_EXTERNAL = "Link External Location";
	public static final String TEXT_UPLOAD_SUCCESS = "File Uploaded to Synapse";
	public static final String TEXT_ATTACHMENT_SUCCESS = "Attachment Uploaded to Synapse";
	public static final String TEXT_PROFILE_PICTURE_SUCCESS = "Profile Photo Uploaded to Synapse";
	public static final String ERROR_UPLOAD = "An error occured during upload. Please try again.";
	public static final String LABEL_UPLOADING = "Uploading...";
	public static final String LABEL_CREATE_LINK = "Save Link To This Page";
	public static final String LABEL_WHERE_SAVE_LINK = "Where would you like to save a Link to this page?";
	public static final String ERROR_NOT_FOUND = "Sorry, the requested object was not found or no longer exists.";
	public static final String ERROR_NOT_AUTHORIZED = "Sorry, you are not authorized to modify the requested entity.";
	public static final String ERROR_ENTITY_CANT_LINKS = "Sorry, the requested entity can not contain links.";
	public static final String ERROR_ENTITY_CREATION = "Sorry, creation failed. Please try again.";
	public static final String LABEL_SUCCESS = "Success";
	public static final String TEXT_LINK_SAVED = "Link saved.";
	public static final String ERROR_UPDATE_FAILED = "Update failed. Please try again.";
	public static final String TEXT_NO_REFERENCES = "No others point to this";
	public static final String BUTTON_FILTER_ONLY_MY_CREATION = "Show Projects I Created";
	public static final String BUTTON_FILTER_USER_UPDATABLE = "Show Projects I Can Edit";
	public static final String STATUS_CAN_EDIT = "Can Edit";
	public static final String STATUS_CREATED_BY = "Owner";
	public static final String LABEL_NONE_FOUND = "None Found";
	public static final String LABEL_NO_DESCRIPTION = "Please add a description by clicking the \"Edit\" button.";
	public static final String LABEL_NO_ACTIVITY = "Activity Feed is empty.";
	public static final String ERROR_GENERIC_NOTIFY = "An error occurred. Please report the problem to synapseInfo@sagebase.org";
	public static final String ERROR_CANT_SAVE_LINK_HERE = "Sorry, you cannot save a Link to the requested spot.";
	public static final String ERROR_CANT_MOVE_HERE = "Sorry, you cannot move this item to the requested spot.";
	public static final String TEXT_RIGHT_CLICK_FOR_CONTEXT_MENU = "Right click on an item in the list to Edit or Delete.";
	public static final String ERROR_USER_PROFILE_SAVE = "An error occurred saving to your user profile.";
	public static final String LABEL_UPDATED = "Upated";
	public static final String TEXT_USER_PROFILE_UPDATED = "Change save to your User Profile.";
	public static final String TEXT_NO_ATTACHMENTS = "No Attachments.";
	public static final String ERROR_DELETING_ATTACHMENT = "An error occurred deleting the Attachment. Please try again.";
	public static final String LABEL_DELETED = "deleted";
	public static final String ERRROR_ATTACHMENT_FAILED = "Failed to upload attachment: ";
	public static final String ERROR_PROFILE_PICTURE_FAILED = "Failed to upload photo: ";
	public static final String WARNING_PROJECT_NAME_EXISTS = "Sorry, a project with that name already exists. Please try another name.";
	public static final String ERROR_FAILURE_PRIVLEDGES = "Sorry, you do not have sufficient privileges for access.";
	public static final String TITLE_UNAUTHORIZED = "Unauthorized";
	public static final String ATTACHMENT_DIALOG_BUTTON_TEXT = "Attach";
	public static final String ATTACHMENT_DIALOG_WINDOW_TITLE = "Add New File Attachment";
	public static final String ATTACH_PROFILE_PIC_DIALOG_TITLE = "Upload a New Photo";
	public static final String ATTACH_PROFILE_PIC_DIALOG_BUTTON_TEXT = "Upload Photo";
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
	public static final String FLAG_TOOL_TIP = "Flag this data object for review by ACT";

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
	public static final String ID_BTN_START_PROJECT = "id_btn_start_project";
	public static final String ID_BTN_DOWNLOAD = "id_btn_download";
	public static final String ID_BTN_EDIT = "id_btn_edit";
	public static final String ID_BTN_SHARE = "id_btn_share";
	public static final String ID_BTN_ADD = "id_btn_add";
	public static final String ID_BTN_TOOLSMNU = "id_btn_toolsmnu";

	// constants used in GovernanceDialogHelper
	public static final String FLAG_DIALOG_PREFIX = 
		"<p>If you think this data is posted inappropriately or should have different access conditions, please alert the Synapse Access and Compliance Team";
	public static final String FLAG_DIALOG_ANONYMOUS_SUFFIX = ".  You must log in order to contact the ACT.</p>";
	public static final String FLAG_DIALOG_LOGGED_IN_SUFFIX = " by clicking below.</p>";
	public static final String FLAG_DIALOG_LOGGED_IN_SUFFIX_WITH_HYPERLINK_1 = " by clicking <a href=\"";
	public static final String FLAG_DIALOG_LOGGED_IN_SUFFIX_WITH_HYPERLINK_2 = "\" target=\"_blank\" class=\"link\">here</a>.</p>";
	public static final String BUTTON_TEXT_CONTACT_ACT = "Contact ACT";
	public static final String BUTTON_TEXT_RESTRICT_DATA = "Add Restrictions";
	public static final String BUTTON_TEXT_REQUEST_ACCESS_FROM_ACT = "Request Access";
	public static final String BUTTON_TEXT_ACCEPT_TERMS_OF_USE = "Accept Terms of Use";
	public static final String FLAG_DIALOG_TITLE = "Flag Data";
	public static String UNRESTRICTED_DATA_DIALOG_TITLE = "Unrestricted Data";
	public static String RESTRICTED_DATA_DIALOG_TITLE = "Restricted Data";
	public static String RESTRICTED_DATA_DIALOG_PROMPT = "Access to this data is restricted.";
	public static String RESTRICTION_FULFILLED_DATA_DIALOG_TITLE = "Access Requirements Fulfilled";
	
	public static String RESTRICTION_DIALOG_TEXT_1 = "<p>This data is currently accessible to all registered Synapse users.  ";
	public static String RESTRICTION_DIALOG_TEXT_2 = "You are responsible to ensure your data is shared in compliance with the "+
	"<a href=\"#Governance:0\"  target=\"_blank\" class=\"link\">Synapse guidelines</a>"+
	" and all applicable laws. ";
	public static String RESTRICTION_DIALOG_TEXT_3 = "To add data add restrictions, contact the Access and Compliance Team, by clicking below.</p>";
	public static String RESTRICTION_DIALOG_TEXT_4 = "<p class=\"strong\">NOTE:  If you continue, data access to all registered Synapse users will be temporarily restricted pending review by the ACT.</p>";
	
	public static final String TOU_RESTRICTION_HEADER = "In order to access this data you must be a registered Synapse user and agree to the following terms:";
	public static final String TOU_PROMPT = "Access to the data requires that you agree to the following terms and conditions:";
	public static final String ACT_PROMPT = "Access to the data requires that you fulfill the following requirements:";
	public static final String RESTRICTION_FULFILLED_STATEMENT = "<p class=\"strong\">You have access to this data under the following terms:</p>";
	public static final String RESTRICTED_DATA_LOGIN_WARNING = "Note:  You must log in to gain access to restricted data.";
	public static final String FILE_DOWNLOAD_NOTE = 
			"You are responsible to ensure your data is shared in compliance with the "+
			"<a href=\"#Governance:0\" target=\"_blank\" class=\"link\">Synapse guidelines/a>"+
			" and all applicable laws. If this data requires restrictions for user access, select \"Upload Restricted\", below. "+
			"If the data may be accessed by registered Synapse users without restrictions, select \"Upload Unrestricted\", below.";	

}

