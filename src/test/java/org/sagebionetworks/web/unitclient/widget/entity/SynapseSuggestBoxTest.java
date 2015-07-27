package org.sagebionetworks.web.unitclient.widget.entity;

import static org.mockito.Matchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;

import org.junit.Before;
import org.junit.Test;
import org.sagebionetworks.web.client.GlobalApplicationState;
import org.sagebionetworks.web.client.SageImageBundle;
import org.sagebionetworks.web.client.SynapseClientAsync;
import org.sagebionetworks.web.client.security.AuthenticationController;
import org.sagebionetworks.web.client.utils.CallbackP;
import org.sagebionetworks.web.client.widget.search.SynapseSuggestOracle;
import org.sagebionetworks.web.client.widget.search.SynapseSuggestion;
import org.sagebionetworks.web.client.widget.search.SynapseSuggestBox;
import org.sagebionetworks.web.client.widget.search.SynapseSuggestBoxView;
import org.sagebionetworks.web.client.widget.search.UserGroupSuggestionProvider;
import org.sagebionetworks.web.client.widget.search.UserGroupSuggestionProvider.UserGroupSuggestion;

public class SynapseSuggestBoxTest {

	SynapseSuggestBoxView mockView;
	SynapseClientAsync mockSynapseClient;
	SageImageBundle mockSageImageBundle;
	AuthenticationController mockAuthenticationController;
	GlobalApplicationState mockGlobalApplicationState;
	SynapseSuggestOracle mockOracle;
	SynapseSuggestBox suggestBox;
	UserGroupSuggestionProvider mockSuggestionProvider;

	@Before
	public void before() {
		mockView = mock(SynapseSuggestBoxView.class);
		mockSynapseClient = mock(SynapseClientAsync.class);
		mockSageImageBundle = mock(SageImageBundle.class);
		mockAuthenticationController = mock(AuthenticationController.class);
		mockGlobalApplicationState = mock(GlobalApplicationState.class);
		mockOracle = mock(SynapseSuggestOracle.class);
		mockSuggestionProvider = mock(UserGroupSuggestionProvider.class);
		suggestBox = new SynapseSuggestBox(mockView, mockAuthenticationController, mockGlobalApplicationState, mockSynapseClient, mockSageImageBundle, mockOracle);
		suggestBox.setSuggestionProvider(mockSuggestionProvider);
	}
	
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
	
}
