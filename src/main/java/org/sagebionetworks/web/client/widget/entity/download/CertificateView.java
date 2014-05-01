package org.sagebionetworks.web.client.widget.entity.download;

import java.util.Date;

import org.sagebionetworks.repo.model.UserProfile;
import org.sagebionetworks.web.client.SynapseView;

import com.google.gwt.place.shared.Place;
import com.google.gwt.user.client.ui.IsWidget;

public interface CertificateView extends IsWidget, SynapseView {

	void setPresenter(Presenter presenter);
	void setProfile(UserProfile profile);
	void setCertificationDate(Date dateCertified);
	
	/**
	 * Presenter interface
	 */
	public interface Presenter {
		void goTo(Place place);
		void goToLastPlace();
	}
}
