package org.sagebionetworks.web.unitclient.presenter;

import static org.mockito.Matchers.any;
import static org.mockito.Matchers.anyString;
import static org.mockito.Mockito.*;
import static org.junit.Assert.*;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.junit.Before;
import org.junit.Test;
import org.mockito.ArgumentCaptor;
import org.mockito.Mockito;
import org.sagebionetworks.repo.model.UserProfile;
import org.sagebionetworks.repo.model.UserSessionData;
import org.sagebionetworks.repo.model.questionnaire.Question;
import org.sagebionetworks.repo.model.questionnaire.Questionnaire;
import org.sagebionetworks.repo.model.questionnaire.QuestionnaireResponse;
import org.sagebionetworks.schema.adapter.AdapterFactory;
import org.sagebionetworks.schema.adapter.JSONObjectAdapter;
import org.sagebionetworks.schema.adapter.JSONObjectAdapterException;
import org.sagebionetworks.schema.adapter.org.json.AdapterFactoryImpl;
import org.sagebionetworks.schema.adapter.org.json.JSONObjectAdapterImpl;
import org.sagebionetworks.web.client.GWTWrapper;
import org.sagebionetworks.web.client.GlobalApplicationState;
import org.sagebionetworks.web.client.SynapseClientAsync;
import org.sagebionetworks.web.client.place.Quiz;
import org.sagebionetworks.web.client.presenter.QuizPresenter;
import org.sagebionetworks.web.client.security.AuthenticationController;
import org.sagebionetworks.web.client.view.QuizView;
import org.sagebionetworks.web.server.servlet.SynapseClientStubUtil;
import org.sagebionetworks.web.test.helper.AsyncMockStubber;

import com.google.gwt.user.client.rpc.AsyncCallback;

public class QuizPresenterTest {
	
	QuizPresenter presenter;
	QuizView mockView;
	SynapseClientAsync mockSynapseClient;
	AuthenticationController mockAuthenticationController;
	GlobalApplicationState mockGlobalApplicationState;
	AdapterFactory adapterFactory = new AdapterFactoryImpl();
	JSONObjectAdapter adapter = new JSONObjectAdapterImpl();
	GWTWrapper mockGwt;
	Quiz place;
	
	@Before
	public void setup() throws JSONObjectAdapterException {
		mockView = mock(QuizView.class);
		mockSynapseClient = mock(SynapseClientAsync.class);
		mockAuthenticationController = mock(AuthenticationController.class);
		mockGlobalApplicationState = mock(GlobalApplicationState.class);
		mockGwt = mock(GWTWrapper.class);
		presenter = new QuizPresenter(mockView, mockAuthenticationController, mockGlobalApplicationState, mockSynapseClient, adapterFactory, adapter, mockGwt);
		when(mockGwt.getRandomNextInt(anyInt())).thenReturn(0);
		when(mockAuthenticationController.isLoggedIn()).thenReturn(true);
		when(mockAuthenticationController.getCurrentUserSessionData()).thenReturn(new UserSessionData());
		Questionnaire questionnaire = SynapseClientStubUtil.mockQuestionnaire();
		String questionnaireJson = questionnaire.writeToJSONObject(adapter.createNew()).toJSONString();
		AsyncMockStubber.callSuccessWith(questionnaireJson).when(mockSynapseClient).getCertificationQuestionnaire(any(AsyncCallback.class));
		AsyncMockStubber.callSuccessWith(true).when(mockSynapseClient).submitCertificationQuestionnaireResponse(anyString(), any(AsyncCallback.class));
		verify(mockView).setPresenter(presenter);
		place = Mockito.mock(Quiz.class);
	}	
	
	@Test
	public void testGetQuestionaire() {
		presenter.getQuestionnaire();
		verify(mockSynapseClient).getCertificationQuestionnaire(any(AsyncCallback.class));
		ArgumentCaptor<List> arg = ArgumentCaptor.forClass(List.class);
		verify(mockView).showQuiz(anyString(), arg.capture());
		//mock quiz has 3 questions, one having 2 variants, verify that a single question is passed to the view
		assertEquals(3, arg.getValue().size());
	}
	
	@Test
	public void testGetQuestionaireFailure() {
		AsyncMockStubber.callFailureWith(new Exception("unhandled exception")).when(mockSynapseClient).getCertificationQuestionnaire(any(AsyncCallback.class));
		presenter.getQuestionnaire();
		verify(mockSynapseClient).getCertificationQuestionnaire(any(AsyncCallback.class));
		verify(mockView).showErrorMessage(anyString());
	}
	
	@Test
	public void testSubmitAnswersPass() throws JSONObjectAdapterException {
		Map<Long, List<Long>> questionIndex2AnswerIndices = new HashMap<Long, List<Long>>();
		//let's say I have 2 answers
		
		//question index 0 has answer index 3
		List<Long> answerIndices = new ArrayList<Long>();
		answerIndices.add(3L);
		questionIndex2AnswerIndices.put(0L, answerIndices);
		//and question index 4 has answer indices 0 and 3
		answerIndices = new ArrayList<Long>();
		answerIndices.add(0L);
		answerIndices.add(3L);
		questionIndex2AnswerIndices.put(4L, answerIndices);
		
		presenter.submitAnswers(questionIndex2AnswerIndices);
		
		//since we set it up to return true, it should show the success/pass UI
		verify(mockView).showSuccess(any(UserProfile.class));
		
		//let's also check the response object
		ArgumentCaptor<String> arg = ArgumentCaptor.forClass(String.class);
		verify(mockSynapseClient).submitCertificationQuestionnaireResponse(arg.capture(), any(AsyncCallback.class));
		//reconstruct the QuestionnaireResponse, and sanity check that it should have 2 question responses
		QuestionnaireResponse questionnaireResponse = new QuestionnaireResponse(adapterFactory.createNew(arg.getValue()));
		assertEquals(2, questionnaireResponse.getQuestionResponses().size());
	}

	@Test
	public void testSubmitAnswersFailed() throws JSONObjectAdapterException {
		AsyncMockStubber.callSuccessWith(false).when(mockSynapseClient).submitCertificationQuestionnaireResponse(anyString(), any(AsyncCallback.class));
		presenter.submitAnswers(new HashMap<Long, List<Long>>());
		
		//since we set it up to return false, it should show the failed UI
		verify(mockView).showFailure();
	}
	
	@Test
	public void testSubmitAnswersError() throws JSONObjectAdapterException {
		AsyncMockStubber.callFailureWith(new Exception("unhandled")).when(mockSynapseClient).submitCertificationQuestionnaireResponse(anyString(), any(AsyncCallback.class));
		presenter.submitAnswers(new HashMap<Long, List<Long>>());
		verify(mockView).showErrorMessage(anyString());
	}
	
}
