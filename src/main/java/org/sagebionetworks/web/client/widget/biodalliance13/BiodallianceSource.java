package org.sagebionetworks.web.client.widget.biodalliance13;

import com.google.gwt.json.client.JSONObject;
import com.google.gwt.json.client.JSONParser;
import com.google.gwt.json.client.JSONString;
import com.google.inject.Inject;

public class BiodallianceSource implements BiodallianceSourceView.Presenter{
	String sourceName, sourceURI, entityId;
	Long version;
	public static final String DEFAULT_STYLE_TYPE = "default";
	public static final String DEFAULT_STYLE_GLYPH_TYPE = "HISTOGRAM";
	public static final String DEFAULT_STYLE_COLOR = "grey";
	public static final Integer DEFAULT_HEIGHT = 30;
	
	String styleType, styleGlyphType, styleColor;
	int trackHeightPx;

	//view, may not be set if only using this class to pass data around
	BiodallianceSourceView view;
	
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
		view = null;
		initDefaults();
	}
	
	@Inject
	public BiodallianceSource(BiodallianceSourceView view) {
		this.view = view;
		view.setPresenter(this);
		initDefaults();
	}
	
	public void initDefaults() {
		styleType = DEFAULT_STYLE_TYPE;
		styleGlyphType = DEFAULT_STYLE_GLYPH_TYPE;
		styleColor = DEFAULT_STYLE_COLOR;
		trackHeightPx = DEFAULT_HEIGHT;
		if (view != null) {
			view.setHeight(Integer.toString(DEFAULT_HEIGHT));
			view.setColor(styleColor);
		}
	}
	
	
	public BiodallianceSource(String json) {
		JSONObject value = (JSONObject)JSONParser.parseStrict(json);
		sourceName = value.get(SOURCE_NAME_KEY).isString().stringValue();
		entityId = value.get(SOURCE_ENTITY_ID_KEY).isString().stringValue();
		String versionString = value.get(SOURCE_ENTITY_VERSION_KEY).isString().stringValue();
		version = Long.parseLong(versionString);
		styleType = value.get(STYLE_TYPE_KEY).isString().stringValue();
		styleGlyphType = value.get(STYLE_GLYPH_TYPE_KEY).isString().stringValue();
		styleColor = value.get(STYLE_COLOR_KEY).isString().stringValue();
		trackHeightPx = Integer.parseInt(value.get(STYLE_HEIGHT).isString().stringValue());
		String sourceTypeString = value.get(SOURCE_TYPE).isString().stringValue();
		configure(sourceName, entityId, version, SourceType.valueOf(sourceTypeString));
		setStyle(styleType, styleGlyphType, styleColor, trackHeightPx);
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
		
		if (view != null) {
			view.setSourceName(sourceName);
			view.setEntity(entityId, version);
		}
	}
	
	public void setStyle(String styleType, String styleGlyphType, String styleColor, int trackHeightPx) {
		this.styleType = styleType;
		this.styleGlyphType = styleGlyphType;
		this.styleColor = styleColor;
		this.trackHeightPx = trackHeightPx;
		if (view != null) {
			view.setColor(styleColor);
			view.setHeight(Integer.toString(trackHeightPx));
		}
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
