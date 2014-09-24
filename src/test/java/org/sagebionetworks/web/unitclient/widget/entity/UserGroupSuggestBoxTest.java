package org.sagebionetworks.web.unitclient.widget.entity;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;
import static org.mockito.Mockito.*;

import java.util.ArrayList;
import java.util.List;

import org.junit.Before;
import org.junit.Test;
import org.mockito.Mockito;
import org.sagebionetworks.repo.model.UserGroupHeader;
import org.sagebionetworks.repo.model.UserGroupHeaderResponsePage;
import org.sagebionetworks.web.client.GlobalApplicationState;
import org.sagebionetworks.web.client.SageImageBundle;
import org.sagebionetworks.web.client.SynapseClientAsync;
import org.sagebionetworks.web.client.security.AuthenticationController;
import org.sagebionetworks.web.client.widget.search.UserGroupSuggestBox;
import org.sagebionetworks.web.client.widget.search.UserGroupSuggestBoxView;
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
	
	
	@Before
	public void before() {
		mockView = mock(UserGroupSuggestBoxView.class);
		mockSynapseClient = mock(SynapseClientAsync.class);
		mockSageImageBundle = mock(SageImageBundle.class);
		mockAuthenticationController = mock(AuthenticationController.class);
		mockGlobalApplicationState = mock(GlobalApplicationState.class);
		suggestBox = new UserGroupSuggestBox(mockView, mockAuthenticationController, mockGlobalApplicationState, mockSynapseClient, mockSageImageBundle);
	}
	
	@Test
	public void testGetSuggestions() throws RestServiceException {
		UserGroupHeaderResponsePage testPage = getResponsePage();
		AsyncMockStubber.callSuccessWith(testPage).when(mockSynapseClient).getUserGroupHeadersByPrefix(anyString(), anyLong(), anyLong(), any(AsyncCallback.class));
		
		SuggestOracle.Request request = new SuggestOracle.Request();
		request.setQuery("test");
		try {
			suggestBox.getSuggestions(request, null);
		} catch(NullPointerException e) {
			
		} finally {
			verify(mockView).updateFieldStateForSuggestions(any(UserGroupHeaderResponsePage.class), anyInt());
		}
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
	
}
