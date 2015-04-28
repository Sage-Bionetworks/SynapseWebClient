package org.sagebionetworks.web.client.presenter;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.sagebionetworks.repo.model.UserProfile;
import org.sagebionetworks.repo.model.quiz.MultichoiceResponse;
import org.sagebionetworks.repo.model.quiz.PassingRecord;
import org.sagebionetworks.repo.model.quiz.Question;
import org.sagebionetworks.repo.model.quiz.QuestionResponse;
import org.sagebionetworks.repo.model.quiz.Quiz;
import org.sagebionetworks.repo.model.quiz.QuizResponse;
import org.sagebionetworks.repo.model.quiz.ResponseCorrectness;
import org.sagebionetworks.schema.adapter.AdapterFactory;
import org.sagebionetworks.schema.adapter.JSONObjectAdapter;
import org.sagebionetworks.schema.adapter.JSONObjectAdapterException;
import org.sagebionetworks.web.client.DisplayConstants;
import org.sagebionetworks.web.client.DisplayUtils;
import org.sagebionetworks.web.client.GlobalApplicationState;
import org.sagebionetworks.web.client.PortalGinInjector;
import org.sagebionetworks.web.client.SynapseClientAsync;
import org.sagebionetworks.web.client.security.AuthenticationController;
import org.sagebionetworks.web.client.view.QuestionContainerWidget;
import org.sagebionetworks.web.client.view.QuizView;
import org.sagebionetworks.web.shared.exceptions.NotFoundException;

import com.google.gwt.activity.shared.AbstractActivity;
import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.event.dom.client.ClickHandler;
import com.google.gwt.event.shared.EventBus;
import com.google.gwt.place.shared.Place;
import com.google.gwt.user.client.rpc.AsyncCallback;
import com.google.gwt.user.client.ui.AcceptsOneWidget;
import com.google.gwt.user.client.ui.HTML;
import com.google.gwt.user.client.ui.InlineHTML;
import com.google.inject.Inject;

public class QuizPresenter extends AbstractActivity implements QuizView.Presenter, Presenter<org.sagebionetworks.web.client.place.Quiz> {

	private QuizView view;
	private GlobalApplicationState globalApplicationState;
	private AuthenticationController authenticationController;
	private SynapseClientAsync synapseClient;
	private AdapterFactory adapterFactory;
	private Quiz quiz;
	private PortalGinInjector ginInjector;
	private boolean isSubmitInitialized;
	private Map<Long, QuestionContainerWidget> questionIndexToQuestionWidget;
	
	@Inject
	public QuizPresenter(QuizView view,  
			AuthenticationController authenticationController, 
			GlobalApplicationState globalApplicationState,
			SynapseClientAsync synapseClient,
			AdapterFactory adapterFactory,
			JSONObjectAdapter jsonObjectAdapter,
			PortalGinInjector ginInjector){
		this.view = view;
		// Set the presenter on the view
		this.authenticationController = authenticationController;
		this.globalApplicationState = globalApplicationState;
		this.synapseClient = synapseClient;
		this.adapterFactory = adapterFactory;
		this.ginInjector = ginInjector;
		this.view.setPresenter(this);
		questionIndexToQuestionWidget = new HashMap<Long, QuestionContainerWidget>();
	}
	
	@Override
	public void start(AcceptsOneWidget panel, EventBus eventBus) {
		panel.setWidget(this.view.asWidget());
	}

	
	@Override
	public void goTo(Place place) {
		globalApplicationState.getPlaceChanger().goTo(place);
	}
	
	@Override
	public void goToLastPlace() {
		view.hideLoading();
		globalApplicationState.gotoLastPlace();
	}
	
	@Override
	public void showQuiz(Quiz quiz) {
		view.clear();
		if (quiz.getHeader() != null)
			view.setQuizHeader(quiz.getHeader());
		//clear old questions
		List<Question> questions = quiz.getQuestions();
		final int currentQuestionCount = questions.size();
		Long questionNumber = Long.valueOf(1);
		for (Question question : questions) {
			QuestionContainerWidget newQuestion = ginInjector.getQuestionContainerWidget();
			questionIndexToQuestionWidget.put(questionNumber, newQuestion);
			newQuestion.configure(questionNumber, question, null);
			view.addQuestionContainerWidget(newQuestion.asWidget());
			questionNumber++;
		}
		
		//initialize if necessary
		if (!isSubmitInitialized) {
			isSubmitInitialized = true;
			view.addSubmitHandler(new ClickHandler() {
				@Override
				public void onClick(ClickEvent event) {
					if (!checkAllAnswered()) {
						view.showErrorMessage(DisplayConstants.ERROR_ALL_QUESTIONS_REQUIRED);
					} else {
						view.setSubmitEnabled(false);
						submitAnswers();
					}
						
				}
			});
		}
		view.reset();
	}
	
	private boolean checkAllAnswered() {
		for (Long questionNumber : questionIndexToQuestionWidget.keySet()) {
			if (questionIndexToQuestionWidget.get(questionNumber).getAnswers().isEmpty()) 
				return false;
		}
		return true;
	}
	
	// For testing only
	public void setQuestionIndexToQuestionWidgetMap(Map<Long, QuestionContainerWidget> ans) {
		this.questionIndexToQuestionWidget = ans;
	}
	
	@Override
	public void submitAnswers() {
		//submit question/answer combinations for approval
		//create response object from answers
		QuizResponse submission = new QuizResponse();
		List<QuestionResponse> questionResponses = new ArrayList<QuestionResponse>();

		for (Long questionNumber : questionIndexToQuestionWidget.keySet()) {
			QuestionContainerWidget questionWidget = questionIndexToQuestionWidget.get(questionNumber);
			Set<Long> answers = questionWidget.getAnswers();
			Long questionIndex = questionWidget.getQuestionIndex();
			MultichoiceResponse response = new MultichoiceResponse();
			response.setQuestionIndex(questionIndex);
			response.setAnswerIndex(answers);
			questionResponses.add(response);
		}
		submission.setQuestionResponses(questionResponses);
		synapseClient.submitCertificationQuizResponse(submission, new AsyncCallback<PassingRecord>() {
			@Override
			public void onSuccess(PassingRecord passingRecord) {
				if (passingRecord.getPassed())
					showSuccess(authenticationController.getCurrentUserSessionData().getProfile(), passingRecord);
				else
					showFailure(passingRecord);
			}
			
			@Override
			public void onFailure(Throwable caught) {
				if(!DisplayUtils.handleServiceException(caught, globalApplicationState, authenticationController.isLoggedIn(), view)) {					
					view.showErrorMessage(caught.getMessage());
				} 
			}
		});
	}
	
	@Override
	public void showSuccess(UserProfile profile, PassingRecord passingRecord) {
		scoreQuiz(passingRecord);
		//show success UI (certificate) and quiz
		view.showSuccess(profile, passingRecord);
	}
	
	@Override
	public void showFailure(PassingRecord passingRecord) {
		scoreQuiz(passingRecord);
		//show failure message and quiz
		view.showFailure(passingRecord);
	}
	
	private void scoreQuiz(PassingRecord passingRecord) {
		if (passingRecord.getCorrections() == null)
			return;
		Long questionNumber = Long.valueOf(1);
		for (ResponseCorrectness correctness : passingRecord.getCorrections()) {			
			//indicate success/failure
			if (correctness.getQuestion() != null) {
				QuestionContainerWidget question = questionIndexToQuestionWidget.get(questionNumber++);
				question.addCorrectnessStyle(correctness.getIsCorrect());
				question.setEnabled(false);
			}
		}
		//scored quiz cannot be resubmitted
		view.setSubmitVisible(false);		
		if (passingRecord.getCorrections() != null) {
			view.showScore("Score: " + passingRecord.getScore() + "/" + passingRecord.getCorrections().size());
		}
	}
	

	@Override
    public String mayStop() {
        view.clear();
        return null;
    }

	@Override
	public void setPlace(org.sagebionetworks.web.client.place.Quiz place) {
		view.setPresenter(this);
		view.clear();
		this.isSubmitInitialized = false;
		questionIndexToQuestionWidget = new HashMap<Long, QuestionContainerWidget>();
		getIsCertified();
	}
	
	public void getIsCertified() {
		view.showLoading();
		synapseClient.getCertifiedUserPassingRecord(authenticationController.getCurrentUserPrincipalId(), new AsyncCallback<String>() {
			@Override
			public void onSuccess(String passingRecordJson) {
				try {
					//if certified, show the certificate
					//otherwise, show the quiz
					PassingRecord passingRecord = new PassingRecord(adapterFactory.createNew(passingRecordJson));
					view.hideLoading();
					view.showSuccess(authenticationController.getCurrentUserSessionData().getProfile(), passingRecord);
				
				} catch (JSONObjectAdapterException e) {
					onFailure(e);
				}
			}
			@Override
			public void onFailure(Throwable caught) {
				view.hideLoading();
				if (caught instanceof NotFoundException) {
					getQuiz();
				} else {
					if (!DisplayUtils.handleServiceException(caught, globalApplicationState, authenticationController.isLoggedIn(), view)) {
						view.showErrorMessage(caught.getMessage());
					}
				}
			}
		});
	}
	
	public void getQuiz() {
		view.showLoading();
		synapseClient.getCertificationQuiz(new AsyncCallback<String>() {
			@Override
			public void onSuccess(String quizJson) {
				try {
					quiz = new Quiz(adapterFactory.createNew(quizJson));
					view.hideLoading();
					showQuiz(quiz);
				} catch (JSONObjectAdapterException e) {
					onFailure(e);
				}
			}
			
			@Override
			public void onFailure(Throwable caught) {
				view.hideLoading();
				if(!DisplayUtils.handleServiceException(caught, globalApplicationState, authenticationController.isLoggedIn(), view)) {					
					view.showErrorMessage(caught.getMessage());
				} 
			}
		});
	}
}
