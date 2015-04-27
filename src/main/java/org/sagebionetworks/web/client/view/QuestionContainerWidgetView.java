package org.sagebionetworks.web.client.view;

import java.util.Map;
import java.util.Set;

import org.sagebionetworks.repo.model.quiz.MultichoiceResponse;
import org.sagebionetworks.repo.model.quiz.Question;
import org.sagebionetworks.web.client.SynapseView;

import com.google.gwt.user.client.ui.IsWidget;
import com.google.gwt.user.client.ui.SimplePanel;
import com.google.gwt.user.client.ui.Widget;

public interface QuestionContainerWidgetView extends IsWidget {

	public Widget asWidget();
	public void addStyleName(String style);
	
	/**
	 * Presenter interface
	 */
	public interface Presenter {

		Set<Long> getAnswers();
		
		void configure(Long questionNumber, Question question,
				MultichoiceResponse response);
		
		void addCorrectnessStyle(boolean isCorrect);

		Long getQuestionIndex();

		void setEnabled(boolean enabled);

	}

	void addAnswer(Widget answerContainer);
	void setQuestionHeader(Widget questionHeader);
	void configureMoreInfo(String href);
	void showSuccess(boolean isShown);
	void showFailure(boolean isShown);}
