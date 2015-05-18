package org.sagebionetworks.web.client;

import java.util.Arrays;
import java.util.HashSet;

import org.sagebionetworks.web.client.resources.WebResource;
import org.sagebionetworks.web.client.resources.WebResource.ResourceType;
import org.sagebionetworks.web.shared.WebConstants;

public class ClientProperties {

	public static final String VIDEO_HTML5_BROWSER_LINK = "http://en.wikipedia.org/wiki/HTML5_video#Browser_support";
	
	public static final String HELP_EMAIL_ADDRESS = "synapseInfo@sagebase.org";
	public static final String HELP_EMAIL_ADDRESS_LINK = "<a href=\""+ HELP_EMAIL_ADDRESS +"\" class=\"link\">contact us</a>";
	public static final String NEWS_FEED_URL = "https://sagesynapse.wordpress.com/feed/";
	public static final String SUPPORT_RECENT_ACTIVITY_URL = "http://support.sagebase.org/sagebase?view=recent";
	public static final String WIKI_URL = "https://sagebionetworks.jira.com/wiki";
	public static final String USER_GUIDE_ID = "syn1669771";
	public static final String ABOUT_SYNAPSE_URL = "https://s3.amazonaws.com/static.synapse.org/About_Synapse.pdf";
	public static final String TEAM2CHALLENGE_WHITELIST_URL = "https://s3.amazonaws.com/static.synapse.org/teamId2challengeEntityId.json";
	
	public static final String BCC_SUMMARY_CONTENT_PAGE_ID = "24084489";
	
	public static final String CHALLENGE_OVERVIEW_CONTENT_PROVIDER_ID = "challengeOverviewContent";
	public static final String BCC_SUMMARY_PROVIDER_ID = "bccSummaryContent";
	public static final String DATA_ACCESS_LEVELS_PROVIDER_ID = "dataAccessLevelsContent";
	public static final String NEWS_FEED_PROVIDER_ID = "newsFeed";
	
	
	public static final String FASTPASS_LOGIN_COOKIE_VALUE = "fastpass-logging-in";
	public static final String FASTPASS_SIGNOVER_URL = "http://support.sagebase.org/fastpass/finish_signover?company=sagebase&fastpass=";
	public static final String WIKI_CONTENT_URL = "https://sagebionetworks.jira.com/wiki/rest/prototype/1/content/";
	public static final String WIKI_PAGE_SOURCE_CONTENT_URL = "https://sagebionetworks.jira.com/wiki/plugins/viewsource/viewpagesrc.action";
	public static final String WIKI_SOURCE_DELIMITER = "<p>&nbsp;</p>";
	
	public static final String SUPPORT_URL = "support.sagebase.org";
	
	public static final String ALERT_CONTAINER_ID = "alertContainer";
	public static final String REGEX_CLEAN_ANNOTATION_KEY = "^[a-z,A-Z,0-9,_,.]+";
	public static final String REGEX_CLEAN_ENTITY_NAME = "^[a-z,A-Z,0-9,_,., ,\\-,\\+,(,)]+";
	public static final String REPO_ENTITY_NAME_KEY = "name";
	public static final String WHITE_SPACE = "&nbsp;";
	public static final String BREADCRUMB_SEP = "&nbsp;&raquo;&nbsp;";
	
	public static final String NODE_DESCRIPTION_KEY = "description";
	public static final String LAYER_COLUMN_DESCRIPTION_KEY_PREFIX = "colDesc_";
	public static final String LAYER_COLUMN_UNITS_KEY_PREFIX = "colUnits_";
	
	public static final String MIME_TYPE_JPEG = "image/jpeg";
	public static final String MIME_TYPE_PNG = "image/png";
	public static final String MIME_TYPE_GIF = "image/gif";
	
	public static final String DEFAULT_PLACE_TOKEN = "0";
	
	public static final String R_CLIENT_DOWNLOAD_CODE = "source('http://depot.sagebase.org/CRAN.R')<br/>pkgInstall(c(\"synapseClient\"))";
	public static final String PYTHON_CLIENT_DOWNLOAD_CODE = "# From Terminal Prompt:<br/>pip install synapseclient<br/><br/># or<br/>easy_install synapseclient";
	public static final String FILE_UPLOADER_SERVLET_PATH = "fileUploaderJnlp";
	public static final String PROFILE_ATTACHMENT_SERVLET_PATH = "profileAttachment";

	
	public static final String ERROR_OBJ_REASON_KEY = "reason";
	public static final String SYNAPSE_ID_PREFIX = "syn";
	public static final String DEFAULT_RSTUDIO_URL = "http://localhost:8787";
	public static final int BIG_BUTTON_HEIGHT_PX = 36;
	public static final int MARKDOWN_WIDTH_WIDE_PX = 940;
	public static final int MARKDOWN_WIDTH_NARROW_PX = 660;

	
	public static final Character[] ESCAPE_CHARACTERS = new Character[] { '.','{','}','(',')','+','-' };
	public static final HashSet<Character> ESCAPE_CHARACTERS_SET = new HashSet<Character>(Arrays.asList(ESCAPE_CHARACTERS));
	
	public static final String[] IMAGE_CONTENT_TYPES = new String[] {"image/bmp","image/pjpeg","image/jpeg","image/jpg", "image/jpe","image/gif","image/png"};
	public static final HashSet<String> IMAGE_CONTENT_TYPES_SET = new HashSet<String>(Arrays.asList(IMAGE_CONTENT_TYPES));
	
	public static final String[] TABLE_CONTENT_TYPES = new String[] {"text/csv","text/tab-separated-values","text/plain", "text/txt", "", "text/tsv"};
	public static final HashSet<String> TABLE_CONTENT_TYPES_SET = new HashSet<String>(Arrays.asList(TABLE_CONTENT_TYPES));
	
	public static final double BASE = 1024, KB = BASE, MB = KB*BASE, GB = MB*BASE, TB = GB*BASE;
	
	/**
	 * Sometimes we are forced to use a table to center an image in a fixed space. 
	 * This is the third option from: http://stackoverflow.com/questions/388180/how-to-make-an-image-center-vertically-horizontally-inside-a-bigger-div
	 * It should only be used when the first two options are not an option.
	 * Place your image between the start and end.
	 */
	public static final String IMAGE_CENTERING_TABLE_START = "<table width=\"100%\" height=\"100%\" align=\"center\" valign=\"center\"><tr><td>";
	public static final String IMAGE_CENTERING_TABLE_END = "</td></tr></table>";
	
	/*
	 * Style names
	 */
	public static final String STYLE_CODE_CONTENT = "codeContent";
	public static final String STYLE_SMALL_GREY_TEXT = "smallGreyText";
	public static final String HOMESEARCH_BOX_STYLE_NAME = "homesearchbox";
	public static final String STYLE_SMALL_SEARCHBOX = "smallsearchbox";
	public static final String STYLE_OTHER_SEARCHBOX = "othersearchbox";
	public static final String STYLE_BREAK_WORD = "break-word";
	public static final String STYLE_WHITE_BACKGROUND = "whiteBackground";
	public static final String STYLE_DISPLAY_INLINE = "inline-block";
	public static final String STYLE_BLACK_TEXT = "blackText";
	
	/*
	 * Client Documentation
	 */
	public static final String REST_API_URL = "http://rest.synapse.org";
	public static final String CLIENT_R_API_URL = "http://r-docs.synapse.org";
	public static final String CLIENT_R_EXAMPLE_CODE_URL = "#!Help:"+WebConstants.R_CLIENT;
	public static final String CLIENT_PYTHON_API_URL = "http://python-docs.synapse.org";
	public static final String CLIENT_PYTHON_EXAMPLE_CODE_URL = "#!Help:"+WebConstants.PYTHON_CLIENT;
	public static final String CLIENT_CL_API_URL = "http://python-docs.synapse.org/CommandLineClient.html";
	public static final String CLIENT_CL_EXAMPLE_CODE_URL = "#!Help:"+WebConstants.COMMAND_LINE_CLIENT;
	public static final String CLIENT_JAVA_API_URL = "https://github.com/Sage-Bionetworks/SynapseWebClient/blob/develop/src/main/java/org/sagebionetworks/web/server/servlet/SynapseClientImpl.java";
	public static final String CLIENT_JAVA_EXAMPLE_CODE_URL = "https://github.com/Sage-Bionetworks/SynapseWebClient/blob/develop/src/main/java/org/sagebionetworks/web/server/servlet/SynapseClientImpl.java";	

	/*
	 * JavaScript WebResources
	 */
	public static final WebResource CYTOSCAPE_JS = new WebResource("js/cytoscape-2.0.2.min.js", ResourceType.JAVASCRIPT);
	public static final WebResource MATHJAX_LOADER_JS = new WebResource("js/mathjax-loader.js", ResourceType.JAVASCRIPT);
	public static final WebResource MATHJAX_JS = new WebResource("https://cdn.mathjax.org/mathjax/latest/MathJax.js?config=default", ResourceType.JAVASCRIPT);
	public static final WebResource JQUERY_JS = new WebResource("https://ajax.googleapis.com/ajax/libs/jqueryui/1.8.14/jquery-ui.min.js", ResourceType.JAVASCRIPT);
	public static final String QUERY_SERVICE_PREFIX = "/query?query=";
	public static final String EVALUATION_QUERY_SERVICE_PREFIX = "/evaluation/submission/query?query=";
	
}

