package org.sagebionetworks.web.client.view;

import java.util.HashSet;
import java.util.Set;

import org.sagebionetworks.repo.model.dao.WikiPageKey;
import org.sagebionetworks.repo.model.quiz.MultichoiceAnswer;
import org.sagebionetworks.repo.model.quiz.MultichoiceQuestion;
import org.sagebionetworks.repo.model.quiz.MultichoiceResponse;
import org.sagebionetworks.repo.model.quiz.Question;
import org.sagebionetworks.web.client.place.Wiki;

import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.event.dom.client.ClickHandler;
import com.google.gwt.safehtml.shared.SimpleHtmlSanitizer;
import com.google.gwt.user.client.ui.CheckBox;
import com.google.gwt.user.client.ui.HTML;
import com.google.gwt.user.client.ui.InlineHTML;
import com.google.gwt.user.client.ui.RadioButton;
import com.google.gwt.user.client.ui.SimplePanel;
import com.google.gwt.user.client.ui.Widget;
import com.google.inject.Inject;

public class QuestionContainerWidget implements QuestionContainerWidgetView.Presenter{

	private QuestionContainerWidgetView view;
	private Set<Long> answers;
	// Used to disable the buttons after scoring is performed
	private Set<Widget> answerUI;
	private Long questionIndex;
	
	@Inject
	public QuestionContainerWidget(QuestionContainerWidgetView view) {
		this.view = view;
		answers = new HashSet<Long>();
		answerUI = new HashSet<Widget>();
	}
	
	@Override 
	public Long getQuestionIndex() {
		return questionIndex;
	}
	
	@Override
	public void configure(Long questionNumber, Question question, MultichoiceResponse response) {
		view.setQuestionHeader(new InlineHTML("<small class=\"margin-right-10\">"+questionNumber+".</small>"+SimpleHtmlSanitizer.sanitizeHtml(question.getPrompt()).asString()+"</small>"));
		final MultichoiceQuestion multichoiceQuestion = (MultichoiceQuestion) question;
		this.questionIndex = question.getQuestionIndex();
		if (question instanceof MultichoiceQuestion) {
			if (multichoiceQuestion.getExclusive()) {
				for (final MultichoiceAnswer answer : multichoiceQuestion.getAnswers()) {
					SimplePanel answerContainer = new SimplePanel();
					answerContainer.addStyleName("radio padding-left-30 control-label");
					RadioButton answerButton = new RadioButton("question-"+question.getQuestionIndex());
					answerButton.setHTML(SimpleHtmlSanitizer.sanitizeHtml(answer.getPrompt()));
					answerButton.addClickHandler(new ClickHandler() {
						@Override
						public void onClick(ClickEvent event) {
							answers.clear();
							answers.add(answer.getAnswerIndex());
						}
					});
					answerUI.add(answerButton);
					answerContainer.add(answerButton.asWidget());
					view.addAnswer(answerContainer.asWidget());
				}
			} else {
				//checkbox
				for (final MultichoiceAnswer answer : multichoiceQuestion.getAnswers()) {
					SimplePanel answerContainer = new SimplePanel();
					answerContainer.addStyleName("checkbox padding-left-30 control-label");
					final CheckBox checkbox= new CheckBox();
					checkbox.setHTML(SimpleHtmlSanitizer.sanitizeHtml(answer.getPrompt()));
					checkbox.addClickHandler(new ClickHandler() {
						@Override
						public void onClick(ClickEvent event) {
							//not exclusive, include all possible answer indexes
							if (checkbox.getValue()) {
								answers.add(answer.getAnswerIndex());	
							} else {
								answers.remove(answer.getAnswerIndex());
							}
						}
					});
					answerUI.add(checkbox);
					answerContainer.add(checkbox);
					view.addAnswer(answerContainer);
				}
			}
			final WikiPageKey moreInfoKey = question.getReference();
			if (moreInfoKey != null && moreInfoKey.getOwnerObjectId() != null) {
				Wiki place = new Wiki(moreInfoKey.getOwnerObjectId(), moreInfoKey.getOwnerObjectType().name(), moreInfoKey.getWikiPageId());
				view.configureMoreInfo("#!Wiki:" + place.toToken());
			}
		}

	}
	
	@Override 
	public void addCorrectnessStyle(boolean isCorrect) {
		HTML html = new InlineHTML();
		html.addStyleName("margin-right-5");
		if (isCorrect) {
			view.showSuccess(true);
		} else {
			view.showFailure(true);
			view.addStyleName("has-error");
		}
		view.addAnswer(html);
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
		for (Widget answerWidget: answerUI) {
			if (answerWidget instanceof CheckBox) {
				((CheckBox)answerWidget).setEnabled(enabled);
			} else if (answerWidget instanceof RadioButton) {
				((RadioButton)answerWidget).setEnabled(enabled);
			}
		}
	}
}
