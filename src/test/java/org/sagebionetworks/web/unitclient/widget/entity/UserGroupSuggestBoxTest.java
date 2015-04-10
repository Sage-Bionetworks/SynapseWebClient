package org.sagebionetworks.web.unitclient.widget.entity;

import static org.mockito.Mockito.*;

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
import org.sagebionetworks.web.client.widget.search.UserGroupSuggestBox;
import org.sagebionetworks.web.client.widget.search.UserGroupSuggestBoxView;
import org.sagebionetworks.web.client.widget.search.UserGroupSuggestOracle;
import org.sagebionetworks.web.client.widget.search.UserGroupSuggestOracle.UserGroupSuggestion;
import org.sagebionetworks.web.shared.exceptions.RestServiceException;
import org.sagebionetworks.web.test.helper.AsyncMockStubber;

import com.google.gwt.user.client.rpc.AsyncCallback;
import com.google.gwt.user.client.ui.SuggestOracle;

public class UserGroupSuggestBoxTest {

	UserGroupSuggestBoxView mockView;
	SynapseClientAsync mockSynapseClient;
	SageImageBundle mockSageImageBundle;
	AuthenticationController mockAuthenticationController;
	GlobalApplicationState mockGlobalApplicationState;
	
	UserGroupSuggestBox suggestBox;
	UserGroupSuggestOracle mockOracle;
	
	
	@Before
	public void before() {
		mockView = mock(UserGroupSuggestBoxView.class);
		mockSynapseClient = mock(SynapseClientAsync.class);
		mockSageImageBundle = mock(SageImageBundle.class);
		mockAuthenticationController = mock(AuthenticationController.class);
		mockGlobalApplicationState = mock(GlobalApplicationState.class);
		mockOracle = mock(UserGroupSuggestOracle.class);
		suggestBox = new UserGroupSuggestBox(mockView, mockAuthenticationController, mockGlobalApplicationState, mockSynapseClient, mockSageImageBundle);
		suggestBox.setOracle(mockOracle);
	}
	
	@Test
	public void testGetSuggestions() throws RestServiceException {
		UserGroupHeaderResponsePage testPage = getResponsePage();
		AsyncMockStubber.callSuccessWith(testPage).when(mockSynapseClient).getUserGroupHeadersByPrefix(anyString(), anyLong(), anyLong(), any(AsyncCallback.class));
		when(mockOracle.makeUserGroupSuggestion(any(UserGroupHeader.class), anyString())).thenReturn(null);
		
		SuggestOracle.Request mockRequest = mock(SuggestOracle.Request.class);
		when(mockRequest.getQuery()).thenReturn("test");
		SuggestOracle.Callback mockCallback = mock(SuggestOracle.Callback.class);
		
		suggestBox.getSuggestions(mockRequest, mockCallback);
		
		verify(mockRequest).getQuery();
		verify(mockSynapseClient).getUserGroupHeadersByPrefix(anyString(), anyLong(), anyLong(), any(AsyncCallback.class));
		verify(mockView).updateFieldStateForSuggestions(any(UserGroupHeaderResponsePage.class), anyInt());
		verify(mockCallback).onSuggestionsReady(any(SuggestOracle.Request.class), any(SuggestOracle.Response.class));
	}
	
	private UserGroupHeaderResponsePage getResponsePage() {
		UserGroupHeaderResponsePage testPage = new UserGroupHeaderResponsePage();
		testPage.setPrefixFilter("test");
		
		UserGroupHeader head1 = new UserGroupHeader();
		head1.setFirstName("Test");
		head1.setLastName("One");
		
		UserGroupHeader head2 = new UserGroupHeader();
		head1.setFirstName("Test");
		head1.setLastName("Two");
		
		List<UserGroupHeader> children = new ArrayList<UserGroupHeader>();
		children.add(head1);
		children.add(head2);
		
		testPage.setChildren(children);
		
		testPage.setTotalNumberOfResults((long) 6);
		
		return testPage;
	}
	
	@Test
	public void testSelectedSuggestionCallback() {
		CallbackP<UserGroupSuggestion> mockCallback = mock(CallbackP.class);
		suggestBox.addItemSelectedHandler(mockCallback);
		UserGroupSuggestion suggestion = mock(UserGroupSuggestion.class);
		suggestBox.setSelectedSuggestion(suggestion);
		
		verify(mockCallback).invoke(suggestion);
	}
	
	@Test
	public void testSelectedSuggestionCallbackNullSelection() {
		CallbackP<UserGroupSuggestion> mockCallback = mock(CallbackP.class);
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
}
