package org.sagebionetworks.web.client.widget.entity.registration;

public class WidgetConstants {
	public static final String YOUTUBE_CONTENT_TYPE = "youtube";
	public static final String YOUTUBE_FRIENDLY_NAME = "YouTube";
	
	public static final String PROVENANCE_CONTENT_TYPE = "provenance";
	public static final String PROVENANCE_FRIENDLY_NAME = "ProvenanceGraph";
	
	public static final String IMAGE_CONTENT_TYPE = "image";
	public static final String IMAGE_FRIENDLY_NAME ="Image";
	
	public static final String LINK_CONTENT_TYPE = "link";
	public static final String LINK_FRIENDLY_NAME ="Link";

	public static final String API_TABLE_CONTENT_TYPE = "supertable";
	public static final String API_TABLE_FRIENDLY_NAME = "Super Table (Synapse API Based)";
	
	public static final String WIDGET_START_MARKDOWN = "${";
	public static final String WIDGET_START_MARKDOWN_ESCAPED = "\\$\\{";
	
	/**
	 * Widget parameter keys
	 */
	public static final String IMAGE_WIDGET_FILE_NAME_KEY = "fileName";
	public static final String IMAGE_WIDGET_WIDTH_KEY = "width";
	public static final String PROV_WIDGET_ENTITY_ID_KEY = "entityId";
	public static final String PROV_WIDGET_DEPTH_KEY = "depth";
	public static final String PROV_WIDGET_EXPAND_KEY = "showExpand";
	public static final String YOUTUBE_WIDGET_VIDEO_ID_KEY = "videoId";
	public static final String API_TABLE_WIDGET_PATH_KEY = "path";
	public static final String API_TABLE_WIDGET_PAGING_KEY = "paging";
	public static final String API_TABLE_WIDGET_SHOW_ROW_NUMBER_KEY = "showRowNumber";
	public static final String API_TABLE_WIDGET_RESULTS_KEY = "jsonResultsKeyName";
	public static final String API_TABLE_WIDGET_CSS_STYLE = "cssStyle";
	public static final String API_TABLE_WIDGET_ROW_NUMBER_DISPLAY_NAME_KEY = "rowNumberDisplayName";
	public static final String API_TABLE_WIDGET_WIDTH_KEY = "width";
	public static final String API_TABLE_WIDGET_PAGESIZE_KEY = "pageSize";
	public static final String API_TABLE_WIDGET_COLUMNS_KEY = "columns";
	public static final String API_TABLE_WIDGET_DISPLAY_COLUMN_NAMES_KEY = "displayNames";
	public static final String API_TABLE_WIDGET_RENDERERS_KEY = "renderers";
	
	
	
	/**
	 * API Table Column Renderers
	 */
	public static final String API_TABLE_COLUMN_RENDERER_NONE = "none";
	public static final String API_TABLE_COLUMN_RENDERER_USER_ID = "userid";
	public static final String API_TABLE_COLUMN_RENDERER_DATE = "date";
	public static final String API_TABLE_COLUMN_RENDERER_SYNAPSE_ID = "synapseid";
	public static final String API_TABLE_COLUMN_RENDERER_ANNOTATIONS = "annotations";
}
