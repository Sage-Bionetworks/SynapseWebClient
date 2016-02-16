package org.sagebionetworks.web.client.widget.entity.browse;

import org.sagebionetworks.repo.model.Entity;
import org.sagebionetworks.repo.model.Reference;
import org.sagebionetworks.repo.model.VersionInfo;
import org.sagebionetworks.web.client.DisplayConstants;
import org.sagebionetworks.web.client.DisplayUtils.SelectedHandler;
import org.sagebionetworks.web.client.GlobalApplicationState;
import org.sagebionetworks.web.client.SynapseClientAsync;
import org.sagebionetworks.web.client.cache.ClientCache;
import org.sagebionetworks.web.client.security.AuthenticationController;
import org.sagebionetworks.web.shared.PaginatedResults;
import org.sagebionetworks.web.shared.exceptions.ForbiddenException;
import org.sagebionetworks.web.shared.exceptions.NotFoundException;

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
	
	@Inject
	public EntityFinder(EntityFinderView view,
			SynapseClientAsync synapseClient,
			GlobalApplicationState globalApplicationState,
			AuthenticationController authenticationController,
			ClientCache cache) {
		this.view = view;
		this.synapseClient = synapseClient;
		this.globalApplicationState = globalApplicationState;
		this.authenticationController = authenticationController;
		this.cache = cache;
		view.setPresenter(this);
	}	

	@SuppressWarnings("unchecked")
	public void clearState() {
		view.clear();
	}

	public void configure(boolean showVersions, SelectedHandler<Reference> handler) {
		this.showVersions = showVersions;
		this.selectedHandler = handler;
	}
	
	@Override
	public void setSelectedEntity(Reference selected) {
		selectedEntity = selected;
	}
	
	@Override
	public void okClicked() {
		//check for valid selection
		if (selectedEntity == null || selectedEntity.getTargetId() == null) {
			view.showErrorMessage(DisplayConstants.PLEASE_MAKE_SELECTION);
		} else if (selectedHandler != null) {
			selectedHandler.onSelected(selectedEntity);
		}
	}
	public Reference getSelectedEntity() {
		return selectedEntity;
	}

	@Override
	public void lookupEntity(String entityId, final AsyncCallback<Entity> callback) {
		synapseClient.getEntity(entityId, new AsyncCallback<Entity>() {
			@Override
			public void onSuccess(Entity result) {
				callback.onSuccess(result);
			}
			@Override
			public void onFailure(Throwable caught) {
				if(caught instanceof NotFoundException) {
					view.showErrorMessage(DisplayConstants.ERROR_NOT_FOUND);
				} else if (caught instanceof ForbiddenException) {
					view.showErrorMessage(DisplayConstants.ERROR_FAILURE_PRIVLEDGES);
				} else {
					view.showErrorMessage(DisplayConstants.ERROR_GENERIC);
				}
				callback.onFailure(caught);
			}
		});
	}

	@Override
	public void loadVersions(String entityId) {
		synapseClient.getEntityVersions(entityId, 1, 200, new AsyncCallback<PaginatedResults<VersionInfo>>() {
			@Override
			public void onSuccess(PaginatedResults<VersionInfo> result) {
				PaginatedResults<VersionInfo> versions = result;
				view.setVersions(versions.getResults());
			}
			@Override
			public void onFailure(Throwable caught) {
				view.showErrorMessage(DisplayConstants.UNABLE_TO_LOAD_VERSIONS);
			}
		});
	}
	
	@Override
	public boolean showVersions() {
		return this.showVersions;
	}
	
	@Override
	public void show() {
		if (!view.isShowing()) {
			view.clear();
			//get the area from the cache, if available
			String areaString = cache.get(ENTITY_FINDER_AREA_KEY);
			//default to browse
			EntityFinderArea area = EntityFinderArea.BROWSE;
			if (areaString != null) {
				area = EntityFinderArea.valueOf(areaString);
			}
			view.initFinderComponents();
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
	
	@Override
	public Widget asWidget() {
		return view.asWidget();
	}
}
