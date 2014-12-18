package org.sagebionetworks.web.client.widget.entity.browse;

import org.sagebionetworks.repo.model.Entity;
import org.sagebionetworks.repo.model.Reference;
import org.sagebionetworks.repo.model.VersionInfo;
import org.sagebionetworks.schema.adapter.JSONObjectAdapterException;
import org.sagebionetworks.web.client.DisplayConstants;
import org.sagebionetworks.web.client.DisplayUtils.SelectedHandler;
import org.sagebionetworks.web.client.GlobalApplicationState;
import org.sagebionetworks.web.client.SynapseClientAsync;
import org.sagebionetworks.web.client.security.AuthenticationController;
import org.sagebionetworks.web.client.transform.NodeModelCreator;
import org.sagebionetworks.web.shared.EntityWrapper;
import org.sagebionetworks.web.shared.PaginatedResults;
import org.sagebionetworks.web.shared.exceptions.ForbiddenException;
import org.sagebionetworks.web.shared.exceptions.NotFoundException;
import org.sagebionetworks.web.shared.exceptions.UnknownErrorException;

import com.google.gwt.user.client.rpc.AsyncCallback;
import com.google.inject.Inject;

public class EntityFinder implements EntityFinderView.Presenter {
	
	private EntityFinderView view;	
	private NodeModelCreator nodeModelCreator;
	private SynapseClientAsync synapseClient;
	private boolean showVersions = true;
	private Reference selectedEntity;
	GlobalApplicationState globalApplicationState;
	AuthenticationController authenticationController;
	private SelectedHandler<Reference> selectedHandler;
	
	@Inject
	public EntityFinder(EntityFinderView view,
			NodeModelCreator nodeModelCreator,
			SynapseClientAsync synapseClient,
			GlobalApplicationState globalApplicationState,
			AuthenticationController authenticationController) {
		this.view = view;
		this.nodeModelCreator = nodeModelCreator;
		this.synapseClient = synapseClient;
		this.globalApplicationState = globalApplicationState;
		this.authenticationController = authenticationController;

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
		if (selectedHandler != null)
			selectedHandler.onSelected(selectedEntity);
	}
	public Reference getSelectedEntity() {
		return selectedEntity;
	}

	@Override
	public void lookupEntity(String entityId, final AsyncCallback<Entity> callback) {
		synapseClient.getEntity(entityId, new AsyncCallback<EntityWrapper>() {
			@Override
			public void onSuccess(EntityWrapper result) {
				try {					
					callback.onSuccess(nodeModelCreator.createEntity(result));
				} catch (JSONObjectAdapterException e) {
					view.showErrorMessage(DisplayConstants.ERROR_INCOMPATIBLE_CLIENT_VERSION);
					callback.onFailure(null);
				}
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
		synapseClient.getEntityVersions(entityId, 1, 200, new AsyncCallback<String>() {
			@Override
			public void onSuccess(String result) {
				PaginatedResults<VersionInfo> versions;
				try {
					versions = nodeModelCreator.createPaginatedResults(result, VersionInfo.class);
					view.setVersions(versions.getResults());
				} catch (JSONObjectAdapterException e) {
					onFailure(new UnknownErrorException(DisplayConstants.ERROR_INCOMPATIBLE_CLIENT_VERSION));
				}
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
		view.clear();
		view.show();
	}
	
	@Override
	public void hide() {
		view.hide();
	}
}
