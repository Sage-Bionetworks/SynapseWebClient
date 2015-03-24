package org.sagebionetworks.web.client.widget.entity;

import org.sagebionetworks.repo.model.Entity;
import org.sagebionetworks.repo.model.EntityBundle;
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
import org.sagebionetworks.web.client.widget.pagination.DetailedPaginationWidget;
import org.sagebionetworks.web.client.widget.pagination.PageChangeListener;
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
public class FileHistoryWidget implements FileHistoryWidgetView.Presenter, IsWidget, PageChangeListener {
	
	private FileHistoryWidgetView view;
	private EntityBundle bundle;
	private EntityUpdatedHandler entityUpdatedHandler;
	private SynapseClientAsync synapseClient;
	private NodeModelCreator nodeModelCreator;
	private JSONObjectAdapter jsonObjectAdapter;
	private GlobalApplicationState globalApplicationState;
	private AuthenticationController authenticationController;
	
	private static final Integer VERSION_LIMIT = 100;
	
	private DetailedPaginationWidget paginationWidget;
	private boolean canEdit;
	
	@Inject
	public FileHistoryWidget(FileHistoryWidgetView view, NodeModelCreator nodeModelCreator,
			 SynapseClientAsync synapseClient, JSONObjectAdapter jsonObjectAdapter, GlobalApplicationState globalApplicationState, AuthenticationController authenticationController,
			 DetailedPaginationWidget paginationWidget) {
		super();
		this.nodeModelCreator = nodeModelCreator;
		this.synapseClient = synapseClient;
		this.jsonObjectAdapter = jsonObjectAdapter;
		this.view = view;
		this.globalApplicationState = globalApplicationState;
		this.authenticationController = authenticationController;
		this.paginationWidget = paginationWidget;
		view.setPaginationWidget(paginationWidget.asWidget());
		this.view.setPresenter(this);
	}
	
	public void setEntityBundle(EntityBundle bundle, Long versionNumber) {
		this.bundle = bundle;
		this.canEdit = bundle.getPermissions().getCanCertifiedUserEdit();
		view.setEntityBundle(bundle.getEntity(), versionNumber != null);
		//initialize versions
		onPageChange(0L);
	}

	@Override
	public void editCurrentVersionInfo(final Long version, final String comment) {
		//SWC-771. The current bundle may be pointing to an old version (not the current version).  
		//First ask for the current version of the entity
		synapseClient.getEntity(bundle.getEntity().getId(), new AsyncCallback<EntityWrapper>() {
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

	private void editCurrentVersionInfo(Entity entity, Long version, String comment) throws JSONObjectAdapterException {
		if (entity instanceof Versionable) {
			final Versionable vb = (Versionable)entity;
			if (version != null && version.equals(vb.getVersionLabel()) &&
				comment != null && comment.equals(vb.getVersionComment())) {
				view.showInfo("Version Info Unchanged", "You didn't change anything about the version info.");
				return;
			}
			String versionLabel = null;
			if (version!=null)
				versionLabel = version.toString();
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
	public void deleteVersion(final Long versionNumber) {
		synapseClient.deleteEntityVersionById(bundle.getEntity().getId(), versionNumber, new AsyncCallback<Void>() {
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
				view.showInfo("Version deleted", "Version "+ versionNumber + " of " + bundle.getEntity().getId() + " " + DisplayConstants.LABEL_DELETED);
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
	
	@Override
	public void onPageChange(final Long newOffset) {
		view.clearVersions();
		// TODO: If we ever change the offset api to actually take 0 as a valid
		// offset, then we need to remove "+1"
		synapseClient.getEntityVersions(bundle.getEntity().getId(), newOffset.intValue() + 1, VERSION_LIMIT,
				new AsyncCallback<String>() {
					@Override
					public void onSuccess(String result) {
						PaginatedResults<VersionInfo> paginatedResults;
						try {
							paginatedResults = nodeModelCreator.createPaginatedResults(result, VersionInfo.class);
							paginationWidget.configure(VERSION_LIMIT.longValue(), newOffset + 1, paginatedResults.getTotalNumberOfResults(), FileHistoryWidget.this);
							for (VersionInfo versionInfo : paginatedResults.getResults()) {
								view.addVersion(versionInfo, canEdit);
							}
						} catch (JSONObjectAdapterException e) {							
							onFailure(new UnknownErrorException(DisplayConstants.ERROR_INCOMPATIBLE_CLIENT_VERSION));
						}
					}

					@Override
					public void onFailure(Throwable caught) {
						view.showErrorMessage(caught.getMessage());
					}
				});
	}

}
