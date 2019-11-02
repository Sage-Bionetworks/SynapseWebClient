package org.sagebionetworks.web.unitclient.widget.entity.editor;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.mockito.Mockito.when;
import static org.sagebionetworks.web.shared.WidgetConstants.BUTTON_TEXT_KEY;
import static org.sagebionetworks.web.shared.WidgetConstants.EVALUATION_ID_KEY;
import static org.sagebionetworks.web.shared.WidgetConstants.FORM_CONTAINER_ID_KEY;
import static org.sagebionetworks.web.shared.WidgetConstants.JSON_SCHEMA_ID_KEY;
import static org.sagebionetworks.web.shared.WidgetConstants.PROJECT_ID_KEY;
import static org.sagebionetworks.web.shared.WidgetConstants.UI_SCHEMA_ID_KEY;
import static org.sagebionetworks.web.shared.WidgetConstants.UNAVAILABLE_MESSAGE;
import java.util.HashMap;
import java.util.Map;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;
import org.sagebionetworks.web.client.widget.entity.dialog.DialogCallback;
import org.sagebionetworks.web.client.widget.entity.editor.EvaluationSubmissionConfigEditor;
import org.sagebionetworks.web.client.widget.entity.editor.EvaluationSubmissionConfigView;
import org.sagebionetworks.web.shared.WikiPageKey;

@RunWith(MockitoJUnitRunner.class)
public class EvaluationSubmissionConfigEditorTest {

	EvaluationSubmissionConfigEditor editor;
	@Mock
	EvaluationSubmissionConfigView mockView;
	@Mock
	WikiPageKey mockWikiKey;
	@Mock
	DialogCallback mockDialogCallback;

	Map<String, String> descriptor;

	@Before
	public void testSetServicePrefix() {
		editor = new EvaluationSubmissionConfigEditor(mockView);
		descriptor = new HashMap<>();
	}

	@Test
	public void testButtonText() {
		String customButtonText = "Submit it";
		when(mockView.getButtonText()).thenReturn(customButtonText);
		editor.configure(mockWikiKey, descriptor, mockDialogCallback);

		editor.updateDescriptorFromView();

		assertEquals(customButtonText, descriptor.get(BUTTON_TEXT_KEY));
	}

	@Test
	public void testChallengeProject() {
		String projectKey = "syn122345";
		when(mockView.isChallengeProjectIdSelected()).thenReturn(true);
		when(mockView.getChallengeProjectId()).thenReturn(projectKey);
		descriptor.put(EVALUATION_ID_KEY, "oldEvaluationId");
		editor.configure(mockWikiKey, descriptor, mockDialogCallback);

		editor.updateDescriptorFromView();

		assertEquals(projectKey, descriptor.get(PROJECT_ID_KEY));
		assertFalse(descriptor.containsKey(EVALUATION_ID_KEY));
	}

	@Test
	public void testEvaluationId() {
		String evaluationId = "9384875";
		when(mockView.isChallengeProjectIdSelected()).thenReturn(false);
		when(mockView.getEvaluationQueueId()).thenReturn(evaluationId);
		descriptor.put(PROJECT_ID_KEY, "oldProjectId");
		editor.configure(mockWikiKey, descriptor, mockDialogCallback);

		editor.updateDescriptorFromView();

		assertEquals(evaluationId, descriptor.get(EVALUATION_ID_KEY));
		assertFalse(descriptor.containsKey(PROJECT_ID_KEY));
	}

	@Test
	public void testFormSubmission() {
		String formContainerId = "syn1";
		String jsonSchemaId = "syn2";
		String uiSchemaId = "syn3";
		when(mockView.isFormSubmission()).thenReturn(true);
		when(mockView.getFormContainerId()).thenReturn(formContainerId);
		when(mockView.getFormJsonSchemaId()).thenReturn(jsonSchemaId);
		when(mockView.getFormUiSchemaId()).thenReturn(uiSchemaId);
		editor.configure(mockWikiKey, descriptor, mockDialogCallback);

		editor.updateDescriptorFromView();

		assertEquals(formContainerId, descriptor.get(FORM_CONTAINER_ID_KEY));
		assertEquals(jsonSchemaId, descriptor.get(JSON_SCHEMA_ID_KEY));
		assertEquals(uiSchemaId, descriptor.get(UI_SCHEMA_ID_KEY));
	}

	@Test
	public void testEntitySubmission() {
		when(mockView.isFormSubmission()).thenReturn(false);
		descriptor.put(FORM_CONTAINER_ID_KEY, "oldContainerId");
		descriptor.put(JSON_SCHEMA_ID_KEY, "oldSchemaId");
		descriptor.put(UI_SCHEMA_ID_KEY, "oldUiSchemaId");
		editor.configure(mockWikiKey, descriptor, mockDialogCallback);

		editor.updateDescriptorFromView();

		assertFalse(descriptor.containsKey(FORM_CONTAINER_ID_KEY));
		assertFalse(descriptor.containsKey(JSON_SCHEMA_ID_KEY));
		assertFalse(descriptor.containsKey(UI_SCHEMA_ID_KEY));
	}

	@Test
	public void testUnavailableMessage() {
		String customUnavailableMessage = "Join the team before trying to submit";
		when(mockView.getUnavailableMessage()).thenReturn(customUnavailableMessage);
		editor.configure(mockWikiKey, descriptor, mockDialogCallback);

		editor.updateDescriptorFromView();

		assertEquals(customUnavailableMessage, descriptor.get(UNAVAILABLE_MESSAGE));
	}
}
