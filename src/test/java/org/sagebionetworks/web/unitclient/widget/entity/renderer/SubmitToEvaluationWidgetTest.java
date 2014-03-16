package org.sagebionetworks.web.unitclient.widget.entity.renderer;

import static org.mockito.Matchers.any;
import static org.mockito.Matchers.anySet;
import static org.mockito.Matchers.anyString;
import static org.mockito.Matchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import org.junit.Before;
import org.junit.Test;
import org.sagebionetworks.evaluation.model.Evaluation;
import org.sagebionetworks.markdown.constants.WidgetConstants;
import org.sagebionetworks.repo.model.AutoGenFactory;
import org.sagebionetworks.repo.model.Entity;
import org.sagebionetworks.repo.model.ObjectType;
import org.sagebionetworks.schema.adapter.AdapterFactory;
import org.sagebionetworks.schema.adapter.JSONObjectAdapterException;
import org.sagebionetworks.schema.adapter.org.json.AdapterFactoryImpl;
import org.sagebionetworks.web.client.GlobalApplicationState;
import org.sagebionetworks.web.client.SynapseClientAsync;
import org.sagebionetworks.web.client.security.AuthenticationController;
import org.sagebionetworks.web.client.transform.NodeModelCreator;
import org.sagebionetworks.web.client.widget.entity.EvaluationSubmitter;
import org.sagebionetworks.web.client.widget.entity.renderer.SubmitToEvaluationWidget;
import org.sagebionetworks.web.client.widget.entity.renderer.SubmitToEvaluationWidgetView;
import org.sagebionetworks.web.shared.PaginatedResults;
import org.sagebionetworks.web.shared.WikiPageKey;
import org.sagebionetworks.web.shared.exceptions.RestServiceException;
import org.sagebionetworks.web.test.helper.AsyncMockStubber;

import com.google.gwt.user.client.rpc.AsyncCallback;

public class SubmitToEvaluationWidgetTest {
	private static final String EVALUATION_2_SUBMISSION_RECEIPT_MESSAGE = "Evaluation 2 Submission Receipt Message";
	private static final String EVALUATION_1_SUBMISSION_RECEIPT_MESSAGE = "Evaluation 1 Submission Receipt Message";
	private static final String TEST_UNAVAILABLE_MESSAGE = "Submission is unavailable until you fulfill some requirements";
	private static final String EVAL_ID_1 = "1";
	private static final String EVAL_ID_2 = "2";
	
	SubmitToEvaluationWidgetView mockView;
	SynapseClientAsync mockSynapseClient;
	NodeModelCreator mockNodeModelCreator;
	AdapterFactory adapterFactory;
	AutoGenFactory autoGenFactory;
	GlobalApplicationState mockGlobalApplicationState;
	AuthenticationController mockAuthenticationController;
	EvaluationSubmitter mockEvaluationSubmitter;
	SubmitToEvaluationWidget widget;
	Set<String> targetEvaluations;
	ArrayList<Evaluation> evaluationList;
	Map<String, String> descriptor = new HashMap<String, String>();
	String entityId = "syn123";
	
	@Before
	public void before() throws RestServiceException, JSONObjectAdapterException {
		mockView = mock(SubmitToEvaluationWidgetView.class);
		mockSynapseClient = mock(SynapseClientAsync.class);
		mockNodeModelCreator = mock(NodeModelCreator.class);
		adapterFactory = new AdapterFactoryImpl();
		autoGenFactory = new AutoGenFactory();
		mockEvaluationSubmitter = mock(EvaluationSubmitter.class);
		mockGlobalApplicationState = mock(GlobalApplicationState.class);
		mockAuthenticationController = mock(AuthenticationController.class);
		widget = new SubmitToEvaluationWidget(mockView, mockSynapseClient, mockAuthenticationController, mockGlobalApplicationState, mockNodeModelCreator, mockEvaluationSubmitter);
		verify(mockView).setPresenter(widget);
		targetEvaluations = new HashSet<String>();
		
		descriptor = new HashMap<String, String>();
		
		descriptor.put(WidgetConstants.UNAVAILABLE_MESSAGE, TEST_UNAVAILABLE_MESSAGE);
		String subchallengeList = EVAL_ID_1 + WidgetConstants.JOIN_WIDGET_SUBCHALLENGE_ID_LIST_DELIMETER + EVAL_ID_2;
		descriptor.put(WidgetConstants.JOIN_WIDGET_SUBCHALLENGE_ID_LIST_KEY, subchallengeList);
		
		targetEvaluations.add(EVAL_ID_1);
		targetEvaluations.add(EVAL_ID_2);
		
		AsyncMockStubber.callSuccessWith("fake evaluation results json").when(mockSynapseClient).getAvailableEvaluations(anySet(), any(AsyncCallback.class));
		PaginatedResults<Evaluation> availableEvaluations = new PaginatedResults<Evaluation>();
		availableEvaluations.setTotalNumberOfResults(2);
		evaluationList = new ArrayList<Evaluation>();
		Evaluation e1 = new Evaluation();
		e1.setId(EVAL_ID_1);
		e1.setName("Test Evaluation 1");
		e1.setSubmissionReceiptMessage(EVALUATION_1_SUBMISSION_RECEIPT_MESSAGE);
		evaluationList.add(e1);
		Evaluation e2 = new Evaluation();
		e2.setId(EVAL_ID_2);
		e2.setName("Test Evaluation 2");
		e2.setSubmissionReceiptMessage(EVALUATION_2_SUBMISSION_RECEIPT_MESSAGE);
		evaluationList.add(e2);
		availableEvaluations.setResults(evaluationList);

		when(mockNodeModelCreator.createPaginatedResults(anyString(), any(Class.class))).thenReturn(availableEvaluations);
	}

	@Test
	public void testHappyCaseConfigure() throws Exception {
		widget.configure(new WikiPageKey(entityId, ObjectType.ENTITY.toString(), null), descriptor, null, null);
		verify(mockSynapseClient).getAvailableEvaluations(eq(targetEvaluations), any(AsyncCallback.class));
		verify(mockView).configure(any(WikiPageKey.class), eq(true), eq(TEST_UNAVAILABLE_MESSAGE));
	}

	@Test
	public void testConfigureServiceFailure() throws Exception {
		AsyncMockStubber.callFailureWith(new IllegalArgumentException()).when(mockSynapseClient).getAvailableEvaluations(anySet(), any(AsyncCallback.class));
		widget.configure(new WikiPageKey(entityId, ObjectType.ENTITY.toString(), null), descriptor, null, null);
		verify(mockSynapseClient).getAvailableEvaluations(eq(targetEvaluations), any(AsyncCallback.class));
		verify(mockView).showErrorMessage(anyString());
	}
	
	
	@Test
	public void testSubmitToChallengeClickedAnonymous() throws Exception {
		widget.configure(new WikiPageKey(entityId, ObjectType.ENTITY.toString(), null), descriptor, null, null);
		when(mockAuthenticationController.isLoggedIn()).thenReturn(false);
		widget.submitToChallengeClicked();
		verify(mockView).showAnonymousRegistrationMessage();
		verify(mockEvaluationSubmitter, times(0)).configure(any(Entity.class), anySet());
	}
	
	@Test
	public void testSubmitToChallengeClickedLoggedIn() throws Exception {
		widget.configure(new WikiPageKey(entityId, ObjectType.ENTITY.toString(), null), descriptor, null, null);
		when(mockAuthenticationController.isLoggedIn()).thenReturn(true);
		widget.submitToChallengeClicked();
		verify(mockView, times(0)).showAnonymousRegistrationMessage();
		verify(mockEvaluationSubmitter).configure(any(Entity.class), anySet());
	}
	
	@Test
	public void testAsWidget() {
		widget.asWidget();
		verify(mockView).asWidget();
	}
}











