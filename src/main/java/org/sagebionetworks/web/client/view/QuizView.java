package org.sagebionetworks.web.client.view;

import org.sagebionetworks.repo.model.UserProfile;
import org.sagebionetworks.web.client.SynapsePresenter;
import org.sagebionetworks.web.client.SynapseView;

import com.google.gwt.place.shared.Place;
import com.google.gwt.user.client.ui.IsWidget;

public interface QuizView extends IsWidget, SynapseView {
	
	void setPresenter(Presenter loginPresenter);	
	
	void showTest(Object questionsAndAnswers);
	void showSuccess(UserProfile profile);
	void showFailure();
	void hideLoading();
	
	public interface Presenter extends SynapsePresenter {
		void goTo(Place place);
		void goToLastPlace();
		
		void submitAnswers(Object questionsAndSelectedAnswers);
    }
	
}
