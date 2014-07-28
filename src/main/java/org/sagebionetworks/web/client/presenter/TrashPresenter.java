package org.sagebionetworks.web.client.presenter;

import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.sagebionetworks.web.shared.PaginatedResults;
import org.sagebionetworks.web.shared.exceptions.NotFoundException;
import org.sagebionetworks.repo.model.TrashedEntity;
import org.sagebionetworks.schema.adapter.JSONObjectAdapterException;
import org.sagebionetworks.web.client.DisplayConstants;
import org.sagebionetworks.web.client.DisplayUtils;
import org.sagebionetworks.web.client.GlobalApplicationState;
import org.sagebionetworks.web.client.SynapseClientAsync;
import org.sagebionetworks.web.client.place.Trash;
import org.sagebionetworks.web.client.security.AuthenticationController;
import org.sagebionetworks.web.client.transform.NodeModelCreator;
import org.sagebionetworks.web.client.view.TrashView;
import org.sagebionetworks.web.client.widget.search.PaginationEntry;
import org.sagebionetworks.web.client.widget.search.PaginationUtil;
import com.google.gwt.activity.shared.AbstractActivity;
import com.google.gwt.event.shared.EventBus;
import com.google.gwt.user.client.rpc.AsyncCallback;
import com.google.gwt.user.client.ui.AcceptsOneWidget;
import com.google.inject.Inject;

public class TrashPresenter extends AbstractActivity implements TrashView.Presenter, Presenter<Trash> {
	
	public static final int TRASH_LIMIT = 10;
	
	public static final String TRASH_RESTORED_TITLE = "Restored";
	public static final String TRASH_PURGED_TITLE = "Permanently Removed";
	public static final String TRASH_EMPTIED_TITLE = "Trash Emptied!";
	public static final String TRASH_EMPTIED_MESSAGE = "Your trash was successfully emptied.";
	
	private Trash place;
	private TrashView view;
	private SynapseClientAsync synapseClient;
	private GlobalApplicationState globalAppState;
	private AuthenticationController authController;
	private NodeModelCreator nodeModelCreator;
	private PaginatedResults<TrashedEntity> trashList;
	private int offset;

	@Inject
	public TrashPresenter(TrashView view,
			SynapseClientAsync synapseClient,
			GlobalApplicationState globalAppState,
			AuthenticationController authController,
			NodeModelCreator nodeModelCreator){
		this.view = view;
		this.synapseClient = synapseClient;
		this.globalAppState = globalAppState;
		this.authController = authController;
		this.nodeModelCreator = nodeModelCreator;
		
		this.view.setPresenter(this);
	}
			
	
	@Override
	public void start(AcceptsOneWidget panel, EventBus eventBus) {
		// Install the view
		panel.setWidget(view);
	}

	@Override
	public void setPlace(Trash place) {
		this.place = place;
		this.view.setPresenter(this);
		this.view.clear();
		showView(place);
	}
	
	private void showView(Trash place) {
		Integer offset = place.getStart();
		getTrash(offset);
	}
	
	@Override
	public void purgeAll() {
		synapseClient.purgeTrashForUser(new AsyncCallback<Void>() {	
			@Override
			public void onSuccess(Void result) {
				view.showInfo(TRASH_EMPTIED_TITLE, TRASH_EMPTIED_MESSAGE);
				view.refreshTable();
			}
			
			@Override
			public void onFailure(Throwable caught) {
				createFailureDisplay(caught);
			}
			
		});
	}

	@Override
	public void getTrash(final Integer offset) {
		if (offset == null)
			this.offset = 0;
		else
			this.offset = offset;
		synapseClient.viewTrashForUser(this.offset, TRASH_LIMIT, new AsyncCallback<String>() {
			@Override
			public void onSuccess(String result) {
				
				try {
					trashList = nodeModelCreator.createPaginatedResults(result, TrashedEntity.class);
					if (trashList.getTotalNumberOfResults() > 0) {
						view.configure(trashList.getResults());
					} else {
						view.displayEmptyTrash();
					}
				} catch (JSONObjectAdapterException e) {
					view.showErrorMessage(DisplayConstants.ERROR_INCOMPATIBLE_CLIENT_VERSION);
				}
				
			}
			
			@Override
			public void onFailure(Throwable caught) {
				createFailureDisplay(caught);
			}
			
		});
	}
	
	@Override
	public void purgeEntities(Set<TrashedEntity> trashedEntities) {
		// Get ids and names for purging and displaying purged entities.
		final Set<String> entityIds = new HashSet<String>();
		final Set<String> entityNames = new HashSet<String>();
		for (TrashedEntity trashedEntity : trashedEntities) {
			entityIds.add(trashedEntity.getEntityId());
			entityNames.add(trashedEntity.getEntityName());
		}
		synapseClient.purgeMultipleTrashedEntitiesForUser(entityIds, new AsyncCallback<Void>() {

			@Override
			public void onSuccess(Void result) {
				String entityNamesSet = entityNames.toString();
				view.showInfo(TRASH_PURGED_TITLE, entityNames.toString().
											substring(1, entityNamesSet.length() - 1) + ".");
				// Refresh table.
				view.clear();
				getTrash(offset);
			}
			
			@Override
			public void onFailure(Throwable caught) {
				createFailureDisplay(caught);
			}
			
		});
	}
	
	@Override
	public void restoreEntity(final TrashedEntity trashedEntity) {
				synapseClient.restoreFromTrash(trashedEntity.getEntityId(), trashedEntity.getOriginalParentId(), new AsyncCallback<Void>() {

					@Override
					public void onSuccess(Void result) {
						view.showInfo(TRASH_RESTORED_TITLE, trashedEntity.getEntityName());
						view.refreshTable();
					}
					
					@Override
					public void onFailure(Throwable caught) {
						if (caught instanceof NotFoundException) {
							view.showErrorMessage(DisplayConstants.ERROR_RESTORING_TRASH_PARENT_NOT_FOUND);
						} else {
							createFailureDisplay(caught);
						}
					}
					
				});
	}
	
	@Override
	public List<PaginationEntry> getPaginationEntries(int nPerPage, int nPagesToShow) {
		Long nResults = trashList.getTotalNumberOfResults();
		if(nResults == null)
			return null;
		return PaginationUtil.getPagination(nResults.intValue(), offset, nPerPage, nPagesToShow);
	}
	
	@Override
	public int getOffset() {
		return offset;
	}

	/**
	 * Handles a failure in server call.
	 * 
	 * Note: public for testing.
	 * @param caught
	 */
	public void createFailureDisplay(Throwable caught) {
		if (!DisplayUtils.handleServiceException(caught, globalAppState, authController.isLoggedIn(), view)) {                    
			view.showErrorMessage(caught.getMessage());
		}
	}
}
