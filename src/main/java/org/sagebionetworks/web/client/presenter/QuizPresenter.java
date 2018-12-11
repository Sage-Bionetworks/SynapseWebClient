package org.sagebionetworks.web.client.presenter;

import static org.sagebionetworks.web.client.ServiceEntryPointUtils.fixServiceEntryPoint;

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
import org.sagebionetworks.web.client.GlobalApplicationState;
import org.sagebionetworks.web.client.PortalGinInjector;
import org.sagebionetworks.web.client.SynapseClientAsync;
import org.sagebionetworks.web.client.security.AuthenticationController;
import org.sagebionetworks.web.client.view.QuizView;
import org.sagebionetworks.web.client.widget.entity.controller.SynapseAlert;
import org.sagebionetworks.web.shared.exceptions.NotFoundException;

import com.google.gwt.activity.shared.AbstractActivity;
import com.google.gwt.event.shared.EventBus;
import com.google.gwt.place.shared.Place;
import com.google.gwt.user.client.rpc.AsyncCallback;
import com.google.gwt.user.client.ui.AcceptsOneWidget;
import com.google.inject.Inject;

public class QuizPresenter extends AbstractActivity implements QuizView.Presenter, Presenter<org.sagebionetworks.web.client.place.Quiz> {

	private QuizView view;
	private GlobalApplicationState globalApplicationState;
	private AuthenticationController authenticationController;
	private SynapseClientAsync synapseClient;
	private AdapterFactory adapterFactory;
	private Quiz quiz;
	private PortalGinInjector ginInjector;
	private Map<Long, QuestionContainerWidget> questionIndexToQuestionWidget;
	private SynapseAlert synAlert;
	
	@Inject
	public QuizPresenter(QuizView view,  
			AuthenticationController authenticationController, 
			GlobalApplicationState globalApplicationState,
			SynapseClientAsync synapseClient,
			AdapterFactory adapterFactory,
			JSONObjectAdapter jsonObjectAdapter,
			PortalGinInjector ginInjector,
			SynapseAlert synAlert){
		this.view = view;
		// Set the presenter on the view
		this.authenticationController = authenticationController;
		this.globalApplicationState = globalApplicationState;
		this.synapseClient = synapseClient;
		fixServiceEntryPoint(synapseClient);
		this.adapterFactory = adapterFactory;
		this.ginInjector = ginInjector;
		this.synAlert = synAlert;
		this.view.setPresenter(this);
		view.setSynAlertWidget(synAlert.asWidget());
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
		questionIndexToQuestionWidget.clear();
		if (quiz.getHeader() != null)
			view.setQuizHeader(quiz.getHeader());
		List<Question> questions = quiz.getQuestions();
		Long questionNumber = Long.valueOf(1);
		for (Question question : questions) {
			QuestionContainerWidget newQuestion = ginInjector.getQuestionContainerWidget();
			questionIndexToQuestionWidget.put(questionNumber, newQuestion);
			newQuestion.configure(questionNumber, question, null);
			view.addQuestionContainerWidget(newQuestion.asWidget());
			questionNumber++;
		}	
		view.reset();
	}
	
	private boolean checkAllAnswered() {
		for (Long questionIndex : questionIndexToQuestionWidget.keySet()) {
			if (questionIndexToQuestionWidget.get(questionIndex).getAnswers().isEmpty()) {
				return false;
			}
		}
		return true;
	}
	
	// For testing only
	public void setQuestionIndexToQuestionWidgetMap(Map<Long, QuestionContainerWidget> ans) {
		this.questionIndexToQuestionWidget = ans;
	}
	
	@Override
	public void submitAnswers() {
		synAlert.clear();
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
		AsyncCallback<PassingRecord> callback = new AsyncCallback<PassingRecord>() {
			@Override
			public void onSuccess(PassingRecord passingRecord) {
				if (passingRecord.getPassed())
					showSuccess(authenticationController.getCurrentUserProfile(), passingRecord);
				else
					showFailure(passingRecord);
			}
			
			@Override
			public void onFailure(Throwable caught) {
				synAlert.handleException(caught);
			}
		};
		synapseClient.submitCertificationQuizResponse(submission, callback);
	}
	
	@Override
	public void showSuccess(UserProfile profile, PassingRecord passingRecord) {
		showQuizFromPassingRecord(passingRecord);
		scoreQuiz(passingRecord);
		//show success UI (certificate) and quiz
		view.showSuccess(profile, passingRecord);
	}
	
	@Override
	public void showFailure(PassingRecord passingRecord) {
		showQuizFromPassingRecord(passingRecord);
		scoreQuiz(passingRecord);
		//show failure message and quiz
		view.showFailure(passingRecord);
	}
	
	@Override
	public void showQuizFromPassingRecord(PassingRecord passingRecord) {
		view.clear();
		List<ResponseCorrectness> responseCorrections = passingRecord.getCorrections();
		Long questionNumber = Long.valueOf(1);
		for (ResponseCorrectness response: responseCorrections) {
			QuestionContainerWidget newQuestion = ginInjector.getQuestionContainerWidget();
			questionIndexToQuestionWidget.put(questionNumber, newQuestion);
			newQuestion.configure(questionNumber, response.getQuestion(), (MultichoiceResponse) response.getResponse());
			view.addQuestionContainerWidget(newQuestion.asWidget());
			questionNumber++;
		}
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
	public void submitClicked() {
		if (!checkAllAnswered()) {
			view.showErrorMessage(DisplayConstants.ERROR_ALL_QUESTIONS_REQUIRED);
		} else {
			view.setSubmitEnabled(false);
			submitAnswers();
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
		questionIndexToQuestionWidget.clear();
		getIsCertified();
	}
	
	public void getIsCertified() {
		synAlert.clear();
		view.showLoading();
		AsyncCallback<String> callback = new AsyncCallback<String>() {
			@Override
			public void onSuccess(String passingRecordJson) {
				try {
					//if certified, show the certificate
					//otherwise, show the quiz
					PassingRecord passingRecord = new PassingRecord(adapterFactory.createNew(passingRecordJson));
					view.hideLoading();
					showSuccess(authenticationController.getCurrentUserProfile(), passingRecord);
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
					synAlert.handleException(caught);
				}
			}
		};
		synapseClient.getCertifiedUserPassingRecord(authenticationController.getCurrentUserPrincipalId(), callback);
	}
	
	public void getQuiz() {
		synAlert.clear();
		view.showLoading();
		AsyncCallback<String> callback = new AsyncCallback<String>() {
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
				synAlert.handleException(caught);
			}
		};
		synapseClient.getCertificationQuiz(callback);
	}
}
