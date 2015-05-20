package org.sagebionetworks.web.client.presenter;

import org.sagebionetworks.repo.model.ResponseMessage;
import org.sagebionetworks.web.client.GWTWrapper;
import org.sagebionetworks.web.client.GlobalApplicationState;
import org.sagebionetworks.web.client.SynapseClientAsync;
import org.sagebionetworks.web.client.place.SignedToken;
import org.sagebionetworks.web.client.view.SignedTokenView;
import org.sagebionetworks.web.client.widget.entity.controller.SynapseAlert;

import com.google.gwt.activity.shared.AbstractActivity;
import com.google.gwt.event.shared.EventBus;
import com.google.gwt.user.client.rpc.AsyncCallback;
import com.google.gwt.user.client.ui.AcceptsOneWidget;
import com.google.inject.Inject;

public class SignedTokenPresenter extends AbstractActivity implements SignedTokenView.Presenter, Presenter<SignedToken> {
		
	private SignedToken place;
	private SignedTokenView view;
	private SynapseClientAsync synapseClient;
	private GWTWrapper gwt;
	private SynapseAlert synapseAlert;
	private GlobalApplicationState globalApplicationState;
	@Inject
	public SignedTokenPresenter(SignedTokenView view, 
			SynapseClientAsync synapseClient, 
			GWTWrapper gwt, 
			SynapseAlert synapseAlert,
			GlobalApplicationState globalApplicationState){
		this.view = view;
		this.synapseClient = synapseClient;
		this.synapseAlert = synapseAlert;
		this.gwt = gwt;
		this.globalApplicationState = globalApplicationState;
		view.setPresenter(this);
		view.setSynapseAlert(synapseAlert.asWidget());
	}

	@Override
	public void start(AcceptsOneWidget panel, EventBus eventBus) {
		// Install the view
		panel.setWidget(view);
	}

	@Override
	public void setPlace(SignedToken place) {
		this.place = place;
		this.view.setPresenter(this);
		
		configure(place.getTokenType(), place.getSignedEncodedToken());
	}
	
	public void configure(String tokenType, String signedEncodedToken) {
		synapseAlert.clear();
		view.clear();
		synapseClient.handleSignedToken(tokenType, signedEncodedToken, gwt.getHostPageBaseURL(), new AsyncCallback<ResponseMessage>() {
			@Override
			public void onSuccess(ResponseMessage result) {
				view.showSuccess(result.getMessage());		
			}
			@Override
			public void onFailure(Throwable caught) {
				synapseAlert.handleException(caught);
			}
		});
	}
	
	@Override
    public String mayStop() {
        view.clear();
        return null;
    }
	
	@Override
	public void okClicked() {
		globalApplicationState.gotoLastPlace();
	}
}
