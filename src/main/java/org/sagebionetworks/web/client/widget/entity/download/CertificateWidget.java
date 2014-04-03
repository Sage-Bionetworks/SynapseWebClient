package org.sagebionetworks.web.client.widget.entity.download;

import org.sagebionetworks.repo.model.UserProfile;
import org.sagebionetworks.repo.model.quiz.PassingRecord;
import org.sagebionetworks.web.client.ClientProperties;
import org.sagebionetworks.web.client.GlobalApplicationState;
import org.sagebionetworks.web.client.place.Home;
import org.sagebionetworks.web.client.widget.SynapseWidgetPresenter;
import org.sagebionetworks.web.client.widget.entity.download.CertificateView.Presenter;

import com.google.gwt.place.shared.Place;
import com.google.gwt.user.client.ui.Widget;
import com.google.inject.Inject;

public class CertificateWidget implements Presenter, SynapseWidgetPresenter {
	private GlobalApplicationState globalApplicationState;
	private CertificateView view;
	
	@Inject
	public CertificateWidget(CertificateView view, 
			GlobalApplicationState globalApplicationState 
		) {
		this.view = view;
		this.globalApplicationState = globalApplicationState;
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
