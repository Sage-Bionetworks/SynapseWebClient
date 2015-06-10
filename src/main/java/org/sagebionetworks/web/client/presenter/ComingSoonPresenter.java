package org.sagebionetworks.web.client.presenter;

import org.sagebionetworks.repo.model.Entity;
import org.sagebionetworks.web.client.GlobalApplicationState;
import org.sagebionetworks.web.client.SynapseClientAsync;
import org.sagebionetworks.web.client.SynapseJSNIUtils;
import org.sagebionetworks.web.client.place.ComingSoon;
import org.sagebionetworks.web.client.services.LayoutServiceAsync;
import org.sagebionetworks.web.client.transform.JsoProvider;
import org.sagebionetworks.web.client.view.ComingSoonView;

import com.google.gwt.activity.shared.AbstractActivity;
import com.google.gwt.event.shared.EventBus;
import com.google.gwt.user.client.rpc.AsyncCallback;
import com.google.gwt.user.client.ui.AcceptsOneWidget;
import com.google.inject.Inject;

public class ComingSoonPresenter extends AbstractActivity implements ComingSoonView.Presenter, Presenter<ComingSoon> {
		
	private ComingSoon place;
	private ComingSoonView view;
	private GlobalApplicationState globalApplicationState;
	private SynapseClientAsync synapseClient;
	LayoutServiceAsync layoutService;
	JsoProvider jsoProvider;
	SynapseJSNIUtils jsniUtils;
	
	@Inject
	public ComingSoonPresenter(ComingSoonView view,
			GlobalApplicationState globalApplicationState,
			SynapseClientAsync synapseClient,
			LayoutServiceAsync layoutService,
			JsoProvider jsoProvider,
			SynapseJSNIUtils jsniUtils) {
		this.view = view;
		this.globalApplicationState = globalApplicationState;
		this.synapseClient = synapseClient;
		this.layoutService = layoutService;
		this.jsoProvider = jsoProvider;
		this.jsniUtils = jsniUtils;
		
		view.setPresenter(this);
	}

	@Override
	public void start(AcceptsOneWidget panel, EventBus eventBus) {
		// Install the view
		panel.setWidget(view);
	}

	@Override
	public void setPlace(ComingSoon place) {
		this.place = place;
		this.view.setPresenter(this);
		final String token = place.toToken();
		synapseClient.getEntity(token, new AsyncCallback<Entity>() {			
			@Override
			public void onSuccess(Entity result) {
				Entity entity = result;
				view.setEntity(entity);
			}			
			@Override
			public void onFailure(Throwable caught) {
				view.showInfo("Error", "error getting: " + token);
			}
		});
	}

	@Override
    public String mayStop() {
        view.clear();
        return null;
        
    }
	
}
