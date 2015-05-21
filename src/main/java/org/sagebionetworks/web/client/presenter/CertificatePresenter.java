package org.sagebionetworks.web.client.presenter;

import org.sagebionetworks.repo.model.UserProfile;
import org.sagebionetworks.repo.model.quiz.PassingRecord;
import org.sagebionetworks.schema.adapter.AdapterFactory;
import org.sagebionetworks.schema.adapter.JSONObjectAdapterException;
import org.sagebionetworks.web.client.DisplayUtils;
import org.sagebionetworks.web.client.GlobalApplicationState;
import org.sagebionetworks.web.client.SynapseClientAsync;
import org.sagebionetworks.web.client.cache.ClientCache;
import org.sagebionetworks.web.client.security.AuthenticationController;
import org.sagebionetworks.web.client.view.CertificateView;
import org.sagebionetworks.web.client.widget.entity.controller.SynapseAlert;
import org.sagebionetworks.web.client.widget.user.UserBadge;
import org.sagebionetworks.web.shared.exceptions.NotFoundException;

import com.google.gwt.activity.shared.AbstractActivity;
import com.google.gwt.event.shared.EventBus;
import com.google.gwt.place.shared.Place;
import com.google.gwt.user.client.rpc.AsyncCallback;
import com.google.gwt.user.client.ui.AcceptsOneWidget;
import com.google.inject.Inject;

public class CertificatePresenter extends AbstractActivity implements CertificateView.Presenter, Presenter<org.sagebionetworks.web.client.place.Certificate> {

	private CertificateView view;
	private GlobalApplicationState globalApplicationState;
	private AuthenticationController authenticationController;
	private SynapseClientAsync synapseClient;
	private AdapterFactory adapterFactory;
	private ClientCache clientCache;
	private SynapseAlert synapseAlert;
	
	@Inject
	public CertificatePresenter(CertificateView view,  
			AuthenticationController authenticationController, 
			GlobalApplicationState globalApplicationState,
			SynapseClientAsync synapseClient,
			AdapterFactory adapterFactory,
			ClientCache clientCache){
		this.view = view;
		this.synapseAlert = synapseAlert;
		// Set the presenter on the view
		this.authenticationController = authenticationController;
		this.globalApplicationState = globalApplicationState;
		this.synapseClient = synapseClient;
		this.adapterFactory = adapterFactory;
		this.clientCache = clientCache;
		this.view.setPresenter(this);
	}
	
	@Override
	public void start(AcceptsOneWidget panel, EventBus eventBus) {
		panel.setWidget(this.view.asWidget());
	}

	
	@Override
	public void goTo(Place place) {
		globalApplicationState.getPlaceChanger().goTo(place);
	}
	
	@Override
	public void goToLastPlace() {
		view.hideLoading();
		globalApplicationState.gotoLastPlace();
	}
	
	@Override
    public String mayStop() {
        view.clear();
        return null;
    }

	@Override
	public void setPlace(org.sagebionetworks.web.client.place.Certificate place) {
		view.setPresenter(this);
		initStep1(place.toToken());
	}
	
	public void initStep1(final String principalId) {
		view.clear();
		view.showLoading();
		UserBadge.getUserProfile(principalId, adapterFactory, synapseClient, clientCache, new AsyncCallback<UserProfile>() {
			@Override
			public void onSuccess(UserProfile profile) {
				initStep2(principalId, profile);
			}
			
			@Override
			public void onFailure(Throwable caught) {
				if (!DisplayUtils.handleServiceException(caught, globalApplicationState, authenticationController.isLoggedIn(), view)) {
					view.showErrorMessage(caught.getMessage());
				}
			}
		});
	}
	
	public void initStep2(String principalId, final UserProfile profile) {
		synapseClient.getCertifiedUserPassingRecord(principalId, new AsyncCallback<String>() {
			@Override
			public void onSuccess(String passingRecordJson) {
				try {
					//if certified, show the certificate
					PassingRecord passingRecord = new PassingRecord(adapterFactory.createNew(passingRecordJson));
					view.showSuccess(profile, passingRecord);
				} catch (JSONObjectAdapterException e) {
					onFailure(e);
				}
			}
			@Override
			public void onFailure(Throwable caught) {
				if (caught instanceof NotFoundException) {
					//show user is not certified
					view.showNotCertified(profile);
				} else {
					if (!DisplayUtils.handleServiceException(caught, globalApplicationState, authenticationController.isLoggedIn(), view)) {
						view.showErrorMessage(caught.getMessage());
					}
				}
			}
		});
	}
	
	@Override
	public void okButtonClicked() {
		goToLastPlace();
	}
}
