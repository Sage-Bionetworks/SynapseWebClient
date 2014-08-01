package org.sagebionetworks.web.client.view;

import java.util.Map;
import java.util.Set;

import org.sagebionetworks.repo.model.UserProfile;
import org.sagebionetworks.repo.model.quiz.PassingRecord;
import org.sagebionetworks.repo.model.quiz.Quiz;
import org.sagebionetworks.web.client.SynapsePresenter;
import org.sagebionetworks.web.client.SynapseView;

import com.google.gwt.place.shared.Place;
import com.google.gwt.user.client.ui.IsWidget;

public interface QuizView extends IsWidget, SynapseView {
	
	void setPresenter(Presenter loginPresenter);	
	void showQuiz(Quiz questions);
	void showSuccess(UserProfile profile, PassingRecord passingRecord);
	void showFailure(PassingRecord passingRecord);
	void hideLoading();
	
	public interface Presenter extends SynapsePresenter {
		void goTo(Place place);
		void goToLastPlace();
		
		void submitAnswers(Map<Long, Set<Long>> questionIndex2AnswerIndices);
    }
	
}
