package org.sagebionetworks.web.client.presenter;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.sagebionetworks.repo.model.quiz.MultichoiceResponse;
import org.sagebionetworks.repo.model.quiz.PassingRecord;
import org.sagebionetworks.repo.model.quiz.QuestionResponse;
import org.sagebionetworks.repo.model.quiz.Quiz;
import org.sagebionetworks.repo.model.quiz.QuizResponse;
import org.sagebionetworks.schema.adapter.AdapterFactory;
import org.sagebionetworks.schema.adapter.JSONObjectAdapter;
import org.sagebionetworks.schema.adapter.JSONObjectAdapterException;
import org.sagebionetworks.web.client.DisplayConstants;
import org.sagebionetworks.web.client.DisplayUtils;
import org.sagebionetworks.web.client.GlobalApplicationState;
import org.sagebionetworks.web.client.SynapseClientAsync;
import org.sagebionetworks.web.client.security.AuthenticationController;
import org.sagebionetworks.web.client.view.QuizView;
import org.sagebionetworks.web.shared.exceptions.NotFoundException;

import com.google.gwt.activity.shared.AbstractActivity;
import com.google.gwt.event.shared.EventBus;
import com.google.gwt.place.shared.Place;
import com.google.gwt.user.client.rpc.AsyncCallback;
import com.google.gwt.user.client.ui.AcceptsOneWidget;
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
	
	@Inject
	public QuizPresenter(QuizView view,  
			AuthenticationController authenticationController, 
			GlobalApplicationState globalApplicationState,
			SynapseClientAsync synapseClient,
			AdapterFactory adapterFactory,
			JSONObjectAdapter jsonObjectAdapter){
		this.view = view;
		// Set the presenter on the view
		this.authenticationController = authenticationController;
		this.globalApplicationState = globalApplicationState;
		this.synapseClient = synapseClient;
		this.adapterFactory = adapterFactory;
		this.jsonObjectAdapter = jsonObjectAdapter;
		this.view.setPresenter(this);
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
		DisplayUtils.goToLastPlace(globalApplicationState);
	}
	
	@Override
	public void submitAnswers(Map<Long, Set<Long>> questionIndex2AnswerIndices) {
		try {
			//submit question/answer combinations for approval
			//create response object from answers
			QuizResponse submission = new QuizResponse();
			List<QuestionResponse> questionResponses = new ArrayList<QuestionResponse>();
			for (Long questionIndex : questionIndex2AnswerIndices.keySet()) {
				Set<Long> answerIndices = questionIndex2AnswerIndices.get(questionIndex);
				MultichoiceResponse response = new MultichoiceResponse();
				response.setQuestionIndex(questionIndex);
				response.setAnswerIndex(answerIndices);
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
		synapseClient.getCertifiedUserPassingRecord(authenticationController.getCurrentUserPrincipalId(), new AsyncCallback<String>() {
			@Override
			public void onSuccess(String passingRecordJson) {
				try {
					//if certified, show the certificate
					//otherwise, show the quiz
					PassingRecord passingRecord = new PassingRecord(adapterFactory.createNew(passingRecordJson));
					view.showSuccess(authenticationController.getCurrentUserSessionData().getProfile(), passingRecord);
				
				} catch (JSONObjectAdapterException e) {
					onFailure(e);
				}
			}
			@Override
			public void onFailure(Throwable caught) {
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
		synapseClient.getCertificationQuiz(new AsyncCallback<String>() {
			@Override
			public void onSuccess(String quizJson) {
				try {
					quiz = new Quiz(adapterFactory.createNew(quizJson));
					view.showQuiz(quiz);
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
	}
}
