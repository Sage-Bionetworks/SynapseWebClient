package org.sagebionetworks.web.client.presenter;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.Set;

import org.sagebionetworks.web.shared.PaginatedResults;
import org.sagebionetworks.repo.model.Reference;
import org.sagebionetworks.repo.model.TrashedEntity;
import org.sagebionetworks.schema.adapter.AdapterFactory;
import org.sagebionetworks.schema.adapter.JSONArrayAdapter;
import org.sagebionetworks.schema.adapter.JSONObjectAdapter;
import org.sagebionetworks.schema.adapter.JSONObjectAdapterException;
import org.sagebionetworks.web.client.DisplayUtils;
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
	//private JSONObjectAdapter jsonObjectAdapter;
	private NodeModelCreator nodeModelCreator;
	private Set<TrashedEntity> selectedTrash;
	
	// TODO:
	//@Inject
	//big constructor.
	@Inject
	public TrashPresenter(TrashView view,
			SynapseClientAsync synapseClient,
			GlobalApplicationState globalAppState,
			AuthenticationController authController,
			//JSONObjectAdapter jsonObjectAdapter,	// replace with NodeModelCreator
			NodeModelCreator nodeModelCreator){
		// TODO: ALL of this is copied... pretty much.
		this.view = view;
		this.synapseClient = synapseClient;
		this.globalAppState = globalAppState;
		this.authController = authController;
		//this.jsonObjectAdapter = jsonObjectAdapter;
		this.nodeModelCreator = nodeModelCreator;
		selectedTrash = new HashSet<TrashedEntity>();
		
		view.setPresenter(this);
		getTrash();		// TODO: Where to make this call? In constructor?
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
	public void deleteAll() {
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
	
	// TODO: Where should this method go? How to "Show trash"?
	public void getTrash() {
		synapseClient.viewTrashForUser(0, 10, new AsyncCallback<String>() {
			
			@Override
			public void onSuccess(String result) {
				// TODO: view.showTrash()
				
				try {
					PaginatedResults<TrashedEntity> trashedEntities = nodeModelCreator.createPaginatedResults(result, TrashedEntity.class);
					for (TrashedEntity trashedEntity : trashedEntities.getResults()) {
						view.displayIndividualRow(trashedEntity);
					}
				} catch (JSONObjectAdapterException e) {
					// TODO: Some error handling.
				}
				
			}
			
			@Override
			public void onFailure(Throwable caught) {
				// TODO: view.showErrorLoadingTrash();
			}
			
		});
	}
	
	@Override
	public void selectTrash(TrashedEntity trash) {
		selectedTrash.add(trash);
	}
	
	@Override
	public void deselectTrash(TrashedEntity trash) {
		selectedTrash.remove(trash);
	}
}
