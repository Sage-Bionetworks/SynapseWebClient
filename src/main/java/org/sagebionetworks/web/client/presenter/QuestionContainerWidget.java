package org.sagebionetworks.web.client.presenter;

import java.util.HashSet;
import java.util.Set;
import org.sagebionetworks.repo.model.quiz.MultichoiceAnswer;
import org.sagebionetworks.repo.model.quiz.MultichoiceQuestion;
import org.sagebionetworks.repo.model.quiz.MultichoiceResponse;
import org.sagebionetworks.repo.model.quiz.Question;
import org.sagebionetworks.web.client.DisplayUtils;
import org.sagebionetworks.web.client.view.QuestionContainerWidgetView;
import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.event.dom.client.ClickHandler;
import com.google.gwt.user.client.ui.CheckBox;
import com.google.gwt.user.client.ui.Widget;
import com.google.inject.Inject;

public class QuestionContainerWidget implements QuestionContainerWidgetView.Presenter {

	private QuestionContainerWidgetView view;
	private Set<Long> answers;
	// Used to disable the buttons after scoring is performed
	private Long questionIndex;

	@Inject
	public QuestionContainerWidget(QuestionContainerWidgetView view) {
		this.view = view;
	}

	@Override
	public Long getQuestionIndex() {
		return questionIndex;
	}

	@Override
	public void configure(Long questionNumber, Question question, MultichoiceResponse response) {
		answers = new HashSet<Long>();
		view.configure(questionNumber, question.getPrompt());
		final MultichoiceQuestion multichoiceQuestion = (MultichoiceQuestion) question;
		this.questionIndex = question.getQuestionIndex();
		if (question instanceof MultichoiceQuestion) {
			if (multichoiceQuestion.getExclusive()) {
				for (final MultichoiceAnswer answer : multichoiceQuestion.getAnswers()) {
					view.addRadioButton(questionIndex, answer.getPrompt(), new ClickHandler() {
						@Override
						public void onClick(ClickEvent event) {
							answers.clear();
							answers.add(answer.getAnswerIndex());
						}
					}, wasSelected(response, answer.getAnswerIndex()));
				}
			} else {
				// checkbox
				for (final MultichoiceAnswer answer : multichoiceQuestion.getAnswers()) {
					view.addCheckBox(questionIndex, answer.getPrompt(), new ClickHandler() {
						@Override
						public void onClick(ClickEvent event) {
							// not exclusive, include all possible answer indexes
							// typecasting to CheckBox seems fragile
							if (((CheckBox) (event.getSource())).getValue()) {
								answers.add(answer.getAnswerIndex());
							} else {
								answers.remove(answer.getAnswerIndex());
							}
						}
					}, wasSelected(response, answer.getAnswerIndex()));
				}
			}
			String helpLink = question.getDocLink();
			if (DisplayUtils.isDefined(helpLink)) {
				view.setMoreInfoVisible(true);
				view.configureMoreInfo(helpLink, question.getHelpText());
			} else {
				view.setMoreInfoVisible(false);
			}
		}

	}

	private boolean wasSelected(MultichoiceResponse response, Long answerIndex) {
		return response != null && response.getAnswerIndex().contains(answerIndex);
	}

	@Override
	public void addCorrectnessStyle(boolean isCorrect) {
		if (isCorrect) {
			view.showSuccess(true);
		} else {
			view.showFailure(true);
			view.addStyleName("has-error");
		}
	}

	@Override
	public Set<Long> getAnswers() {
		return answers;
	}


	public Widget asWidget() {
		return view.asWidget();
	}

	@Override
	public void setEnabled(boolean enabled) {
		view.setIsEnabled(enabled);
	}
}
