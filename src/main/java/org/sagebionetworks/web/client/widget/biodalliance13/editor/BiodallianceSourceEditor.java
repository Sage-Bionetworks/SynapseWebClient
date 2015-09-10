package org.sagebionetworks.web.client.widget.biodalliance13.editor;

import static org.sagebionetworks.repo.model.EntityBundle.ENTITY;
import static org.sagebionetworks.repo.model.EntityBundle.FILE_HANDLES;

import java.util.List;

import org.gwtvisualizationwrappers.client.biodalliance13.BiodallianceSource;
import org.gwtvisualizationwrappers.client.biodalliance13.BiodallianceSource.SourceType;
import org.sagebionetworks.repo.model.Entity;
import org.sagebionetworks.repo.model.EntityBundle;
import org.sagebionetworks.repo.model.FileEntity;
import org.sagebionetworks.repo.model.Reference;
import org.sagebionetworks.repo.model.file.FileHandle;
import org.sagebionetworks.repo.model.file.PreviewFileHandle;
import org.sagebionetworks.web.client.DisplayUtils.SelectedHandler;
import org.sagebionetworks.web.client.SynapseClientAsync;
import org.sagebionetworks.web.client.widget.biodalliance13.BiodallianceWidget;
import org.sagebionetworks.web.client.widget.entity.browse.EntityFinder;

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
	private BiodallianceSource source;
	BiodallianceSourceActionHandler handler;
	EntityFinder entityFinder, indexEntityFinder;
	@Inject
	public BiodallianceSourceEditor(
			BiodallianceSourceEditorView view, 
			SynapseClientAsync synapseClient, 
			EntityFinder entityFinder, 
			EntityFinder indexEntityFinder,
			BiodallianceSource source) {
		this.view = view;
		this.synapseClient = synapseClient;
		this.entityFinder = entityFinder;
		this.indexEntityFinder = indexEntityFinder;
		this.source = source;
		
		view.setPresenter(this);
		entityFinder.configure(true, new SelectedHandler<Reference>() {					
			@Override
			public void onSelected(Reference selected) {
				entitySelected(selected);
			}
		});
		
		indexEntityFinder.configure(true, new SelectedHandler<Reference>() {					
			@Override
			public void onSelected(Reference selected) {
				indexEntitySelected(selected);
			}
		});
		updateViewFromSource();
	}
	
	public void setSourceActionHandler(BiodallianceSourceActionHandler handler) {
		this.handler = handler;
	}
	
	public void setSourceJson(String sourceJson) {
		source.initializeFromJson(sourceJson);
		updateViewFromSource();
	}
	
	public void checkParams() throws IllegalArgumentException {
		String height = view.getHeight();
		int heightInt;
		try {
			heightInt = Integer.parseInt(height);
			if (heightInt < 1) {
				throw new IllegalArgumentException("Track height must be a positive integer.");
			}
		} catch (NumberFormatException e) {
			throw new IllegalArgumentException("Invalid track height: " + e.getMessage());
		}
		
		//source name and color are optional
		if (source.getEntityId() == null || source.getVersion() == null) {
			throw new IllegalArgumentException("A source file must be specified.");
		}
	}
	
	public JSONObject toJsonObject() {
		updateFromView();
		return source.toJsonObject();
	}
	
	@Override
	public void entitySelected(Reference ref) {
		source.setEntity(null, null);
		source.setSourceType(null);
		//determine the source type of the given reference before accepting
		int mask = ENTITY | FILE_HANDLES;
		AsyncCallback<EntityBundle> callback = new AsyncCallback<EntityBundle>() {
			@Override
			public void onSuccess(EntityBundle bundle) {
				try {
					assertFileEntity(bundle.getEntity());
					FileHandle fileHandle = getFileHandle(bundle.getFileHandles());
					SourceType newSourceType = getSourceType(fileHandle.getFileName());
					String newEntityId = bundle.getEntity().getId();
					Long newVersion = ((FileEntity)bundle.getEntity()).getVersionNumber();
					source.setEntity(newEntityId, newVersion);
					BiodallianceWidget.updateSourceURIs(source);
					source.setSourceType(newSourceType);
					view.setEntityFinderText(getEntityFinderText(newEntityId, newVersion));
					entityFinder.hide();
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
	public static SourceType getSourceType(String fileName) throws IllegalArgumentException{
		if (fileName != null) {
			int lastDot = fileName.lastIndexOf(".");
			if (lastDot > -1) {
				String extension = fileName.substring(lastDot).toLowerCase();
				if (".bw".equals(extension) || "bigwig".equals(extension)) {
					return SourceType.BIGWIG;
				}
				//else
				if (".vcf".equals(extension) || fileName.toLowerCase().endsWith(".vcf.gz")) {
					return SourceType.VCF;
				}
				//else
				if (".bed".equals(extension) || fileName.toLowerCase().endsWith(".bed.gz")) {
					return SourceType.BED;
				}

			}
		}
		throw new IllegalArgumentException("Unsupported source file type.");
	}
	
	@Override
	public void indexEntitySelected(Reference ref) {
		source.setIndexEntity(null, null);
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
					indexEntityFinder.hide();
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
	public void entityPickerClicked() {
		entityFinder.show();
	}
	
	@Override
	public void indexEntityPickerClicked() {
		indexEntityFinder.show();
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
		source.setHeightPx(Integer.parseInt(view.getHeight()));
		//entity id and version are pushed back from the view (on selection), so we don't need to update here
	}
	
	private void updateViewFromSource() {
		view.setSourceName(source.getSourceName());
		view.setEntityFinderText(getEntityFinderText(source.getEntityId(), source.getVersion()));
		view.setIndexEntityFinderText(getEntityFinderText(source.getIndexEntityId(), source.getIndexVersion()));
		view.setColor(source.getStyleColor());
		view.setHeight(Integer.toString(source.getHeightPx()));
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
	
	@Override
	public void deleteClicked() {
		handler.delete(this);
	}
	
	@Override
	public void moveDownClicked() {
		handler.moveDown(this);
	}
	
	@Override
	public void moveUpClicked() {
		handler.moveUp(this);
	}
	
	public BiodallianceSource getBiodallianceSource() {
		return source;
	}
	
	public void setMoveUpEnabled(boolean enabled) {
		view.setMoveUpEnabled(enabled);
	}
	public void setMoveDownEnabled(boolean enabled) {
		view.setMoveDownEnabled(enabled);
	}
}
