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
}
