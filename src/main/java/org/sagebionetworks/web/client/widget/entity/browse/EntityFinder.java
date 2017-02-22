package org.sagebionetworks.web.client.widget.entity.browse;

import java.util.ArrayList;
import java.util.List;

import org.sagebionetworks.repo.model.Entity;
import org.sagebionetworks.repo.model.EntityHeader;
import org.sagebionetworks.repo.model.FileEntity;
import org.sagebionetworks.repo.model.Folder;
import org.sagebionetworks.repo.model.Project;
import org.sagebionetworks.repo.model.Reference;
import org.sagebionetworks.repo.model.VersionInfo;
import org.sagebionetworks.repo.model.request.ReferenceList;
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
	private ReferenceList selectedMultiEntity;
	GlobalApplicationState globalApplicationState;
	AuthenticationController authenticationController;
	ClientCache cache;
	private SelectedHandler<Reference> selectedHandler;
	private SelectedHandler<List<Reference>> selectedMultiHandler;
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
	
	public void configureMultiSelect(EntityFilter filter, SelectedHandler<List<Reference>> handler) {
		this.filter = filter;
		this.selectedMultiHandler = handler;
		this.selectedMultiEntity = new ReferenceList();
		view.setMultiVisible(true);
	}

	public void configure(EntityFilter filter, boolean showVersions, SelectedHandler<Reference> handler) {
		this.filter = filter;
		this.showVersions = showVersions;
		this.selectedHandler = handler;
		this.selectedMultiEntity = new ReferenceList();
	}
	
	@Override
	public void setSelectedEntity(Reference selected) {
		synAlert.clear();
		List<Reference> list = new ArrayList<Reference>();
		list.add(selected);
		setSelectedEntities(list);
	}
	
	@Override
	public void setSelectedEntities(List<Reference> selectedIds) {
		synAlert.clear();
		selectedMultiEntity.setReferences(selectedIds);
	}
	
	@Override
	public void okClicked() {
		synAlert.clear();
		if (selectedMultiEntity.getReferences().isEmpty()) {
			synAlert.showError(DisplayConstants.PLEASE_MAKE_SELECTION);
		} else {
			synapseClient.getEntityHeaderBatch(selectedMultiEntity, new AsyncCallback<PaginatedResults<EntityHeader>>() {
				@Override
				public void onFailure(Throwable caught) {
					synAlert.handleException(caught);
				}
				public void onSuccess(PaginatedResults<EntityHeader> result) {
					//if (validateEntityTypeAgainstFilter(null)) {
						if (selectedHandler != null) {
							selectedHandler.onSelected(selectedMultiEntity.getReferences().get(0));
						} else if (selectedMultiHandler != null) {
							selectedMultiHandler.onSelected(selectedMultiEntity.getReferences());
						}
					//}
				};
			});
		}
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
	public void lookupEntities(String value, final AsyncCallback<PaginatedResults<EntityHeader>> callback) {
		synAlert.clear();
		List<Reference> list = getReferences(value);
		this.selectedMultiEntity.setReferences(list);
		synapseClient.getEntityHeaderBatch(selectedMultiEntity, new AsyncCallback<PaginatedResults<EntityHeader>>() {
			@Override
			public void onFailure(Throwable caught) {
				synAlert.handleException(caught);
				callback.onFailure(caught);
			}
			public void onSuccess(PaginatedResults<EntityHeader> result) {
				callback.onSuccess(result);
			};
		});
	}
	
	private List<Reference> getReferences(String refs) {
		List<Reference> rList = new ArrayList<Reference>();
		String[] entities = refs.split(",");
		for (int i = 0; i < entities.length; i++) {
			String s = entities[i].trim();
			Reference r = new Reference();
			if (s.contains(".")) {
				String[] parts = s.split(".");
				r.setTargetId(parts[0]);
				r.setTargetVersionNumber(Long.getLong(parts[1]));
			} else {
				r.setTargetId(s);				
			}
			rList.add(r);
		}
		return rList;
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
			case MULTI:
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
	
	public void setMultiVisible(boolean b) {
		view.setMultiVisible(b);
	}

}
