package org.sagebionetworks.web.unitclient.presenter;

import static org.junit.Assert.assertEquals;
import static org.mockito.Matchers.any;
import static org.mockito.Matchers.anyString;
import static org.mockito.Matchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.junit.Before;
import org.junit.Test;
import org.mockito.ArgumentCaptor;
import org.mockito.Mockito;
import org.sagebionetworks.repo.model.UserProfile;
import org.sagebionetworks.repo.model.UserSessionData;
import org.sagebionetworks.repo.model.quiz.MultichoiceAnswer;
import org.sagebionetworks.repo.model.quiz.MultichoiceQuestion;
import org.sagebionetworks.repo.model.quiz.MultichoiceResponse;
import org.sagebionetworks.repo.model.quiz.PassingRecord;
import org.sagebionetworks.repo.model.quiz.Question;
import org.sagebionetworks.repo.model.quiz.Quiz;
import org.sagebionetworks.repo.model.quiz.QuizResponse;
import org.sagebionetworks.repo.model.quiz.ResponseCorrectness;
import org.sagebionetworks.schema.adapter.AdapterFactory;
import org.sagebionetworks.schema.adapter.JSONObjectAdapter;
import org.sagebionetworks.schema.adapter.JSONObjectAdapterException;
import org.sagebionetworks.schema.adapter.org.json.AdapterFactoryImpl;
import org.sagebionetworks.schema.adapter.org.json.JSONObjectAdapterImpl;
import org.sagebionetworks.web.client.GlobalApplicationState;
import org.sagebionetworks.web.client.PortalGinInjector;
import org.sagebionetworks.web.client.SynapseClientAsync;
import org.sagebionetworks.web.client.presenter.QuestionContainerWidget;
import org.sagebionetworks.web.client.presenter.QuizPresenter;
import org.sagebionetworks.web.client.security.AuthenticationController;
import org.sagebionetworks.web.client.view.QuizView;
import org.sagebionetworks.web.client.widget.entity.controller.SynapseAlert;
import org.sagebionetworks.web.test.helper.AsyncMockStubber;

import com.google.gwt.user.client.rpc.AsyncCallback;
import com.google.gwt.user.client.ui.Widget;

public class QuizPresenterTest {
	
	QuizPresenter presenter;
	QuizView mockView;
	SynapseClientAsync mockSynapseClient;
	AuthenticationController mockAuthenticationController;
	GlobalApplicationState mockGlobalApplicationState;
	PortalGinInjector mockInjector;
	QuestionContainerWidget mockQuestionContainer;
	PassingRecord mockPassingRecord;
	MultichoiceResponse mockQuestionResponse;
	SynapseAlert mockSynAlert;
	AdapterFactory adapterFactory = new AdapterFactoryImpl();
	JSONObjectAdapter adapter = new JSONObjectAdapterImpl();
	org.sagebionetworks.web.client.place.Quiz place;
	
	
	@Before
	public void setup() throws JSONObjectAdapterException {
		mockView = mock(QuizView.class);
		mockSynapseClient = mock(SynapseClientAsync.class);
		mockAuthenticationController = mock(AuthenticationController.class);
		mockGlobalApplicationState = mock(GlobalApplicationState.class);
		mockQuestionContainer = mock(QuestionContainerWidget.class);
		mockInjector = mock(PortalGinInjector.class);
		mockPassingRecord = mock(PassingRecord.class);
		mockQuestionResponse = mock(MultichoiceResponse.class);
		mockSynAlert = mock(SynapseAlert.class);
		presenter = new QuizPresenter(mockView, mockAuthenticationController, mockGlobalApplicationState, mockSynapseClient, adapterFactory, adapter, mockInjector, mockSynAlert);
		when(mockAuthenticationController.isLoggedIn()).thenReturn(true);
		when(mockAuthenticationController.getCurrentUserProfile()).thenReturn(new UserProfile());
		when(mockInjector.getQuestionContainerWidget()).thenReturn(mockQuestionContainer);
		Quiz questionnaire = mockQuiz();
		String questionnaireJson = questionnaire.writeToJSONObject(adapter.createNew()).toJSONString();
		AsyncMockStubber.callSuccessWith(questionnaireJson).when(mockSynapseClient).getCertificationQuiz(any(AsyncCallback.class));
		verify(mockView).setPresenter(presenter);
		place = Mockito.mock(org.sagebionetworks.web.client.place.Quiz.class);
		AsyncMockStubber.callSuccessWith(questionnaireJson).when(mockSynapseClient).getCertifiedUserPassingRecord(anyString(), any(AsyncCallback.class));
		
	}	
	

	public static Quiz mockQuiz() {
		Quiz quiz = new Quiz();
		quiz.setHeader("Certification");
		List<Question> questionOptions = new ArrayList<Question>();
		MultichoiceQuestion q1 = new MultichoiceQuestion();
		q1.setExclusive(true);
		long questionIndex = 0;
		q1.setQuestionIndex(questionIndex);
		q1.setPrompt("How is information organized in Synapse?");
		List<MultichoiceAnswer> answers = new ArrayList<MultichoiceAnswer>();
		long answerIndex = 0L;
		answers.add(getAnswer(answerIndex++, "Projects"));
		answers.add(getAnswer(answerIndex++, "Relationship"));
		answers.add(getAnswer(answerIndex++, "Versioning"));
		answers.add(getAnswer(answerIndex++, "Data sharing"));
		q1.setAnswers(answers);
		questionOptions.add(q1);
		q1 = new MultichoiceQuestion();
		q1.setExclusive(true);
		questionIndex++;
		q1.setQuestionIndex(questionIndex);
		q1.setPrompt("Who can have an account on Synapse?");
		answers = new ArrayList<MultichoiceAnswer>();
		answerIndex = 0L;
		answers.add(getAnswer(answerIndex++, "Anyone over 13 years old"));
		answers.add(getAnswer(answerIndex++, "only researchers in an accredited academic institution"));
		answers.add(getAnswer(answerIndex++, "only European data privacy regulators"));
		answers.add(getAnswer(answerIndex++, "only people working at not-for-profit research organizations"));
		q1.setAnswers(answers);
		questionOptions.add(q1);
		q1 = new MultichoiceQuestion();
		q1.setExclusive(false);
		questionIndex++;
		q1.setQuestionIndex(questionIndex);
		q1.setPrompt("If I have any questions about Synapse I can do the following:");
		answers = new ArrayList<MultichoiceAnswer>();
		answerIndex = 0L;
		answers.add(getAnswer(answerIndex++, "Send an email to synapseInfo@sagebase.org"));
		answers.add(getAnswer(answerIndex++, "Ask questions in the Synpase support forum"));
		q1.setAnswers(answers);
		questionOptions.add(q1);
		
		q1 = new MultichoiceQuestion();
		q1.setExclusive(true);
		questionIndex++;
		q1.setQuestionIndex(questionIndex);
		q1.setPrompt("What... is the air-speed velocity of an unladen swallow?");
		answers = new ArrayList<MultichoiceAnswer>();
		answerIndex = 0L;
		answers.add(getAnswer(answerIndex++, "42 m/s"));
		answers.add(getAnswer(answerIndex++, "African or European?"));
		answers.add(getAnswer(answerIndex++, "Huh?  I don't know that!"));
		
		q1.setAnswers(answers);
		questionOptions.add(q1);
		
		q1 = new MultichoiceQuestion();
		q1.setExclusive(false);
		questionIndex++;
		q1.setQuestionIndex(questionIndex);
		q1.setPrompt("Mark the following statements true or false about interacting with Synapse:");
		answers = new ArrayList<MultichoiceAnswer>();
		answerIndex = 0L;
		answers.add(getAnswer(answerIndex++, "Synapse only has a web interface"));
		answers.add(getAnswer(answerIndex++, "Synapse has a set of programmatic clients in addition to the web client"));
		answers.add(getAnswer(answerIndex++, "I can <b>only</b> upload or download data via the web client"));
		q1.setAnswers(answers);
		questionOptions.add(q1);
		
		quiz.setQuestions(questionOptions);
		return quiz;
	}
	
	private void configureMockPassingRecord(Quiz quiz) {
		List<ResponseCorrectness> responseCorrectness = new ArrayList<ResponseCorrectness>();
		for (Question question: quiz.getQuestions()) {
			ResponseCorrectness rc = new ResponseCorrectness();
			rc.setQuestion(question);
			rc.setResponse(mockQuestionResponse);
			rc.setIsCorrect(true);
			responseCorrectness.add(rc);
		}
		when(mockPassingRecord.getCorrections()).thenReturn(responseCorrectness);
	}
	
	public static MultichoiceAnswer getAnswer(long answerIndex, String prompt) {
		MultichoiceAnswer a = new MultichoiceAnswer();
		a.setAnswerIndex(answerIndex);
		a.setPrompt(prompt);
		return a;
	}
	
	private void setPassingRecordResponse(PassingRecord pr) throws JSONObjectAdapterException {
		AsyncMockStubber.callSuccessWith(pr).when(mockSynapseClient).submitCertificationQuizResponse(any(QuizResponse.class), any(AsyncCallback.class));
	}
	
	@Test
	public void testGetQuiz() {
		presenter.getQuiz();
		verify(mockSynapseClient).getCertificationQuiz(any(AsyncCallback.class));
		ArgumentCaptor<Quiz> arg = ArgumentCaptor.forClass(Quiz.class);
		verify(mockView).showLoading();
		verify(mockView).hideLoading();
		//mock quiz has 5 questions
		verify(mockView, Mockito.times(5)).addQuestionContainerWidget(any(Widget.class));
	}
	
	@Test
	public void testGetQuizFailure() {
		Exception caught = new Exception("unhandled exception");
		AsyncMockStubber.callFailureWith(caught).when(mockSynapseClient).getCertificationQuiz(any(AsyncCallback.class));
		presenter.getQuiz();
		verify(mockSynapseClient).getCertificationQuiz(any(AsyncCallback.class));
		verify(mockView).showLoading();
		verify(mockView).hideLoading();
		verify(mockSynAlert).handleException(caught);
	}
	
	@Test
	public void testSubmitAnswersPass() throws JSONObjectAdapterException {
		Map<Long, QuestionContainerWidget> questionWidgetMap = new HashMap<Long, QuestionContainerWidget>();
		//let's say I have 2 answers
		QuestionContainerWidget mockQuestionOne = mock(QuestionContainerWidget.class);
		QuestionContainerWidget mockQuestionTwo = mock(QuestionContainerWidget.class);
		//question index 0 has answer index 3
		Set<Long> answerIndices = new HashSet<Long>();
		answerIndices.add(3L);
		when(mockQuestionOne.getAnswers()).thenReturn(answerIndices);
		when(mockQuestionOne.getQuestionIndex()).thenReturn(0L);
		//and question index 4 has answer indices 0 and 3
		answerIndices = new HashSet<Long>();
		answerIndices.add(0L);
		answerIndices.add(3L);
		when(mockQuestionOne.getAnswers()).thenReturn(answerIndices);
		when(mockQuestionOne.getQuestionIndex()).thenReturn(4L);
		questionWidgetMap.put(0L, mockQuestionOne);
		questionWidgetMap.put(4L, mockQuestionTwo);
		presenter.setQuestionIndexToQuestionWidgetMap(questionWidgetMap);
		when(mockPassingRecord.getPassed()).thenReturn(true);
		setPassingRecordResponse(mockPassingRecord);

		presenter.submitAnswers();
		
		//since we set it up to return true, it should show the success/pass UI
		verify(mockView).showSuccess(any(UserProfile.class), eq(mockPassingRecord));
		
		//let's also check the response object
		ArgumentCaptor<QuizResponse> arg = ArgumentCaptor.forClass(QuizResponse.class);
		verify(mockSynapseClient).submitCertificationQuizResponse(arg.capture(), any(AsyncCallback.class));
		//reconstruct the QuestionnaireResponse, and sanity check that it should have 2 question responses
		assertEquals(2, arg.getValue().getQuestionResponses().size());
	}

	@Test
	public void testSubmitAnswersFailed() throws JSONObjectAdapterException {
		when(mockPassingRecord.getPassed()).thenReturn(false);
		setPassingRecordResponse(mockPassingRecord);
		presenter.submitAnswers();
		
		//since we set it up to return false, it should show the failed UI
		verify(mockView).showFailure(eq(mockPassingRecord));
	}
	
	@Test
	public void testSubmitAnswersError() throws JSONObjectAdapterException {
		Exception caught = new Exception("unhandled");
		AsyncMockStubber.callFailureWith(caught).when(mockSynapseClient).submitCertificationQuizResponse(any(QuizResponse.class), any(AsyncCallback.class));
		presenter.submitAnswers();
		verify(mockSynAlert).handleException(caught);
	}
	
	@Test
	public void testShowQuizFromPassingRecord() {
		configureMockPassingRecord(mockQuiz());
		presenter.showQuizFromPassingRecord(mockPassingRecord);
		verify(mockView, Mockito.times(5)).addQuestionContainerWidget(any(Widget.class));
	}
	
	@Test
	public void testShowQuiz() {
		presenter.showQuiz(mockQuiz());
		verify(mockView, Mockito.times(5)).addQuestionContainerWidget(any(Widget.class));
	}
	
}
