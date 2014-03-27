package org.sagebionetworks.web.client.presenter;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import org.sagebionetworks.repo.model.questionnaire.MultichoiceResponse;
import org.sagebionetworks.repo.model.questionnaire.Question;
import org.sagebionetworks.repo.model.questionnaire.QuestionResponse;
import org.sagebionetworks.repo.model.questionnaire.QuestionVariety;
import org.sagebionetworks.repo.model.questionnaire.Questionnaire;
import org.sagebionetworks.repo.model.questionnaire.QuestionnaireResponse;
import org.sagebionetworks.schema.adapter.AdapterFactory;
import org.sagebionetworks.schema.adapter.JSONObjectAdapter;
import org.sagebionetworks.schema.adapter.JSONObjectAdapterException;
import org.sagebionetworks.web.client.ClientProperties;
import org.sagebionetworks.web.client.DisplayConstants;
import org.sagebionetworks.web.client.DisplayUtils;
import org.sagebionetworks.web.client.GWTWrapper;
import org.sagebionetworks.web.client.GlobalApplicationState;
import org.sagebionetworks.web.client.SynapseClientAsync;
import org.sagebionetworks.web.client.place.Home;
import org.sagebionetworks.web.client.place.Quiz;
import org.sagebionetworks.web.client.security.AuthenticationController;
import org.sagebionetworks.web.client.view.QuizView;

import com.google.gwt.activity.shared.AbstractActivity;
import com.google.gwt.event.shared.EventBus;
import com.google.gwt.place.shared.Place;
import com.google.gwt.user.client.rpc.AsyncCallback;
import com.google.gwt.user.client.ui.AcceptsOneWidget;
import com.google.inject.Inject;

public class QuizPresenter extends AbstractActivity implements QuizView.Presenter, Presenter<Quiz> {

	private Quiz testPlace;
	private QuizView view;
	private GlobalApplicationState globalApplicationState;
	private AuthenticationController authenticationController;
	private SynapseClientAsync synapseClient;
	private AdapterFactory adapterFactory;
	private JSONObjectAdapter jsonObjectAdapter;
	private GWTWrapper gwt;
	private List<Question> quiz;
	
	@Inject
	public QuizPresenter(QuizView view,  
			AuthenticationController authenticationController, 
			GlobalApplicationState globalApplicationState,
			SynapseClientAsync synapseClient,
			AdapterFactory adapterFactory,
			JSONObjectAdapter jsonObjectAdapter,
			GWTWrapper gwt){
		this.view = view;
		// Set the presenter on the view
		this.authenticationController = authenticationController;
		this.globalApplicationState = globalApplicationState;
		this.synapseClient = synapseClient;
		this.adapterFactory = adapterFactory;
		this.jsonObjectAdapter = jsonObjectAdapter;
		this.gwt = gwt;
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
		Place forwardPlace = globalApplicationState.getLastPlace();
		if(forwardPlace == null) {
			forwardPlace = new Home(ClientProperties.DEFAULT_PLACE_TOKEN);
		}
		goTo(forwardPlace);
	}
	
	@Override
	public void submitAnswers(Map<Long, List<Long>> questionIndex2AnswerIndices) {
		try {
			//submit question/answer combinations for approval
			//create response object from answers
			QuestionnaireResponse submission = new QuestionnaireResponse();
			List<QuestionResponse> questionResponses = new ArrayList<QuestionResponse>();
			for (Long questionIndex : questionIndex2AnswerIndices.keySet()) {
				List<Long> answerIndices = questionIndex2AnswerIndices.get(questionIndex);
				MultichoiceResponse response = new MultichoiceResponse();
				response.setQuestionIndex(questionIndex);
				//TODO: these should be ints, but they are currently incorrectly Strings in the json schema.
				List<String> answerIndicesStrings = new ArrayList<String>();
				for (Long index : answerIndices) {
					answerIndicesStrings.add(index.toString());
				}
				response.setAnswerIndex(answerIndicesStrings);
				questionResponses.add(response);
			}
			submission.setQuestionResponses(questionResponses);
			JSONObjectAdapter adapter = submission.writeToJSONObject(jsonObjectAdapter.createNew());
			String questionnaireResponse = adapter.toJSONString();
			synapseClient.submitCertificationQuestionnaireResponse(questionnaireResponse, new AsyncCallback<Boolean>() {
				
				@Override
				public void onSuccess(Boolean passed) {
					if (passed)
						view.showSuccess(authenticationController.getCurrentUserSessionData().getProfile());
					else
						view.showFailure();
				}
				
				@Override
				public void onFailure(Throwable caught) {
					if(!DisplayUtils.handleServiceException(caught, globalApplicationState.getPlaceChanger(), authenticationController.isLoggedIn(), view)) {					
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
	public void setPlace(Quiz place) {
		this.testPlace = place;
		view.setPresenter(this);
		view.clear();
		//ask for questions/answers and pass to view
		getQuestionaire();
	}
	
	public void getQuestionaire() {
		synapseClient.getCertificationQuestionnaire(new AsyncCallback<String>() {
			@Override
			public void onSuccess(String questionnaireJson) {
				try {
					Questionnaire questionnaire = new Questionnaire(adapterFactory.createNew(questionnaireJson));
					quiz = new ArrayList<Question>();
					//create a quiz
					for (QuestionVariety qv : questionnaire.getQuestions()) {
						List<Question> possibleQuestions = qv.getQuestionOptions();
						quiz.add(possibleQuestions.get(gwt.getRandomNextInt(possibleQuestions.size())));
					}
					view.showQuiz(questionnaire.getHeader(), quiz);
				} catch (JSONObjectAdapterException e) {
					onFailure(e);
				}
			}
			
			@Override
			public void onFailure(Throwable caught) {
				if(!DisplayUtils.handleServiceException(caught, globalApplicationState.getPlaceChanger(), authenticationController.isLoggedIn(), view)) {					
					view.showErrorMessage(caught.getMessage());
				} 
			}
		});
	}
}
