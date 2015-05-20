package org.sagebionetworks.web.client.view;

import org.sagebionetworks.repo.model.UserProfile;
import org.sagebionetworks.repo.model.quiz.PassingRecord;
import org.sagebionetworks.web.client.SynapsePresenter;
import org.sagebionetworks.web.client.SynapseView;

import com.google.gwt.place.shared.Place;
import com.google.gwt.user.client.ui.IsWidget;
import com.google.gwt.user.client.ui.Widget;

public interface CertificateView extends IsWidget, SynapseView {
	
	void setPresenter(Presenter presenter);	
	void showSuccess(UserProfile profile, PassingRecord passingRecord);
	void showNotCertified(UserProfile profile);
	void hideLoading();
	
	public interface Presenter extends SynapsePresenter {
		void goTo(Place place);
		void goToLastPlace();
		void okButtonClicked();
    }

	void setSynapseAlertWidget(Widget synAlert);
	
}
