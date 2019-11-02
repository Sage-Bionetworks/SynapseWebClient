package org.sagebionetworks.web.client.widget.biodalliance13.editor;

import org.gwtvisualizationwrappers.client.biodalliance13.BiodallianceSource;
import org.gwtvisualizationwrappers.client.biodalliance13.BiodallianceSource.SourceType;
import org.sagebionetworks.repo.model.Entity;
import org.sagebionetworks.repo.model.FileEntity;
import org.sagebionetworks.repo.model.Reference;
import org.sagebionetworks.repo.model.entitybundle.v2.EntityBundle;
import org.sagebionetworks.repo.model.entitybundle.v2.EntityBundleRequest;
import org.sagebionetworks.web.client.DisplayUtils.SelectedHandler;
import org.sagebionetworks.web.client.SynapseJavascriptClient;
import org.sagebionetworks.web.client.utils.Callback;
import org.sagebionetworks.web.client.widget.SelectableListItem;
import org.sagebionetworks.web.client.widget.biodalliance13.BiodallianceWidget;
import org.sagebionetworks.web.client.widget.entity.browse.EntityFilter;
import org.sagebionetworks.web.client.widget.entity.browse.EntityFinder;
import com.google.gwt.json.client.JSONObject;
import com.google.gwt.user.client.rpc.AsyncCallback;
import com.google.gwt.user.client.ui.IsWidget;
import com.google.gwt.user.client.ui.Widget;
import com.google.inject.Inject;

/**
 * Source object represents a Biodalliance track source. The view is optional, and only needs to be
 * injected if showing an editor for the source object.
 */
public class BiodallianceSourceEditor implements BiodallianceSourceEditorView.Presenter, IsWidget, SelectableListItem {
	// view, may not be set if only using this class to pass data around
	BiodallianceSourceEditorView view;
	private BiodallianceSource source;
	EntityFinder entityFinder, indexEntityFinder;
	Callback selectionChangedCallback;
	SynapseJavascriptClient jsClient;

	@Inject
	public BiodallianceSourceEditor(BiodallianceSourceEditorView view, EntityFinder entityFinder, EntityFinder indexEntityFinder, BiodallianceSource source, SynapseJavascriptClient jsClient) {
		this.view = view;
		this.entityFinder = entityFinder;
		this.indexEntityFinder = indexEntityFinder;
		this.source = source;
		this.jsClient = jsClient;

		view.setPresenter(this);
		entityFinder.configure(EntityFilter.ALL_BUT_LINK, true, new SelectedHandler<Reference>() {
			@Override
			public void onSelected(Reference selected) {
				entitySelected(selected);
			}
		});

		indexEntityFinder.configure(EntityFilter.ALL_BUT_LINK, true, new SelectedHandler<Reference>() {
			@Override
			public void onSelected(Reference selected) {
				indexEntitySelected(selected);
			}
		});
		updateViewFromSource();
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

		// source name and color are optional
		if (source.getEntityId() == null || source.getVersion() == null) {
			throw new IllegalArgumentException("A source file must be specified.");
		}

		// if a tabix source, then an index file is required
		if (SourceType.VCF.equals(source.getSourceType()) || SourceType.BED.equals(source.getSourceType())) {
			if (source.getIndexEntityId() == null || source.getIndexVersion() == null) {
				throw new IllegalArgumentException("An index file must be specified for a tabix source.");
			}
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
		// determine the source type of the given reference before accepting
		EntityBundleRequest bundleRequest = new EntityBundleRequest();
		bundleRequest.setIncludeEntity(true);
		bundleRequest.setIncludeFileName(true);
		AsyncCallback<EntityBundle> callback = new AsyncCallback<EntityBundle>() {
			@Override
			public void onSuccess(EntityBundle bundle) {
				try {
					assertFileEntity(bundle.getEntity());
					SourceType newSourceType = getSourceType(bundle.getFileName());
					String newEntityId = bundle.getEntity().getId();
					Long newVersion = ((FileEntity) bundle.getEntity()).getVersionNumber();
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
				entityFinder.showError(caught.getMessage());
			}
		};
		if (ref.getTargetVersionNumber() == null) {
			jsClient.getEntityBundle(ref.getTargetId(), bundleRequest, callback);
		} else {
			jsClient.getEntityBundleForVersion(ref.getTargetId(), ref.getTargetVersionNumber(), bundleRequest, callback);
		}
	}

	@Override
	public void indexEntitySelected(Reference ref) {
		source.setIndexEntity(null, null);
		EntityBundleRequest bundleRequest = new EntityBundleRequest();
		bundleRequest.setIncludeEntity(true);
		bundleRequest.setIncludeFileName(true);
		AsyncCallback<EntityBundle> callback = new AsyncCallback<EntityBundle>() {
			@Override
			public void onSuccess(EntityBundle bundle) {
				try {
					assertFileEntity(bundle.getEntity());
					assertIndexFile(bundle.getFileName());
					String newIndexEntityId = bundle.getEntity().getId();
					Long newIndexVersion = ((FileEntity) bundle.getEntity()).getVersionNumber();
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
				indexEntityFinder.showError(caught.getMessage());
			}
		};
		if (ref.getTargetVersionNumber() == null) {
			jsClient.getEntityBundle(ref.getTargetId(), bundleRequest, callback);
		} else {
			jsClient.getEntityBundleForVersion(ref.getTargetId(), ref.getTargetVersionNumber(), bundleRequest, callback);
		}
	}


	/**
	 * Based on the file extension only, return the source type. If it can't be determined, then this
	 * method will return null.
	 * 
	 * @param fileName
	 * @return
	 */
	public SourceType getSourceType(String fileName) throws IllegalArgumentException {
		if (fileName != null) {
			int lastDot = fileName.lastIndexOf(".");
			if (lastDot > -1) {
				String extension = fileName.substring(lastDot).toLowerCase();
				if (".bw".equals(extension) || ".bigwig".equals(extension)) {
					return SourceType.BIGWIG;
				}
				// else
				if (".vcf".equals(extension) || fileName.toLowerCase().endsWith(".vcf.gz")) {
					return SourceType.VCF;
				}
				// else
				if (".bed".equals(extension) || fileName.toLowerCase().endsWith(".bed.gz")) {
					return SourceType.BED;
				}
			}
		}
		throw new IllegalArgumentException("Unsupported source file type.");
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

	private void updateFromView() {
		source.setSourceName(view.getSourceName());
		source.setStyleColor(view.getColor());
		source.setHeightPx(Integer.parseInt(view.getHeight()));
		// entity id and version are pushed back from the view (on selection), so we don't need to update
		// here
	}

	public void updateViewFromSource() {
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
			entityFinderText += "." + version;
		}
		return entityFinderText;
	}

	@Override
	public Widget asWidget() {
		return view.asWidget();
	}

	public BiodallianceSource getBiodallianceSource() {
		return source;
	}

	public boolean isSelected() {
		return view.isSelected();
	}

	public void setSelected(boolean selected) {
		view.setSelected(selected);
	}

	public void setSelectionChangedCallback(Callback selectionChangedCallback) {
		this.selectionChangedCallback = selectionChangedCallback;
	}

	@Override
	public void onSelectionChanged() {
		if (selectionChangedCallback != null) {
			selectionChangedCallback.invoke();
		}
	}
}
