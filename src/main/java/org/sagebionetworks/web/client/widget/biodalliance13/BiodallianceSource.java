package org.sagebionetworks.web.client.widget.biodalliance13;

import static org.sagebionetworks.repo.model.EntityBundle.ENTITY;
import static org.sagebionetworks.repo.model.EntityBundle.FILE_HANDLES;

import java.util.List;

import org.sagebionetworks.repo.model.Entity;
import org.sagebionetworks.repo.model.EntityBundle;
import org.sagebionetworks.repo.model.FileEntity;
import org.sagebionetworks.repo.model.Reference;
import org.sagebionetworks.repo.model.file.FileHandle;
import org.sagebionetworks.repo.model.file.PreviewFileHandle;
import org.sagebionetworks.web.client.SynapseClientAsync;
import org.sagebionetworks.web.client.exceptions.IllegalArgumentException;

import com.google.gwt.core.client.GWT;
import com.google.gwt.json.client.JSONObject;
import com.google.gwt.json.client.JSONParser;
import com.google.gwt.json.client.JSONString;
import com.google.gwt.user.client.rpc.AsyncCallback;
import com.google.gwt.user.client.ui.IsWidget;
import com.google.gwt.user.client.ui.Widget;
import com.google.inject.Inject;

/**
 * Source object represents a Biodalliance track source.  The view is optional, and only needs to be injected if showing an editor for the source object.
 */
public class BiodallianceSource implements BiodallianceSourceView.Presenter, IsWidget{
	String sourceName, sourceURI, entityId, indexEntityId, indexSourceURI;
	Long version, indexVersion;
	public static final String DEFAULT_STYLE_TYPE = "default";
	public static final String DEFAULT_STYLE_GLYPH_TYPE = "HISTOGRAM";
	public static final String DEFAULT_STYLE_COLOR = "#808080"; //grey
	public static final Integer DEFAULT_HEIGHT = 30;
	
	String styleType, styleGlyphType, styleColor;
	int trackHeightPx;

	//view, may not be set if only using this class to pass data around
	BiodallianceSourceView view;
	private SynapseClientAsync synapseClient;
	public enum SourceType {
		BIGWIG, VCF
	}
	SourceType sourceType;
	
	/**
	 * json keys
	 */
	public static final String SOURCE_NAME_KEY = "name";
	public static final String SOURCE_TYPE = "type";
	public static final String SOURCE_ENTITY_ID_KEY = "entityId";
	public static final String SOURCE_ENTITY_VERSION_KEY = "entityVersion";
	public static final String SOURCE_INDEX_ENTITY_ID_KEY = "indexEntityId";
	public static final String SOURCE_INDEX_ENTITY_VERSION_KEY = "indexEntityVersion";
	public static final String STYLE_TYPE_KEY = "styleType";
	public static final String STYLE_GLYPH_TYPE_KEY = "styleGlyphType";
	public static final String STYLE_COLOR_KEY = "color";
	public static final String STYLE_HEIGHT = "height";
	
	@Inject
	public BiodallianceSource(BiodallianceSourceView view, SynapseClientAsync synapseClient) {
		this.view = view;
		this.synapseClient = synapseClient;
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
	
	public BiodallianceSource() {
	}
	
	public JSONObject toJsonObject() {
		updateFromView();
		JSONObject jsonObject = new JSONObject();
		jsonObject.put(SOURCE_NAME_KEY, new JSONString(sourceName));
		jsonObject.put(SOURCE_ENTITY_ID_KEY, new JSONString(entityId));
		if (version != null) {
			jsonObject.put(SOURCE_ENTITY_VERSION_KEY, new JSONString(version.toString()));	
		}
		if (indexEntityId != null) {
			jsonObject.put(SOURCE_INDEX_ENTITY_ID_KEY, new JSONString(indexEntityId));	
		}
		if (indexVersion != null) {
			jsonObject.put(SOURCE_INDEX_ENTITY_VERSION_KEY, new JSONString(indexVersion.toString()));	
		}
		jsonObject.put(STYLE_TYPE_KEY, new JSONString(styleType));
		jsonObject.put(STYLE_GLYPH_TYPE_KEY, new JSONString(styleGlyphType));
		jsonObject.put(STYLE_COLOR_KEY, new JSONString(styleColor));
		jsonObject.put(SOURCE_TYPE, new JSONString(sourceType.name()));
		jsonObject.put(STYLE_HEIGHT, new JSONString(Integer.toString(trackHeightPx)));
		return jsonObject;
	}
	
	@Override
	public void entitySelected(Reference ref) {
		entityId = null;
		version = null;
		sourceType = null;
		updateFromView();
		//determine the source type of the given reference before accepting
		int mask = ENTITY | FILE_HANDLES ;
		AsyncCallback<EntityBundle> callback = new AsyncCallback<EntityBundle>() {
			@Override
			public void onSuccess(EntityBundle bundle) {
				try{
					assertFileEntity(bundle.getEntity());
					FileHandle fileHandle = getFileHandle(bundle.getFileHandles());
					SourceType newSourceType = getSourceType(fileHandle.getFileName());
					String newEntityId = bundle.getEntity().getId();
					Long newVersion = ((FileEntity)bundle.getEntity()).getVersionNumber();
					configure(sourceName, newEntityId, newVersion, newSourceType, indexEntityId, indexVersion);
					view.hideEntityFinder();
				} catch (Exception e) {
					onFailure(e);
				}
			}
			
			@Override
			public void onFailure(Throwable caught) {
				view.showErrorMessage(caught.getMessage());
			}			
		};
		if (ref.getTargetVersionNumber() == null) {
			synapseClient.getEntityBundle(ref.getTargetId(), mask, callback);
		} else {
			synapseClient.getEntityBundleForVersion(ref.getTargetId(), ref.getTargetVersionNumber(), mask, callback);
		}
	}
	
	@Override
	public void indexEntitySelected(Reference ref) {
		indexEntityId = null;
		indexVersion = null;
		updateFromView();
		int mask = ENTITY | FILE_HANDLES ;
		AsyncCallback<EntityBundle> callback = new AsyncCallback<EntityBundle>() {
			@Override
			public void onSuccess(EntityBundle bundle) {
				try{
					assertFileEntity(bundle.getEntity());
					FileHandle fileHandle = getFileHandle(bundle.getFileHandles());
					assertIndexFile(fileHandle.getFileName());
					String newIndexEntityId = bundle.getEntity().getId();
					Long newIndexVersion = ((FileEntity)bundle.getEntity()).getVersionNumber();
					configure(sourceName, entityId, version, sourceType, newIndexEntityId, newIndexVersion);
					view.hideEntityFinder();
				} catch (Exception e) {
					onFailure(e);
				}
			}
			
			@Override
			public void onFailure(Throwable caught) {
				view.showErrorMessage(caught.getMessage());
			}			
		};
		if (ref.getTargetVersionNumber() == null) {
			synapseClient.getEntityBundle(ref.getTargetId(), mask, callback);
		} else {
			synapseClient.getEntityBundleForVersion(ref.getTargetId(), ref.getTargetVersionNumber(), mask, callback);
		}
	}
	
	public void assertFileEntity(Entity entity) throws IllegalArgumentException {
		if (!(entity instanceof FileEntity)) {
			throw new IllegalArgumentException("Must select a file.");
		}
	}
	
	public void assertIndexFile(String fileName) throws IllegalArgumentException {
		if (!(fileName.toLowerCase().endsWith(".tbi"))) {
			throw new IllegalArgumentException("Unrecognized index file: " + fileName);
		}
	}
	
	
	public FileHandle getFileHandle(List<FileHandle> fileHandles) throws IllegalArgumentException {
		FileHandle fileHandle = null;
		for (FileHandle handle : fileHandles) {
			if (!(handle instanceof PreviewFileHandle)) {
				fileHandle = handle;
			}
		}
		if (fileHandle == null) {
			throw new IllegalArgumentException("Could not find a valid file in the selection.");
		}
		return fileHandle;
	}
	
	/**
	 * Based on the file extension only, return the source type.  If it can't be determined, then this method will throw an illegal argument exception.
	 * @param fileName
	 * @return
	 * @throws IllegalArgumentException 
	 */
	public SourceType getSourceType(String fileName) throws IllegalArgumentException {
		String lowercaseFileName = fileName.toLowerCase();
		if (lowercaseFileName.endsWith(".bw") || lowercaseFileName.endsWith(".bigwig")) {
			return SourceType.BIGWIG;
		}
		//else
		if (lowercaseFileName.endsWith(".vcf") || lowercaseFileName.endsWith(".vcf.gz")) {
			return SourceType.VCF;
		}
		
		throw new IllegalArgumentException("Could not determine a valid source file type from the file name: " + fileName);
	}

	
	private void updateFromView() {
		if (view != null) {
			sourceName = view.getSourceName();
			styleColor = view.getColor();
			trackHeightPx = Integer.parseInt(view.getHeight());
			//entity id and version are pushed back from the view (on selection), so we don't need to update here
		}
	}
	
	public void initializeFromJson(String json) {
		JSONObject value = (JSONObject)JSONParser.parseStrict(json);
		String sourceName = value.get(SOURCE_NAME_KEY).isString().stringValue();
		String entityId = value.get(SOURCE_ENTITY_ID_KEY).isString().stringValue();
		Long version = null;
		if (value.containsKey(SOURCE_ENTITY_VERSION_KEY)) {
			String versionString = value.get(SOURCE_ENTITY_VERSION_KEY).isString().stringValue();
			version = Long.parseLong(versionString);
		}
		String indexEntityId = null;
		if (value.containsKey(SOURCE_INDEX_ENTITY_ID_KEY)) {
			indexEntityId = value.get(SOURCE_INDEX_ENTITY_ID_KEY).isString().stringValue();
		}
		Long indexVersion = null;
		if (value.containsKey(SOURCE_INDEX_ENTITY_VERSION_KEY)) {
			String versionString = value.get(SOURCE_INDEX_ENTITY_VERSION_KEY).isString().stringValue();
			indexVersion = Long.parseLong(versionString);
		}
		
		String styleType = value.get(STYLE_TYPE_KEY).isString().stringValue();
		String styleGlyphType = value.get(STYLE_GLYPH_TYPE_KEY).isString().stringValue();
		String styleColor = value.get(STYLE_COLOR_KEY).isString().stringValue();
		int trackHeightPx = Integer.parseInt(value.get(STYLE_HEIGHT).isString().stringValue());
		String sourceTypeString = value.get(SOURCE_TYPE).isString().stringValue();
		configure(sourceName, entityId, version, SourceType.valueOf(sourceTypeString), indexEntityId, indexVersion);
		setStyle(styleType, styleGlyphType, styleColor, trackHeightPx);
	}
	
	public void configure(String sourceName, String entityId, Long version, SourceType sourceType) {
		configure(sourceName, entityId, version, sourceType, null, null);
	}
	
	public void configure(String sourceName, String entityId, Long version, SourceType sourceType, String indexEntityId, Long indexVersion) {
		this.sourceName = sourceName;
		this.version = version;
		this.indexVersion = indexVersion;
		this.entityId = entityId;
		this.indexEntityId = indexEntityId;
		String indexVersionString = indexVersion != null ? indexVersion.toString() : null;
		this.indexSourceURI = BiodallianceWidget.getFileResolverURL(indexEntityId, indexVersionString);
		
		String versionString = version != null ? version.toString() : null;
		this.sourceURI = BiodallianceWidget.getFileResolverURL(entityId, versionString);
		this.sourceType = sourceType;
		
		if (view != null) {
			view.setSourceName(sourceName);
			view.setEntityFinderText(getEntityFinderText(entityId, version));
			view.setIndexEntityFinderText(getEntityFinderText(indexEntityId, indexVersion));
		}
	}
	
	public String getEntityFinderText(String entityId, Long version) {
		String entityFinderText = entityId;
		if (version != null) {
			entityFinderText += "."+version;
		}
		return entityFinderText;
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
	public String getSourceIndexURI() {
		return indexSourceURI;
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
	
	@Override
	public Widget asWidget() {
		if (view != null) {
			return view.asWidget();
		}
		return null;
	}
	
}
