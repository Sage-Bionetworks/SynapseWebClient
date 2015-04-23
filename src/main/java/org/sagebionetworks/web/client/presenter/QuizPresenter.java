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
import org.sagebionetworks.web.client.view.QuizView;
import org.sagebionetworks.web.client.widget.entity.registration.QuestionContainerWidget;
import org.sagebionetworks.web.shared.exceptions.NotFoundException;

import com.google.gwt.activity.shared.AbstractActivity;
import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.event.dom.client.ClickHandler;
import com.google.gwt.event.shared.EventBus;
import com.google.gwt.place.shared.Place;
import com.google.gwt.user.client.rpc.AsyncCallback;
import com.google.gwt.user.client.ui.AcceptsOneWidget;
import com.google.gwt.user.client.ui.FlowPanel;
import com.google.gwt.user.client.ui.HTML;
import com.google.gwt.user.client.ui.InlineHTML;
import com.google.inject.Inject;

public class QuizPresenter extends AbstractActivity implements QuizView.Presenter, Presenter<org.sagebionetworks.web.client.place.Quiz> {

	private org.sagebionetworks.web.client.place.Quiz testPlace;
	private QuizView view;
	private GlobalApplicationState globalApplicationState;
	private AuthenticationController authenticationController;
	private SynapseClientAsync synapseClient;
	private AdapterFactory adapterFactory;
	private JSONObjectAdapter jsonObjectAdapter;
	private Quiz quiz;
	private PortalGinInjector ginInjector;
	private boolean isSubmitInitialized;
	private Map<Long, Set<Long>> questionIndex2AnswerIndices;
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
		this.jsonObjectAdapter = jsonObjectAdapter;
		this.questionIndex2AnswerIndices = new HashMap<Long, Set<Long>>();
		this.view.setPresenter(this);
		this.isSubmitInitialized = false;
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
		questionIndexToQuestionWidget = new HashMap<Long, QuestionContainerWidget>();
		final int currentQuestionCount = questions.size();
		Long questionNumber = Long.valueOf(1);
		for (Question question : questions) {
			QuestionContainerWidget newQuestion = ginInjector.getQuestionContainerWidget();
			questionIndexToQuestionWidget.put(questionNumber, newQuestion);
			newQuestion.configure(questionNumber++, question, null);
			view.addQuestionContainerWidget(newQuestion.asWidget());
		}
		
		//initialize if necessary
		if (!isSubmitInitialized) {
			isSubmitInitialized = true;
			view.addSubmitHandler(new ClickHandler() {
				@Override
				public void onClick(ClickEvent event) {
					//gather answers and pass them back to the presenter
					if (questionIndex2AnswerIndices.keySet().size() < currentQuestionCount) {
						view.showErrorMessage(DisplayConstants.ERROR_ALL_QUESTIONS_REQUIRED);
					} else {
						view.setSubmitEnabled(false);
						submitAnswers();
					}
						
				}
			});
		}
		view.reset();
//		quizContainer.setVisible(true);
//		submitButton.setVisible(true);
//		submitButton.setEnabled(true);
	}
	
	@Override
	public void submitAnswers() {
		try {
			//submit question/answer combinations for approval
			//create response object from answers
			QuizResponse submission = new QuizResponse();
			List<QuestionResponse> questionResponses = new ArrayList<QuestionResponse>();
//			for (Long questionIndex : questionIndex2AnswerIndices.keySet()) {
//				Set<Long> answerIndices = questionIndex2AnswerIndices.get(questionIndex);
//				MultichoiceResponse response = new MultichoiceResponse();
//				response.setQuestionIndex(questionIndex);
//				response.setAnswerIndex(answerIndices);
//				questionResponses.add(response);
//			}
			for (Long questionIndex : questionIndexToQuestionWidget.keySet()) {
				Set<Long> answers = questionIndexToQuestionWidget.get(questionIndex).getAnswers();
				MultichoiceResponse response = new MultichoiceResponse();
				response.setQuestionIndex(questionIndex);
				response.setAnswerIndex(answers);
				questionResponses.add(response);
			}
			submission.setQuestionResponses(questionResponses);
			JSONObjectAdapter adapter = submission.writeToJSONObject(jsonObjectAdapter.createNew());
			String questionnaireResponse = adapter.toJSONString();
			synapseClient.submitCertificationQuizResponse(questionnaireResponse, new AsyncCallback<String>() {
				@Override
				public void onSuccess(String passingRecordJson) {
					try {
						PassingRecord passingRecord = new PassingRecord(adapterFactory.createNew(passingRecordJson));
						if (passingRecord.getPassed())
							view.showSuccess(authenticationController.getCurrentUserSessionData().getProfile(), passingRecord);
						else
							view.showFailure(passingRecord);
					} catch (JSONObjectAdapterException e) {
						onFailure(e);
					}
				}
				
				@Override
				public void onFailure(Throwable caught) {
					if(!DisplayUtils.handleServiceException(caught, globalApplicationState, authenticationController.isLoggedIn(), view)) {					
						view.showErrorMessage(caught.getMessage());
					} 
				}
			});
		} catch (JSONObjectAdapterException e) {
			view.showErrorMessage(DisplayConstants.ERROR_INCOMPATIBLE_CLIENT_VERSION);
		}
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
		//go through and highlight correct/incorrect answers
		view.clearTestContainer();
		if (passingRecord.getCorrections() == null)
			return;
		Long questionNumber = Long.valueOf(1);
		for (ResponseCorrectness correctness : passingRecord.getCorrections()) {
			//indicate success/failure
			if (correctness.getQuestion() != null) {
				QuestionContainerWidget question = ginInjector.getQuestionContainerWidget();
				question.configure(questionNumber++, correctness.getQuestion(), (MultichoiceResponse)correctness.getResponse());
				view.addQuestionContainerWidget(question.asWidget());
				HTML html = new InlineHTML();
				html.addStyleName("margin-right-5");
				question.addCorrectnessStyle(correctness.getIsCorrect());
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
		this.testPlace = place;
		view.setPresenter(this);
		view.clear();
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
