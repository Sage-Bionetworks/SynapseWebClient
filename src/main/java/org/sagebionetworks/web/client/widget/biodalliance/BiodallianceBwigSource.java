package org.sagebionetworks.web.client.widget.biodalliance;

public class BiodallianceBwigSource {
	String sourceName;
	String sourceBwgURI;
	public static final String DEFAULT_STYLE_TYPE = "default";
	public static final String DEFAULT_STYLE_GLYPH_TYPE = "HISTOGRAM";
	public static final String DEFAULT_STYLE_COLOR = "grey";
	public static final Integer DEFAULT_HEIGHT = 30;
	String styleType;
	String styleGlyphType;
	String styleColor;
	int trackHeightPx;
	public BiodallianceBwigSource(String sourceName, String entityId, Long version,
			String styleType, String styleGlyphType, String styleColor,
			Integer trackHeightPx) {
		this.sourceName = sourceName;
		String versionString = version != null ? version.toString() : null;
		this.sourceBwgURI = BiodallianceWidget.getFileResolverURL(entityId, versionString);
		this.styleType = styleType == null ? DEFAULT_STYLE_TYPE : styleType;
		this.styleGlyphType = styleGlyphType == null ? DEFAULT_STYLE_GLYPH_TYPE : styleGlyphType;
		this.styleColor = styleColor == null ? DEFAULT_STYLE_COLOR : styleColor;
		this.trackHeightPx = trackHeightPx == null ? DEFAULT_HEIGHT : trackHeightPx;
	}
	public String getSourceName() {
		return sourceName;
	}
	public String getSourceBwgURI() {
		return sourceBwgURI;
	}
	public String getStyleType() {
		return styleType;
	}
	public String getStyleGlyphType() {
		return styleGlyphType;
	}
	public String getStyleColor() {
		return styleColor;
	}
	public int getTrackHeightPx() {
		return trackHeightPx;
	}
	
	

}
