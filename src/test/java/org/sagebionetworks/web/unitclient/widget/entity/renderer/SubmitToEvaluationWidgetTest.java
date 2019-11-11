package org.sagebionetworks.web.unitclient.widget.entity.renderer;

import static org.junit.Assert.assertEquals;
import static org.mockito.Matchers.any;
import static org.mockito.Matchers.anyInt;
import static org.mockito.Matchers.anyBoolean;
import static org.mockito.Matchers.anySet;
import static org.mockito.Matchers.anyString;
import static org.mockito.Matchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Captor;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;
import org.sagebionetworks.evaluation.model.Evaluation;
import org.sagebionetworks.repo.model.Entity;
import org.sagebionetworks.repo.model.ObjectType;
import org.sagebionetworks.schema.adapter.JSONObjectAdapterException;
import org.sagebionetworks.web.client.ChallengeClientAsync;
import org.sagebionetworks.web.client.GlobalApplicationState;
import org.sagebionetworks.web.client.PortalGinInjector;
import org.sagebionetworks.web.client.SynapseJavascriptClient;
import org.sagebionetworks.web.client.security.AuthenticationController;
import org.sagebionetworks.web.client.utils.CallbackP;
import org.sagebionetworks.web.client.widget.entity.renderer.SubmitToEvaluationWidget;
import org.sagebionetworks.web.client.widget.entity.renderer.SubmitToEvaluationWidgetView;
import org.sagebionetworks.web.client.widget.evaluation.EvaluationSubmitter;
import org.sagebionetworks.web.shared.FormParams;
import org.sagebionetworks.web.shared.PaginatedResults;
import org.sagebionetworks.web.shared.WidgetConstants;
import org.sagebionetworks.web.shared.WikiPageKey;
import org.sagebionetworks.web.shared.exceptions.RestServiceException;
import org.sagebionetworks.web.test.helper.AsyncMockStubber;
import com.google.gwt.user.client.rpc.AsyncCallback;

@RunWith(MockitoJUnitRunner.class)
public class SubmitToEvaluationWidgetTest {
	private static final String EVALUATION_2_SUBMISSION_RECEIPT_MESSAGE = "Evaluation 2 Submission Receipt Message";
	private static final String EVALUATION_1_SUBMISSION_RECEIPT_MESSAGE = "Evaluation 1 Submission Receipt Message";
	private static final String TEST_UNAVAILABLE_MESSAGE = "Submission is unavailable until you fulfill some requirements";
	private static final String EVAL_ID_1 = "1";
	private static final String EVAL_ID_2 = "2";

	@Mock
	SubmitToEvaluationWidgetView mockView;
	@Mock
	ChallengeClientAsync mockChallengeClient;
	@Mock
	GlobalApplicationState mockGlobalApplicationState;
	@Mock
	AuthenticationController mockAuthenticationController;
	@Mock
	EvaluationSubmitter mockEvaluationSubmitter;
	@Mock
	PortalGinInjector mockPortalGinInjector;
	@Mock
	SynapseJavascriptClient mockJsClient;
	@Captor
	ArgumentCaptor<FormParams> formParamsCaptor;
	SubmitToEvaluationWidget widget;
	Set<String> targetEvaluations;
	ArrayList<Evaluation> evaluationList;
	Map<String, String> descriptor = new HashMap<String, String>();
	String entityId = "syn123";

	@Before
	public void before() throws RestServiceException, JSONObjectAdapterException {
		when(mockPortalGinInjector.getEvaluationSubmitter()).thenReturn(mockEvaluationSubmitter);
		widget = new SubmitToEvaluationWidget(mockView, mockJsClient, mockChallengeClient, mockAuthenticationController, mockGlobalApplicationState, mockPortalGinInjector);
		verify(mockView).setPresenter(widget);
		targetEvaluations = new HashSet<String>();

		descriptor = new HashMap<String, String>();

		descriptor.put(WidgetConstants.UNAVAILABLE_MESSAGE, TEST_UNAVAILABLE_MESSAGE);
		String subchallengeList = EVAL_ID_1 + WidgetConstants.JOIN_WIDGET_SUBCHALLENGE_ID_LIST_DELIMETER + EVAL_ID_2;
		descriptor.put(WidgetConstants.JOIN_WIDGET_SUBCHALLENGE_ID_LIST_KEY, subchallengeList);

		targetEvaluations.add(EVAL_ID_1);
		targetEvaluations.add(EVAL_ID_2);

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
		AsyncMockStubber.callSuccessWith(evaluationList).when(mockJsClient).getAvailableEvaluations(anySet(), anyBoolean(), anyInt(), anyInt(), any(AsyncCallback.class));
	}

	@Test
	public void testHappyCaseConfigure() throws Exception {
		widget.configure(new WikiPageKey(entityId, ObjectType.ENTITY.toString(), null), descriptor, null, null);
		verify(mockJsClient).getAvailableEvaluations(eq(targetEvaluations), eq(true), anyInt(), anyInt(), any(AsyncCallback.class));
		verify(mockView).configure(anyString());
	}

	@Test
	public void testGetEvaluationIds() throws Exception {
		// test resolving evaluation ids from challenge id
		CallbackP<Set<String>> mockCallback = mock(CallbackP.class);
		descriptor.remove(WidgetConstants.JOIN_WIDGET_SUBCHALLENGE_ID_LIST_KEY);
		descriptor.put(WidgetConstants.CHALLENGE_ID_KEY, "1");
		Set<String> evaluationIds = Collections.singleton("5");
		AsyncMockStubber.callSuccessWith(evaluationIds).when(mockChallengeClient).getChallengeEvaluationIds(anyString(), any(AsyncCallback.class));
		widget.getEvaluationIds(descriptor, mockCallback);
		verify(mockChallengeClient).getChallengeEvaluationIds(anyString(), any(AsyncCallback.class));
		verify(mockCallback).invoke(evaluationIds);
	}

	@Test
	public void testGetEvaluationIdsEmpty() throws Exception {
		// test resolving evaluation ids from challenge id
		CallbackP<Set<String>> mockCallback = mock(CallbackP.class);
		descriptor.remove(WidgetConstants.JOIN_WIDGET_SUBCHALLENGE_ID_LIST_KEY);
		descriptor.put(WidgetConstants.CHALLENGE_ID_KEY, "1");
		Set<String> evaluationIds = Collections.emptySet();
		AsyncMockStubber.callSuccessWith(evaluationIds).when(mockChallengeClient).getChallengeEvaluationIds(anyString(), any(AsyncCallback.class));
		widget.getEvaluationIds(descriptor, mockCallback);
		verify(mockChallengeClient).getChallengeEvaluationIds(anyString(), any(AsyncCallback.class));
		verify(mockCallback, never()).invoke(anySet());
		verify(mockView).showUnavailable(anyString());
	}

	@Test
	public void testGetEvaluationIdsError() throws Exception {
		// test resolving evaluation ids from challenge id
		CallbackP<Set<String>> mockCallback = mock(CallbackP.class);
		descriptor.remove(WidgetConstants.JOIN_WIDGET_SUBCHALLENGE_ID_LIST_KEY);
		descriptor.put(WidgetConstants.CHALLENGE_ID_KEY, "1");
		AsyncMockStubber.callFailureWith(new Exception("unhandled")).when(mockChallengeClient).getChallengeEvaluationIds(anyString(), any(AsyncCallback.class));
		widget.getEvaluationIds(descriptor, mockCallback);
		verify(mockChallengeClient).getChallengeEvaluationIds(anyString(), any(AsyncCallback.class));
		verify(mockView).showErrorMessage(anyString());
	}

	@Test
	public void testGetEvaluationIdsFromProject() throws Exception {
		// test resolving evaluation ids from project id
		CallbackP<Set<String>> mockCallback = mock(CallbackP.class);
		descriptor.remove(WidgetConstants.JOIN_WIDGET_SUBCHALLENGE_ID_LIST_KEY);
		descriptor.put(WidgetConstants.PROJECT_ID_KEY, "1");
		Set<String> evaluationIds = Collections.singleton("5");
		AsyncMockStubber.callSuccessWith(evaluationIds).when(mockChallengeClient).getProjectEvaluationIds(anyString(), any(AsyncCallback.class));
		widget.getEvaluationIds(descriptor, mockCallback);
		verify(mockChallengeClient).getProjectEvaluationIds(anyString(), any(AsyncCallback.class));
		verify(mockCallback).invoke(evaluationIds);
	}

	@Test
	public void testGetEvaluationIdsEmptyFromProject() throws Exception {
		// test resolving evaluation ids from project id
		CallbackP<Set<String>> mockCallback = mock(CallbackP.class);
		descriptor.remove(WidgetConstants.JOIN_WIDGET_SUBCHALLENGE_ID_LIST_KEY);
		descriptor.put(WidgetConstants.PROJECT_ID_KEY, "1");
		Set<String> evaluationIds = Collections.emptySet();
		AsyncMockStubber.callSuccessWith(evaluationIds).when(mockChallengeClient).getProjectEvaluationIds(anyString(), any(AsyncCallback.class));
		widget.getEvaluationIds(descriptor, mockCallback);
		verify(mockChallengeClient).getProjectEvaluationIds(anyString(), any(AsyncCallback.class));
		verify(mockCallback, never()).invoke(anySet());
		verify(mockView).showUnavailable(anyString());
	}

	@Test
	public void testGetEvaluationIdsErrorFromProject() throws Exception {
		// test resolving evaluation ids from challenge id
		CallbackP<Set<String>> mockCallback = mock(CallbackP.class);
		descriptor.remove(WidgetConstants.JOIN_WIDGET_SUBCHALLENGE_ID_LIST_KEY);
		descriptor.put(WidgetConstants.PROJECT_ID_KEY, "1");
		AsyncMockStubber.callFailureWith(new Exception("unhandled")).when(mockChallengeClient).getProjectEvaluationIds(anyString(), any(AsyncCallback.class));
		widget.getEvaluationIds(descriptor, mockCallback);
		verify(mockChallengeClient).getProjectEvaluationIds(anyString(), any(AsyncCallback.class));
		verify(mockView).showErrorMessage(anyString());
	}

	@Test
	public void testConfigureServiceFailure() throws Exception {
		AsyncMockStubber.callFailureWith(new IllegalArgumentException()).when(mockJsClient).getAvailableEvaluations(anySet(), anyBoolean(), anyInt(), anyInt(), any(AsyncCallback.class));
		widget.configure(new WikiPageKey(entityId, ObjectType.ENTITY.toString(), null), descriptor, null, null);
		verify(mockJsClient).getAvailableEvaluations(eq(targetEvaluations), anyBoolean(), anyInt(), anyInt(), any(AsyncCallback.class));
		verify(mockView).showUnavailable(anyString());
	}

	@Test
	public void testSubmitToChallengeClickedAnonymous() throws Exception {
		widget.configure(new WikiPageKey(entityId, ObjectType.ENTITY.toString(), null), descriptor, null, null);
		when(mockAuthenticationController.isLoggedIn()).thenReturn(false);
		widget.submitToChallengeClicked();
		verify(mockView).showAnonymousRegistrationMessage();
		verify(mockEvaluationSubmitter, times(0)).configure(any(Entity.class), anySet(), any(FormParams.class));
	}

	@Test
	public void testSubmitToChallengeClickedLoggedIn() throws Exception {
		widget.configure(new WikiPageKey(entityId, ObjectType.ENTITY.toString(), null), descriptor, null, null);
		when(mockAuthenticationController.isLoggedIn()).thenReturn(true);
		widget.submitToChallengeClicked();
		verify(mockView, times(0)).showAnonymousRegistrationMessage();
		verify(mockEvaluationSubmitter).configure(any(Entity.class), anySet(), eq(null));
	}

	@Test
	public void testSubmitToChallengeUsingForm() throws Exception {
		String formContainerId = "syn1";
		String schemaId = "syn2";
		String uiSchemaId = "syn3";
		descriptor.put(WidgetConstants.FORM_CONTAINER_ID_KEY, formContainerId);
		descriptor.put(WidgetConstants.JSON_SCHEMA_ID_KEY, schemaId);
		descriptor.put(WidgetConstants.UI_SCHEMA_ID_KEY, uiSchemaId);
		widget.configure(new WikiPageKey(entityId, ObjectType.ENTITY.toString(), null), descriptor, null, null);
		when(mockAuthenticationController.isLoggedIn()).thenReturn(true);

		widget.submitToChallengeClicked();

		verify(mockEvaluationSubmitter).configure(any(Entity.class), anySet(), formParamsCaptor.capture());
		FormParams formParams = formParamsCaptor.getValue();
		assertEquals(formContainerId, formParams.getContainerSynId());
		assertEquals(schemaId, formParams.getJsonSchemaSynId());
		assertEquals(uiSchemaId, formParams.getUiSchemaSynId());
	}

	@Test
	public void testAsWidget() {
		widget.asWidget();
		verify(mockView).asWidget();
	}
}
