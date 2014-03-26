package org.sagebionetworks.web.client.view;

import java.util.List;
import java.util.Map;

import org.sagebionetworks.repo.model.UserProfile;
import org.sagebionetworks.repo.model.questionnaire.Question;
import org.sagebionetworks.web.client.SynapsePresenter;
import org.sagebionetworks.web.client.SynapseView;

import com.google.gwt.place.shared.Place;
import com.google.gwt.user.client.ui.IsWidget;

public interface QuizView extends IsWidget, SynapseView {
	
	void setPresenter(Presenter loginPresenter);	
	void showQuiz(List<Question> questions);
	void showSuccess(UserProfile profile);
	void showFailure();
	void hideLoading();
	
	public interface Presenter extends SynapsePresenter {
		void goTo(Place place);
		void goToLastPlace();
		
		void submitAnswers(Map<Long, List<Long>> questionIndex2AnswerIndices);
    }
	
}
