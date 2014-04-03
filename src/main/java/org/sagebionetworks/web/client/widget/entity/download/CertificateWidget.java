package org.sagebionetworks.web.client.widget.entity.download;

import org.sagebionetworks.repo.model.UserProfile;
import org.sagebionetworks.repo.model.quiz.PassingRecord;
import org.sagebionetworks.web.client.ClientProperties;
import org.sagebionetworks.web.client.DisplayUtils;
import org.sagebionetworks.web.client.GlobalApplicationState;
import org.sagebionetworks.web.client.SynapseClientAsync;
import org.sagebionetworks.web.client.place.Home;
import org.sagebionetworks.web.client.security.AuthenticationController;
import org.sagebionetworks.web.client.widget.SynapseWidgetPresenter;
import org.sagebionetworks.web.client.widget.entity.download.CertificateView.Presenter;

import com.google.gwt.place.shared.Place;
import com.google.gwt.user.client.rpc.AsyncCallback;
import com.google.gwt.user.client.ui.Widget;
import com.google.inject.Inject;

public class CertificateWidget implements Presenter, SynapseWidgetPresenter {
	private GlobalApplicationState globalApplicationState;
	private SynapseClientAsync synapseClient;
	private AuthenticationController authenticationController;
	private CertificateView view;
	
	@Inject
	public CertificateWidget(CertificateView view, 
			GlobalApplicationState globalApplicationState, 
			AuthenticationController authenticationController, 
			SynapseClientAsync synapseClient) {
		this.view = view;
		this.globalApplicationState = globalApplicationState;
		this.authenticationController = authenticationController;
		this.synapseClient = synapseClient;
		view.setPresenter(this);
	}

	public void configure(UserProfile profile, PassingRecord passingRecord) {
		view.setProfile(profile);
		view.setCertificationDate(passingRecord.getPassedOn());
	}
	
	@Override
	public void goTo(Place place) {
		globalApplicationState.getPlaceChanger().goTo(place);
	}
	
	@Override
	public void goToLastPlace() {
		Place forwardPlace = globalApplicationState.getLastPlace();
		if(forwardPlace == null) {
			forwardPlace = new Home(ClientProperties.DEFAULT_PLACE_TOKEN);
		}
		goTo(forwardPlace);		
	}
	
	@Override
	public Widget asWidget() {
		return view.asWidget();
	}

}
