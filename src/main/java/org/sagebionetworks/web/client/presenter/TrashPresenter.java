package org.sagebionetworks.web.client.presenter;

import static org.sagebionetworks.web.client.ServiceEntryPointUtils.fixServiceEntryPoint;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import org.sagebionetworks.repo.model.TrashedEntity;
import org.sagebionetworks.web.client.DisplayConstants;
import org.sagebionetworks.web.client.SynapseClientAsync;
import org.sagebionetworks.web.client.place.Trash;
import org.sagebionetworks.web.client.view.TrashView;
import org.sagebionetworks.web.client.widget.entity.controller.SynapseAlert;
import org.sagebionetworks.web.client.widget.search.PaginationEntry;
import org.sagebionetworks.web.client.widget.search.PaginationUtil;
import org.sagebionetworks.web.shared.PaginatedResults;
import org.sagebionetworks.web.shared.exceptions.ForbiddenException;
import com.google.gwt.activity.shared.AbstractActivity;
import com.google.gwt.event.shared.EventBus;
import com.google.gwt.user.client.rpc.AsyncCallback;
import com.google.gwt.user.client.ui.AcceptsOneWidget;
import com.google.inject.Inject;

public class TrashPresenter extends AbstractActivity implements TrashView.Presenter, Presenter<Trash> {

	public static final int TRASH_LIMIT = 10;

	public static final String TRASH_RESTORED_TITLE = "Restored ";
	public static final String TRASH_PURGED_TITLE = "Permanently Removed ";
	public static final String TRASH_EMPTIED_TITLE = "Trash Emptied!";

	public static final String ERROR_RESTORING_ENTITY_TITLE = "Sorry, an error occurred while restoring this entity.";
	public static final String ERROR_DELETING_SELECTED_TITLE = "Sorry, an error occured while deleting your selected entities.";
	public static final String ERROR_FETCHING_TRASH_TITLE = "Sorry, an error occured while fetching your trash.";


	private Trash place;
	private TrashView view;
	private SynapseClientAsync synapseClient;
	private PaginatedResults<TrashedEntity> trashList;
	private int offset;
	private SynapseAlert synAlert;

	@Inject
	public TrashPresenter(TrashView view, SynapseClientAsync synapseClient, SynapseAlert synAlert) {
		this.view = view;
		this.synapseClient = synapseClient;
		fixServiceEntryPoint(synapseClient);
		this.synAlert = synAlert;
		this.view.setPresenter(this);
		this.view.setSynAlertWidget(synAlert.asWidget());
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

	@Override
	public void getTrash(Integer offset) {
		synAlert.clear();
		if (offset == null) {
			this.offset = 0;
		} else {
			this.offset = offset;
		}
		synapseClient.viewTrashForUser(this.offset, TRASH_LIMIT, new AsyncCallback<PaginatedResults<TrashedEntity>>() {
			@Override
			public void onSuccess(PaginatedResults<TrashedEntity> result) {
				trashList = result;
				if (trashList.getTotalNumberOfResults() > 0) {
					view.configure(trashList.getResults());
				} else {
					view.displayEmptyTrash();
				}
			}

			@Override
			public void onFailure(Throwable caught) {
				createFailureDisplay(ERROR_FETCHING_TRASH_TITLE, caught);
			}

		});
	}

	@Override
	public void purgeEntities(Set<TrashedEntity> trashedEntities) {
		synAlert.clear();
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
				view.showInfo(TRASH_PURGED_TITLE + entityNames.toString().substring(1, entityNamesSet.length() - 1) + ".");
				// Refresh table.
				view.refreshTable();
			}

			@Override
			public void onFailure(Throwable caught) {
				createFailureDisplay(ERROR_DELETING_SELECTED_TITLE, caught);
			}

		});
	}

	@Override
	public void restoreEntity(final TrashedEntity trashedEntity) {
		synAlert.clear();
		synapseClient.restoreFromTrash(trashedEntity.getEntityId(), trashedEntity.getOriginalParentId(), new AsyncCallback<Void>() {

			@Override
			public void onSuccess(Void result) {
				view.showInfo(TRASH_RESTORED_TITLE + trashedEntity.getEntityName());
				view.refreshTable();
			}

			@Override
			public void onFailure(Throwable caught) {
				if (caught instanceof ForbiddenException) {
					// Show to view, as handleServiceException call in createFailureDisplay
					// will show message about insufficient privileges.
					view.showErrorMessage(DisplayConstants.ERROR_RESTORING_TRASH_PARENT_NOT_FOUND + " " + caught.getMessage());
				} else {
					createFailureDisplay(ERROR_RESTORING_ENTITY_TITLE, caught);
				}
			}

		});
	}

	@Override
	public List<PaginationEntry> getPaginationEntries(int nPerPage, int nPagesToShow) {
		Long nResults = trashList.getTotalNumberOfResults();
		if (nResults == null)
			return null;
		// broken
		return PaginationUtil.getPagination(nResults.intValue(), offset, nPerPage, nPagesToShow);
	}

	@Override
	public int getOffset() {
		return offset;
	}


	/*
	 * Private Methods
	 */

	private void showView(Trash place) {
		Integer offset = place.getStart();
		getTrash(offset);
	}

	private void createFailureDisplay(String title, Throwable caught) {
		synAlert.showError(title + " could not be deleted. Reason: " + caught.getMessage());
	}
}
