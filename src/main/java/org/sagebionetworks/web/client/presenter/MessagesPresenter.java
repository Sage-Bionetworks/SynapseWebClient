package org.sagebionetworks.web.client.presenter;

import org.sagebionetworks.schema.adapter.JSONObjectAdapter;
import org.sagebionetworks.web.client.GlobalApplicationState;
import org.sagebionetworks.web.client.SynapseClientAsync;
import org.sagebionetworks.web.client.place.Messages;
import org.sagebionetworks.web.client.security.AuthenticationController;
import org.sagebionetworks.web.client.view.MessagesView;

import com.google.gwt.activity.shared.AbstractActivity;
import com.google.gwt.event.shared.EventBus;
import com.google.gwt.user.client.ui.AcceptsOneWidget;
import com.google.inject.Inject;

public class MessagesPresenter extends AbstractActivity implements MessagesView.Presenter, Presenter<Messages> {
	
	private Messages place;
	private MessagesView view;
	private SynapseClientAsync synapseClient;
	private GlobalApplicationState globalAppState;
	private AuthenticationController authController;
	private JSONObjectAdapter jsonObjectAdapter;
	
	// TODO:
	//@Inject
	//big constructor.
	@Inject
	public MessagesPresenter(MessagesView view,
			SynapseClientAsync synapseClient,
			GlobalApplicationState globalAppState,
			AuthenticationController authController,
			JSONObjectAdapter jsonObjectAdapter){
		// TODO: ALL of this is copied... pretty much.
		this.view = view;
		this.synapseClient = synapseClient;
		this.globalAppState = globalAppState;
		this.authController = authController;
		this.jsonObjectAdapter = jsonObjectAdapter;
		view.setPresenter(this);
	}
			
	
	@Override
	public void start(AcceptsOneWidget panel, EventBus eventBus) {
		// Install the view
		panel.setWidget(view);

	}

	@Override
	public void setPlace(Messages place) {
		this.place = place;
		this.view.setPresenter(this);
	}
	
	// TODO: EVERYTHING!!
}
