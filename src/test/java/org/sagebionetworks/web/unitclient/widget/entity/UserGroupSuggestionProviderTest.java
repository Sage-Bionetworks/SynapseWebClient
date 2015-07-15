package org.sagebionetworks.web.unitclient.widget.entity;

import static org.junit.Assert.assertEquals;
import static org.mockito.Matchers.any;
import static org.mockito.Matchers.anyLong;
import static org.mockito.Matchers.anyString;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;

import java.util.ArrayList;
import java.util.List;

import org.junit.Before;
import org.junit.Test;
import org.mockito.ArgumentCaptor;
import org.sagebionetworks.repo.model.UserGroupHeader;
import org.sagebionetworks.repo.model.UserGroupHeaderResponsePage;
import org.sagebionetworks.web.client.SynapseClientAsync;
import org.sagebionetworks.web.client.SynapseJSNIUtils;
import org.sagebionetworks.web.client.widget.search.SynapseSuggestionBundle;
import org.sagebionetworks.web.client.widget.search.UserGroupSuggestionProvider;
import org.sagebionetworks.web.test.helper.AsyncMockStubber;

import com.google.gwt.user.client.rpc.AsyncCallback;

public class UserGroupSuggestionProviderTest {

	UserGroupSuggestionProvider presenter;
	SynapseClientAsync mockSynapseClient;
	SynapseJSNIUtils mockJSNI;
	AsyncCallback<SynapseSuggestionBundle> mockCallback;
	
	int offset = 0;
	int pageSize = 10;
	int width = 568;
	String prefix = "test";
	
	
	@Before
	public void setup() {
		mockCallback = mock(AsyncCallback.class);
		mockSynapseClient = mock(SynapseClientAsync.class);
		mockJSNI = mock(SynapseJSNIUtils.class);
		presenter = new UserGroupSuggestionProvider(mockSynapseClient, mockJSNI);
	}
	
	@Test
	public void testGetSuggestions() {	
		ArgumentCaptor<SynapseSuggestionBundle> captor = ArgumentCaptor.forClass(SynapseSuggestionBundle.class);
		UserGroupHeaderResponsePage testPage = getResponsePage();
		AsyncMockStubber.callSuccessWith(testPage).when(mockSynapseClient).getUserGroupHeadersByPrefix(anyString(), anyLong(), anyLong(), any(AsyncCallback.class));		
		presenter.getSuggestions(offset, pageSize, width, prefix, mockCallback);
		verify(mockSynapseClient).getUserGroupHeadersByPrefix(anyString(), anyLong(), anyLong(), any(AsyncCallback.class));
		verify(mockCallback).onSuccess(captor.capture());
		SynapseSuggestionBundle testBundle = captor.getValue();
		assertEquals(testBundle.getTotalNumberOfResults(), 6);
		assertEquals(testBundle.getSuggestionBundle().size(), 2);
	}
	
	@Test
	public void testGetSuggestionsFailure() {
		Exception caught = new Exception("this is an exception");
		AsyncMockStubber.callFailureWith(caught).when(mockSynapseClient).getUserGroupHeadersByPrefix(anyString(), anyLong(), anyLong(), any(AsyncCallback.class));		
		presenter.getSuggestions(offset, pageSize, width, prefix, mockCallback);
		verify(mockSynapseClient).getUserGroupHeadersByPrefix(anyString(), anyLong(), anyLong(), any(AsyncCallback.class));	
		verify(mockCallback).onFailure(caught);
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
