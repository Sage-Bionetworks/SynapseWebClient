package org.sagebionetworks.web.client.presenter;

import org.sagebionetworks.repo.model.PaginatedResults;
import org.sagebionetworks.repo.model.TrashedEntity;
import org.sagebionetworks.schema.adapter.AdapterFactory;
import org.sagebionetworks.schema.adapter.JSONArrayAdapter;
import org.sagebionetworks.schema.adapter.JSONObjectAdapter;
import org.sagebionetworks.schema.adapter.JSONObjectAdapterException;
import org.sagebionetworks.web.client.GlobalApplicationState;
import org.sagebionetworks.web.client.SynapseClientAsync;
import org.sagebionetworks.web.client.place.Trash;
import org.sagebionetworks.web.client.security.AuthenticationController;
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
	private AdapterFactory adapterFactory;
	
	// TODO:
	//@Inject
	//big constructor.
	@Inject
	public TrashPresenter(TrashView view,
			SynapseClientAsync synapseClient,
			GlobalApplicationState globalAppState,
			AuthenticationController authController,
			//JSONObjectAdapter jsonObjectAdapter,	// replace with adapterFactory
			AdapterFactory adapterFactory){
		// TODO: ALL of this is copied... pretty much.
		this.view = view;
		this.synapseClient = synapseClient;
		this.globalAppState = globalAppState;
		this.authController = authController;
		//this.jsonObjectAdapter = jsonObjectAdapter;
		this.adapterFactory = adapterFactory;

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
			}
			
			@Override
			public void onFailure(Throwable caught) {
				// TODO: Do this. Just show some method?
				// Log something?
				
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
					//PaginatedResults<TrashedEntity> trashedEntities = (PaginatedResults<TrashedEntity>) jsonObjectAdapter.get(result);
					//PaginatedResults<TrashedEntity> header = new PaginatedResults<TrashedEntity>(adapterFactory.createNew(headerString))
					//JSONObjectAdapter jsonObjectAdapter = adapterFactory.createNew(result);
					//JSONArrayAdapter jsonArrayAdapter = jsonObjectAdapter.getJSONArray("results");
					
					//TrashedEntity result1 = (TrashedEntity) jsonArrayAdapter.get(0);
					//TrashedEntity[] results = (PaginatedResults<TrashedEntity>) jsonObjectAdapter.get("results");
					PaginatedResults<TrashedEntity> results = new PaginatedResults<TrashedEntity>(adapterFactory.createNew(result));
					
					String hello = "= > )";
				} catch (JSONObjectAdapterException e) {
					// Some error thing.
					@SuppressWarnings("unused")
					String hello = "= > )";
				}
				
			}
			
			@Override
			public void onFailure(Throwable caught) {
				// TODO: view.showErrorLoadingTrash();
			}
			
		});
	}
}
