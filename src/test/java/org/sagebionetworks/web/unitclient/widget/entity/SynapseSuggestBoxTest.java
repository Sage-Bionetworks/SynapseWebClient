package org.sagebionetworks.web.unitclient.widget.entity;

import static org.junit.Assert.assertEquals;
import static org.mockito.Matchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import org.junit.Before;
import org.junit.Test;
import org.mockito.Mockito;
import org.sagebionetworks.web.client.GlobalApplicationState;
import org.sagebionetworks.web.client.SageImageBundle;
import org.sagebionetworks.web.client.SynapseClientAsync;
import org.sagebionetworks.web.client.security.AuthenticationController;
import org.sagebionetworks.web.client.utils.CallbackP;
import org.sagebionetworks.web.client.widget.search.SynapseSuggestBox;
import org.sagebionetworks.web.client.widget.search.SynapseSuggestBoxView;
import org.sagebionetworks.web.client.widget.search.SynapseSuggestOracle;
import org.sagebionetworks.web.client.widget.search.SynapseSuggestion;
import org.sagebionetworks.web.client.widget.search.UserGroupSuggestionProvider;
import org.sagebionetworks.web.client.widget.search.UserGroupSuggestionProvider.UserGroupSuggestion;

import com.google.gwt.event.dom.client.KeyDownHandler;
import com.google.gwt.event.shared.GwtEvent;

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
	
	@Test
	public void testAddKeyDownHandler() {
		KeyDownHandler handler = Mockito.mock(KeyDownHandler.class);
		suggestBox.addKeyDownHandler(handler);
		verify(mockView).addKeyDownHandler(handler);
	}
	
	@Test
	public void testFireEvent() {
		GwtEvent event = Mockito.mock(GwtEvent.class);
		suggestBox.fireEvent(event);
		verify(mockView).fireEvent(event);
	}
	
	@Test
	public void testGetTabIndex() {
		int tabIndex = 4;
		when(mockView.getTabIndex()).thenReturn(tabIndex);
		assertEquals(tabIndex, suggestBox.getTabIndex());
	}
	
	@Test
	public void testSetAccessKey() {
		char key = 'a';
		suggestBox.setAccessKey(key);
		verify(mockView).setAccessKey(key);
	}
	
	@Test
	public void testSetTabIndex() {
		int tabIndex = 5;
		suggestBox.setTabIndex(tabIndex);
		verify(mockView).setTabIndex(tabIndex);
	}
}
