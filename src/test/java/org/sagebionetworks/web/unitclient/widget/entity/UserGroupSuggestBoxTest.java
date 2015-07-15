package org.sagebionetworks.web.unitclient.widget.entity;

import static org.mockito.Matchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;

import java.util.ArrayList;
import java.util.List;

import org.junit.Before;
import org.junit.Test;
import org.sagebionetworks.repo.model.UserGroupHeader;
import org.sagebionetworks.repo.model.UserGroupHeaderResponsePage;
import org.sagebionetworks.web.client.GlobalApplicationState;
import org.sagebionetworks.web.client.SageImageBundle;
import org.sagebionetworks.web.client.SynapseClientAsync;
import org.sagebionetworks.web.client.security.AuthenticationController;
import org.sagebionetworks.web.client.utils.CallbackP;
import org.sagebionetworks.web.client.widget.search.SynapseSuggestOracle;
import org.sagebionetworks.web.client.widget.search.SynapseSuggestion;
import org.sagebionetworks.web.client.widget.search.UserGroupSuggestBox;
import org.sagebionetworks.web.client.widget.search.UserGroupSuggestBoxView;
import org.sagebionetworks.web.client.widget.search.UserGroupSuggestionProvider;
import org.sagebionetworks.web.client.widget.search.UserGroupSuggestionProvider.UserGroupSuggestion;

public class UserGroupSuggestBoxTest {

	UserGroupSuggestBoxView mockView;
	SynapseClientAsync mockSynapseClient;
	SageImageBundle mockSageImageBundle;
	AuthenticationController mockAuthenticationController;
	GlobalApplicationState mockGlobalApplicationState;
	SynapseSuggestOracle mockOracle;
	UserGroupSuggestBox suggestBox;
	UserGroupSuggestionProvider mockSuggestionProvider;
	
	
	@Before
	public void before() {
		mockView = mock(UserGroupSuggestBoxView.class);
		mockSynapseClient = mock(SynapseClientAsync.class);
		mockSageImageBundle = mock(SageImageBundle.class);
		mockAuthenticationController = mock(AuthenticationController.class);
		mockGlobalApplicationState = mock(GlobalApplicationState.class);
		mockOracle = mock(SynapseSuggestOracle.class);
		mockSuggestionProvider = mock(UserGroupSuggestionProvider.class);
		suggestBox = new UserGroupSuggestBox(mockView, mockAuthenticationController, mockGlobalApplicationState, mockSynapseClient, mockSageImageBundle, mockOracle);
		suggestBox.setSuggestionProvider(mockSuggestionProvider);
	}
	
//	@Test
//	public void testGetSuggestions() throws RestServiceException {
//		UserGroupHeaderResponsePage testPage = getResponsePage();
//		AsyncMockStubber.callSuccessWith(testPage).when(mockSynapseClient).getUserGroupHeadersByPrefix(anyString(), anyLong(), anyLong(), any(AsyncCallback.class));		
//		SuggestOracle.Request mockRequest = mock(SuggestOracle.Request.class);
//		when(mockRequest.getQuery()).thenReturn("test");
//		SuggestOracle.Callback mockCallback = mock(SuggestOracle.Callback.class);
//		
//		suggestBox.getSuggestions();
//		
//		verify(mockRequest).getQuery();
//		verify(mockSynapseClient).getUserGroupHeadersByPrefix(anyString(), anyLong(), anyLong(), any(AsyncCallback.class));
//		verify(mockView).updateFieldStateForSuggestions(any(UserGroupHeaderResponsePage.class), anyInt());
//		verify(mockCallback).onSuggestionsReady(any(SuggestOracle.Request.class), any(SuggestOracle.Response.class));
//		
//		//calling again causes another request (since this completed successfully)
////		suggestBox.getSuggestions(mockRequest, mockCallback);
//		verify(mockSynapseClient, times(2)).getUserGroupHeadersByPrefix(anyString(), anyLong(), anyLong(), any(AsyncCallback.class));
//	}
	
//	@Test
//	public void testGetSuggestionsFailure() throws RestServiceException {
//		String errorMessage = "an error message";
//		AsyncMockStubber.callFailureWith(new Exception(errorMessage)).when(mockSynapseClient).getUserGroupHeadersByPrefix(anyString(), anyLong(), anyLong(), any(AsyncCallback.class));
//		SuggestOracle.Request mockRequest = mock(SuggestOracle.Request.class);
//		SuggestOracle.Callback mockCallback = mock(SuggestOracle.Callback.class);
////		suggestBox.getSuggestions(mockRequest, mockCallback);
//		
//		verify(mockSynapseClient).getUserGroupHeadersByPrefix(anyString(), anyLong(), anyLong(), any(AsyncCallback.class));
//		verify(mockView).showErrorMessage(errorMessage);
//		verify(mockCallback, never()).onSuggestionsReady(any(SuggestOracle.Request.class), any(SuggestOracle.Response.class));
//		
//		//calling again causes another request (since this completed successfully)
////		suggestBox.getSuggestions(mockRequest, mockCallback);
//		verify(mockSynapseClient, times(2)).getUserGroupHeadersByPrefix(anyString(), anyLong(), anyLong(), any(AsyncCallback.class));
//	}
	

	
	@Test
	public void testSelectedSuggestionCallback() {
		CallbackP<SynapseSuggestion> mockCallback = mock(CallbackP.class);
		suggestBox.addItemSelectedHandler(mockCallback);
		UserGroupSuggestion suggestion = mock(UserGroupSuggestion.class);
		suggestBox.setSelectedSuggestion(suggestion);
		
		verify(mockCallback).invoke(suggestion);
	}
	
	@Test
	public void testSelectedSuggestionCallbackNullSelection() {
		CallbackP<SynapseSuggestion> mockCallback = mock(CallbackP.class);
		suggestBox.addItemSelectedHandler(mockCallback);
		suggestBox.setSelectedSuggestion(null);
		
		verify(mockCallback, never()).invoke(any(UserGroupSuggestion.class));
	}
	
	@Test
	public void testSelectedSuggestionNullCallback() {
		suggestBox.addItemSelectedHandler(null);
		
		UserGroupSuggestion suggestion = mock(UserGroupSuggestion.class);
		suggestBox.setSelectedSuggestion(suggestion);
		//no error
	}
	
//	@Test
//	public void testGetSuggestionsDelayResponse() throws RestServiceException {
//		SuggestOracle.Request mockRequest = mock(SuggestOracle.Request.class);
//		SuggestOracle.Callback mockCallback = mock(SuggestOracle.Callback.class);
////		suggestBox.getSuggestions(mockRequest, mockCallback);
//		//called once
//		verify(mockSynapseClient).getUserGroupHeadersByPrefix(anyString(), anyLong(), anyLong(), any(AsyncCallback.class));
//		verify(mockCallback, never()).onSuggestionsReady(any(SuggestOracle.Request.class), any(SuggestOracle.Response.class));
////		suggestBox.getSuggestions(mockRequest, mockCallback);
//		//still, has called just once (from the first time, since it did not return).
//		verify(mockSynapseClient).getUserGroupHeadersByPrefix(anyString(), anyLong(), anyLong(), any(AsyncCallback.class));
//	}
}
