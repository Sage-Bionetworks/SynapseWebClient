package org.sagebionetworks.web.client.widget.biodalliance;

public abstract class BiodallianceSource {
	String sourceName;
	String sourceURI;
	public static final String DEFAULT_STYLE_TYPE = "default";
	public static final String DEFAULT_STYLE_GLYPH_TYPE = "HISTOGRAM";
	public static final String DEFAULT_STYLE_COLOR = "grey";
	public static final Integer DEFAULT_HEIGHT = 30;
	String styleType = DEFAULT_STYLE_TYPE;
	String styleGlyphType = DEFAULT_STYLE_GLYPH_TYPE;
	String styleColor = DEFAULT_STYLE_COLOR;
	int trackHeightPx = DEFAULT_HEIGHT;
	public BiodallianceSource(String sourceName, String entityId, Long version) {
		this.sourceName = sourceName;
		String versionString = version != null ? version.toString() : null;
		this.sourceURI = BiodallianceWidget.getFileResolverURL(entityId, versionString);
	}
	
	public void setStyle(String styleType, String styleGlyphType, String styleColor, int trackHeightPx) {
		this.styleType = styleType;
		this.styleGlyphType = styleGlyphType;
		this.styleColor = styleColor;
		this.trackHeightPx = trackHeightPx;
	}
	
	public String getSourceName() {
		return sourceName;
	}
	public String getSourceURI() {
		return sourceURI;
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
