package org.sagebionetworks.web.client.widget.biodalliance;

import com.google.gwt.json.client.JSONObject;
import com.google.gwt.json.client.JSONParser;
import com.google.gwt.json.client.JSONString;

public class BiodallianceSource {
	String sourceName, sourceURI, entityId;
	Long version;
	public static final String DEFAULT_STYLE_TYPE = "default";
	public static final String DEFAULT_STYLE_GLYPH_TYPE = "HISTOGRAM";
	public static final String DEFAULT_STYLE_COLOR = "grey";
	public static final Integer DEFAULT_HEIGHT = 30;
	
	String styleType = DEFAULT_STYLE_TYPE;
	String styleGlyphType = DEFAULT_STYLE_GLYPH_TYPE;
	String styleColor = DEFAULT_STYLE_COLOR;
	int trackHeightPx = DEFAULT_HEIGHT;

	public enum SourceType {
		BIGWIG, VCF
	}
	SourceType sourceType;
	
	/**
	 * json keys
	 */
	public static final String SOURCE_NAME_KEY = "sourceName";
	public static final String SOURCE_TYPE = "sourceType";
	public static final String SOURCE_ENTITY_ID_KEY = "sourceEntityId";
	public static final String SOURCE_ENTITY_VERSION_KEY = "sourceEntityVersion";
	public static final String STYLE_TYPE_KEY = "styleType";
	public static final String STYLE_GLYPH_TYPE_KEY = "styleGlyphType";
	public static final String STYLE_COLOR_KEY = "color";
	public static final String STYLE_HEIGHT = "height";
	
	public BiodallianceSource() {
	}
	
	public BiodallianceSource(String json) {
		JSONObject value = (JSONObject)JSONParser.parseStrict(json);
		sourceName = value.get(SOURCE_NAME_KEY).toString();
		entityId = value.get(SOURCE_ENTITY_ID_KEY).toString();
		version = Long.parseLong(value.get(SOURCE_ENTITY_VERSION_KEY).toString());
		styleType = value.get(STYLE_TYPE_KEY).toString();
		styleGlyphType = value.get(STYLE_GLYPH_TYPE_KEY).toString();
		styleColor = value.get(STYLE_COLOR_KEY).toString();
		trackHeightPx = Integer.parseInt(value.get(STYLE_HEIGHT).toString());
		String sourceTypeString = value.get(SOURCE_TYPE).toString();
		configure(sourceName, entityId, version, SourceType.valueOf(sourceTypeString));
	}
	
	public JSONObject toJsonObject() {
		JSONObject jsonObject = new JSONObject();
		jsonObject.put(SOURCE_NAME_KEY, new JSONString(sourceName));
		jsonObject.put(SOURCE_ENTITY_ID_KEY, new JSONString(entityId));
		jsonObject.put(SOURCE_ENTITY_VERSION_KEY, new JSONString(version.toString()));
		jsonObject.put(STYLE_TYPE_KEY, new JSONString(styleType));
		jsonObject.put(STYLE_GLYPH_TYPE_KEY, new JSONString(styleGlyphType));
		jsonObject.put(STYLE_COLOR_KEY, new JSONString(styleColor));
		jsonObject.put(SOURCE_TYPE, new JSONString(sourceType.name()));
		jsonObject.put(STYLE_HEIGHT, new JSONString(Integer.toString(trackHeightPx)));
		return jsonObject;
	}
	
	public void configure(String sourceName, String entityId, Long version, SourceType sourceType) {
		this.sourceName = sourceName;
		this.version = version;
		String versionString = version != null ? version.toString() : null;
		this.entityId = entityId;
		
		this.sourceURI = BiodallianceWidget.getFileResolverURL(entityId, versionString);
		this.sourceType = sourceType;
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
	
	public SourceType getSourceType() {
		return sourceType;
	}
	
	
}
