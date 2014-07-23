package org.sagebionetworks.web.client.presenter;

import java.util.ArrayList;
import java.util.List;

import org.sagebionetworks.web.shared.EntityWrapper;
import org.sagebionetworks.web.shared.PaginatedResults;
import org.sagebionetworks.web.shared.exceptions.NotFoundException;
import org.sagebionetworks.repo.model.BatchResults;
import org.sagebionetworks.repo.model.EntityHeader;
import org.sagebionetworks.repo.model.EntityPath;
import org.sagebionetworks.repo.model.TrashedEntity;
import org.sagebionetworks.schema.adapter.JSONObjectAdapterException;
import org.sagebionetworks.web.client.GlobalApplicationState;
import org.sagebionetworks.web.client.SynapseClientAsync;
import org.sagebionetworks.web.client.place.Trash;
import org.sagebionetworks.web.client.security.AuthenticationController;
import org.sagebionetworks.web.client.transform.NodeModelCreator;
import org.sagebionetworks.web.client.view.TrashView;

import com.google.gwt.activity.shared.AbstractActivity;
import com.google.gwt.event.shared.EventBus;
import com.google.gwt.user.client.rpc.AsyncCallback;
import com.google.gwt.user.client.ui.AcceptsOneWidget;
import com.google.inject.Inject;

public class TrashPresenter extends AbstractActivity implements TrashView.Presenter, Presenter<Trash> {
	
	private Trash place;
	private TrashView view;
	private SynapseClientAsync synapseClient;
	private GlobalApplicationState globalAppState;
	private AuthenticationController authController;
	private NodeModelCreator nodeModelCreator;

	@Inject
	public TrashPresenter(TrashView view,
			SynapseClientAsync synapseClient,
			GlobalApplicationState globalAppState,
			AuthenticationController authController,
			NodeModelCreator nodeModelCreator){
		// TODO: ALL of this is copied... pretty much.
		this.view = view;
		this.synapseClient = synapseClient;
		this.globalAppState = globalAppState;
		this.authController = authController;
		this.nodeModelCreator = nodeModelCreator;
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
	}
	
	// TODO: EVERYTHING!!
	@Override
	public void purgeAll() {
		synapseClient.purgeTrashForUser(new AsyncCallback<Void>() {	
			@Override
			public void onSuccess(Void result) {
				// show some method "Trash emptied."
				view.showInfo("Trash Emptied!", "Your trash was successfully emptied.");
				
				// Get trash? Or just clear table?
				view.clear();
			}
			
			@Override
			public void onFailure(Throwable caught) {
				// TODO: Some error.
				view.showInfo("Trash Not Emptied!", "Your trash was not successfully emptied.");
			}
			
		});
	}
	
	// TODO: Where should this method go or be called?? How to "Show trash"?
	@Override
	public void getTrash() {
		
		synapseClient.viewTrashForUser(0, 10, new AsyncCallback<String>() {
			@Override
			public void onSuccess(String result) {
				try {
					PaginatedResults<TrashedEntity> trashedEntities = nodeModelCreator.createPaginatedResults(result, TrashedEntity.class);
					for (TrashedEntity trashedEntity : trashedEntities.getResults()) {
						view.displayTrashedEntity(trashedEntity);
					}

//					List<String> ids = new ArrayList<String>();
//					for (TrashedEntity trashedEntity : trashedEntities.getResults()) {
//						ids.add(trashedEntity.getEntityId());
//					}
//					// Get type information for the trashed entities.
//					// TODO: Cannot get EntityHeaders since all entities are in trash.
//					synapseClient.getEntityTypeBatch(ids, new AsyncCallback<String>() {
//
//						@Override
//						public void onFailure(Throwable caught) {
//							view.showErrorMessage("ERROR GETTING ENTITY HEADERS.");
//						}
//
//						@Override
//						public void onSuccess(String result) {
//							try {
//								BatchResults<EntityHeader> results = nodeModelCreator.createBatchResults(result, EntityHeader.class);
//								assert(trashedEntities.getResults().size() == results.getResults().size());	// TODO: Remove.
//								for (int i = 0; i < trashedEntities.getResults().size(); i++) {
//									
//								}
//							} catch (JSONObjectAdapterException e) {
//								// TODO Auto-generated catch block
//								e.printStackTrace();
//							}
//						}
//						
//					});
				} catch (JSONObjectAdapterException e) {
					// TODO: Some error handling.
				}
				
			}
			
			@Override
			public void onFailure(Throwable caught) {
				// TODO: view.showErrorLoadingTrash();
				view.showErrorMessage("Something went wrong.");
			}
			
		});
		
		
	}

	@Override
	public void purgeEntity(final TrashedEntity trashedEntity) {
		synapseClient.purgeTrashForUser(trashedEntity.getEntityId(), new AsyncCallback<Void>() {

			@Override
			public void onSuccess(Void result) {
				view.showInfo("Purged: ", trashedEntity.getEntityName());
				view.removeDisplayTrashedEntity(trashedEntity);
			}
			
			@Override
			public void onFailure(Throwable caught) {
				// TODO: view.showErrorPurgingTrash();
			}
			
		});
	}
	
	@Override
	public void restoreEntity(final TrashedEntity trashedEntity) {
		// TODO: check if original parent exists and is not in trash
		// if not - prompt user for new parent... ???
		
		// Check if parent is not in trash.
		// TODO: Better way to check if parent is not in trash??
		synapseClient.getEntity(trashedEntity.getOriginalParentId(), new AsyncCallback<EntityWrapper>() {
			
			@Override
			public void onSuccess(EntityWrapper result) {
				synapseClient.restoreFromTrash(trashedEntity.getEntityId(), trashedEntity.getOriginalParentId() , new AsyncCallback<Void>() {

					@Override
					public void onSuccess(Void result) {
						// TODO: This code still runs, even if given parentId is in trash
						view.showInfo("Restored: ", trashedEntity.getEntityName());
						view.removeDisplayTrashedEntity(trashedEntity);
					}
					
					@Override
					public void onFailure(Throwable caught) {
						view.showErrorMessage("Error restoring " + trashedEntity.getEntityName() + " to original parent.");
					}
					
				});
			}
			
			@Override
			public void onFailure(Throwable caught) {
				// TODO Auto-generated method stub
				if (caught instanceof NotFoundException) {
					view.showErrorMessage("Original parent to " + trashedEntity.getEntityName() + " is either in trash or deleted.");
				} else {
					view.showErrorMessage("Something else went wrong!!");
				}
			}
			
		});
	}
}
