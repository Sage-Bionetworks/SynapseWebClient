package org.sagebionetworks.web.client.widget.entity.browse;

import static org.sagebionetworks.web.client.widget.entity.browse.EntityFilter.ALL;

import java.util.ArrayList;
import java.util.List;

import org.sagebionetworks.repo.model.EntityHeader;
import org.sagebionetworks.repo.model.EntityPath;
import org.sagebionetworks.repo.model.Reference;
import org.sagebionetworks.repo.model.entitybundle.v2.EntityBundle;
import org.sagebionetworks.repo.model.entitybundle.v2.EntityBundleRequest;
import org.sagebionetworks.web.client.DisplayConstants;
import org.sagebionetworks.web.client.DisplayUtils.SelectedHandler;
import org.sagebionetworks.web.client.GlobalApplicationState;
import org.sagebionetworks.web.client.SynapseJavascriptClient;
import org.sagebionetworks.web.client.place.Synapse;
import org.sagebionetworks.web.client.security.AuthenticationController;
import org.sagebionetworks.web.client.widget.entity.controller.SynapseAlert;

import com.google.gwt.place.shared.Place;
import com.google.gwt.user.client.rpc.AsyncCallback;
import com.google.gwt.user.client.ui.IsWidget;
import com.google.gwt.user.client.ui.Widget;
import com.google.inject.Inject;

public class EntityFinderV2Impl implements EntityFinder, EntityFinderV2View.Presenter, IsWidget {
	private EntityFinderV2View view;
	private boolean showVersions;
	private boolean multiSelect;
	private List<Reference> selectedEntities;
	String initialContainerId;
	GlobalApplicationState globalApplicationState;
	AuthenticationController authenticationController;
	private SelectedHandler<Reference> selectedHandler;
	private SelectedHandler<List<Reference>> selectedMultiHandler;
	private EntityFilter visibleFilter;
	private EntityFilter selectableFilter;
	private SynapseAlert synAlert;
	private SynapseJavascriptClient jsClient;


	@Inject
	public EntityFinderV2Impl(EntityFinderV2View view, GlobalApplicationState globalApplicationState, AuthenticationController authenticationController, SynapseAlert synAlert, SynapseJavascriptClient jsClient) {
		this.view = view;
		this.globalApplicationState = globalApplicationState;
		this.authenticationController = authenticationController;
		this.synAlert = synAlert;
		this.jsClient = jsClient;
		this.selectedEntities = new ArrayList<>();
		view.setPresenter(this);
		view.setSynAlert(synAlert.asWidget());
	}

	@Override
	public void configure(boolean showVersions, SelectedHandler<Reference> handler) {
		configure(ALL, showVersions, handler);
	}

	@Override
	public void configure(EntityFilter selectable, boolean showVersions, SelectedHandler<Reference> handler) {
		configure(ALL, selectable, showVersions, handler);
	}

	@Override
	public void configure(EntityFilter visible, EntityFilter selectable, boolean showVersions, SelectedHandler<Reference> handler) {
		this.showVersions = showVersions;
		this.visibleFilter = visible;
		this.selectableFilter = selectable;
		this.selectedHandler = handler;
		this.multiSelect = false;
		selectedEntities.clear();
		loadCurrentContext();
	}

	@Override
	public void configureMulti(boolean showVersions, SelectedHandler<List<Reference>> handler) {
		configureMulti(ALL, showVersions, handler);
	}

	@Override
	public void configureMulti(EntityFilter filter, boolean showVersions, SelectedHandler<List<Reference>> handler) {
		configureMulti(ALL, filter, showVersions, handler);
	}

	@Override
	public void configureMulti(EntityFilter visible, EntityFilter selectable, boolean showVersions, SelectedHandler<List<Reference>> handler) {
		this.showVersions = showVersions;
		this.visibleFilter = visible;
		this.selectableFilter = selectable;
		this.selectedMultiHandler = handler;
		this.multiSelect = true;
		selectedEntities.clear();
		loadCurrentContext();
	}


	public void loadCurrentContext() {
		// get the entity path, and ask for each entity to add to the tree
		Place currentPlace = globalApplicationState.getCurrentPlace();
		boolean isSynapsePlace = currentPlace instanceof Synapse;
		if (isSynapsePlace) {
			String entityId = ((Synapse) currentPlace).getEntityId();
			EntityBundleRequest bundleRequest = new EntityBundleRequest();
			bundleRequest.setIncludeEntityPath(true);
			jsClient.getEntityBundle(entityId, bundleRequest, new AsyncCallback<EntityBundle>() {
				@Override
				public void onFailure(Throwable caught) {
					showError(caught.getMessage());
				}

				public void onSuccess(EntityBundle result) {
					EntityPath path = result.getPath();
					List<EntityHeader> pathHeaders = path.getPath();
					if (pathHeaders.size() > 1) {
						initialContainerId = pathHeaders.get(1).getId();
					}
					renderComponent();
				}
			});
		}
	}


	@Override
	public void setSelectedEntity(Reference selected) {
		synAlert.clear();
		selectedEntities.clear();
		selectedEntities.add(selected);
	}

	@Override
	public void setSelectedEntities(List<Reference> selected) {
		synAlert.clear();
		selectedEntities.clear();
		selectedEntities.addAll(selected);
	}

	@Override
	public void clearSelectedEntities() {
		selectedEntities.clear();
	}

	@Override
	public void okClicked() {
		synAlert.clear();
		// check for valid selection
		if (selectedEntities == null || selectedEntities.isEmpty()) {
			synAlert.showError(DisplayConstants.PLEASE_MAKE_SELECTION);
		} else {
			fireEntitiesSelected();
		}
	}

	@Override
	public void renderComponent() {
		view.renderComponent(initialContainerId, showVersions, multiSelect, visibleFilter, selectableFilter);
	}

	private void fireEntitiesSelected() {
		if (selectedHandler != null) {
			selectedHandler.onSelected(selectedEntities.get(0));
		}
		if (selectedMultiHandler != null) {
			selectedMultiHandler.onSelected(selectedEntities);
		}
	}

	@Override
	public void show() {
		view.clear();
		view.show();
	}

	@Override
	public void hide() {
		// save area
		view.hide();
	}

	@Override
	public void clearState() {
		view.clear();
	}

	public void showError(String error) {
		synAlert.showError(error);
	}

	@Override
	public Widget asWidget() {
		return view.asWidget();
	}
}
