package org.sagebionetworks.web.client.widget.entity;

import org.sagebionetworks.repo.model.Entity;
import org.sagebionetworks.repo.model.VersionInfo;
import org.sagebionetworks.repo.model.Versionable;
import org.sagebionetworks.schema.adapter.JSONObjectAdapter;
import org.sagebionetworks.schema.adapter.JSONObjectAdapterException;
import org.sagebionetworks.web.client.DisplayConstants;
import org.sagebionetworks.web.client.DisplayUtils;
import org.sagebionetworks.web.client.GlobalApplicationState;
import org.sagebionetworks.web.client.SynapseClientAsync;
import org.sagebionetworks.web.client.events.EntityUpdatedEvent;
import org.sagebionetworks.web.client.events.EntityUpdatedHandler;
import org.sagebionetworks.web.client.security.AuthenticationController;
import org.sagebionetworks.web.client.transform.NodeModelCreator;
import org.sagebionetworks.repo.model.EntityBundle;
import org.sagebionetworks.web.shared.EntityWrapper;
import org.sagebionetworks.web.shared.PaginatedResults;
import org.sagebionetworks.web.shared.exceptions.UnknownErrorException;

import com.google.gwt.user.client.rpc.AsyncCallback;
import com.google.gwt.user.client.ui.IsWidget;
import com.google.gwt.user.client.ui.Widget;
import com.google.inject.Inject;

/**
 * This widget shows the properties and annotations as a non-editable table grid.
 *
 * @author jayhodgson
 */
public class FileHistoryWidget implements FileHistoryWidgetView.Presenter, IsWidget {
	
	private FileHistoryWidgetView view;
	private EntityBundle bundle;
	private EntityUpdatedHandler entityUpdatedHandler;
	private SynapseClientAsync synapseClient;
	private NodeModelCreator nodeModelCreator;
	private JSONObjectAdapter jsonObjectAdapter;
	private GlobalApplicationState globalApplicationState;
	private AuthenticationController authenticationController;
	//the version that we're currently looking at
	private Long currentVersion;
		
	@Inject
	public FileHistoryWidget(FileHistoryWidgetView view, NodeModelCreator nodeModelCreator,
			 SynapseClientAsync synapseClient, JSONObjectAdapter jsonObjectAdapter, GlobalApplicationState globalApplicationState, AuthenticationController authenticationController) {
		super();
		this.nodeModelCreator = nodeModelCreator;
		this.synapseClient = synapseClient;
		this.jsonObjectAdapter = jsonObjectAdapter;
		this.view = view;
		this.globalApplicationState = globalApplicationState;
		this.authenticationController = authenticationController;
		this.view.setPresenter(this);
	}
	

	@Override
	public void loadVersions(String id, final int offset, int limit,
			final AsyncCallback<PaginatedResults<VersionInfo>> asyncCallback) {
		// TODO: If we ever change the offset api to actually take 0 as a valid
		// offset, then we need to remove "+1"
		synapseClient.getEntityVersions(id, offset + 1, limit,
				new AsyncCallback<String>() {
					@Override
					public void onSuccess(String result) {
						PaginatedResults<VersionInfo> paginatedResults;
						try {
							paginatedResults = nodeModelCreator.createPaginatedResults(result, VersionInfo.class);
							asyncCallback.onSuccess(paginatedResults);
						} catch (JSONObjectAdapterException e) {							
							onFailure(new UnknownErrorException(DisplayConstants.ERROR_INCOMPATIBLE_CLIENT_VERSION));
						}
					}

					@Override
					public void onFailure(Throwable caught) {
						asyncCallback.onFailure(caught);
					}
				});
	}
	
	public void setEntityBundle(EntityBundle bundle, Long versionNumber) {
		this.bundle = bundle;
		this.currentVersion = versionNumber;
		view.setEntityBundle(bundle, bundle.getPermissions().getCanChangePermissions(), bundle.getPermissions().getCanCertifiedUserEdit(), versionNumber != null);
	}
	

	@Override
	public void editCurrentVersionInfo(String entityId, final String version, final String comment) {
		//SWC-771. The current bundle may be pointing to an old version (not the current version).  
		//First ask for the current version of the entity
		synapseClient.getEntity(entityId, new AsyncCallback<EntityWrapper>() {
			@Override
			public void onSuccess(EntityWrapper result) {
				try {
					Entity entity = nodeModelCreator.createEntity(result);
					editCurrentVersionInfo(entity, version, comment);
				} catch (JSONObjectAdapterException e) {
					view.showErrorMessage(DisplayConstants.ERROR_INCOMPATIBLE_CLIENT_VERSION);
				}

			}
			@Override
			public void onFailure(Throwable caught) {
				if (!DisplayUtils.handleServiceException(caught,
						globalApplicationState,
						authenticationController.isLoggedIn(), view)) {
					view.showErrorMessage(DisplayConstants.ERROR_UPDATE_FAILED + "\n" + caught.getMessage());
				}

			}
		});
	}

	private void editCurrentVersionInfo(Entity entity, String version, String comment) throws JSONObjectAdapterException {
		if (entity instanceof Versionable) {
			final Versionable vb = (Versionable)entity;
			if (version != null && version.equals(vb.getVersionLabel()) &&
				comment != null && comment.equals(vb.getVersionComment())) {
				view.showInfo("Version Info Unchanged", "You didn't change anything about the version info.");
				return;
			}
			String versionLabel = version;
			if (version == null || version.equals(""))
				versionLabel = null; // Null out the version field if empty so it defaults to number
			vb.setVersionLabel(versionLabel);
			vb.setVersionComment(comment);
			JSONObjectAdapter joa = jsonObjectAdapter.createNew();
			
			vb.writeToJSONObject(joa);
			synapseClient.updateEntity(joa.toJSONString(),
					new AsyncCallback<EntityWrapper>() {
						@Override
						public void onFailure(Throwable caught) {
							if (!DisplayUtils.handleServiceException(
									caught, globalApplicationState,
									authenticationController.isLoggedIn(), view)) {
								view.showErrorMessage(DisplayConstants.ERROR_UPDATE_FAILED
										+ "\n" + caught.getMessage());
							}
						}
						@Override
						public void onSuccess(EntityWrapper result) {
							view.showInfo(DisplayConstants.VERSION_INFO_UPDATED, "Updated " + vb.getName());
							fireEntityUpdatedEvent();
						}
					});
		}
	}
	
	@Override
	public void deleteVersion(final String entityId, final Long versionNumber) {
		synapseClient.deleteEntityVersionById(entityId, versionNumber, new AsyncCallback<Void>() {
			@Override
			public void onFailure(Throwable caught) {
				if (!DisplayUtils.handleServiceException(caught,
						globalApplicationState,
						authenticationController.isLoggedIn(), view)) {
					view.showErrorMessage(DisplayConstants.ERROR_ENTITY_DELETE_FAILURE + "\n" + caught.getMessage());
				}
			}
			@Override
			public void onSuccess(Void result) {
				view.showInfo("Version deleted", "Version "+ versionNumber + " of " + entityId + " " + DisplayConstants.LABEL_DELETED);
				fireEntityUpdatedEvent();
			}
		});
	}
	
	@Override
	public Widget asWidget() {
		// The view is the real widget.
		return view.asWidget();
	}
	
	
	public void fireEntityUpdatedEvent() {
		if (entityUpdatedHandler != null)
			entityUpdatedHandler.onPersistSuccess(new EntityUpdatedEvent());
	}
	
	public void setEntityUpdatedHandler(EntityUpdatedHandler handler) {
		this.entityUpdatedHandler = handler;
	}

}
