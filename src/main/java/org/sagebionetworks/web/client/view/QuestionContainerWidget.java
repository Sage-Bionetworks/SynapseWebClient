package org.sagebionetworks.web.client.view;

import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import org.sagebionetworks.repo.model.dao.WikiPageKey;
import org.sagebionetworks.repo.model.quiz.MultichoiceAnswer;
import org.sagebionetworks.repo.model.quiz.MultichoiceQuestion;
import org.sagebionetworks.repo.model.quiz.MultichoiceResponse;
import org.sagebionetworks.repo.model.quiz.Question;
import org.sagebionetworks.web.client.DisplayUtils;
import org.sagebionetworks.web.client.PortalGinInjector;
import org.sagebionetworks.web.client.place.Wiki;

import com.google.gwt.core.client.GWT;
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
	private PortalGinInjector ginInjector;
	private Set<Long> answers;
	
	@Inject
	public QuestionContainerWidget(QuestionContainerWidgetView view,
			PortalGinInjector ginInjector) {
		this.view = view;
		this.ginInjector = ginInjector;
		answers = new HashSet<Long>();
	}
	
	@Override
	public void configure(Long questionNumber, Question question, MultichoiceResponse response) {
		view.setQuestionHeader(new InlineHTML("<small class=\"margin-right-10\">"+questionNumber+".</small>"+SimpleHtmlSanitizer.sanitizeHtml(question.getPrompt()).asString()+"</small>"));
		final MultichoiceQuestion multichoiceQuestion = (MultichoiceQuestion) question;
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
							GWT.debugger();
							answers.clear();
							answers.add(answer.getAnswerIndex());
						}
					});
					answerContainer.add(answerButton.asWidget());
					view.addAnswer(answerContainer.asWidget());
					//handleIfPreviouslyAnswered(answerButton, response, answer.getAnswerIndex());
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
							GWT.debugger();
							//not exclusive, include all possible answer indexes
							if (checkbox.getValue()) {
								answers.add(answer.getAnswerIndex());	
							} else {
								answers.remove(answer.getAnswerIndex());
							}
						}
					});
					answerContainer.add(checkbox);
					view.addAnswer(answerContainer);
					//handleIfPreviouslyAnswered(checkbox, response, answer.getAnswerIndex());
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
			html.setHTML(DisplayUtils.getIcon("glyphicon-ok font-size-15 text-success"));
		} else {
			html.setHTML(DisplayUtils.getIcon("glyphicon-remove font-size-15 text-danger"));
			view.addStyleName("has-error");
		}
		
		
		
		// CONSIDER RENAMING THE METHOD FOR THIS USE CASE!!!!!!!!
		view.addAnswer(html);
	}
	
	
	@Override
	public Set<Long> getAnswers() {
		return answers;
	}
	
//	private void handleIfPreviouslyAnswered(CheckBox checkbox, MultichoiceResponse response, Long answerIndex) {
//		if (response != null) {
//			if (response.getAnswerIndex().contains(answerIndex))
//				checkbox.setValue(true);
//			checkbox.setEnabled(false);
//		}
//	}
	
//	private Set<Long> getAnswerIndexes(Long questionIndex) {
//		Set<Long> answers = questionIndex2AnswerIndices.get(questionIndex);
//		if (answers == null) {
//			answers = new HashSet<Long>();
//			questionIndex2AnswerIndices.put(questionIndex, answers);
//		}
//		return answers;
//	}

	public Widget asWidget() {
		return view.asWidget();
	}
}
