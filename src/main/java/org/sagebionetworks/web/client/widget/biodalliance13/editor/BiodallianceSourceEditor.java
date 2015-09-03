package org.sagebionetworks.web.client.widget.biodalliance13.editor;

import static org.sagebionetworks.repo.model.EntityBundle.ENTITY;
import static org.sagebionetworks.repo.model.EntityBundle.FILE_HANDLES;

import java.util.List;

import org.gwtvisualizationwrappers.client.biodalliance.BiodallianceSource;
import org.gwtvisualizationwrappers.client.biodalliance.BiodallianceSource.SourceType;
import org.sagebionetworks.repo.model.Entity;
import org.sagebionetworks.repo.model.EntityBundle;
import org.sagebionetworks.repo.model.FileEntity;
import org.sagebionetworks.repo.model.Reference;
import org.sagebionetworks.repo.model.file.FileHandle;
import org.sagebionetworks.repo.model.file.PreviewFileHandle;
import org.sagebionetworks.web.client.SynapseClientAsync;
import org.sagebionetworks.web.client.exceptions.IllegalArgumentException;
import org.sagebionetworks.web.client.widget.biodalliance13.BiodallianceWidget;

import com.google.gwt.json.client.JSONObject;
import com.google.gwt.user.client.rpc.AsyncCallback;
import com.google.gwt.user.client.ui.IsWidget;
import com.google.gwt.user.client.ui.Widget;
import com.google.inject.Inject;

/**
 * Source object represents a Biodalliance track source.  The view is optional, and only needs to be injected if showing an editor for the source object.
 */
public class BiodallianceSourceEditor implements BiodallianceSourceEditorView.Presenter, IsWidget{
	//view, may not be set if only using this class to pass data around
	BiodallianceSourceEditorView view;
	private SynapseClientAsync synapseClient;
	BiodallianceSource source;
	
	@Inject
	public BiodallianceSourceEditor(BiodallianceSourceEditorView view, SynapseClientAsync synapseClient) {
		this.view = view;
		this.synapseClient = synapseClient;
		view.setPresenter(this);
		source = new BiodallianceSource();
		view.setHeight(Integer.toString(source.getTrackHeightPx()));
		view.setColor(source.getStyleColor());
	}
	
	public void setSource(BiodallianceSource source) {
		this.source = source;
		updateViewFromSource();
	}
	
	public JSONObject toJsonObject() {
		updateFromView();
		return source.toJsonObject();
	}
	
	@Override
	public void entitySelected(Reference ref) {
		updateFromView();
		source.setEntity(null, null);
		source.setSourceType(null);
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
					source.setEntity(newEntityId, newVersion);
					BiodallianceWidget.updateSourceURIs(source);
					source.setSourceType(newSourceType);
					view.setEntityFinderText(getEntityFinderText(newEntityId, newVersion));
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
	
	/**
	 * Based on the file extension only, return the source type.  If it can't be determined, then this method will return null.
	 * @param fileName
	 * @return
	 */
	public static SourceType getSourceType(String fileName) {
		if (fileName != null) {
			int lastDot = fileName.lastIndexOf(".");
			if (lastDot > -1) {
				String extension = fileName.substring(lastDot).toLowerCase();
				if (".bw".equals(extension) || "bigwig".equals(extension)) {
					return SourceType.BIGWIG;
				}
				//else
				if (".vcf".equals(extension)) {
					return SourceType.VCF;
				}
			}
		}
		return null;
	}
	
	@Override
	public void indexEntitySelected(Reference ref) {
		source.setIndexEntity(null, null);
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
					source.setIndexEntity(newIndexEntityId, newIndexVersion);
					BiodallianceWidget.updateSourceURIs(source);
					view.setIndexEntityFinderText(getEntityFinderText(newIndexEntityId, newIndexVersion));
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
	
	private void updateFromView() {
		source.setSourceName(view.getSourceName());
		source.setStyleColor(view.getColor());
		source.setTrackHeightPx(Integer.parseInt(view.getHeight()));
		//entity id and version are pushed back from the view (on selection), so we don't need to update here
	}
	
	private void updateViewFromSource() {
		view.setSourceName(source.getSourceName());
		view.setEntityFinderText(getEntityFinderText(source.getEntityId(), source.getVersion()));
		view.setIndexEntityFinderText(getEntityFinderText(source.getIndexEntityId(), source.getIndexVersion()));
		view.setColor(source.getStyleColor());
		view.setHeight(Integer.toString(source.getTrackHeightPx()));
	}
	
	public void initializeFromJson(String json) {
		source.initializeFromJson(json);
		BiodallianceWidget.updateSourceURIs(source);
		updateViewFromSource();
	}
	
	public String getEntityFinderText(String entityId, Long version) {
		String entityFinderText = entityId;
		if (version != null) {
			entityFinderText += "."+version;
		}
		return entityFinderText;
	}
	
	@Override
	public Widget asWidget() {
		return view.asWidget();
	}
	
}
