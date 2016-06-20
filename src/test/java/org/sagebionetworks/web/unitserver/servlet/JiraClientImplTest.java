package org.sagebionetworks.web.unitserver.servlet;

import static org.mockito.Mockito.*;
import static org.junit.Assert.*;
import java.util.Map;
import java.util.concurrent.ExecutionException;

import org.junit.Before;
import org.junit.Test;
import org.mockito.ArgumentCaptor;
import org.sagebionetworks.web.server.servlet.JiraClientImpl;
import org.sagebionetworks.web.server.servlet.JiraJavaClient;
import org.sagebionetworks.web.shared.exceptions.RestServiceException;
import org.sagebionetworks.web.shared.exceptions.UnknownErrorException;

import com.atlassian.jira.rest.client.IssueRestClient;
import com.atlassian.jira.rest.client.domain.BasicIssue;
import com.atlassian.jira.rest.client.domain.input.ComplexIssueInputFieldValue;
import com.atlassian.jira.rest.client.domain.input.IssueInput;
import com.atlassian.util.concurrent.Promise;
import com.google.gwt.dev.util.collect.HashMap;

public class JiraClientImplTest {
	
	JiraJavaClient mockJiraJavaClient;
	JiraClientImpl jiraClient;
	IssueRestClient mockIssueClient;
	Promise<BasicIssue> mockPromise;
	
	
	String testSummary = "This is the summary";
	String testDescription = "this is the description";
	String testReporter = "reporter_name";
	Map<String,String> customFieldValues = new HashMap<String, String>();
	
	@Before
	public void setup() throws Exception{
		mockJiraJavaClient = mock(JiraJavaClient.class);
		jiraClient = new JiraClientImpl();
		jiraClient.setJiraJavaClient(mockJiraJavaClient);
		mockIssueClient = mock(IssueRestClient.class);
		when(mockJiraJavaClient.getIssueClient()).thenReturn(mockIssueClient);
		mockPromise = mock(Promise.class);
		when(mockIssueClient.createIssue(any(IssueInput.class))).thenReturn(mockPromise);
		BasicIssue mockBasicIssue = mock(BasicIssue.class);
		when(mockPromise.get()).thenReturn(mockBasicIssue);
	}
	
	@Test
	public void testCreateJiraIssue() throws RestServiceException {
		//capture the issue that is being sent for creation, and validate values
		
		String customFieldKey = "custom_field_1111";
		String customFieldValue = "2048";
		customFieldValues.put(customFieldKey, customFieldValue);
		jiraClient.createJiraIssue(testSummary, testDescription, testReporter, customFieldValues);
		ArgumentCaptor<IssueInput> issueInputCaptor = ArgumentCaptor.forClass(IssueInput.class);
		verify(mockIssueClient).createIssue(issueInputCaptor.capture());
		IssueInput actualIssueInput = issueInputCaptor.getValue();
		
		assertEquals(testSummary, actualIssueInput.getField("summary").getValue());
		assertEquals(testDescription, actualIssueInput.getField("description").getValue());
		ComplexIssueInputFieldValue reporterValue = (ComplexIssueInputFieldValue)actualIssueInput.getField("reporter").getValue(); 
		assertEquals(testReporter, reporterValue.getValuesMap().get("name"));
		assertEquals(customFieldValue, actualIssueInput.getField(customFieldKey).getValue());
	}
	
	@Test
	public void testCreateJiraIssueLongSummary() throws RestServiceException {
		// verify that summary (if greater than maximum size allowed) is truncated.  Also verify that the full summary is captured in the description.
		testSummary = "Quisque egestas arcu id purus venenatis, quis dictum risus luctus. Proin in elementum erat. Mauris volutpat elit vestibulum vulputate dapibus. Sed et tellus mattis, imperdiet eros vitae, mollis nisl. Quisque elementum vehicula mauris ac rutrum. Proin pretium dignissim elit in egestas. Sed sollicitudin nibh ac tortor gravida, in sollicitudin turpis lacinia. Proin sed maximus ex. Aliquam lacinia id tellus posuere bibendum. Aenean quis ipsum et nulla tristique blandit vel sed mauris. Pellentesque id ex non massa tempus elementum a quis elit. Mauris condimentum, nulla eu tristique feugiat, orci felis molestie orci, porta maximus odio erat sed nunc. Cras ut turpis eu metus facilisis scelerisque ut et ligula.";
		jiraClient.createJiraIssue(testSummary, testDescription, testReporter, customFieldValues);
		ArgumentCaptor<IssueInput> issueInputCaptor = ArgumentCaptor.forClass(IssueInput.class);
		verify(mockIssueClient).createIssue(issueInputCaptor.capture());
		IssueInput actualIssueInput = issueInputCaptor.getValue();
		String actualSummary = actualIssueInput.getField("summary").getValue().toString();
		String actualDescription = actualIssueInput.getField("description").getValue().toString();
		assertEquals(JiraClientImpl.SUMMARY_MAX_SIZE, actualSummary.length());
		assertTrue(actualDescription.startsWith(testSummary));
	}
	
	@Test (expected=UnknownErrorException.class)
	public void testCreateJiraIssueFailure() throws Exception {
		when(mockPromise.get()).thenThrow(new ExecutionException(new Exception()));
		jiraClient.createJiraIssue(testSummary, testDescription, testReporter, customFieldValues);
	}
	
		
}
