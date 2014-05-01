package org.sagebionetworks.web.client.view;

import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.sagebionetworks.repo.model.UserProfile;
import org.sagebionetworks.repo.model.quiz.MultichoiceAnswer;
import org.sagebionetworks.repo.model.quiz.MultichoiceQuestion;
import org.sagebionetworks.repo.model.quiz.PassingRecord;
import org.sagebionetworks.repo.model.quiz.Question;
import org.sagebionetworks.repo.model.quiz.Quiz;
import org.sagebionetworks.web.client.DisplayConstants;
import org.sagebionetworks.web.client.DisplayUtils;
import org.sagebionetworks.web.client.IconsImageBundle;
import org.sagebionetworks.web.client.SageImageBundle;
import org.sagebionetworks.web.client.place.Help;
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
import com.google.gwt.user.client.ui.Button;
import com.google.gwt.user.client.ui.CheckBox;
import com.google.gwt.user.client.ui.Composite;
import com.google.gwt.user.client.ui.FlowPanel;
import com.google.gwt.user.client.ui.HTMLPanel;
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
	DivElement quizHighlightBox;
	
	@UiField
	FlowPanel testContainer;
	@UiField
	Button submitButton;
	
	@UiField
	SimplePanel successContainer;
	
	@UiField
	HTMLPanel failureContainer;
	@UiField
	Button tutorialButton1;
	@UiField
	Button tutorialButton2;

	
	
	private Presenter presenter;
	private IconsImageBundle iconsImageBundle;
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
	public QuizViewImpl(Binder uiBinder, IconsImageBundle icons,
			Header headerWidget, Footer footerWidget,
			SageImageBundle sageImageBundle, LoginWidget loginWidget, 
			CertificateWidget certificateWidget) {
		initWidget(uiBinder.createAndBindUi(this));
		this.iconsImageBundle = icons;
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
		ClickHandler gotoGettingStarted = new ClickHandler() {
			@Override
			public void onClick(ClickEvent event) {
				presenter.goTo(new Help(WebConstants.GETTING_STARTED));
			}
		};
		
		ClickHandler gotoGettingStartedNewWindow = new ClickHandler() {
			@Override
			public void onClick(ClickEvent event) {
				DisplayUtils.newWindow(DisplayUtils.getHelpPlaceHistoryToken(WebConstants.GETTING_STARTED), "", "");
			}
		};
		
		tutorialButton1.addClickHandler(gotoGettingStartedNewWindow);
		tutorialButton2.addClickHandler(gotoGettingStarted);
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
		hideAll();
		if (quiz.getHeader() != null)
			quizHighlightBox.setAttribute("title", quiz.getHeader());
		//clear old questions
		clear();
		List<Question> questions = quiz.getQuestions();
		currentQuestionCount = questions.size();
		int questionNumber = 1;
		for (Question question : questions) {
			testContainer.add(addQuestion(questionNumber++, question));
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
					} else
						presenter.submitAnswers(questionIndex2AnswerIndices);
				}
			});
		}
		quizContainer.setVisible(true);
    }
	
	@Override
	public void showSuccess(UserProfile profile, PassingRecord passingRecord) {
		hideAll();
		certificateWidget.configure(profile, passingRecord);
		successContainer.setVisible(true);
	}
	
	@Override
	public void showFailure() {
		hideAll();
		failureContainer.setVisible(true);
	}

	private FlowPanel addQuestion(int questionNumber, Question question) {
		FlowPanel questionContainer = new FlowPanel();
		if (question instanceof MultichoiceQuestion) {
			final MultichoiceQuestion multichoiceQuestion = (MultichoiceQuestion)question;
			questionContainer.addStyleName("margin-bottom-40 margin-left-15");
			questionContainer.add(new HTMLPanel("<h5 class=\"inline-block\"><small>"+questionNumber+". </small>"+SimpleHtmlSanitizer.sanitizeHtml(question.getPrompt()).asString()+"</small></h5>"));
			//now add possible answers
			
			boolean isRadioButton = multichoiceQuestion.getExclusive();
			if (isRadioButton) {
				for (final MultichoiceAnswer answer : multichoiceQuestion.getAnswers()) {
					SimplePanel answerContainer = new SimplePanel();
					answerContainer.addStyleName("radio margin-left-15");
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
				}
			} else {
				//checkbox
				for (final MultichoiceAnswer answer : multichoiceQuestion.getAnswers()) {
					SimplePanel answerContainer = new SimplePanel();
					answerContainer.addStyleName("checkbox margin-left-15");
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
				}
			}
		}
		return questionContainer;
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
		failureContainer.setVisible(false);
	}

	@Override
	public void hideLoading() {
		if(loadingWindow != null) loadingWindow.hide();
	}
}
