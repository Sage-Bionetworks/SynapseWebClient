package org.sagebionetworks.web.client.widget.entity.browse;

import static org.sagebionetworks.web.client.widget.entity.browse.EntityFilter.ALL;

import java.util.ArrayList;
import java.util.List;

import org.sagebionetworks.repo.model.EntityHeader;
import org.sagebionetworks.repo.model.EntityPath;
import org.sagebionetworks.repo.model.Reference;
import org.sagebionetworks.repo.model.entitybundle.v2.EntityBundle;
import org.sagebionetworks.repo.model.entitybundle.v2.EntityBundleRequest;
import org.sagebionetworks.repo.model.request.ReferenceList;
import org.sagebionetworks.web.client.DisplayConstants;
import org.sagebionetworks.web.client.DisplayUtils.SelectedHandler;
import org.sagebionetworks.web.client.GlobalApplicationState;
import org.sagebionetworks.web.client.SynapseJavascriptClient;
import org.sagebionetworks.web.client.place.Synapse;
import org.sagebionetworks.web.client.security.AuthenticationController;
import org.sagebionetworks.web.client.utils.CallbackP;
import org.sagebionetworks.web.client.widget.entity.controller.SynapseAlert;
import org.sagebionetworks.web.shared.exceptions.NotFoundException;

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
					view.renderComponent(initialContainerId, showVersions, multiSelect, visibleFilter, selectableFilter);
				};
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
		synAlert.clear();
		selectedEntities.clear();
	}

	@Override
	public void okClicked() {
		synAlert.clear();
		// check for valid selection
		if (selectedEntities == null || selectedEntities.isEmpty()) {
			synAlert.showError(DisplayConstants.PLEASE_MAKE_SELECTION);
		} else {
			if (!ALL.equals(visibleFilter)) {
				// fetch the entity for a type check
				ReferenceList rl = new ReferenceList();
				rl.setReferences(selectedEntities);
				lookupEntity(rl, result -> {
					if (validateEntityTypeAgainstFilter(result)) {
						fireEntitiesSelected();
					}
				});
			} else {
				// skip type check if ALL
				fireEntitiesSelected();
			}
		}
	}

	private void fireEntitiesSelected() {
		if (selectedHandler != null) {
			selectedHandler.onSelected(selectedEntities.get(0));
		}
		if (selectedMultiHandler != null) {
			selectedMultiHandler.onSelected(selectedEntities);
		}

	}

	public boolean validateEntityTypeAgainstFilter(List<EntityHeader> list) {
		boolean flag = selectedEntities.size() == visibleFilter.filterForBrowsing(list).size();
		if (!flag) {
			synAlert.showError("Please select a " + visibleFilter.toString().toLowerCase());
		}
		return flag;
	}

	public List<Reference> getSelectedEntity() {
		return selectedEntities;
	}

	@Override
	public void lookupEntity(ReferenceList rl, final CallbackP<List<EntityHeader>> callback) {
		synAlert.clear();
		jsClient.getEntityHeaderBatchFromReferences(rl.getReferences(), new AsyncCallback<ArrayList<EntityHeader>>() {
			@Override
			public void onFailure(Throwable caught) {
				synAlert.handleException(caught);
			}

			@Override
			public void onSuccess(ArrayList<EntityHeader> result) {
				if (result.size() == 0) {
					synAlert.handleException(new NotFoundException());
				} else {
					callback.invoke(result);
				}
			}
		});
	}

	@Override
	public void lookupEntity(String entityId, final CallbackP<List<EntityHeader>> callback) {
		synAlert.clear();
		processEntities(entityId);
		ReferenceList rl = new ReferenceList();
		rl.setReferences(selectedEntities);
		lookupEntity(rl, callback);
	}

	private void processEntities(String entityId) {
		String[] entities = entityId.split("[,\\s]\\s*");
		selectedEntities.clear();
		for (int i = 0; i < entities.length; i++) {
			Reference r = new Reference();
			String target = entities[i].trim();
			if (target.contains(".")) {
				String[] parts = target.split("[.]");
				r.setTargetId(parts[0]);
				r.setTargetVersionNumber(Long.parseLong(parts[1]));
			} else {
				r.setTargetId(target);
			}
			selectedEntities.add(r);
		}
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
