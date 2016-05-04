package org.sagebionetworks.web.unitclient.widget.entity.editor;

import static org.junit.Assert.*;
import static org.mockito.Mockito.*;
import static org.mockito.Matchers.*;

import org.junit.Before;
import org.junit.Test;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.sagebionetworks.repo.model.UserProfile;
import org.sagebionetworks.web.client.SynapseClientAsync;
import org.sagebionetworks.web.client.utils.CallbackP;
import org.sagebionetworks.web.client.widget.entity.controller.SynapseAlert;
import org.sagebionetworks.web.client.widget.entity.editor.TabbedTableConfigEditor;
import org.sagebionetworks.web.client.widget.entity.editor.TabbedTableConfigView;
import org.sagebionetworks.web.client.widget.entity.editor.UserSelector;
import org.sagebionetworks.web.client.widget.entity.editor.UserSelectorView;
import org.sagebionetworks.web.client.widget.search.SynapseSuggestBox;
import org.sagebionetworks.web.client.widget.search.SynapseSuggestion;
import org.sagebionetworks.web.client.widget.search.UserGroupSuggestionProvider;
import org.sagebionetworks.web.test.helper.AsyncMockStubber;

import com.google.gwt.user.client.rpc.AsyncCallback;
import com.google.gwt.user.client.ui.Widget;

public class UserSelectorTest {
		
	UserSelector widget;
	@Mock
	UserSelectorView mockView;
	@Mock
	SynapseSuggestBox mockSuggestBox;
	@Mock
	SynapseAlert mockSynAlert;
	@Mock
	SynapseClientAsync mockSynapseClient;
	@Mock
	UserGroupSuggestionProvider mockUserGroupSuggestionProvider;
	@Mock
	CallbackP<String> mockUsernameCallback;
	@Mock
	SynapseSuggestion mockSuggestion;
	@Mock
	UserProfile mockUserProfile;
	
	public static final String SUGGESTION_ID = "Maythe4thBeWithYou";
	public static final String USERNAME = "Y0da";
	
	@Before
	public void setup(){
		MockitoAnnotations.initMocks(this);
		widget = new UserSelector(mockView, mockSuggestBox, mockUserGroupSuggestionProvider, mockSynAlert, mockSynapseClient);
		widget.configure(mockUsernameCallback);
		
		when(mockSuggestion.getId()).thenReturn(SUGGESTION_ID);
		
		AsyncMockStubber.callSuccessWith(mockUserProfile).when(mockSynapseClient).getUserProfile(anyString(), any(AsyncCallback.class));
		when(mockUserProfile.getUserName()).thenReturn(USERNAME);
	}
	
	@Test
	public void testConstruction() {
		verify(mockView).setPresenter(widget);
		verify(mockView).setSynAlert(any(Widget.class));
		verify(mockSuggestBox).setSuggestionProvider(mockUserGroupSuggestionProvider);
		verify(mockView).setSelectBox(any(Widget.class));
		verify(mockSuggestBox).addItemSelectedHandler(any(CallbackP.class));
	}
	
	@Test
	public void testOnSynapseSuggestSelected() {
		widget.onSynapseSuggestSelected(mockSuggestion);
		verify(mockSynAlert).clear();
		verify(mockSynapseClient).getUserProfile(anyString(), any(AsyncCallback.class));
		verify(mockUsernameCallback).invoke(USERNAME);
		verify(mockView).hide();
	}
	
	@Test
	public void testOnSynapseSuggestSelectedFailure() {
		Exception ex = new Exception("fail");
		AsyncMockStubber.callFailureWith(ex).when(mockSynapseClient).getUserProfile(anyString(), any(AsyncCallback.class));
		widget.onSynapseSuggestSelected(mockSuggestion);
		verify(mockSynAlert).clear();
		verify(mockSynapseClient).getUserProfile(anyString(), any(AsyncCallback.class));
		verify(mockSynAlert).handleException(ex);
	}
	
	@Test
	public void testOnModalShown() {
		widget.onModalShown();
		verify(mockSuggestBox).setFocus(true);
	}
	
	@Test
	public void testShow() {
		widget.show();
		verify(mockSynAlert).clear();
		verify(mockSuggestBox).clear();
		verify(mockView).show();
	}
	
	
	@Test
	public void testAsWidget() {
		widget.asWidget();
		verify(mockView).asWidget();
	}
}
