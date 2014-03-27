package org.sagebionetworks.web.client.view;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.sagebionetworks.repo.model.UserProfile;
import org.sagebionetworks.repo.model.questionnaire.MultichoiceAnswer;
import org.sagebionetworks.repo.model.questionnaire.MultichoiceQuestion;
import org.sagebionetworks.repo.model.questionnaire.Question;
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
import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.event.dom.client.ClickHandler;
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
	FlowPanel testContainer;
	@UiField
	Button submitButton;
	
	@UiField
	SimplePanel successContainer;
	
	@UiField
	HTMLPanel failureContainer;
	@UiField
	Button tutorialButton;

	
	
	private Presenter presenter;
	private IconsImageBundle iconsImageBundle;
	private SageImageBundle sageImageBundle;
	private CertificateWidget certificateWidget;
	private Window loadingWindow;
	private Header headerWidget;
	private Footer footerWidget;
	public interface Binder extends UiBinder<Widget, QuizViewImpl> {}
	boolean isSubmitInitialized;
	Map<Long, List<Long>> questionIndex2AnswerIndices; 
	
	
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
		questionIndex2AnswerIndices = new HashMap<Long, List<Long>>();
		
		tutorialButton.addClickHandler(new ClickHandler() {
			@Override
			public void onClick(ClickEvent event) {
				presenter.goTo(new Help(WebConstants.USER_CERTIFICATION_TUTORIAL));
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
		testContainer.clear();
		questionIndex2AnswerIndices.clear();
		hideLoading();
	}
	
	@Override
	public void showQuiz(List<Question> quiz) {
		hideAll();
		//clear old questions
		clear();
		int questionNumber = 1;
		for (Question question : quiz) {
			testContainer.add(addQuestion(questionNumber++, question));
		}
		
		//initialize if necessary
		if (!isSubmitInitialized) {
			isSubmitInitialized = true;
			submitButton.addClickHandler(new ClickHandler() {
				@Override
				public void onClick(ClickEvent event) {
					//gather answers and pass them back to the presenter
					presenter.submitAnswers(questionIndex2AnswerIndices);
				}
			});
		}
		quizContainer.setVisible(true);
    }
	
	@Override
	public void showSuccess(UserProfile profile) {
		hideAll();
		certificateWidget.setProfile(profile);
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
			questionContainer.add(new HTMLPanel("<h5 class=\"inline-block\"><small>"+questionNumber+". </small>"+question.getPrompt()+"</small></h5>"));
			//now add possible answers
			
			boolean isRadioButton = multichoiceQuestion.getExclusive();
			if (isRadioButton) {
				for (final MultichoiceAnswer answer : multichoiceQuestion.getAnswers()) {
					SimplePanel answerContainer = new SimplePanel();
					answerContainer.addStyleName("radio margin-left-15");
					RadioButton answerButton = new RadioButton("question-"+question.getQuestionIndex(), answer.getPrompt());
					answerButton.addClickHandler(new ClickHandler() {
						@Override
						public void onClick(ClickEvent event) {
							List<Long> answers = getAnswerIndexes(multichoiceQuestion.getQuestionIndex());
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
					final CheckBox checkbox= new CheckBox(answer.getPrompt());
					checkbox.addClickHandler(new ClickHandler() {
						@Override
						public void onClick(ClickEvent event) {
							//not exclusive, include all possible answer indexes
							List<Long> answers = getAnswerIndexes(multichoiceQuestion.getQuestionIndex());
							if (checkbox.getValue()) {
								if (!answers.contains(answer.getAnswerIndex()))
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
	
	private List<Long> getAnswerIndexes(Long questionIndex) {
		List<Long> answers = questionIndex2AnswerIndices.get(questionIndex);
		if (answers == null) {
			answers = new ArrayList<Long>();
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
