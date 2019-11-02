package org.sagebionetworks.web.client.view;

import org.sagebionetworks.repo.model.quiz.PassingRecord;
import org.sagebionetworks.repo.model.quiz.Quiz;
import org.sagebionetworks.web.client.SynapseView;
import com.google.gwt.place.shared.Place;
import com.google.gwt.user.client.ui.IsWidget;
import com.google.gwt.user.client.ui.Widget;

public interface QuizView extends IsWidget, SynapseView {

	void setPresenter(Presenter loginPresenter);

	void hideLoading();

	void setQuizHeader(String quizHeader);

	void addQuestionContainerWidget(Widget widget);

	void setSubmitEnabled(boolean isEnabled);

	void reset();

	void setSubmitVisible(boolean isVisible);

	void showScore(String scoreContainerText);

	void hideAll();

	void showSuccess(PassingRecord passingRecord);

	void showFailure(PassingRecord passingRecord);

	public interface Presenter {
		void goTo(Place place);

		void goToLastPlace();

		void showQuiz(Quiz quiz);

		void submitAnswers();

		void showSuccess(PassingRecord passingRecord);

		void showFailure(PassingRecord passingRecord);

		void submitClicked();

		void showQuizFromPassingRecord(PassingRecord passingRecord);
	}

	void setSynAlertWidget(Widget synAlert);
}
