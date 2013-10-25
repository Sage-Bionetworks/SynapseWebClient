package org.sagebionetworks.web.client.widget.entity.registration;


public class WidgetConstants {
	public static final String BOOKMARK_CONTENT_TYPE = "bookmark";
	public static final String BOOKMARK_FRIENDLY_NAME = "Bookmark";
	public static final String BOOKMARK_TARGET_CONTENT_TYPE = "bookmarktarget";
	public static final String BOOKMARK_LINK_IDENTIFIER = "#Bookmark";
	
	public static final String YOUTUBE_CONTENT_TYPE = "youtube";
	public static final String YOUTUBE_FRIENDLY_NAME = "YouTube";
	
	public static final String TUTORIAL_WIZARD_CONTENT_TYPE = "tutorial";
	public static final String TUTORIAL_WIZARD_FRIENDLY_NAME = "Tutorial Wizard";
	
	public static final String PROVENANCE_CONTENT_TYPE = "provenance";
	public static final String PROVENANCE_FRIENDLY_NAME = "Provenance Graph";
	
	public static final String IMAGE_CONTENT_TYPE = "image";
	public static final String IMAGE_FRIENDLY_NAME ="Image";
	
	public static final String ATTACHMENT_PREVIEW_CONTENT_TYPE = "previewattachment";
	public static final String ATTACHMENT_PREVIEW_FRIENDLY_NAME ="Attachment";
	
	public static final String LINK_CONTENT_TYPE = "link";
	public static final String LINK_FRIENDLY_NAME ="Link";
	
	public static final String REFERENCE_CONTENT_TYPE = "reference";
	public static final String REFERENCE_FRIENDLY_NAME = "Reference";

	public static final String TABBED_TABLE_CONTENT_TYPE = "tabbedtable";
	public static final String TABBED_TABLE_FRIENDLY_NAME ="Table";
	
	public static final String QUERY_TABLE_CONTENT_TYPE = "querytable";
	public static final String QUERY_TABLE_FRIENDLY_NAME = "Query Table";
	
	public static final String API_TABLE_CONTENT_TYPE = "supertable";
	public static final String API_TABLE_FRIENDLY_NAME = "Super Table (Synapse API Based)";

	public static final String ENTITYLIST_CONTENT_TYPE = "entitylist";
	public static final String ENTITYLIST_FRIENDLY_NAME ="Entity List";
	
	public static final String SHINYSITE_CONTENT_TYPE = "iframe";
	public static final String SHINYSITE_FRIENDLY_NAME ="Shiny Site";

	public static final String USERBADGE_CONTENT_TYPE = "userbadge";
	public static final String USERBADGE_FRIENDLY_NAME ="User Badge";
	
	public static final String JOIN_EVALUATION_CONTENT_TYPE = "joinevaluation";
	public static final String JOIN_EVALUATION_FRIENDLY_NAME ="Join Evaluation";
	
	public static final String BUTTON_LINK_CONTENT_TYPE = "buttonlink";
	public static final String BUTTON_LINK_FRIENDLY_NAME ="Button Link";
	
	public static final String WIDGET_START_MARKDOWN = "${";
	public static final String WIDGET_END_MARKDOWN =  "}";
	public static final String WIDGET_START_MARKDOWN_ESCAPED = "\\$\\{";
	public static final String WIDGET_END_MARKDOWN_ESCAPED = "\\}";
	
	public static final String TOC_CONTENT_TYPE = "toc";
	public static final String WIKI_SUBPAGES_CONTENT_TYPE = "wikipages";
	
	public static final String NO_AUTO_WIKI_SUBPAGES = "nowikipages";
	
	public static final String WIKI_FILES_PREVIEW_CONTENT_TYPE = "wikifilepreview";
	
	/**
	 * Widget parameter keys
	 */
	public static final String IMAGE_WIDGET_FILE_NAME_KEY = "fileName";
	public static final String IMAGE_WIDGET_SCALE_KEY = "scale";
	public static final String IMAGE_WIDGET_ALIGNMENT_KEY = "align";
	public static final String IMAGE_WIDGET_SYNAPSE_ID_KEY = "synapseId";
	public static final String JOIN_WIDGET_EVALUATION_ID_KEY = "evaluationId";
	public static final String JOIN_WIDGET_TEAM_ID_KEY = "teamId";
	public static final String JOIN_WIDGET_SUBCHALLENGE_ID_LIST_KEY = "subchallengeIdList";
	public static final String JOIN_WIDGET_SUBCHALLENGE_ID_LIST_DELIMETER = ",";
	public static final String FLOAT_NONE = "None";
	public static final String FLOAT_LEFT = "Left";
	public static final String FLOAT_RIGHT = "Right";
	public static final String FLOAT_CENTER = "Center";
	
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
	public static final String API_TABLE_WIDGET_PATH_KEY = "path";
	public static final String API_TABLE_WIDGET_PAGING_KEY = "paging";
	public static final String API_TABLE_WIDGET_SHOW_ROW_NUMBER_KEY = "showRowNumber";
	public static final String API_TABLE_WIDGET_RESULTS_KEY = "jsonResultsKeyName";
	public static final String API_TABLE_WIDGET_CSS_STYLE = "cssStyle";
	public static final String API_TABLE_WIDGET_COLUMN_CONFIG_PREFIX = "columnConfig";
	public static final String API_TABLE_WIDGET_ROW_NUMBER_DISPLAY_NAME_KEY = "rowNumberDisplayName";
	public static final String API_TABLE_WIDGET_PAGESIZE_KEY = "pageSize";
	public static final String ENTITYLIST_WIDGET_LIST_KEY = "list";
	public static final String USERBADGE_WIDGET_ID_KEY = "id";
	public static final String WIDGET_ENTITY_ID_KEY = "entityId";
	
	public static final String SHINYSITE_SITE_KEY = "site";	
	public static final String SHINYSITE_INCLUDE_PRINCIPAL_ID_KEY = "includePrincipalId";
	public static final String SHINYSITE_HEIGHT_KEY = "height";
	public static final int SHINYSITE_DEFAULT_HEIGHT_PX = 400;
	
	public static final String LINK_URL_KEY = "url";
	public static final String TEXT_KEY = "text";
	
	public static final String BOOKMARK_KEY = "bookmarkID";
	public static final String REFERENCE_FOOTNOTE_KEY = "footnoteId";
	public static final String INLINE_WIDGET_KEY = "inlineWidget";	/**
	 * API Table Column Renderers
	 */
	public static final String API_TABLE_COLUMN_RENDERER_NONE = "none";
	public static final String API_TABLE_COLUMN_RENDERER_USER_ID = "userid";
	public static final String API_TABLE_COLUMN_RENDERER_DATE = "date";
	public static final String API_TABLE_COLUMN_RENDERER_SYNAPSE_ID = "synapseid";
	public static final String API_TABLE_COLUMN_RENDERER_ANNOTATIONS = "annotations";
	
	public static final String MARKDOWN_HEADING_ID_PREFIX = "synapseheading";
	public static final String MARKDOWN_TABLE_ID_PREFIX = "markdown-table-";

}
