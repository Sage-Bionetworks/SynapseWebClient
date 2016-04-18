package org.sagebionetworks.web.client.widget.entity.browse;

import org.sagebionetworks.repo.model.Entity;
import org.sagebionetworks.repo.model.FileEntity;
import org.sagebionetworks.repo.model.Folder;
import org.sagebionetworks.repo.model.Project;
import org.sagebionetworks.repo.model.Reference;
import org.sagebionetworks.repo.model.VersionInfo;
import org.sagebionetworks.web.client.DisplayConstants;
import org.sagebionetworks.web.client.DisplayUtils.SelectedHandler;
import org.sagebionetworks.web.client.GlobalApplicationState;
import org.sagebionetworks.web.client.SynapseClientAsync;
import org.sagebionetworks.web.client.cache.ClientCache;
import org.sagebionetworks.web.client.security.AuthenticationController;
import org.sagebionetworks.web.client.widget.entity.controller.SynapseAlert;
import org.sagebionetworks.web.shared.PaginatedResults;

import com.google.gwt.user.client.rpc.AsyncCallback;
import com.google.gwt.user.client.ui.IsWidget;
import com.google.gwt.user.client.ui.Widget;
import com.google.inject.Inject;

public class EntityFinder implements EntityFinderView.Presenter, IsWidget {
	public static final String ENTITY_FINDER_AREA_KEY = "org.sagebionetworks.web.client.entityfinder.area";
	private EntityFinderView view;
	private SynapseClientAsync synapseClient;
	private boolean showVersions = true;
	private Reference selectedEntity;
	GlobalApplicationState globalApplicationState;
	AuthenticationController authenticationController;
	ClientCache cache;
	private SelectedHandler<Reference> selectedHandler;
	private EntityFilter filter;
	private SynapseAlert synAlert;
	@Inject
	public EntityFinder(EntityFinderView view,
			SynapseClientAsync synapseClient,
			GlobalApplicationState globalApplicationState,
			AuthenticationController authenticationController,
			ClientCache cache,
			SynapseAlert synAlert) {
		this.view = view;
		this.synapseClient = synapseClient;
		this.globalApplicationState = globalApplicationState;
		this.authenticationController = authenticationController;
		this.cache = cache;
		this.synAlert = synAlert;
		view.setPresenter(this);
		view.setSynAlert(synAlert.asWidget());
	}	

	@SuppressWarnings("unchecked")
	public void clearState() {
		view.clear();
	}

	public void configure(boolean showVersions, SelectedHandler<Reference> handler) {
		configure(EntityFilter.ALL, showVersions, handler);
	}
	

	public void configure(EntityFilter filter, boolean showVersions, SelectedHandler<Reference> handler) {
		this.filter = filter;
		this.showVersions = showVersions;
		this.selectedHandler = handler;
	}
	
	@Override
	public void setSelectedEntity(Reference selected) {
		synAlert.clear();
		selectedEntity = selected;
	}
	
	@Override
	public void okClicked() {
		synAlert.clear();
		//check for valid selection
		if (selectedEntity == null || selectedEntity.getTargetId() == null) {
			synAlert.showError(DisplayConstants.PLEASE_MAKE_SELECTION);
		} else {
			// fetch the entity for a type check
			lookupEntity(selectedEntity.getTargetId(), new AsyncCallback<Entity>() {
				@Override
				public void onFailure(Throwable caught) {
					synAlert.handleException(caught);
				}
				public void onSuccess(Entity result) {
					if (validateEntityTypeAgainstFilter(result)) {
						if (selectedHandler != null) {
							selectedHandler.onSelected(selectedEntity);
						}
					}
				};
			});
		}
	}
	
	public boolean validateEntityTypeAgainstFilter(Entity entity) {
		boolean isCorrectType = true;
		synAlert.clear();
		switch (filter) {
			case CONTAINER:
				isCorrectType = entity instanceof Project || entity instanceof Folder;
				break;
			case PROJECT:
				isCorrectType = entity instanceof Project;
				break;
			case FOLDER:
				isCorrectType = entity instanceof Folder;
				break;
			case FILE:
				isCorrectType = entity instanceof FileEntity;
				break;
			case ALL:
			default:
				break;
		}
		if (!isCorrectType) {
			synAlert.showError("Please select a " + filter.toString().toLowerCase());
		}
		return isCorrectType;
	}
	
	public Reference getSelectedEntity() {
		return selectedEntity;
	}

	@Override
	public void lookupEntity(String entityId, final AsyncCallback<Entity> callback) {
		synAlert.clear();
		synapseClient.getEntity(entityId, new AsyncCallback<Entity>() {
			@Override
			public void onSuccess(Entity result) {
				callback.onSuccess(result);
			}
			@Override
			public void onFailure(Throwable caught) {
				synAlert.handleException(caught);
				callback.onFailure(caught);
			}
		});
	}

	@Override
	public void loadVersions(String entityId) {
		synAlert.clear();
		synapseClient.getEntityVersions(entityId, 1, 200, new AsyncCallback<PaginatedResults<VersionInfo>>() {
			@Override
			public void onSuccess(PaginatedResults<VersionInfo> result) {
				PaginatedResults<VersionInfo> versions = result;
				view.setVersions(versions.getResults());
			}
			@Override
			public void onFailure(Throwable caught) {
				synAlert.handleException(caught);
			}
		});
	}
	
	@Override
	public boolean showVersions() {
		return this.showVersions;
	}
	
	@Override
	public void show() {
		view.clear();
		//get the area from the cache, if available
		String areaString = cache.get(ENTITY_FINDER_AREA_KEY);
		//default to browse
		EntityFinderArea area = EntityFinderArea.BROWSE;
		if (areaString != null) {
			area = EntityFinderArea.valueOf(areaString);
		}
		view.initFinderComponents(filter);
		switch (area) {
			case SEARCH:
				view.setSearchAreaVisible();
				break;
			case SYNAPSE_ID:
				view.setSynapseIdAreaVisible();
				break;
			case BROWSE:
			default:
				view.setBrowseAreaVisible();
				break;
		}
		view.show();
	}
	
	@Override
	public void hide() {
		//save area
		EntityFinderArea area = view.getCurrentArea();
		if (area != null) {
			cache.put(ENTITY_FINDER_AREA_KEY, area.toString());	
		}
		
		view.hide();
	}
	
	public void showError(String error) {
		synAlert.showError(error);
	}
	
	@Override
	public Widget asWidget() {
		return view.asWidget();
	}
}
