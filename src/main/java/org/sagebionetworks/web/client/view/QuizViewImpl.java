package org.sagebionetworks.web.client.view;

import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.sagebionetworks.repo.model.UserProfile;
import org.sagebionetworks.repo.model.dao.WikiPageKey;
import org.sagebionetworks.repo.model.quiz.MultichoiceAnswer;
import org.sagebionetworks.repo.model.quiz.MultichoiceQuestion;
import org.sagebionetworks.repo.model.quiz.MultichoiceResponse;
import org.sagebionetworks.repo.model.quiz.PassingRecord;
import org.sagebionetworks.repo.model.quiz.Question;
import org.sagebionetworks.repo.model.quiz.QuestionResponse;
import org.sagebionetworks.repo.model.quiz.Quiz;
import org.sagebionetworks.repo.model.quiz.ResponseCorrectness;
import org.sagebionetworks.web.client.DisplayConstants;
import org.sagebionetworks.web.client.DisplayUtils;
import org.sagebionetworks.web.client.SageImageBundle;
import org.sagebionetworks.web.client.place.Wiki;
import org.sagebionetworks.web.client.widget.entity.download.CertificateWidget;
import org.sagebionetworks.web.client.widget.footer.Footer;
import org.sagebionetworks.web.client.widget.header.Header;
import org.sagebionetworks.web.client.widget.login.LoginWidget;
import org.sagebionetworks.web.shared.WebConstants;

import com.extjs.gxt.ui.client.widget.Window;
import com.google.gwt.dom.client.DivElement;
import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.event.dom.client.ClickHandler;
import com.google.gwt.safehtml.shared.SimpleHtmlSanitizer;
import com.google.gwt.uibinder.client.UiBinder;
import com.google.gwt.uibinder.client.UiField;
import com.google.gwt.user.client.ui.Anchor;
import com.google.gwt.user.client.ui.Button;
import com.google.gwt.user.client.ui.CheckBox;
import com.google.gwt.user.client.ui.Composite;
import com.google.gwt.user.client.ui.FlowPanel;
import com.google.gwt.user.client.ui.HTML;
import com.google.gwt.user.client.ui.HTMLPanel;
import com.google.gwt.user.client.ui.InlineHTML;
import com.google.gwt.user.client.ui.RadioButton;
import com.google.gwt.user.client.ui.SimplePanel;
import com.google.gwt.user.client.ui.Widget;
import com.google.inject.Inject;

public class QuizViewImpl extends Composite implements QuizView {
	
	@UiField
	SimplePanel header;
	@UiField
	SimplePanel footer;

	@UiField
	HTMLPanel quizContainer;
	
	@UiField
	DivElement quizHeader;
	
	@UiField
	FlowPanel testContainer;
	@UiField
	Button submitButton;
	
	@UiField
	SimplePanel successContainer;
	
	@UiField
	DivElement quizFailure;
	
	@UiField
	DivElement scoreContainer;
	
	@UiField
	Button tutorialButton1;
	
	@UiField
	Anchor tryAgainLink;
	
	
	private Presenter presenter;
	private SageImageBundle sageImageBundle;
	private CertificateWidget certificateWidget;
	private Window loadingWindow;
	private Header headerWidget;
	private Footer footerWidget;
	public interface Binder extends UiBinder<Widget, QuizViewImpl> {}
	boolean isSubmitInitialized;
	Map<Long, Set<Long>> questionIndex2AnswerIndices; 
	private int currentQuestionCount;
	
	@Inject
	public QuizViewImpl(Binder uiBinder,
			Header headerWidget, 
			Footer footerWidget,
			SageImageBundle sageImageBundle, 
			LoginWidget loginWidget, 
			CertificateWidget certificateWidget) {
		initWidget(uiBinder.createAndBindUi(this));
		this.sageImageBundle = sageImageBundle;
		this.headerWidget = headerWidget;
		this.footerWidget = footerWidget;
		this.certificateWidget = certificateWidget;
		headerWidget.configure(false);
		header.add(headerWidget.asWidget());
		footer.add(footerWidget.asWidget());
		successContainer.setWidget(certificateWidget.asWidget());
		
		isSubmitInitialized = false;
		questionIndex2AnswerIndices = new HashMap<Long, Set<Long>>();
		ClickHandler gotoGettingStartedNewWindow = new ClickHandler() {
			@Override
			public void onClick(ClickEvent event) {
				DisplayUtils.newWindow(DisplayUtils.getHelpPlaceHistoryToken(WebConstants.GETTING_STARTED), "", "");
			}
		};
		
		tutorialButton1.addClickHandler(gotoGettingStartedNewWindow);
		quizHeader.setInnerHTML("<h3>Certification Quiz</h3>");
		
		tryAgainLink.addClickHandler(new ClickHandler() {
			@Override
			public void onClick(ClickEvent event) {
				presenter.goTo(new org.sagebionetworks.web.client.place.Quiz(WebConstants.CERTIFICATION));
			}
		});
	}

	@Override
	public void setPresenter(Presenter loginPresenter) {
		this.presenter = loginPresenter;
		header.clear();
		headerWidget.configure(false);
		header.add(headerWidget.asWidget());
		footer.clear();
		footer.add(footerWidget.asWidget());
		com.google.gwt.user.client.Window.scrollTo(0, 0); // scroll user to top of page
	}
	
	@Override
	public void showErrorMessage(String message) {
		DisplayUtils.showErrorMessage(message);
	}

	@Override
	public void showLoading() {
		if(loadingWindow == null) {
			loadingWindow = DisplayUtils.createLoadingWindow(sageImageBundle, "");
		}
		loadingWindow.show();

	}

	@Override
	public void showInfo(String title, String message) {
		DisplayUtils.showInfo(title, message);
	}


	@Override
	public void clear() {
		hideAll();
		testContainer.clear();
		questionIndex2AnswerIndices.clear();
		hideLoading();
	}
	
	@Override
	public void showQuiz(Quiz quiz) {
		clear();
		if (quiz.getHeader() != null)
			quizHeader.setInnerHTML(SimpleHtmlSanitizer.sanitizeHtml(quiz.getHeader()).asString());
		//clear old questions
		List<Question> questions = quiz.getQuestions();
		currentQuestionCount = questions.size();
		int questionNumber = 1;
		for (Question question : questions) {
			FlowPanel questionUI = addQuestion(questionNumber++, question, null);
			testContainer.add(questionUI);
		}
		
		//initialize if necessary
		if (!isSubmitInitialized) {
			isSubmitInitialized = true;
			submitButton.addClickHandler(new ClickHandler() {
				@Override
				public void onClick(ClickEvent event) {
					//gather answers and pass them back to the presenter
					if (questionIndex2AnswerIndices.keySet().size() < currentQuestionCount) {
						showErrorMessage(DisplayConstants.ERROR_ALL_QUESTIONS_REQUIRED);
					} else {
						submitButton.setEnabled(false);
						presenter.submitAnswers(questionIndex2AnswerIndices);
					}
						
				}
			});
		}
		quizContainer.setVisible(true);
		submitButton.setVisible(true);
		submitButton.setEnabled(true);
    }
	
	@Override
	public void showSuccess(UserProfile profile, PassingRecord passingRecord) {
		hideAll();
		scoreQuiz(passingRecord);
		//show success UI (certificate) and quiz
		certificateWidget.configure(profile, passingRecord);
		successContainer.setVisible(true);
		quizContainer.setVisible(true);
		DisplayUtils.scrollToTop();
	}
	private void scoreQuiz(PassingRecord passingRecord) {
		//go through and highlight correct/incorrect answers
		testContainer.clear();
		int questionNumber = 1;
		for (ResponseCorrectness correctness : passingRecord.getCorrections()) {
			//indicate success/failure
			if (correctness.getQuestion() != null) {
				FlowPanel questionUI = addQuestion(questionNumber++, correctness.getQuestion(), (MultichoiceResponse)correctness.getResponse());
				testContainer.add(questionUI);

				HTML html = new InlineHTML();
				html.addStyleName("margin-right-5");
				if (correctness.getIsCorrect()) {
					//green checkmark
					html.setHTML(DisplayUtils.getIcon("glyphicon-ok font-size-15 text-success"));
				} else {
					//red X
					html.setHTML(DisplayUtils.getIcon("glyphicon-remove font-size-15 text-danger"));
					questionUI.addStyleName("has-error");
				}
				questionUI.insert(html, 0);
			}
		}
		//scored quiz cannot be resubmitted
		submitButton.setVisible(false);
		DisplayUtils.show(scoreContainer);
		scoreContainer.setInnerHTML("Score: " + passingRecord.getScore() + "/" + passingRecord.getCorrections().size() );
	}
	
	@Override
	public void showFailure(PassingRecord passingRecord) {
		hideAll();
		scoreQuiz(passingRecord);
		//show failure message and quiz
		DisplayUtils.show(quizFailure);
		quizFailure.scrollIntoView();
		quizContainer.setVisible(true);
	}

	private FlowPanel addQuestion(int questionNumber, Question question, MultichoiceResponse response) {
		FlowPanel parentQuestionContainer = new FlowPanel();
		if (question instanceof MultichoiceQuestion) {
			FlowPanel questionContainer = new FlowPanel();
			final MultichoiceQuestion multichoiceQuestion = (MultichoiceQuestion)question;
			questionContainer.addStyleName("margin-bottom-40 margin-left-15");
			questionContainer.add(new InlineHTML("<h5 class=\"inline-block control-label\"><small class=\"margin-right-10\">"+questionNumber+".</small>"+SimpleHtmlSanitizer.sanitizeHtml(question.getPrompt()).asString()+"</small></h5>"));
			//now add possible answers
			boolean isRadioButton = multichoiceQuestion.getExclusive();
			if (isRadioButton) {
				for (final MultichoiceAnswer answer : multichoiceQuestion.getAnswers()) {
					SimplePanel answerContainer = new SimplePanel();
					answerContainer.addStyleName("radio padding-left-30 control-label");
					RadioButton answerButton = new RadioButton("question-"+question.getQuestionIndex());
					answerButton.setHTML(SimpleHtmlSanitizer.sanitizeHtml(answer.getPrompt()));
					answerButton.addClickHandler(new ClickHandler() {
						@Override
						public void onClick(ClickEvent event) {
							Set<Long> answers = getAnswerIndexes(multichoiceQuestion.getQuestionIndex());
							answers.clear();
							answers.add(answer.getAnswerIndex());
						}
					});
					answerContainer.add(answerButton);
					questionContainer.add(answerContainer);
					handleIfPreviouslyAnswered(answerButton, response, answer.getAnswerIndex());
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
							Set<Long> answers = getAnswerIndexes(multichoiceQuestion.getQuestionIndex());
							if (checkbox.getValue()) {
								answers.add(answer.getAnswerIndex());	
							} else {
								answers.remove(answer.getAnswerIndex());
							}
						}
					});
					answerContainer.add(checkbox);
					questionContainer.add(answerContainer);
					handleIfPreviouslyAnswered(checkbox, response, answer.getAnswerIndex());
				}
			}
			
			//add help reference
			final WikiPageKey moreInfoKey = question.getReference();
			if (moreInfoKey != null && moreInfoKey.getOwnerObjectId() != null) {
				Anchor moreInfoLink = new Anchor();
				moreInfoLink.setHTML(DisplayUtils.getIcon("glyphicon-question-sign font-size-15") + "<span class=\"margin-left-5\">Need help answering this question?</span>");
				moreInfoLink.setTarget("_blank");
				moreInfoLink.addStyleName("margin-left-9");
				Wiki place = new Wiki(moreInfoKey.getOwnerObjectId(), moreInfoKey.getOwnerObjectType().name(), moreInfoKey.getWikiPageId());
				moreInfoLink.setHref("#!Wiki:" + place.toToken());
				questionContainer.add(moreInfoLink);
			}
			parentQuestionContainer.add(questionContainer);
		}
		return parentQuestionContainer;
	}
	private void handleIfPreviouslyAnswered(CheckBox checkbox, MultichoiceResponse response, Long answerIndex) {
		if (response != null) {
			if (response.getAnswerIndex().contains(answerIndex))
				checkbox.setValue(true);
			checkbox.setEnabled(false);
		}
	}
	private Set<Long> getAnswerIndexes(Long questionIndex) {
		Set<Long> answers = questionIndex2AnswerIndices.get(questionIndex);
		if (answers == null) {
			answers = new HashSet<Long>();
			questionIndex2AnswerIndices.put(questionIndex, answers);
		}
		return answers;
	}
	
	private void hideAll() {
		quizContainer.setVisible(false);
		successContainer.setVisible(false);
		DisplayUtils.hide(quizFailure);
		DisplayUtils.hide(scoreContainer);
	}

	@Override
	public void hideLoading() {
		if(loadingWindow != null) loadingWindow.hide();
	}
}
