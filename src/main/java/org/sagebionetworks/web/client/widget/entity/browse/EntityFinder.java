package org.sagebionetworks.web.client.widget.entity.browse;

import static org.sagebionetworks.web.client.widget.entity.browse.EntityFilter.ALL;
import java.util.ArrayList;
import java.util.List;
import org.sagebionetworks.repo.model.EntityHeader;
import org.sagebionetworks.repo.model.Reference;
import org.sagebionetworks.repo.model.VersionInfo;
import org.sagebionetworks.repo.model.request.ReferenceList;
import org.sagebionetworks.web.client.DisplayConstants;
import org.sagebionetworks.web.client.DisplayUtils.SelectedHandler;
import org.sagebionetworks.web.client.GlobalApplicationState;
import org.sagebionetworks.web.client.SynapseJavascriptClient;
import org.sagebionetworks.web.client.cache.ClientCache;
import org.sagebionetworks.web.client.security.AuthenticationController;
import org.sagebionetworks.web.client.utils.CallbackP;
import org.sagebionetworks.web.client.widget.entity.controller.SynapseAlert;
import org.sagebionetworks.web.shared.WebConstants;
import org.sagebionetworks.web.shared.exceptions.NotFoundException;
import com.google.gwt.user.client.rpc.AsyncCallback;
import com.google.gwt.user.client.ui.IsWidget;
import com.google.gwt.user.client.ui.Widget;
import com.google.inject.Inject;

public class EntityFinder implements EntityFinderView.Presenter, IsWidget {
	public static final String ENTITY_FINDER_AREA_KEY = "org.sagebionetworks.web.client.entityfinder.area";
	private EntityFinderView view;
	private boolean showVersions = true;
	private List<Reference> selectedEntities;
	GlobalApplicationState globalApplicationState;
	AuthenticationController authenticationController;
	ClientCache cache;
	private SelectedHandler<Reference> selectedHandler;
	private SelectedHandler<List<Reference>> selectedMultiHandler;
	private EntityFilter filter;
	private SynapseAlert synAlert;
	private SynapseJavascriptClient jsClient;

	@Inject
	public EntityFinder(EntityFinderView view, GlobalApplicationState globalApplicationState, AuthenticationController authenticationController, ClientCache cache, SynapseAlert synAlert, SynapseJavascriptClient jsClient) {
		this.view = view;
		this.globalApplicationState = globalApplicationState;
		this.authenticationController = authenticationController;
		this.cache = cache;
		this.synAlert = synAlert;
		this.jsClient = jsClient;
		this.selectedEntities = new ArrayList<Reference>();
		view.setPresenter(this);
		view.setSynAlert(synAlert.asWidget());
	}

	public void clearState() {
		view.clear();
	}

	public void configure(boolean showVersions, SelectedHandler<Reference> handler) {
		configure(ALL, showVersions, handler);
	}

	public void configure(EntityFilter filter, boolean showVersions, SelectedHandler<Reference> handler) {
		this.filter = filter;
		this.showVersions = showVersions;
		this.selectedHandler = handler;
		selectedEntities.clear();
		view.setMultiVisible(false);
	}

	public void configureMulti(EntityFilter filter, boolean showVersions, SelectedHandler<List<Reference>> handler) {
		this.filter = filter;
		this.showVersions = showVersions;
		this.selectedMultiHandler = handler;
		selectedEntities.clear();
		view.setMultiVisible(true);
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
			if (!ALL.equals(filter)) {
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
		boolean flag = selectedEntities.size() == filter.filterForBrowsing(list).size();
		if (!flag) {
			synAlert.showError("Please select a " + filter.toString().toLowerCase());
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
	public void loadVersions(String entityId) {
		synAlert.clear();
		jsClient.getEntityVersions(entityId, WebConstants.ZERO_OFFSET.intValue(), 200, new AsyncCallback<List<VersionInfo>>() {
			@Override
			public void onSuccess(List<VersionInfo> results) {
				view.setVersions(results);
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
		// get the area from the cache, if available
		String areaString = cache.get(ENTITY_FINDER_AREA_KEY);
		// default to browse
		EntityFinderArea area = EntityFinderArea.BROWSE;
		if (areaString != null) {
			area = EntityFinderArea.valueOf(areaString.toUpperCase());
		}
		view.initFinderComponents(filter);
		switch (area) {
			case SEARCH:
				view.setSearchAreaVisible();
				break;
			case SYNAPSE_ID:
				view.setSynapseIdAreaVisible();
				break;
			case SYNAPSE_MULTI_ID:
				view.setSynapseMultiIdAreaVisible();
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
		// save area
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
