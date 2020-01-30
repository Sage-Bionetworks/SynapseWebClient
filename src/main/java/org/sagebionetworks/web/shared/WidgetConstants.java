package org.sagebionetworks.web.shared;


public class WidgetConstants {
	public static final String DIV_ID_MATHJAX_PREFIX = "mathjax-";
	public static final String FOOTNOTE_ID_WIDGET_PREFIX = "wikiFootnote";

	public static final String BOOKMARK_CONTENT_TYPE = "bookmark";
	public static final String BOOKMARK_FRIENDLY_NAME = "Bookmark";
	public static final String BOOKMARK_TARGET_CONTENT_TYPE = "bookmarktarget";
	public static final String BOOKMARK_LINK_IDENTIFIER = "#Bookmark";

	public static final String YOUTUBE_CONTENT_TYPE = "youtube";
	public static final String YOUTUBE_FRIENDLY_NAME = "YouTube";

	public static final String VIMEO_CONTENT_TYPE = "vimeo";
	public static final String VIMEO_FRIENDLY_NAME = "Vimeo";

	public static final String TUTORIAL_WIZARD_CONTENT_TYPE = "tutorial";
	public static final String TUTORIAL_WIZARD_FRIENDLY_NAME = "Tutorial Wizard";

	public static final String PROVENANCE_CONTENT_TYPE = "provenance";
	public static final String PROVENANCE_FRIENDLY_NAME = "Provenance Graph";

	public static final String IMAGE_CONTENT_TYPE = "image";
	public static final String IMAGE_FRIENDLY_NAME = "Image";

	public static final String IMAGE_LINK_EDITOR_CONTENT_TYPE = "imageLink";
	public static final String IMAGE_LINK_FRIENDLY_NAME = "Image Link";

	public static final String VIDEO_CONTENT_TYPE = "video";
	public static final String VIDEO_FRIENDLY_NAME = "Video";

	public static final String SYNAPSE_FORM_CONTENT_TYPE = "synapseForm";
	public static final String SYNAPSE_FORM_FRIENDLY_NAME = "Synapse Form";

	public static final String CYTOSCAPE_CONTENT_TYPE = "cytoscapeJs25";
	public static final String CYTOSCAPE_FRIENDLY_NAME = "Cytoscape JS";


	public static final String SYNAPSE_TABLE_CONTENT_TYPE = "synapsetable";
	public static final String SYNAPSE_TABLE_FRIENDLY_NAME = "Synapse Table/View";

	public static final String ATTACHMENT_PREVIEW_CONTENT_TYPE = "previewattachment";
	public static final String ATTACHMENT_PREVIEW_FRIENDLY_NAME = "Attachment";

	public static final String LINK_CONTENT_TYPE = "link";
	public static final String LINK_FRIENDLY_NAME = "Link";

	public static final String DETAILS_SUMMARY_CONTENT_TYPE = "summarydetails";
	public static final String COLLAPSED_SECTION = "Collapsible Section (Details/Summary)";

	public static final String REFERENCE_CONTENT_TYPE = "reference";
	public static final String REFERENCE_FRIENDLY_NAME = "Reference";

	public static final String TABBED_TABLE_CONTENT_TYPE = "tabbedtable";
	public static final String TABBED_TABLE_FRIENDLY_NAME = "Table";

	public static final String QUERY_TABLE_CONTENT_TYPE = "querytable";
	public static final String QUERY_TABLE_FRIENDLY_NAME = "Query Table";

	public static final String LEADERBOARD_CONTENT_TYPE = "leaderboard";
	public static final String LEADERBOARD_FRIENDLY_NAME = "Leaderboard";


	public static final String API_TABLE_CONTENT_TYPE = "supertable";
	public static final String API_TABLE_FRIENDLY_NAME = "Super Table (Synapse API Based)";

	public static final String ENTITYLIST_CONTENT_TYPE = "entitylist";
	public static final String ENTITYLIST_FRIENDLY_NAME = "Entity List";

	public static final String SHINYSITE_CONTENT_TYPE = "iframe";
	public static final String SHINYSITE_FRIENDLY_NAME = "External Website";

	public static final String USERBADGE_CONTENT_TYPE = "userbadge";
	public static final String USERBADGE_FRIENDLY_NAME = "User Badge";

	public static final String USER_TEAM_BADGE_CONTENT_TYPE = "badge";
	public static final String USER_TEAM_BADGE_FRIENDLY_NAME = "Badge";


	public static final String OLD_JOIN_EVALUATION_CONTENT_TYPE = "joinevaluation";
	public static final String JOIN_TEAM_CONTENT_TYPE = "jointeam";
	public static final String JOIN_EVALUATION_FRIENDLY_NAME = "Join Evaluation";
	public static final String JOIN_TEAM_FRIENDLY_NAME = "Join Team Button";

	public static final String SUBMIT_TO_EVALUATION_CONTENT_TYPE = "evalsubmit";

	public static final String BUTTON_TEXT_KEY = "buttonText";

	public static final String BUTTON_LINK_CONTENT_TYPE = "buttonlink";
	public static final String BUTTON_LINK_FRIENDLY_NAME = "Button Link";

	public static final String TEAM_MEMBERS_CONTENT_TYPE = "members";
	public static final String TEAM_MEMBERS_FRIENDLY_NAME = "Team Members";

	public static final String TEAM_MEMBER_COUNT_CONTENT_TYPE = "teammembercount";
	public static final String TEAM_MEMBER_COUNT_FRIENDLY_NAME = "Team Member Count";
	public static final String PLOT_CONTENT_TYPE = "plot";
	public static final String PLOT_FRIENDLY_NAME = "Plot";

	public static final String WIDGET_START_MARKDOWN = "${";
	public static final String WIDGET_END_MARKDOWN = "}";

	public static final String TOC_CONTENT_TYPE = "toc";
	public static final String IS_TOC_KEY = "isTOC";
	public static final String WIKI_SUBPAGES_CONTENT_TYPE = "wikipages";

	public static final String NO_AUTO_WIKI_SUBPAGES = "nowikipages";

	public static final String WIKI_FILES_PREVIEW_CONTENT_TYPE = "wikifilepreview";

	/**
	 * Widget parameter keys
	 */
	public static final String IMAGE_WIDGET_FILE_NAME_KEY = "fileName";
	public static final String IMAGE_WIDGET_SCALE_KEY = "scale";
	public static final String ALIGNMENT_KEY = "align";
	public static final String IMAGE_WIDGET_ALT_TEXT_KEY = "altText";
	public static final String IMAGE_WIDGET_SYNAPSE_ID_KEY = "synapseId";
	public static final String IMAGE_WIDGET_RESPONSIVE_KEY = "responsive";

	public static final String SYNAPSE_ID_KEY = "synapseId";
	public static final String STYLE_SYNAPSE_ID_KEY = "styleSynapseId";

	public static final String VIDEO_WIDGET_MP4_SYNAPSE_ID_KEY = "mp4SynapseId";
	public static final String VIDEO_WIDGET_OGG_SYNAPSE_ID_KEY = "oggSynapseId";
	public static final String VIDEO_WIDGET_WEBM_SYNAPSE_ID_KEY = "webmSynapseId";
	public static final String VIDEO_WIDGET_WIDTH_KEY = "width";

	public static final String EVALUATION_ID_KEY = "evaluationId";
	public static final String TEAM_ID_KEY = "teamId";
	public static final String JOIN_WIDGET_REQUEST_EXPIRES_IN_X_DAYS_KEY = "requestExpiresInXDays";
	public static final String UNAVAILABLE_MESSAGE = "unavailableMessage";
	public static final String IS_MEMBER_MESSAGE = "isMemberMessage";
	public static final String JOIN_TEAM_BUTTON_TEXT = "text";
	public static final String JOIN_TEAM_DEFAULT_BUTTON_TEXT = "Join";
	public static final String JOIN_TEAM_OPEN_REQUEST_TEXT = "requestOpenText";
	public static final String JOIN_TEAM_IS_SIMPLE_REQUEST_BUTTON = "isSimpleRequestButton";
	public static final String JOIN_TEAM_DEFAULT_OPEN_REQUEST_TEXT = "Your request to join this team has been sent.";
	public static final String SUCCESS_MESSAGE = "successMessage";
	public static final String JOIN_TEAM_DEFAULT_SUCCESS_MESSAGE = "Successfully joined";
	public static final String JOIN_WIDGET_SHOW_PROFILE_FORM_KEY = "showProfileForm";
	public static final String JOIN_WIDGET_SUBCHALLENGE_ID_LIST_KEY = "subchallengeIdList";
	public static final String JOIN_WIDGET_SUBCHALLENGE_ID_LIST_DELIMETER = ",";
	public static final String FLOAT_NONE = "None";
	public static final String FLOAT_LEFT = "Left";
	public static final String FLOAT_RIGHT = "Right";
	public static final String FLOAT_CENTER = "Center";

	public static final String SUBMIT_TO_CHALLENGE = "Submit To Challenge";

	@Deprecated
	public static final String PROV_WIDGET_ENTITY_ID_KEY = "entityId";
	public static final String PROV_WIDGET_ENTITY_LIST_KEY = "entityList";
	public static final String PROV_WIDGET_ENTITY_LIST_DELIMETER = ",";
	public static final String PROV_WIDGET_DEPTH_KEY = "depth";
	public static final String PROV_WIDGET_EXPAND_KEY = "showExpand";
	public static final String PROV_WIDGET_UNDEFINED_KEY = "showUndefined";
	public static final String PROV_WIDGET_DISPLAY_HEIGHT_KEY = "displayHeightPx";
	public static final int PROV_WIDGET_HEIGHT_DEFAULT = 275;
	public static final String YOUTUBE_WIDGET_VIDEO_ID_KEY = "videoId";
	public static final String TABLE_ID_KEY = "tableId";
	public static final String VIMEO_WIDGET_VIDEO_ID_KEY = "vimeoId";
	public static final String API_TABLE_WIDGET_PATH_KEY = "path";
	public static final String API_TABLE_WIDGET_PAGING_KEY = "paging";
	public static final String API_TABLE_WIDGET_QUERY_TABLE_RESULTS = "queryTableResults";
	public static final String API_TABLE_WIDGET_SHOW_IF_LOGGED_IN = "showIfLoggedInOnly";
	public static final String API_TABLE_WIDGET_RESULTS_KEY = "jsonResultsKeyName";
	public static final String API_TABLE_WIDGET_CSS_STYLE = "cssStyle";
	public static final String API_TABLE_WIDGET_COLUMN_CONFIG_PREFIX = "columnConfig";
	public static final String API_TABLE_WIDGET_PAGESIZE_KEY = "pageSize";
	public static final String ENTITYLIST_WIDGET_LIST_KEY = "list";
	public static final String ENTITYLIST_WIDGET_SHOW_DESCRIPTION_KEY = "desc";
	public static final String USERBADGE_WIDGET_ID_KEY = "id";
	public static final String USER_TEAM_BADGE_WIDGET_ID_KEY = "id";
	public static final String USER_TEAM_BADGE_WIDGET_USERNAME_KEY = "username";
	public static final String USER_TEAM_BADGE_WIDGET_IS_INDIVIDUAL_KEY = "isUser";

	public static final String ALIAS_KEY = "alias";
	public static final String BIODALLIANCE13_CONTENT_TYPE = "biodalliance13";
	public static final String BIODALLIANCE_FRIENDLY_NAME = "Genome Browser";
	public static final String BIODALLIANCE_SPECIES_KEY = "species";
	public static final String BIODALLIANCE_CHR_KEY = "chr";
	public static final String BIODALLIANCE_VIEW_START_KEY = "viewStart";
	public static final String BIODALLIANCE_VIEW_END_KEY = "viewEnd";
	public static final String BIODALLIANCE_SOURCE_PREFIX = "source";

	public static final String WIDGET_ENTITY_ID_KEY = "entityId";
	public static final String PROJECT_ID_KEY = "projectId";
	public static final String WIDGET_ENTITY_VERSION_KEY = "version";

	// submit form wiki widget params
	public static final String FORM_CONTAINER_ID_KEY = "formContainerId";
	public static final String JSON_SCHEMA_ID_KEY = "jsonSchemaId";
	public static final String UI_SCHEMA_ID_KEY = "uiSchemaId";

	public static final String SHINYSITE_SITE_KEY = "site";
	public static final String INCLUDE_PRINCIPAL_ID_KEY = "includePrincipalId";
	public static final String HEIGHT_KEY = "height";
	public static final int SHINYSITE_DEFAULT_HEIGHT_PX = 400;

	public static final String LINK_URL_KEY = "url";
	public static final String TEXT_KEY = "text";

	public static final String REGISTER_CHALLENGE_TEAM_CONTENT_TYPE = "registerChallengeTeam";
	public static final String CHALLENGE_TEAMS_CONTENT_TYPE = "challengeTeams";
	public static final String CHALLENGE_PARTICIPANTS_CONTENT_TYPE = "challengeParticipants";
	public static final String CHALLENGE_ID_KEY = "challengeId";
	public static final String IS_IN_CHALLENGE_TEAM_KEY = "isInTeam";
	public static final String PREVIEW_CONTENT_TYPE = "preview";
	public static final String PREVIEW_FRIENDLY_NAME = "Preview";
	public static final String BOOKMARK_KEY = "bookmarkID";
	public static final String REFERENCE_FOOTNOTE_KEY = "footnoteId";
	public static final String INLINE_WIDGET_KEY = "inlineWidget";
	/**
	 * API Table Column Renderers
	 */
	public static final String API_TABLE_COLUMN_RENDERER_NONE = "none";
	public static final String API_TABLE_COLUMN_RENDERER_USER_ID = "userid";
	public static final String API_TABLE_COLUMN_RENDERER_DATE = "date";
	public static final String API_TABLE_COLUMN_RENDERER_LINK = "markdown link";
	public static final String API_TABLE_COLUMN_RENDERER_EPOCH_DATE = "epochdate";
	public static final String API_TABLE_COLUMN_RENDERER_SYNAPSE_ID = "synapseid";
	public static final String API_TABLE_COLUMN_RENDERER_ANNOTATIONS = "annotations";
	public static final String API_TABLE_COLUMN_RENDERER_CANCEL_CONTROL = "cancelcontrol";

	public static final String MARKDOWN_HEADING_ID_PREFIX = "synapseheading";
	public static final String MARKDOWN_TABLE_ID_PREFIX = "markdown-table-";

	public static final String TABLE_LIMIT_KEY = "limit";
	public static final String TABLE_OFFSET_KEY = "offset";
	public static final String TABLE_QUERY_KEY = "query";
	public static final String FILL_COLUMN_NAME = "fill";
	public static final String QUERY_VISIBLE = "showquery";

	public static final String TITLE = "title";
	public static final String X_AXIS_TITLE = "xtitle";
	public static final String X_AXIS_TYPE = "xaxistype";
	public static final String Y_AXIS_TITLE = "ytitle";
	public static final String Y_AXIS_TYPE = "yaxistype";
	public static final String SHOW_LEGEND = "showlegend";
	public static final String IS_HORIZONTAL = "horizontal";
	public static final String TYPE = "type";
	public static final String BAR_MODE = "barmode";
}
